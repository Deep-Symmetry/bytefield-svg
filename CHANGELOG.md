# Change Log

All notable changes to this project will be documented in this file.
This change log follows the conventions of
[keepachangelog.com](https://keepachangelog.com/).

## [Unreleased][unreleased]

Nothing so far.

## [1.10.0] - 2024-08-11

### Fixed

- Compiled for release before publishing to npm.

## [1.9.0] - 2024-08-11

### Added

- Gaps (both vertical and inline) can now be filled with a color so
  they properly attach to colored boxes, thanks to a contribution from
  [@simonarnell](https://github.com/simonarnell).

> This was accidentally published as a non-release build, please use
> 1.10 instead!

## [1.8.0] - 2023-02-19

### Added

- It is now possible to change the horizontal alignment of text labels
  within `draw-box` using two new box attributes, `:text-anchor` and
  `:margin`.

## [1.7.0] - 2022-11-15

### Added

- A new function, `char->int`, allowing access to the UTF-16 code
  units of characters. (Thanks to [Gert Goet](https://github.com/eval)
  for this contribution!)
- Access to the JavaScript
  [`String`](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/String)
  object to support the implementation of `char->int`.

## [1.6.1] - 2022-02-10

### Fixed

- The ability to control substitutions, and thereby (for example) use
  document attributes to reduce duplication, was broken. Thanks to
  [Jared Reisinger](https://github.com/JaredReisinger) for noticing
  this and contributing a fix!

## [1.6.0] - 2021-09-08

### Added

- A new predefined value, `svg-attrs`, which starts out as an empty
  map, but can be redefined to add arbitrary SVG attributes to the
  top-level SVG node of your diagram (for example, to set a background
  color for the entire diagram).

## [1.5.0] - 2020-07-12

### Added

- Two new functions, `wrap-link` and `wrap-svg` which allow you to
  create hyperlinks and arbitrary SVG elements around sections of your
  drawings.

## [1.4.3] - 2020-05-15

### Fixed

- Short-lived version 1.4.2 was not properly built for release before
  publishing to npm.

### Changed

- `defattrs` now takes an attribute expression rather than only a
  resolved attribute map as its second expression, so you can use the
  concise attribute mini-language to build new attributes based on
  existing ones.

### Added

- You can now pass `:next-row-height` as an attribute to `draw-box`
  when you need to change row heights as you draw the first box of a
  new row. This makes it more practical to have variable-height rows,
  while still having the automatically-drawn row headers appear in the
  correct posititons.
- The user guide now shows examples of how to draw vertical text and
  boxes which span more than two rows but which are not
  variable-length gaps.


## [1.4.1] - 2020-04-04

### Fixed

- Invoking the `generate` function with a single argument was not
  working properly any longer because of a problem handling default
  JavaScript arguments from ClojureScript code. (Sorry, I am really
  new at the JavaScript/ClojureScript worlds!)
- Generation of the user guide using the local extension now uses
  the new embedded SVG tag mode, to avoid HTML structural issues.

## [1.4.0] - 2020-04-05

### Added

- The `svg` element is now rendered with a proper `viewPort` attribute
  which allows the host page to make it responsive by styling it with
  `max-width="100%"`.
- You can generate a bare `svg` element for inclusion in an HTML
  document by supplying the command-line flag `--embedded` or `-e`, or
  invoking the `generate` function with a new second `options` object
  in which the property `embedded` is `true`.
- New helper functions to support drawing bit-fields from integers.
- New helper functions to support drawing padding until a particular
  address is reached.
- A `draw-inline-gap` function to support drawing discontinuities in
  single-row diagrams.
- The ClojureScript namespaces `clojure.set` and `clojure.string` and
  the JavaScript `Math` object are now available for use by your
  diagram code.

### Fixed

- A new version of the Small Clojure Interpreter is used, which
  enables `dotimes` for index-based iteration within diagrams.
  Previously it would fail because it delegated to the forbidden,
  potentially non-terminating `loop` form.

## [1.3.0] - 2020-03-29

### Added

- A command-line interface to make it easy to use from the shell, and
  support integration with asciidoctor-diagram.
- Finished the language guide documentation site.

### Fixed

- The `draw-column-headers` function was missing its zero-arity
  version, which is the most commonly useful one. It was only a quirk
  of the compiled Clojurescript that was allowing diagrams to work.

## [1.2.0] - 2020-03-26

### Added

- Continued fleshing out the language guide documentation site.

## [1.1.0] - 2020-03-24

### Fixed

- Subscripts were not working properly in Firefox

### Added

- Ability to draw superscript elements as well, even in conjunction
  with subscripts.
- Started creating a documentation site for the domain-specific
  language with which diagrams are created.

## [1.0.1] - 2020-03-22

This was the first release that actually worked when installed via
`npm`.

### Fixed

- The previous release was accidentally published as a development
  build, not a release build, which will not work with out
  shadow-cljs.

## 1.0.0 - 2016-03-22

Initial early release.

[unreleased]: https://github.com/Deep-Symmetry/bytefield-svg/compare/v1.10.0...HEAD
[1.10.0]: https://github.com/Deep-Symmetry/bytefield-svg/compare/v1.9.0...v1.10.0
[1.9.0]: https://github.com/Deep-Symmetry/bytefield-svg/compare/v1.8.0...v1.9.0
[1.8.0]: https://github.com/Deep-Symmetry/bytefield-svg/compare/v1.7.0...v1.8.0
[1.7.0]: https://github.com/Deep-Symmetry/bytefield-svg/compare/v1.6.1...v1.7.0
[1.6.1]: https://github.com/Deep-Symmetry/bytefield-svg/compare/v1.6.0...v1.6.1
[1.6.0]: https://github.com/Deep-Symmetry/bytefield-svg/compare/v1.5.0...v1.6.0
[1.5.0]: https://github.com/Deep-Symmetry/bytefield-svg/compare/v1.4.3...v1.5.0
[1.4.3]: https://github.com/Deep-Symmetry/bytefield-svg/compare/v1.4.1...v1.4.3
[1.4.1]: https://github.com/Deep-Symmetry/bytefield-svg/compare/v1.4.0...v1.4.1
[1.4.0]: https://github.com/Deep-Symmetry/bytefield-svg/compare/v1.3.0...v1.4.0
[1.3.0]: https://github.com/Deep-Symmetry/bytefield-svg/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/Deep-Symmetry/bytefield-svg/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/Deep-Symmetry/bytefield-svg/compare/v1.0.1...v1.1.0
[1.0.1]: https://github.com/Deep-Symmetry/bytefield-svg/compare/v1.0.0...v1.0.1
