# User Guide

This holds the source for the [user
guide](https://bytefield-svg.netlify.com/) that explains how to use
`bytefield-svg`.

It is built by [Antora](https://antora.org) using a [build
script](build.sh) on [Netlify](https://netlify.com), which runs the
appropriate [playbook](netlify.yml) on the [component
descriptor](antora.yml) and [source](modules/ROOT).

## Building Locally

If you would like to build the documentation site in order to preview
changes you are making, you can use an `npm` script to do it, because
`npm` has already installed the dependencies needed for building the
documentation site, including David Jencks’ not-yet-merged enhancements
to Antora.

3. `cd` into the `bytefield-svg` repository and run the following commands:

        npm install
        npm run release
        npm run local-docs

This will install the development dependencies needed for building the
module and documentation site, and then build `bytefield-svg` itself,
which is needed to render the example diagrams.

Running that will result in building the documentation site in the
`doc/build` subdirectory, based on the current source in your
repository. You can view it by telling a browser to open
`doc/build/site/index.html`.
