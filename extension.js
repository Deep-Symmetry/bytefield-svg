'use strict'

// This file is used to build the documentation site using the compiled bytefield-svg
// script to draw the sample diagrams. It will only work if the project has been built
// first, following the instructions in README.md.

const processor = require('./lib.js'); // The compiled byte field diagram generator.

// Register as a block processor of type 'bytefield'
module.exports = function (registry) {

  function toBlock(attrs, parent, source, line_info, self) {
    if (typeof attrs === 'object' && '$$smap' in attrs) {
      attrs = fromHash(attrs)
    }
    const doc = parent.getDocument()
    const subs = attrs.subs
    if (subs) {
      source = doc.$apply_subs(source, doc.$resolve_subs(subs))
    }
    var svgText
    try {
      svgText = processor(source, {
        "embedded": true
      })
    } catch (err) {
      console.log(`error after ${line_info}: ${err.toString()}`)
      svgText = `error after ${line_info}: ${err.toString()}`
    }
    const idAttr = attrs.id ? ` id="${attrs.id}"` : ''
    const classAttr = attrs.role ? `${attrs.role} imageblock bytefield` : `imageblock bytefield`
    const title_el = attrs.title ? `\n<div class="title">${attrs.title}</div>` : ''
    const svgBlock = self.$create_pass_block(
      parent,
      `<div${idAttr} class="${classAttr}">\n<div class="content">${svgText}</div>${title_el}\n</div>`,
      // eslint-disable-next-line no-undef
      Opal.hash({})
    )
    return svgBlock
  }

  registry.block(function () {
    const self = this
    self.named('bytefield')
    self.onContext(['listing', 'literal'])
    self.process(function (parent, reader, attrs) {
      const line_info = reader.$line_info()
      var source = reader.getLines().join('\n')
      return toBlock(attrs, parent, source, line_info, self)
    })
  })
}
