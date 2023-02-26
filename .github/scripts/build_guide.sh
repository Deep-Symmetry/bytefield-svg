#!/usr/bin/env bash

# There is no point in doing this if we lack the SSH key to publish the guide.
if [ "$GUIDE_SSH_KEY" != "" ]; then

    # Set up node dependencies
    npm install

    # If we haven't already, build bytefield-svg so we can create the
    # example diagrams.
    if [ ! -f "lib.js" ]
    then
        npm run release
    fi

    # Use it to build the documentation site.
    npx antora --fetch doc/github-actions.yml

    # Publish it to the right place on the Deep Symmetry web server.
    rsync -avz doc/build/site/ guides@deepsymmetry.org:/var/www/guides/bytefield-svg/

else
    echo "No SSH key present, not building user guide."
fi
