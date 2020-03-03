(ns org.deepsymmetry.bytefield.macros
  "The macros used by the bytefield generator.")

(defmacro self-bind-symbols
  "Builds a map in which each of the supplied list of symbols is mapped
  to itself (minus any namespace that the symbol might have had)."
  [syms]
  (let [syms-without-ns (map (comp symbol name) syms)]
    `(zipmap '[~@syms-without-ns] ~syms)))
