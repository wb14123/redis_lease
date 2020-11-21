(ns jepsen.redis.lease
  (:require [jepsen.cli :as cli]
            [jepsen.tests :as tests]))

(defn redis-lease-test
  "Noop test"
  [opts]
  (merge tests/noop-test
         {:pure-generators true}
         opts))


(defn -main
  "I don't do a whole lot."
  [& args]
  (cli/run! (merge (cli/single-test-cmd {:test-fn redis-lease-test})
                   (cli/serve-cmd))
            args))
