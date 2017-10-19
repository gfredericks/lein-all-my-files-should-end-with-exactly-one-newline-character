# lein-all-my-files-should-end-with-exactly-one-newline-character

lein-all-my-files-should-end-with-exactly-one-newline-character is a
Leiningen plugin that lints your source files to ensure they end with
exactly one newline character.

## Obtention

Add `[com.gfredericks/lein-all-my-files-should-end-with-exactly-one-newline-character "0.1.0"]`
to the `:plugins` vector of your project.clj or `:user` profile.

## Usage

Check for problems:

    $ lein all-my-files-should-end-with-exactly-one-newline-character but-do-they?

Fix them:

    $ lein all-my-files-should-end-with-exactly-one-newline-character so-fix-them

## License

Copyright © 2017 Gary Fredericks

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
