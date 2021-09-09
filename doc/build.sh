#!/usr/bin/env bash

# This script is run by Netlify continuous integration to build the
# Antora site hosting the bytefield-svg user guide.

# Test to try and figure out why CI is not working on Netlify
clojure --version

# If we haven't already, build bytefield-svg so we can create the
# example diagrams.
if [ ! -f "lib.js" ]
then
    npm run release
fi

# Use it to build the documentation site.
DOCSEARCH_ENABLED=true DOCSEARCH_ENGINE=lunr DOCSEARCH_INDEX_VERSION=latest \
  npx antora --fetch --generator antora-site-generator-lunr doc/netlify.yml
