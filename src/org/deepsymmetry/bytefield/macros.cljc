(ns org.deepsymmetry.bytefield.macros
  "The macros used by the bytefield generator.")

(defmacro self-bind-symbols
  "Builds a map in which each of the supplied list of symbols is mapped
  to itself."
  [syms]
  `(zipmap '[~@syms] ~syms))
