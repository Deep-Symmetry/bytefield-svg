(ns org.deepsymmetry.bytefield.core
  "Provides a safe yet robust subset of the Clojure programming
  language, tailored to the task of building SVG diagrams in the style
  of the LaTeX `bytefield` package."
  (:require [clojure.string :as str]
            [analemma.svg :as svg]
            [analemma.xml :as xml]
            [sci.core :as sci])
  (:require-macros [org.deepsymmetry.bytefield.macros :refer [self-bind-symbols]]))

;; Default style definitions.

(def serif-family
  "The font family we use for serif text."
  "Palatino, Georgia, Times New Roman, serif")

(def hex-family
  "The font family we use for hex text."
  "Courier New, monospace")



;; The global symbol table used when evaluating diagram source.

(def ^:dynamic *globals*
  "Holds the globals during the building of a diagram. Dynamically bound
  to a new atom for each thread that calls `generate`, so that can be
  thread-safe. In retrospect, that was over-engineering, but hey... it
  might be used in a web context someday?"
  nil)



;; The diagram-drawing functions we make available for conveniently
;; creating byte field diagrams.

(defn append-svg
  "Adds another svg element to the body being built up."
  [element]
  (sci/alter-var-root ('svg-body @*globals*) concat [element]))


(defn draw-column-headers
  "Generates the header row that identifies each byte/bit box. By
  default uses the lower-case hex digits in increasing order, but you
  can pass in your own list of `:labels`. Normally consumes 14
  vertical pixels, but you can pass in a different `:height`. Defaults
  to a `:font-size` of 7 and `:font-family` of \"Courier New\" but
  these can be overridden as well. Other SVG text options can be
  supplied as keyword arguments, and they will be passed along."
  [& {:keys [labels height font-size font-family]
      :or   {labels      (str/split "0,1,2,3,4,5,6,7,8,9,a,b,c,d,e,f" #",")
             height      14
             font-size   11
             font-family "Courier New, monospace"}
      :as   options}]
  (let [y    (+ @('diagram-y @*globals*) (* 0.5 height))
        body (for [i (range @('boxes-per-row @*globals*))]
               (let [x (+ @('left-margin @*globals*) (* (+ i 0.5) @('box-width @*globals*)))]
                 (svg/text (merge (dissoc options :labels :height)
                                  {:x                 x
                                   :y                 y
                                   :font-family       font-family
                                   :font-size         font-size
                                   :dominant-baseline "middle"
                                   :text-anchor       "middle"})
                           (nth labels i))))]
    (sci/alter-var-root ('diagram-y @*globals*) + height)
    (sci/alter-var-root ('svg-body @*globals*) concat body)))

(defn draw-row-header
  "Generates the label in the left margin which identifies the starting
  byte of a row. Defaults to a `:font-size` of 11 and `:font-family` of
  \"Courier New\" but these can be overridden as well. Other SVG text
  options can be supplied as keyword arguments, and they will be
  passed along."
  [label & {:keys [font-size font-family]
            :or   {font-size   11
                   font-family "Courier New, monospace"}
            :as   options}]
  (let [x (- @('left-margin @*globals*) 5)
        y (+ @('diagram-y @*globals*) (* 0.5 @('row-height @*globals*)))]
    (append-svg (svg/text (merge options
                                 {:x                 x
                                  :y                 y
                                  :font-family       font-family
                                  :font-size         font-size
                                  :dominant-baseline "middle"
                                  :text-anchor       "end"})
                          label))))

(defn draw-line
  "Adds a line to the SVG being built up. SVG line attributes can be
  overridden by passing additional keyword/value pairs."
  [x1 y1 x2 y2]
  (append-svg (svg/line x1 y1 x2 y2 :stroke "#000000" :stroke-width 1)))

(defn next-row
  "Advances drawing to the next row of boxes, reseting the index to 0.
  The height of the row defaults to `row-height` but can be overridden
  by passing a different value with `:height`."
  [& {:keys [height]
      :or   {height @('row-height @*globals*)}}]
  (sci/alter-var-root ('diagram-y @*globals*) + height)
  (sci/alter-var-root ('box-index @*globals*) (constantly 0)))

(defn draw-box
  "Draws a single byte or bit box in the current row at the current
  index. Text content can be passed with `:text`. The default size is
  that of a single byte (or bit) but this can be overridden with
  `:span`. Normally draws all borders, but you can supply the set you
  want drawn in `:borders`. The background can be filled with a color
  passed with `:fill`. Box height defaults to `row-height`, but that
  can be changed with `:height` (you will need to supply the same
  height override when calling `next-row`)."
  [& {:keys [text span borders fill height]
      :or   {span    1
             borders #{:left :right :top :bottom}
             height  @('row-height @*globals*)}}]
  (let [left   (+ @('left-margin @*globals*) (* @('box-index @*globals*) @('box-width @*globals*)))
        width  (* span @('box-width @*globals*))
        right  (+ left width)
        top    @('diagram-y @*globals*)
        bottom (+ top height)]
    (when fill (append-svg (svg/rect left top height width :fill fill)))
    (when (borders :top) (draw-line left top right top))
    (when (borders :bottom) (draw-line left bottom right bottom))
    (when (borders :right) (draw-line right top right bottom))
    (when (borders :left) (draw-line left top left bottom))
    (when text
      (append-svg (xml/add-attrs text
                             :x (/ (+ left right) 2.0)
                             :y (+ top 1 (/ height 2.0))
                             :dominant-baseline "middle"
                             :text-anchor "middle")))
    (sci/alter-var-root ('box-index @*globals*) + span)))

(defn label-text
  "Builds an SVG text object to represent a named value, with an
  optional subscript. Defaults are established for the font size,
  family, and style, but they can be overridden in `options`,
  and other SVG attributes can be passed that way as well."
  ([label]
   (label-text label nil nil))
  ([label subscript]
   (label-text label subscript nil))
  ([label subscript {:keys [ font-size font-family font-style]
                     :or   {font-size   18
                            font-family serif-family
                            font-style  "italic"}
                     :as   options}]
   (apply svg/text (concat
                    [(merge (dissoc options :label :subscript)
                            {:font-size         font-size
                             :font-family       font-family
                             :font-style        font-style
                             :dominant-baseline "middle"
                             :text-anchor       "middle"})
                     label]
                    (when subscript
                      [(svg/tspan {:baseline-shift "sub"
                                   :font-size      "70%"}
                                  subscript)])))))

(defn hex-text
  "Builds an SVG text object to represent a hexadecimal value.
  Defaults are established for the font size and family, but they can
  be overridden in `options`, and other SVG attributes can be passed
  that way as well."
  ([hex]
   (hex-text hex nil))
  ([hex {:keys [font-size font-family]
         :or   {font-size   18
                font-family hex-family}
         :as   options}]
   (svg/text (merge (dissoc options :label :subscript)
                    {:font-size         font-size
                     :font-family       font-family
                     :dominant-baseline "middle"
                     :text-anchor       "middle"})
             hex)))

(defn draw-group-label-header
  "Creates a small borderless box used to draw the textual label headers
  used below the byte labels for `remotedb` message diagrams.
  Arguments are the number of colums to span and the text of the
  label."
  [span text]
  (draw-box :span span :text (label-text text nil {:font-size 12}) :borders #{} :height 14))

(defn draw-gap
  "Draws an indication of discontinuity. Takes a full row, the default
  height is 50 and the default gap is 10, and the default edge on
  either side of the gap is 5, but all can be overridden with keyword
  arguments."
  [& {:keys [height gap edge]
      :or   {height 70
             gap    10
             edge   15}}]
  (let [y      @('diagram-y @*globals*)
        top    (+ y edge)
        left   @('left-margin @*globals*)
        right  (+ left (* @('box-width @*globals*) @('boxes-per-row @*globals*)))
        bottom (+ y (- height edge))]
    (draw-line left y left top)
    (draw-line right y right top)
    (append-svg (svg/line left top right (- bottom gap) :stroke "#000000" :stroke-width 1
                          :stroke-dasharray "1,1"))
    (draw-line right y right (- bottom gap))
    (append-svg (svg/line left (+ top gap) right bottom :stroke "#000000" :stroke-width 1
                          :stroke-dasharray "1,1"))
    (draw-line left (+ top gap) left bottom)
    (draw-line left bottom left (+ y height))
    (draw-line right bottom right (+ y height)))
  (sci/alter-var-root ('diagram-y @*globals*) + height))

(defn draw-bottom
  "Ends the diagram by drawing a line across the box area. Needed if the
  preceding action was drawing a gap, to avoid having to draw an empty
  row of boxes, which would extend the height of the diagram without
  adding useful information."
  []
  (let [y    @('diagram-y @*globals*)
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
  (self-bind-symbols [draw-bottom
                      draw-box
                      draw-column-headers
                      draw-gap
                      draw-group-label-header
                      draw-line
                      draw-row-header
                      hex-text
                      label-text
                      next-row]))

(def initial-globals
  "The contents of the global symbol table that will be established at
  the start of reading a diagram definition."
  (merge
   {'left-margin   40 ; Space for row offsets and other leading marginalia.
    'right-margin  1  ; Space at the right, currently just enough to avoid clipping the rightmost box edges.
    'bottom-margin 1  ; Space at bottom, currently just enough to avoid clipping bottom box edges.
    'box-width     40 ; How much room each byte (or bit) box takes up.

    'boxes-per-row 16 ; How many individual byte/bit boxes fit on each row.
    'row-height    30 ; The height of a standard row of boxes.

    ;; Web-safe font families used for different kinds of text.
    'serif-family serif-family
    'hex-family   hex-family

    ;; Some nice default background colors, used in `remotedb` message headers.
    'green  "#a0ffa0"
    'yellow "#ffffa0"
    'pink   "#ffb0a0"
    'cyan   "#a0fafa"
    'purple "#e4b5f7"

    ;; Values used to track the current state of the diagram being created:
    'box-index 0 ; Row offset of the next box to be drawn.
    'diagram-y 5 ; The y coordinate of the top of the next row to be drawn.
    'svg-body  '()}))

(defn- build-vars
  "Creates the sci vars to populate the symbol table for the
  interpreter."
  []
  (reduce  (fn [acc [k v]]
             (assoc acc k (sci/new-var k v)))
          {}
          initial-globals))

(defn- emit-svg
  "Outputs the finished SVG."
  []
  (let [result @*globals*]
    (xml/emit (apply svg/svg {:width (+ @('left-margin result) @('right-margin result)
                                         (* @('box-width result) @('boxes-per-row result)))
                              :height (+ @('diagram-y result) @('bottom-margin result))}
                     @('svg-body result)))))

(defn generate
  "Accepts Clojure-based diagram specification string and returns the
  corresponding SVG string."
  [source]
  (binding [*globals* (atom (build-vars))]
    (let [env  (atom {})
          opts {:preset     :termination-safe
                :env        env
                :namespaces {'user (merge diagram-bindings @*globals*)
                             'svg  svg-bindings
                             'xml  xml-bindings}}]
      (sci/eval-string "(require '[xml])" opts)
      (sci/eval-string "(require '[svg])" opts)
      (sci/eval-string source opts)
      (emit-svg))))
