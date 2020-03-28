(ns org.deepsymmetry.bytefield.core
  "Provides a safe yet robust subset of the Clojure programming
  language, tailored to the task of building SVG diagrams in the style
  of the LaTeX `bytefield` package."
  (:require [clojure.string :as str]
            [cljs.pprint :refer [cl-format]]
            [analemma.svg :as svg]
            [analemma.xml :as xml]
            [sci.core :as sci])
  (:require-macros [org.deepsymmetry.bytefield.macros :refer [self-bind-symbols]]))


;; The global symbol table used when evaluating diagram source.

(def ^:dynamic *globals*
  "Holds the globals during the building of a diagram. Dynamically bound
  to a new atom for each thread that calls `generate`, so that can be
  thread-safe. In retrospect, that was over-engineering, but hey... it
  might be used in a web context someday?"
  nil)



;; Implement our domain-specific language for building up attribute
;; maps concisely and by composing named starting maps, as well as
;; constructing text with arbitrarily enclosed (and nested) tspan
;; nodes.

(defn eval-attribute-spec
  "Expands an attribute specification into a single map of attribute
  keywords to values. A map is simply returned unchanged. A keyword is
  looked up in the map definitions (there are some predefined ones,
  and they can be augmented and replaced by calling `defattrs`) and
  the corresponding map is returned. A vector or list is used to merge
  multiple attribute specifications together. Each element is
  evaluated recursively in turn, and the results are merged together
  with later definitions winning if the same keyword appears more than
  once."
  [spec]
  (cond
    (nil? spec)  ; Explicitly represent `nil` as an empty map.
    {}

    (map? spec)  ; A map is already what it needs to be, just return it.
    spec

    (keyword? spec)  ; A keyword gets looked up as a named attribute map.
    (let [defs @@('named-attributes @*globals*)]
      (if-let [m (spec defs)]
        m
        (throw (js/Error. (str "Could not resolve attribute spec: " spec)))))

    (sequential? spec)  ; Lists or vectors evaluate and merge elements.
    (reduce (fn [acc v] (merge acc (eval-attribute-spec v)))
            {}
            spec)

    :else (throw (js/Error. (str "Invalid attribute spec: " spec)))))

(declare expand-nested-tspans)

(defn tspan
  "Builds an SVG tspan object from a vector or list that was found in
  the content of a `text` or `tspan` invocation. Any lists or vectors
  found within its own content will be recursively converted into
  tspan objects of their own."
  [attr-spec content]
  (concat [:tspan (eval-attribute-spec attr-spec)] (expand-nested-tspans content)))

(defn expand-nested-tspans
  "Converts any vectors or lists found in the content of a `text` or
  `tspan` invocation into nested `tspan` objects with new attributes
  defined by evaluating their first element as a spec."
  [content]
  (map (fn [element]
         (if (sequential? element)
           (let [[attr-spec & content] element]
             (tspan attr-spec content))
           (str element)))
       content))

(defn text
  "Builds a text object that begins with the specified `label` string.
  Its attributes can be configured through `attr-spec`, which defaults
  to `:plain`, the predefined style for plain text. Any additional
  content will be appended to the initial label, with with special
  support for creating styled children: any lists or vectors found in
  `content` will be expanded into tspan objects (their first element
  will be evaluated as an attribute spec for the tspan itself, and the
  remaining nested content will be expanded the same way as `text`
  content."
  ([label]
   (text label :plain))
  ([label attr-spec & content]
   (concat [:text (eval-attribute-spec attr-spec)] [(str label)] (expand-nested-tspans content))))

;; The diagram-drawing functions we make available for conveniently
;; creating byte field diagrams.

(defn defattrs
  "Registers an attribute map for later use under a keyword."
  [k m]
  (when-not (keyword? k) (throw (js/Error. (str "first argument to defattrs must be a keyword, received: " k))))
  (when-not (map? m) (throw (js/Error. (str "second argument to defattrs must be a map, received: " m))))
  (swap! @('named-attributes @*globals*) assoc k m))

(defn append-svg
  "Adds another svg element to the body being built up."
  [element]
  (swap! @('diagram-state @*globals*) update :svg-body concat [element]))


(defn draw-column-headers
  "Generates the header row that identifies each byte/bit box. By
  default uses the lower-case hex digits in increasing order, but you
  can specify your own list of `:labels` as an attribute. Normally
  consumes 14 vertical pixels, but you can specify a different
  `:height`. Defaults to a `:font-size` of 11 and `:font-family` of
  \"Courier New\" but these can be overridden as well. Other SVG text
  attributes can be supplied, and they will be passed along."
  ([]
   (draw-column-headers nil))
  ([attr-spec]
   (let [{:keys [labels height font-size font-family]
          :or   {labels      @('column-labels @*globals*)
                 height      14
                 font-size   11
                 font-family "Courier New, monospace"}
          :as   options} (eval-attribute-spec attr-spec)
         y               (+ (:y @@('diagram-state @*globals*)) (* 0.5 height))
         body            (for [i (range @('boxes-per-row @*globals*))]
                           (let [x (+ @('left-margin @*globals*) (* (+ i 0.5) @('box-width @*globals*)))]
                             (svg/text (merge (dissoc options :labels :height)
                                              {:x                 x
                                               :y                 y
                                               :font-family       font-family
                                               :font-size         font-size
                                               :dominant-baseline "middle"
                                               :text-anchor       "middle"})
                                       (nth labels i))))]
     (swap! @('diagram-state @*globals*)
            (fn [current]
              (-> current
                  (update :y + height)
                  (update :svg-body concat body)))))))

(defn draw-row-header
  "Generates the label in the left margin which identifies the starting
  byte of a row. Defaults to a `:font-size` of 11 and `:font-family`
  of \"Courier New, monospace\" but these can be overridden, and other
  SVG text attributes can be supplied via `attr-spec`.

  In the most common case, `label` is a string and the SVG text object
  is constructed as described above. If you need to draw a more
  complex structure, you can pass in your own SVG text object (with
  potentially nested tspan objects), and it will simply be
  positioned."
  ([label]
   (draw-row-header label nil))
  ([label attr-spec]
   (let [{:keys [font-size font-family dominant-baseline]
          :or   {font-size         11
                 font-family       "Courier New, monospace"
                 dominant-baseline "middle"}
          :as   options} (eval-attribute-spec attr-spec)
         x               (- @('left-margin @*globals*) 5)
         y               (+ (:y @@('diagram-state @*globals*)) (* 0.5 @('row-height @*globals*)))
         style           (merge options
                                {:x                 x
                                 :y                 y
                                 :font-family       font-family
                                 :font-size         font-size
                                 :dominant-baseline dominant-baseline
                                 :text-anchor       "end"})]
     (if (sequential? label)
       ;; The caller has already generated the SVG for the label, just position it.
       (append-svg (xml/merge-attrs label (select-keys style [:x :y :text-anchor])))
       ;; We are both building and positioning the SVG text object.
       (append-svg (svg/text style (str label)))))))

(defn draw-line
  "Adds a line to the SVG being built up. `:stroke` defaults to black,
  and `:stroke-width` to 1, but these can be overridden through
  `attr-spec`, and other SVG line attributes can be supplied that way
  as well."
  ([x1 y1 x2 y2]
   (draw-line x1 y1 x2 y2 nil))
  ([x1 y1 x2 y2 attr-spec]
   (let [{:keys [stroke stroke-width]
          :or   {stroke       "#000000"
                 stroke-width 1}
          :as   options} (eval-attribute-spec attr-spec)]
     (append-svg [:line (merge options
                               {:x1           x1
                                :y1           y1
                                :x2           x2
                                :y2           y2
                                :stroke       stroke
                                :stroke-width stroke-width})]))))

(defn next-row
  "Advances drawing to the next row of boxes, resetting the column to 0,
  incrementing the row, and adding the row height to the y coordinate.
  The height of the row defaults to `row-height` but can be overridden
  by passing a different value. Does not update the row byte address
  value because this function is also called by user code to draw
  informational rows."
  ([]
   (next-row @('row-height @*globals*)))
  ([height]
   (swap! @('diagram-state @*globals*)
          (fn [current]
            (-> current
                (update :y + height)
                (assoc :column 0))))))

(defn hex-text
  "Formats a number as an SVG text object containing a hexadecimal
  string with the specified number of digits (defaults to 2 if no
  length is specified), styled using the `:hex` predefined attributes.
  This styling can be overridden by passing `attr-spec`."
  ([n]
   (hex-text n 2))
  ([n length]
   (hex-text n length :hex))
  ([n length attr-spec]
   (let [fmt (str "~" length ",'0x")]
     (text (cl-format nil fmt n) [:hex attr-spec]))))

(defn format-box-label
  "Builds an appropriate SVG text object to label a box spanning the
  specified number of byte cells. If `label` is a number the content
  will be a hexadecimal string with two digits for each byte cell the
  box spans styled using the `:hex` predefined attributes. If it is a
  list or a vector, it is assumed to represent an already-formatted
  SVG text object, and returned unchanged. If it is a string, it is
  used as the content of a text object that is styled using the
  `:plain` predefined attributes."
  [label span]
  (cond
    (number? label) ; A number, format it as hexadecimal.
    (hex-text label (* span 2))

    (sequential? label) ; A list or vector, assume it is pre-rendered.
    label

    (string? label) ; A string, format it as plain text.
    (text label)

    :else
    (throw (js/Error. (str "Don't know how to format box label: " label)))))

(defn- center-baseline
  "Recursively ensures that the a tag and any content tags it contains
  have their dominant baseline set to center them vertically, so
  complex box labels align properly."
  [tag]
  (let [centered-content (map (fn [element]
                                (if (sequential? element)
                                  (center-baseline element)
                                  element))
                              (xml/get-content tag))
        centered-tag (xml/add-attrs tag :dominant-baseline "middle")]
    (apply xml/set-content (cons centered-tag centered-content))))

(defn- auto-advance-row
  "If we are about to draw a box just past the end of a row, advance to
  the next row, drawing the header(s) as needed."
  []
  (let [{:keys [column address] :as state} @@('diagram-state @*globals*)
        boxes                              @('boxes-per-row @*globals*)
        header-fn                          @('row-header-fn @*globals*)]
    (when (= column boxes)  ; We have filled the current row, so auto-advance.
      (when (zero? address)  ; This was the first row, so draw its header now we know we need headers.
        (when header-fn (draw-row-header (header-fn state))))
      (next-row)
      (swap! @('diagram-state @*globals*) update :address + boxes)
      (when header-fn (draw-row-header (header-fn @@('diagram-state @*globals*)))))))  ; Draw header for new row.

(defn- interpret-box-border
  "Given a box border element key (`:top`, `:bottom`, `:left`, or
  `:right`), the value supplied for the box's `:borders` attribute,
  and the default style with which the current border would be drawn,
  returns the style that should be used, or `nil` if this border line
  should not be drawn."
  [k borders default]
  (let [spec (k borders)]
    (cond
      (not spec)  ; The border was omitted from the spec, so should not be drawn.
      nil

      (= spec k)  ; The border seems to just be a set, and this value was present, use default style.
      default

      (or (keyword? spec) (map? spec) (sequential? spec))  ; It's an attribute definition for the border style.
      (eval-attribute-spec spec)

      :else  ; It's something else truthy, so use the default style.
      default)))

(defn draw-box
  "Draws a single byte or bit box in the current row at the current
  index. `label` can either be a number (which will be converted to a
  hex-styled hexadecimal string with two digits for each cell the box
  spans), a pre-constructed SVG text object (which will be rendered
  as-is), a string (which will be converted to a plain-styled SVG text
  object), a function (which will be called with the arguments `left`,
  `top`, `width`, and `height` describing the box boundaries, and can
  draw whatever it wants using analemma structures and `append-svg`),
  or `nil`, to have no label at all.

  The default size box is that of a single byte but this can be
  overridden with the `:span` attribute. Normally draws all borders,
  but you can supply the set you want drawn in `:borders`. The
  background can be filled with a color passed with `:fill`. Box
  height defaults to `row-height`, but that can be changed with
  `:height` if you are drawing special header rows rather than normal
  byte boxes (you will need to supply the same height override when
  calling `next-row`)."
  ([label]
   (draw-box label nil))
  ([label attr-spec]
   (auto-advance-row)
   (let [{:keys [span borders fill height]
          :or   {span    1
                 borders #{:left :right :top :bottom}
                 height  @('row-height @*globals*)}} (eval-attribute-spec attr-spec)

         column (:column @@('diagram-state @*globals*))
         left   (+ @('left-margin @*globals*) (* column @('box-width @*globals*)))
         width  (* span @('box-width @*globals*))
         right  (+ left width)
         top    (:y @@('diagram-state @*globals*))
         bottom (+ top height)]
     (when (> (+ column span) @('boxes-per-row @*globals*))
       (throw (js/Error "draw-box called with span larger than remaining columns in row")))
     (when fill (append-svg (svg/rect left top height width :fill fill)))
     (when-let [style (interpret-box-border :top borders :border-unrelated)] (draw-line left top right top style))
     (when-let [style (interpret-box-border :bottom borders :border-unrelated)]
       (draw-line left bottom right bottom style))
     (when-let [style (interpret-box-border :right borders :border-unrelated)] (draw-line right top right bottom style))
     (when-let [style (interpret-box-border :left borders :border-unrelated)] (draw-line left top left bottom style))
     (when label
       (if (fn? label)
         (label left top width height)  ; Box being drawn by custom function.
         (let [label (xml/merge-attrs (format-box-label label span)  ; Normal label.
                                      {:x           (/ (+ left right) 2.0)
                                       :y           (+ top 1 (/ height 2.0))
                                       :text-anchor "middle"})]
           (append-svg (center-baseline label)))))
     (swap! @('diagram-state @*globals*) update :column + span))))

(defn draw-boxes
  "Draws multiple boxes with the same attributes for each. Calls
  `draw-box` with each value in `labels`, passing `attr-spec` (if one
  was supplied) on each call."
  ([labels]
   (draw-boxes labels nil))
  ([labels attr-spec]
   (doseq [label labels]
     (draw-box label attr-spec))))

(defn- related?
  "Checks whether the specified `border` direction fromt the cell at the
  specified `address` is with a cell that is part of a group of boxes
  being drawn. The group ranges from address `start` until just before
  `end`."
  [address start end border]
  (let [width    @('boxes-per-row @*globals*)
        column   (mod address width)
        neighbor (case border
                   :left   (dec address)
                   :right  (inc address)
                   :top    (- address width)
                   :bottom (+ address width))
        edge     (boolean (or (and (= border :left) (zero? column))
                              (and (= border :right) (= column (dec width)))))]
    (and (<= start neighbor (dec end))
         (not edge))))

(defn draw-related-boxes
  "Draws multiple boxes with the same attributes for each. Borders
  between boxes that are both generated by this invocation will be
  styled as `:border-related`, while borders with other boxes or the
  outside of the table will be styled as `:border-unrelated`."
  ([labels]
   (draw-related-boxes labels nil))
  ([labels attr-spec]
   (let [attrs                      (eval-attribute-spec attr-spec)
         {:keys [:address :column]} @@('diagram-state @*globals*)
         start                      (+ address column)
         span                       (:span attrs 1)
         end                        (+ start (* span (count labels)))]
     (doseq [[i label] (map-indexed (fn [i label] [i label]) labels)]
       (let [borders (into {} (map (fn [border]
                                     [border (if (related? (+ start (* span i)) start end border)
                                               :border-related
                                               :border-unrelated)])
                                   [:left :right :top :bottom]))]
         (draw-box label (assoc attrs :borders borders)))))))

(defn draw-gap
  "Draws an indication of discontinuity. Takes a full row, the default
  total `:height` is 70, the default `:gap` distance within that is
  10, the default `:edge` height on either side of the gap is 15, and
  the default `:gap-style` used to draw the gap edges is `:dotted` but
  all of these can be overridden through the optional attribute spec.
  Advances to the next row before drawing.

  If `label` is provided, draws it to identify the content of the
  gap. If there are at least `:min-label-columns` (which defaults to
  8) remaining on the current row, will center the label in the
  remaining space on that row before drawing the gap. Otherwise it
  will advance to the next row, draw the label centered on the entire
  row, then draw the gap.

  When finishing off the previous row, the box is drawn by default in
  the standard `:box-above` style. You can change that by passing
  different attributes under the `:box-above-style` key (for example,
  use `{:box-above-style :box-above-related}` if the gap relates to
  the preceding box)."
  ([]
   (draw-gap nil nil))
  ([label]
   (draw-gap label nil))
  ([label attr-spec]
   (let [{:keys [height gap edge gap-style box-above-style min-label-columns]
          :or   {gap-style         (eval-attribute-spec :dotted)
                 box-above-style   (eval-attribute-spec :box-above)
                 height            70
                 gap               10
                 edge              15
                 min-label-columns 8}} (eval-attribute-spec attr-spec)

         column (:column @@('diagram-state @*globals*))
         boxes  @('boxes-per-row @*globals*)]
     (if label
       ;; We are supposed to draw a label.
       (if (<= min-label-columns (- boxes column))
         (draw-box label [{:span (- boxes column)} box-above-style]) ; And there is room for it on the current line.
         (do ; The label doesn't fit on the current line.
           (draw-box nil [{:span (- boxes column)} box-above-style]) ; Finish off current line with emptiness.
           (auto-advance-row)
           (draw-box label [{:span boxes :borders #{:left :right}}]))) ; Put the label on its own line.
       ;; We are not supposed to draw a label, so just finish the current line if needed.
       (when (not= column boxes)
         (draw-box nil [{:span (- boxes column)} :box-above]))) ; Finish off current line with emptiness.

     ;; Move on to a new row to draw the gap.
     (auto-advance-row)
     (let [y      (:y @@('diagram-state @*globals*))
           top    (+ y edge)
           left   @('left-margin @*globals*)
           right  (+ left (* @('box-width @*globals*) @('boxes-per-row @*globals*)))
           bottom (+ y (- height edge))]
       (draw-line left y left top)
       (draw-line right y right top)
       (draw-line left top right (- bottom gap) gap-style)
       (draw-line right y right (- bottom gap))
       (draw-line left (+ top gap) right bottom gap-style)
       (draw-line left (+ top gap) left bottom)
       (draw-line left bottom left (+ y height))
       (draw-line right bottom right (+ y height)))
     (let [state     (swap! @('diagram-state @*globals*)
                            (fn [current]
                              (-> current
                                  (update :y + height)
                                  (assoc :address 0)
                                  (assoc :gap? true))))
           header-fn @('row-header-fn @*globals*)]
       (when header-fn (draw-row-header (header-fn state)))))))

(defn draw-bottom
  "Ends the diagram by drawing a line across the box area. Needed if the
  preceding action was drawing a gap, to avoid having to draw an empty
  row of boxes, which would extend the height of the diagram without
  adding useful information."
  []
  (let [y    (:y @@('diagram-state @*globals*))
        left @('left-margin @*globals*)]
    (draw-line left y (+ left (* @('box-width @*globals*) @('boxes-per-row @*globals*))) y)))



;; Set up the context for the parser/evaluator for our domain-specific
;; language, a subset of Clojure which requires no compilation at
;; runtime, and which cannot do dangerous things like Java interop,
;; arbitrary I/O, or infinite iteration.

(def xml-bindings
  "The Analemma XML-manipulation functions we make available for
  building diagrams."
  (self-bind-symbols [xml/add-attrs
                      xml/add-content
                      xml/emit
                      xml/emit-attrs
                      xml/emit-tag
                      xml/get-attrs
                      xml/get-content
                      xml/get-name
                      xml/has-attrs?
                      xml/has-content?
                      xml/merge-attrs
                      xml/set-attrs
                      xml/set-content
                      xml/update-attrs]))

(def svg-bindings
  "The Analemma SVG-creation functions we make available for building
  diagrams."
  (self-bind-symbols [svg/add-style
                      svg/animate
                      svg/animate-color
                      svg/animate-motion
                      svg/animate-transform
                      svg/circle
                      svg/defs
                      svg/draw
                      svg/ellipse
                      svg/group
                      svg/image
                      svg/line
                      svg/parse-inline-css
                      svg/path
                      svg/polygon
                      svg/rect
                      svg/rgb
                      svg/rotate
                      svg/style
                      svg/style-map
                      svg/svg
                      svg/text
                      svg/text-path
                      svg/transform
                      svg/translate
                      svg/translate-value
                      svg/tref
                      svg/tspan]))

(def diagram-bindings
  "Our own functions which we want to make available for building
  diagrams."
  (self-bind-symbols [append-svg
                      defattrs
                      draw-bottom
                      draw-box
                      draw-boxes
                      draw-column-headers
                      draw-gap
                      draw-line
                      draw-related-boxes
                      draw-row-header
                      eval-attribute-spec
                      hex-text
                      next-row
                      text
                      tspan]))

(def initial-named-attributes
  "The initial contents of the lookup table for shorthand
  attribute map specifications."
  {:hex   {:font-size   18 ; Default style in which hex values are drawn.
           :font-family "Courier New, monospace"}
   :plain {:font-size   18 ; Default style in which ordinary text labels are drawn.
           :font-family "Palatino, Georgia, Times New Roman, serif"}
   :math  {:font-size   18 ; Default style in which variables and equations are drawn.
           :font-family "Palatino, Georgia, Times New Roman, serif"
           :font-style  "italic"}
   :sub   {:font-size "70%" ; Style for subscripted nested tspan objects.
           :dy        "0.5ex"}
   :super {:font-size "70%" ; Style for superscripted nested tspan objects.
           :dy        "-0.8ex"}
   :bold  {:font-weight "bold"} ; Adds bolding to the font style.

   :dotted {:stroke-dasharray "1,1"} ; Style for dotted lines.

   :border-unrelated {}                        ; Line style for borders between unrelated cells: use defaults.
   :border-related   {:stroke-dasharray "1,3"} ; Line style for borders between related cells.

   :box-first         {:borders {:left   :border-unrelated ; Style for first of a row of related boxes.
                                 :right  :border-related
                                 :top    :border-unrelated
                                 :bottom :border-unrelated}}
   :box-related       {:borders {:left   :border-related ; Style for internal box in a related row.
                                 :top    :border-unrelated
                                 :right  :border-related
                                 :bottom :border-unrelated}}
   :box-last          {:borders {:left   :border-related ; Style for last of a group of related boxes.
                                 :right  :border-unrelated
                                 :top    :border-unrelated
                                 :bottom :border-unrelated}}
   :box-above         {:borders #{:left :right :top}} ; Style for box open to row below.
   :box-above-related {:borders {:left  :border-related ; Stle for box open to row below, related to previous box.
                                 :right :border-unrelated
                                 :top   :border-unrelated}}
   :box-below         {:borders #{:left :right :bottom}}}) ; Style for box open to row above.

(defn default-row-header-fn
  "Returns an SVG text object containing the header for the current row
  in the default style, a hex value of at least two digits based on
  the current row address. If a gap has been generated, prefix that
  with _i+_ to indicate that it is relative to the first byte after
  the gap.

  This function is called with the current diagram state map. Defaults
  to the styling set up in the `:hex` attribute definition but with a
  font size of 11, explicitly \"normal\" font style, and dominant
  baseline of \"middle\", but these can be overridden by using
  `defattrs` to set up up an attribute spec named `:row-header`. Other
  SVG text attributes can be supplied in that attribute definition,
  and they will be passed along.

  The font family and style of the _i+_ post-gap prefix default to the
  values in the default `:math` attribute definition, but this can be
  overridden by setting `:math-family` and `:math-style` in the
  `:row-header` attribute definition."
  [{:keys [address gap?]}]
  (let [addr-label (cl-format nil "~2,'0x" address)
        defs       @@('named-attributes @*globals*)
        attr-spec  (:row-header defs)
        hex        (merge (:hex defs)
                          {:font-size         11
                           :font-style        "normal"
                           :dominant-baseline "middle"}
                          attr-spec)
        math       (merge (:math defs)
                          (select-keys hex [:font-size :dominant-baseline])
                          (when-let [family (:math-family attr-spec)]
                            {:font-family family})
                          (when-let [style (:math-style attr-spec)]
                            {:font-style style}))]
    (if gap?
      (text "i+" math [hex addr-label])
      (text addr-label hex))))

(defn build-initial-globals
  "Creates the contents of the global symbol table that will be
  established at the start of reading a diagram definition."
  []
  (merge
   {'left-margin   40 ; Space for row offsets and other leading marginalia.
    'right-margin  1  ; Space at the right, currently just enough to avoid clipping the rightmost box edges.
    'bottom-margin 1  ; Space at bottom, currently just enough to avoid clipping bottom box edges.
    'box-width     40 ; How much room each byte (or bit) box takes up.

    'column-labels (str/split "0,1,2,3,4,5,6,7,8,9,a,b,c,d,e,f" #",") ; The default column headers.
    'boxes-per-row 16 ; How many individual byte/bit boxes fit on each row.
    'row-height    30 ; The height of a standard row of boxes.

    'named-attributes (atom initial-named-attributes) ; Lookup table for shorthand attribute map specifications.
    'row-header-fn    default-row-header-fn           ; Used to generate row header text when required.

    ;; Values used to track the current state of the diagram being created:
    'diagram-state (atom {:column   0     ; Column of the next box to be drawn.
                          :y        1     ; Y coordinate of the top of the next row to be drawn.
                          :address  0     ; Memory address of the first byte in the current row.
                          :gap?     false ; Has a gap been drawn (making :address relative).
                          :svg-body '()})})) ; Gathers the SVG description as the diagram is built up.

(defn- build-vars
  "Creates the sci vars to populate the symbol table for the
  interpreter."
  []
  (reduce  (fn [acc [k v]]
             (assoc acc k (sci/new-var k v)))
          {}
          (build-initial-globals)))

(defn- emit-svg
  "Outputs the finished SVG."
  []
  (let [result @*globals*]
    (xml/emit (apply svg/svg {:width (+ @('left-margin result) @('right-margin result)
                                         (* @('box-width result) @('boxes-per-row result)))
                              :height (+ (:y @@('diagram-state @*globals*)) @('bottom-margin result))}
                     (:svg-body @@('diagram-state @*globals*))))))

(defn generate
  "Accepts Clojure-based diagram specification string and returns the
  corresponding SVG string."
  [source]
  (binding [*globals* (atom (build-vars))]
    (let [env  (atom {})
          opts {:preset     :termination-safe
                :env        env
                :namespaces {'user         (merge diagram-bindings @*globals*)
                             'analemma.svg svg-bindings
                             'analemma.xml xml-bindings}}]
      (sci/eval-string "(require '[analemma.xml :as xml])" opts)
      (sci/eval-string "(require '[analemma.svg :as svg])" opts)
      (sci/eval-string source opts)
      (when (pos? (:column @@('diagram-state @*globals*)))
        (next-row))  ; Finish off the last row.
      (emit-svg))))
