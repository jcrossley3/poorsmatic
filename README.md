# Poorsmatic

Demo app for Jim Crossley's "Immutant, I am in you!" talk at
Clojure/conj 2012, the slides for which are in the doc folder.

The talk featured Datomic as the application's persistent store, but
there is also a branch using lobos/korma/h2 in here.

To make it work, you'll need a file named `twitter-creds` available
from the classpath, e.g. in the `resources/` directory. It should
contain a vector like so:

    ["app-key" "app-secret" "user-token" "user-token-secret"]

Visit http://dev.twitter.com to find yours.

## Usage

FIXME

## License

Copyright © 2012 Jim Crossley

Distributed under the Eclipse Public License, the same as Clojure.
