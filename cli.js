#!/usr/bin/env node

// A simple command line interface for rendering diagrams from standard input or a file,
// to standard output or a file.

const generate = require('./lib.js'); // The bytefield-svg DSL interpreter and diagram generator
const fs = require('fs'); // Used to read and write files if needed.
const commandLineArgs = require('command-line-args');
const commandLineUsage = require('command-line-usage');

// Set up definitions and parse command-line arguments before deciding how to proceed.
const optionDefinitions = [{
    name: 'help',
    alias: 'h',
    type: Boolean,
    description: 'Display this usage guide.'
  },
  {
    name: 'source',
    alias: 's',
    type: String,
    defaultOption: true,
    description: 'File from which to read the diagram source, defaults to standard in.'
  },
  {
    name: 'output',
    alias: 'o',
    type: String,
    description: 'File to which to write the SVG diagram, defaults to standard out.'
  },
  {
    name: 'embedded',
    alias: 'e',
    type: Boolean,
    description: 'Emit a simple <svg> tag suitable for embedding in an HTML document. ' +
      '(The default is to emit a full SVG file with XML version and namespaces.)'
  }
];
const options = commandLineArgs(optionDefinitions);

// Show usage if the user asked for help.
if (options.help) {
  const usage = commandLineUsage([{
      header: 'bytefield-svg',
      content: `Generate byte field diagrams in SVG format from a Clojure-based
      domain-specific language. See the project home for the language
      definition and examples.`
    },
    {
      header: 'Options',
      optionList: optionDefinitions
    },
    {
      content: 'Project home: {underline https://github.com/Deep-Symmetry/bytefield-svg}'
    }
  ]);
  console.log(usage);
} else {
  // Actually read the source and render a diagram.
  const source = fs.readFileSync(options.source || 0, 'UTF-8');
  const diagram = generate(source, {
    "embedded": options.embedded
  });
  if (options.output) { // We were asked to write to a specific file.
    fs.writeFileSync(options.output, diagram, "UTF-8");
  } else { // Write to standard out.
    process.stdout.write(diagram);
  }
}