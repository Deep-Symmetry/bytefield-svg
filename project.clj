(defproject bytefield-svg "0.1.0"
  :description "Inspired by the LaTeX package for creating byte field diagrams."
  :url "http://github.com/Deep-Symmetry/bytefield-svg"
  :license {:name "Eclipse Public License 2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.597"]
                 [org.clojure/tools.reader "1.3.2"]
                 [org.clojars.brunchboy/analemma "1.1.0"]]

  :plugins [[lein-cljsbuild "1.1.7"]]

  :cljsbuild {:builds {
                       :main {:source-paths ["src"]
                              :compiler     {:output-to      "package/index.js"
                                             :target         :nodejs
                                             :output-dir     "target"
                                             ;; :externs ["externs.js"]
                                             :optimizations  :advanced
                                             :pretty-print   true
                                             :parallel-build true}}}})
