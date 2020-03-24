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
changes you are making, there are some extra steps you need to take
for the time being, because it relies on an as-yet-unreleased fork of
Antora. This will get easier once Antora catches up and these are
released, but for now:

1. Clone and build [this
   branch](https://gitlab.com/djencks/antora/-/tree/issue-585-with-377-582-git-credential-plugin)
   of the Antora fork that [David Jencks](https://gitlab.com/djencks)
   created, which has the unreleased plugin feature.

       git clone https://gitlab.com/djencks/antora.git
       cd antora
       git checkout issue-585-with-377-582-git-credential-plugin
       yarn

2. Set the environment variable `ANTORA_DJ` to the absolute path of
   the file `packages/cli/bin/antora` which was created in the
   directory in which you cloned and built that Antora branch.

3. `cd` into the `bytefield-svg` repository and run the following commands:

       npm install
       npm run release

This will install the remaining development dependencies needed for
building the documentation site, and then build `bytefield-svg`
itself, which is needed to render the example diagrams. Assuming your
`ANTORA_DJ` envronment variable was properly set to point to the
antora fork repository, you can now successfully execute this command
from the root of the `bytefield-svg` repository whenever you want to build
the documentation locally:

       $ANTORA_DJ --fetch doc/local.yml

Running that will result in building the documentation site in the
`doc/build` subdirectory, based on the current source in your
repository. You can view it by telling a browser to open
`doc/build/site/index.html`.
