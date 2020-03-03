# bytefield-svg

A Node module for generating byte field diagrams inspired by the LaTeX
`bytefield` package using a Clojure-based domain specific language.

[![License](https://img.shields.io/badge/License-Eclipse%20Public%20License%202.0-blue.svg)](#license)

## Status

This is at the proof-of-concept stage, but can already generate
diagrams like [this one](https://deepsymmetry.org/images/test.svg) by
running:

    node test.js >test.svg

(The [`test.js`
file](https://github.com/Deep-Symmetry/bytefield-svg/blob/master/test.js)
is present in the project).

The plan is to collaborate on getting this built into an Asciidoctor
extension.

To compile the library, install
[shadow-cljs](https://github.com/thheller/shadow-cljs), and run:

    shadow-cljs compile lib

This will create the file `lib.js`. At that point, you can try
building the test SVG file as shown above.

## License

<a href="http://deepsymmetry.org"><img align="right" alt="Deep Symmetry"
 src="doc/assets/DS-logo-bw-200-padded-left.png" width="216" height="123"></a>

Copyright 2020 [Deep Symmetry, LLC](http://deepsymmetry.org)

Distributed under the [Eclipse Public License
2.0](https://opensource.org/licenses/EPL-2.0). By using this software
in any fashion, you are agreeing to be bound by the terms of this
license. You must not remove this notice, or any other, from this
software.
