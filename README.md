# poorsmatic

Demo app for Jim Crossley's "Immutant, I am in you!" talk at
Clojure/conj 2012, the slides for which are in the doc folder.

The talk featured Datomic as the application's persistent store, the
code for which is on the 'datomic' branch. The 'master' branch uses
korma/lobos to facilitate deployment on OpenShift.

To make it work, you'll need a file named `twitter-creds` available
from the classpath, e.g. in the `resources/` directory. It should
contain a vector like so:

    ["app-key" "app-secret" "user-token" "user-token-secret"]

Visit http://dev.twitter.com to find yours.

## Usage

## License

Copyright © 2013 Jim Crossley

Distributed under the Eclipse Public License, the same as Clojure.
