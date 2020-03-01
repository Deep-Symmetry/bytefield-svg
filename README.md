# bytefield-svg

A Node module for generating byte field diagrams inspired by the LaTeX
`bytefield` package using a Clojure-based domain specific language.

This is at the proof-of-concept stage, but can already generate
diagramas like [this one](https://deepsymmetry.org/images/test.svg) by
running:

    node test.js >test.svg

(The [`test.js` file](http:test.js) is present in the project).

The plan is to make this available through `npm` once it is stable,
and to collaborate on getting it built into an Asciidoctor extension.

To compile the library, install
[shadow-cljs](https://github.com/thheller/shadow-cljs), and run:

    shadow-cljs compile lib

This will create the file `lib.js`. At that point, you can try
building the test SVG file as shown above.
