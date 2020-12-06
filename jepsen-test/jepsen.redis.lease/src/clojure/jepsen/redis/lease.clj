(ns jepsen.redis.lease
  (:require
    [clojure.tools.logging :refer :all]
    [jepsen.cli :as cli]
    [jepsen.tests :as tests]
    [jepsen.checker :as checker]
    [jepsen.os.debian :as debian]
    [jepsen.redis.db :as redis-db]
    [jepsen.redis.client :as test-client]
    [knossos.op :as op]
    [jepsen.generator :as gen]))

(defn r    [_ _] {:type :invoke, :f :read, :value nil})
(defn w    [_ _] {:type :invoke, :f :write, :value (str (rand-int 1000000))})
(def check {:type :invoke, :f :check, :value nil})


(defn check-consistent []
  (reify checker/Checker
    (check [this test history opts]
      (let [errors (->> history
                        (filter op/ok?)
                        (filter #(= :check (:f %)))
                        (filter #(not (= nil (:cache-value (:value %)))))
                        (filter #(not (= (:db-value (:value %)) (:cache-value (:value %)))))
                        )]
        (if (seq errors)
          {:valid? false
           :inconsistencies errors}
          {:valid? true}
          )
        ))))

(defn redis-lease-test
  "Noop test"
  [opts]
  (merge tests/noop-test
         {:name      "redis-lease"
          :os        debian/os
          :db        (redis-db/db)
          :client    (test-client/->Client nil)
          :pure-generators true
          :checker   (check-consistent)
          :generator (repeat 100 (gen/phases (->> (gen/mix [r w])
                                      (gen/stagger (/ 10000))
                                      (gen/nemesis nil)
                                      (gen/time-limit 0.2))
                                 (gen/once check)))
          }
         opts))


(defn -main
  "I don't do a whole lot."
  [& args]
  (cli/run! (merge (cli/single-test-cmd {:test-fn redis-lease-test})
                   (cli/serve-cmd))
            args))
