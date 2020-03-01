(ns org.deepsymmetry.bytefield.svg
  (:require [clojure.string :as str]
            [org.deepsymmetry.bytefield.xml :as xml]))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SVG FUNCTIONS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn svg [& content]
  (let [xmlns {"xmlns:svg" "http://www.w3.org/2000/svg"
	       "xmlns" "http://www.w3.org/2000/svg"
	       "xmlns:xlink" "http://www.w3.org/1999/xlink"
	       "version" "1.0"}
	attrs (if (map? (first content)) (first content) {})
	content (if (map? (first content)) (rest content) content)]
    (concat [:svg (merge xmlns attrs)] content)))

(defn style-map [elem props]
  (let [styling (when (seq props)
		  (reduce (fn [s [k v]]
			    (str s " " (name k) ": "
				 (if (keyword? v)
				   (name v)
				   v)
				 "; "))
			  "" props))]
    (xml/add-attrs elem :style styling)))

(defn style [elem & properties]
  (style-map elem (apply hash-map properties)))

(defn line [x1 y1 x2 y2 & options]
  (let [attrs (apply hash-map options)]
    [:line (apply merge {:x1 x1, :y1 y1, :x2 x2, :y2 y2} attrs)]))

(defn rect [x y height width & options]
  (let [attrs (apply hash-map options)]
    [:rect (apply merge {:x x, :y y, :height height, :width width} attrs)]))

(defn circle [cx cy r & options]
  (let [attrs (apply hash-map options)]
    [:circle (apply merge {:cx cx, :cy cy, :r r} attrs)]))

(defn ellipse [cx cy rx ry & options]
  (let [attrs (apply hash-map options)]
    [:ellipse (apply merge {:cx cx, :cy cy, :rx rx, :ry ry} attrs)]))

(defn polygon [[& points] & options]
  (let [attrs (apply hash-map options)
	points (reduce (fn [s [x y]] (str s " " x "," y))
		       "" (partition 2 points))]
    [:polygon (apply merge {:points points}
		     attrs)]))

(defn text [& content]
  (concat [:text] content))

(defn group [& content]
  (cons :g content))

(defn draw [& commands]
  (reduce (fn [s [cmd args]] (str s " " (name cmd) (apply str (interpose "," args))))
	  "" (partition 2 commands)))

(defn path [draw-commands & options]
  (let [attrs (apply hash-map options)]
    [:path (merge {:d (apply draw draw-commands)} attrs)]))

(defn tref [id]
  [:tref {"xlink:href" (str "#" (name id))}])

(defn rgb [r g b]
  (str "rgb(" r "," g "," b ")"))

(defn animate [elem attr & attrs]
  (-> elem
      (xml/add-content (-> [:animate {:attributeName (name attr),
				      :begin 0, :fill "freeze"}]
			   (xml/merge-attrs (apply hash-map attrs))))))

(defn animate-motion [elem & attrs]
  (-> elem
      (xml/add-content (-> [:animateMotion {:begin 0, :fill "freeze"}]
			   (xml/merge-attrs (apply hash-map attrs))))))

(defn animate-color [elem attr & attrs]
  (-> elem
      (xml/add-content (-> [:animateColor {:attributeName (name attr),
					   :begin 0, :fill "freeze"}]
			   (xml/merge-attrs (apply hash-map attrs))))))

(defn animate-transform [elem & attrs]
  (-> elem
      (xml/add-content (-> [:animateTransform {:attributeName "transform"
					       :begin 0, :fill "freeze"}]
			   (xml/merge-attrs (apply hash-map attrs))))))

(defn transform [elem trans]
  (let [attrs (xml/get-attrs elem)
	trans (if (:transform attrs)
		(str (:transform attrs) " " trans)
		trans)]
    (xml/add-attrs elem :transform trans)))

(defn rotate [elem angle x y]
  (transform elem (str "rotate(" angle "," x "," y ")")))

(defn translate
  ([elem x] (transform elem (str "translate(" x ")")))
  ([elem x y] (transform elem (str "translate(" x "," y ")"))))

(defn defs [[& bindings]]
  (let [bindings (partition 2 bindings)
	f (fn [defs-tag [id tag]]
	    (conj defs-tag
		  (xml/add-attrs tag :id (name id))))]
    (reduce f [:defs] bindings)))

(defn text-path [text path-id]
  [:textPath {"xlink:href" (str "#" (name path-id))} text])

(defn tspan [& content]
  (concat [:tspan] content))

(defn image [href & options]
  (let [attrs (apply hash-map options)]
    [:image (merge {"xlink:href" href} attrs)]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; UTILITY FUNCTIONS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn translate-value [v from-min from-max to-min to-max]
  (let [scale (/ (- to-max to-min)
		 (- from-max from-min))
	trans (- to-min (* from-min scale))]
    (float (+ (* v scale) trans))))

(defn parse-inline-css [css-str]
  (reduce (fn [m [k v]] (assoc m (keyword k) v))
	  {} (map #(str/split % #":") (str/split css-str #";"))))

(defn add-style [elem & styling]
  (let [css-str (or (:style (xml/get-attrs elem)) "")]
    (style-map elem (apply merge (parse-inline-css css-str) (apply hash-map styling)))))
