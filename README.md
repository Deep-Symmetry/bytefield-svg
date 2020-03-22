# bytefield-svg

A Node module for generating byte field diagrams inspired by the LaTeX
`bytefield` package using a Clojure-based domain specific language
(now built on top of [SCI](https://github.com/borkdude/sci), the Small
Clojure Interpreter).

[![License](https://img.shields.io/badge/License-Eclipse%20Public%20License%202.0-blue.svg)](#license)

## Status

The DSL seems feature-complete, and can generate diagrams like [this
one](https://deepsymmetry.org/images/test.svg) by running:

    node test.js >test.svg

(The [`test.js`
file](https://github.com/Deep-Symmetry/bytefield-svg/blob/master/test.js)
is present in the project, and with some well-designed helper
functions in that project's own include file, the source for an even
more attractive version of the diagram shrinks to
[this](https://github.com/Deep-Symmetry/dysentery/blob/379555f21244354c4dc0c9711c8cb3a3552bc64b/doc/modules/ROOT/examples/dbserver_shared.edn)).

[David Jencks](https://gitlab.com/djencks) has been helping build a
framework that will run this as an
[Asciidoctor](https://asciidoctor.org) extension hosted by an
[Antora](https://antora.org) plugin. Until plugin support has been
merged into Antora and released, it needs to run on unreleased
branches, but that is already working very well. The [new version of
the dysentery DJ Link Packet
Analysis](https://djl-analysis.deepsymmetry.org) is being built using
this approach. Follow that link if you want to see exactly why this
was created.

It has not yet been published to npm; I am holding off on doing that
until the DSL has proven itself in the course of converting the LaTeX
documents I developed this for. The DSL and this package itself needs
to be documented as well. I expect it will be ready in a week or two.
Until then, clone the repository and build it by making sure you have
[Node.js](https://nodejs.org/en/) and the [Clojure CLI
tools](https://clojure.org/guides/getting_started) installed, and run:

    npm install
    npm run build

This will create the file `lib.js`. At that point, you can try
building the test SVG file as shown above.

To check for outdated dependencies, you can run:

    clojure -A:outdated

## Releasing

To cut a release, check for outdated dependencies as above, update the
version in `package.json`, tag and push to GitHub, then run:

    npm install
    npm run release
    npm publish

## License

<a href="http://deepsymmetry.org"><img align="right" alt="Deep Symmetry"
 src="doc/assets/DS-logo-bw-200-padded-left.png" width="216" height="123"></a>

Copyright © 2020 [Deep Symmetry, LLC](http://deepsymmetry.org)

Distributed under the [Eclipse Public License
2.0](https://opensource.org/licenses/EPL-2.0). By using this software
in any fashion, you are agreeing to be bound by the terms of this
license. You must not remove this notice, or any other, from this
software.
