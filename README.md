# bytefield-svg

A Node module for generating byte field diagrams inspired by the LaTeX
`bytefield` package using a Clojure-based domain specific language
(now built on top of [SCI](https://github.com/borkdude/sci), the Small
Clojure Interpreter).

[![License](https://img.shields.io/badge/License-Eclipse%20Public%20License%202.0-blue.svg)](#license)

## Status

The DSL has been nicely validated by porting all of the LaTeX
documents I needed it for to an [Antora documentation
site](https://djl-analysis.deepsymmetry.org/djl-analysis/track_metadata.html).

It has been published to npm, so you can install it by simply running:

    npm install bytefield-svg


I am currently in the process of documenting the DSL and this package
itself.

If all you want to do is build an SVG from the DSL, you can simply
write JavaScript to do it, once you've installed `bytefield-svg`:

```javascript
var generate = require('bytefield-svg');

var diagram = `
;; Put your diagram DSL here...
`;

process.stdout.write(generate(diagram));
```

And then you can just run it, assuming you saved it as `diagram.js`,
like so:

    node diagram.js >diagram.svg

But the package's main purpose is to act as an
[Asciidoctor](https://asciidoctor.org) extension hosted by an
[Antora](https://antora.org) plugin. This is done with the help of a
[framework](https://gitlab.com/djencks/asciidoctor-generic-svg-extension.js)
that [David Jencks](https://gitlab.com/djencks) has created.

However, plugin support for Antora is not yet released, so in order
for this to work you need to build and run one of David's [Antora fork
branches](https://gitlab.com/djencks/antora/-/tree/issue-585-with-377-582-git-credential-plugin).

Once those things are released, this will be a lot easier. If you want
to brave it before then, there are
[instructions](https://github.com/Deep-Symmetry/dysentery/tree/master/doc)
showing how to locally build the dysentery project documentation site,
which set up and use those special versions.

## Building

To build a development build of `bytefield-svg` from source, clone the
repository and make sure you have [Node.js](https://nodejs.org/en/)
and the [Clojure CLI
tools](https://clojure.org/guides/getting_started) installed, then
from the top-level directory of your cloned repo run:

    npm install
    npm run build

This will create the file `lib.js`. At that point, you can generate
diagrams like [this one](https://deepsymmetry.org/images/test.svg) by
running:

    node test.js >test.svg

(The [`test.js`
file](https://github.com/Deep-Symmetry/bytefield-svg/blob/master/test.js)
is present in the project, and with some well-designed helper
functions in that project's own include file, the source for an even
more attractive version of the diagram shrinks to
[this](https://github.com/Deep-Symmetry/dysentery/blob/379555f21244354c4dc0c9711c8cb3a3552bc64b/doc/modules/ROOT/examples/dbserver_shared.edn)).

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
