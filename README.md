# bytefield-svg

A Node module for generating byte field diagrams inspired by the LaTeX
`bytefield` package using a Clojure-based domain specific language
(now built on top of [SCI](https://github.com/borkdude/sci), the Small
Clojure Interpreter).

[![License](https://img.shields.io/badge/License-Eclipse%20Public%20License%202.0-blue.svg)](#license)

## Status

This is just past the proof-of-concept stage, and can already generate
diagrams like [this one](https://deepsymmetry.org/images/test.svg) by
running:

    node test.js >test.svg

(The [`test.js`
file](https://github.com/Deep-Symmetry/bytefield-svg/blob/master/test.js)
is present in the project).

[David Jencks](https://gitlab.com/djencks) has been helping build a
framework that will run this as an [Asciidoctor](asciidoctor.org)
extension hosted by an [Antora](antora.org) plugin. Until plugin
support has been merged into Antora and released, it needs to run on
unreleased branches, but that is already working very well. The [new
version of the dysentery DJ Link Packet
Analysis](https://djl-analysis.deepsymmetry.org) is being built using
this approach. Follow that link if you want to see exactly why this
was created.

To compile the library, run:

    npm install
    npm run build

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
