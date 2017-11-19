# Ash Ra Templates

Minimal templating library for Clojure featuring Ruby 2.0 ERB syntax and Clojure language processing.

## Usage

You can write this: <% ... %>  but not necessarily this <% if end %>.

The _motivation_ for writing this library was to make the task of porting a non-trivial amount of ERB-templated content from Middleman to a custom Clojure-based static site generation tool, to focus our efforts on mastering fewer ecosystems and at greater depth, and further enable more sharing of code and data with other projects.
We find the ERB syntax quite clear, and its ability to interpret code in-stream is greatly appealing.

## Todo

- Declare version 1.0.0 once the community deems the codebase feature-complete, reliable, and properly documented.

Appealing:
- ClojureScript support.
- Fully lazy implementation.
- Full ERB syntax as of Ruby 2.0
- Take an optional hash of defines.
- Take an optional seq of code to insert at the beginning.
- Expose fns to users: emit, include.

## Contribute

Yes, please do. Pull requests will be considered in accordance with the minimalist goal. And include tests, or your contributions almost will certainly become broken later.

## License

© Copyright Vivid Inc.

Ash Ra Templates is distributed under the terms of the accompanying [Eclipse Public License](LICENSE).
TODO Contributions become the property of this project? Follow the new licensing lead set by Gitlab. Vivid Contributors Notice + source code -specific terms.
