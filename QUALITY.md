# How Ash Ra Template is Tested

_Referencing [How SQLite Is Tested](https://www.sqlite.org/testing.html)_

Reliability of ART is achieved in part by thorough and careful automated testing.
The range of testing covers each ART component, it's public API, code samples in the documentation, and assumed common execution environments (JDK and Clojure versions).

## Regression testing

Defects reported against ART cannot be considered as resolved until automated tests express the defect and prove remediation.
These regression tests ensure that prior defects do not re-emerge in future.



## Release criteria

A VCS commit is considered releasable provided that all of its components satisfy the following criteria:

- Code quality assessment tools don't indicate any outstanding problems, within reason: TODOs, Clojure & Java library dependency warnings, reflection warnings, ancient, clj-kondo, nvd.
- The documentation is synchronized with the code, including version numbers, and automated testing of all examples.
- The described behavior of code samples from the documentation is confirmed via automated tests.
- Reasonably unified test suite shared between the tools `clj-art` et. al.
- All automated tests pass throughout the matrix of supported versions of Clojure and JDKs.
- Test coverage from automated testing indicates a near-perfect or better test coverage rate.



## Release checklist

### Before release
- Update [CHANGELOG.md](CHANGELOG.md) to reflect the new version.
  - Replace the ``_Unreleased_`` attribute with the actual date.
- Update ART version in assets, code, and documentation.
- Choose a specific VCS commit identifier as the release target.
- Ensure the [release criteria](QUALITY.md) are satisfied.
- All README.md files are synchronized in content.
- CI is building the release branch.

### Executing the release
- Deploy `vivid/art`, `vivid/boot-art`, `vivid/clj-art`, and `lein-art` to Clojars.
- Tag the release and push the tag to upstream Git.

### Immediately after release
- Smoke test each downloadable deliverable.
- Confirm correctness of:
  - All project URLs.
  - Default branch in GitHub.
  - Versions appearing in current documentation.
  - CI build results.
- Announce the release to the Clojure community. Listings at:
  - https://clojure.org/community/resources
  - https://www.reddit.com/r/Clojure/
