(ns jepsen.redis.lease
  (:require
            [clojure.tools.logging :refer :all]
            [jepsen.cli :as cli]
            [jepsen.tests :as tests]
            [jepsen.os.debian :as debian]
            [jepsen.redis.db :as redis-db]))


(defn redis-lease-test
  "Noop test"
  [opts]
  (merge tests/noop-test
         {:name "redis-lease"
          :os   debian/os
          :db (redis-db/db)
          :pure-generators true}
         opts))


(defn -main
  "I don't do a whole lot."
  [& args]
  (cli/run! (merge (cli/single-test-cmd {:test-fn redis-lease-test})
                   (cli/serve-cmd))
            args))
