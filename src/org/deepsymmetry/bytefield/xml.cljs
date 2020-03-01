(ns org.deepsymmetry.bytefield.xml)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; XML FUNCTIONS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn has-attrs? [tag]
  (map? (second tag)))

(defn has-content? [tag]
  (if (has-attrs? tag)
    (> (count tag) 2)
    (> (count tag) 1)))

(defn get-name [tag]
  (if-let [n (first tag)]
    (name n)))

(defn get-attrs [tag]
  (if (has-attrs? tag) (second tag) {}))

(defn get-content [tag]
  (if (has-attrs? tag)
    (drop 2 tag)
    (rest tag)))

(defn set-attrs [tag attrs]
  (concat [(get-name tag) attrs]
	  (get-content tag)))

(defn set-content [tag & content]
  (concat [(get-name tag) (get-attrs tag)]
	  content))

(defn add-attrs [tag & attrs]
  (concat [(get-name tag)
	   (apply assoc (get-attrs tag) attrs)]
	  (get-content tag)))

(defn merge-attrs [tag attrs]
  (concat [(get-name tag)
	   (merge (get-attrs tag) attrs)]
	  (get-content tag)))

(defn add-content [tag & content]
  (concat [(get-name tag)
	   (get-attrs tag)]
	  (concat (get-content tag)
		  content)))

(defn update-attrs [tag [& keys] update-fn & args]
  (set-attrs tag (apply update-in (get-attrs tag) keys update-fn args)))

(defn emit-attrs [attrs]
  (when attrs
    (reduce (fn [s [k v]]
	      (str s (name k) "=\"" (if (keyword? v) (name v) v) "\" "))
	    "" attrs)))

(defn emit-tag [tag]
  (if-let [n (get-name tag)]
    (str "<" n " "
	(emit-attrs (get-attrs tag))
	(if (seq (get-content tag))
	  (str ">" (apply str (map #(if (string? %) % (emit-tag %)) (get-content tag)))
	       "</" n ">")
	  "/>"))))

(defn emit [& tags]
  (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
       (reduce #(str %1 (emit-tag %2)) "" tags)))
