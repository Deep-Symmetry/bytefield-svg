# Change Log

All notable changes to this project will be documented in this file.
This change log follows the conventions of
[keepachangelog.com](http://keepachangelog.com/).

## [Unreleased][unreleased]

### Added

- The `svg` element is now rendered with a proper `viewPort` attribute
  which allows the host page to make it responsive by styling it with
  `max-width="100%"`.
- Functions to support drawing bit-fields from integers.
- Functions to support drawing padding until a particular address is
  reached.


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

Intial early release.


[unreleased]: https://github.com/Deep-Symmetry/bytefield-svg/compare/v1.3.0...HEAD
[1.3.0]: https://github.com/Deep-Symmetry/bytefield-svg/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/Deep-Symmetry/bytefield-svg/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/Deep-Symmetry/bytefield-svg/compare/v1.0.1...v1.1.0
[1.0.1]: https://github.com/Deep-Symmetry/bytefield-svg/compare/v1.0.0...v1.0.1
