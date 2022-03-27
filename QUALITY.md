# How Ash Ra Template is Tested

_Referencing [How SQLite Is Tested](https://www.sqlite.org/testing.html)_

Reliability of ART is achieved in part by thorough and careful automated testing.
The range of testing covers each ART component, it's public API, code samples in the documentation, and assumed common execution environments (JDK and Clojure versions).

## Regression testing

Defects reported against ART cannot be considered as resolved until automated tests express the defect and prove remediation.
These regression tests ensure that prior defects do not re-emerge in future.



## Release criteria

A VCS commit is considered releasable provided that all of its components satisfy the following criteria:

- Code quality assessment tools don't indicate any outstanding problems, within reason: `TODO`s, Clojure & Java library dependency warnings, reflection warnings, ancient, clj-kondo, nvd.
- The documentation is synchronized with the code, including version numbers, and automated testing of all examples.
- The behavior of code samples as described in the documentation is confirmed via automated tests.
- Reasonably unified test suite shared between the tools `clj-art` et. al.
- All automated tests pass throughout the matrix of supported versions of Clojure and JDKs.
- Test coverage from automated testing indicates a near-perfect or better test coverage rate.
- All dependencies are as up-to-date as reasonable, and NVD doesn't indicate serious problems.



## Release checklist

### Before release
- Ensure the contents of the project metadata file [vivid-art-facts.edn](assets/vivid-art-facts.edn) is correct.
- `bin/gen.sh` doesn't change any files. In other words, all generated files are up-to-date.
- Update [CHANGELOG.md](CHANGELOG.md) to reflect the new version.
  - Replace the ``_Unreleased_`` attribute with the actual date.
  - Ensure the entries genuinely reflect the nature of the changes in this release.
- Choose a specific VCS commit identifier as the release target.
- Ensure the [release criteria](QUALITY.md) are satisfied.
- All README.md files are synchronized in content.
- CI is building the release branch.

### Executing the release
- Use `bin/deploy.sh` to deploy all projects to Clojars.
- Tag the release and push the tag to upstream Git.

### Immediately after release
- Smoke test each downloadable deliverable.
- In the GitHub project, change the [default branch](https://github.com/vivid-inc/ash-ra-template/settings/branches) to this new release.
- Confirm correctness of:
  - All project URLs.
  - Default branch in GitHub.
  - Versions appearing in current documentation.
  - CI build results.
- Announce the release to the Clojure community.
  - https://clojure.org/community/resources
  - https://clojureverse.org/
  - https://www.reddit.com/r/Clojure/
  - Twitter
- (Vivid Inc. internal) In an env that doesn't have the new release in its `~/.m2` repo, update all project build dependencies to this release of ART and run their full builds on clean VCS checkouts to ensure correct operation of the new release obtained from Clojars.
