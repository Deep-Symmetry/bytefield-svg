#!/usr/bin/env bash

# This script is run by Netlify continuous integration to build the
# Antora site hosting the bytefield-svg user guide.

# If we haven't already, build bytefield-svg so we can create the
# example diagrams.
if [ ! -f "lib.js" ]
then
    npm run release
fi

# Use it to build the documentation site.
DOCSEARCH_ENABLED=true DOCSEARCH_ENGINE=lunr npx antora --fetch doc/netlify.yml --generator antora-site-generator-lunr
