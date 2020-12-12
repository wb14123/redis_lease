(ns jepsen.redis.lease
  (:require
    [clojure.tools.logging :refer :all]
    [jepsen.cli :as cli]
    [jepsen.tests :as tests]
    [jepsen.checker :as checker]
    [jepsen.nemesis :as nemesis]
    [jepsen.control :as c]
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

(def crash-cache-nemesis
  (nemesis/node-start-stopper
    (fn target [nodes] "redis-cache")
    ; rand-nth
    (fn start [test node] (c/su (c/exec :killall -9 :redis-server)) [:killed node])
    (fn stop  [test node] (redis-db/start-redis node) [:restarted node])
    )
  )

(defn redis-lease-test
  "Noop test"
  [opts]
  (merge tests/noop-test
         {:name      "redis-lease"
          :os        debian/os
          :db        (redis-db/db)
          :client    (test-client/->Client nil)
          :pure-generators true
          :nemesis   crash-cache-nemesis
          :checker   (check-consistent)
          :generator (repeat 500 (gen/phases (->> (gen/mix [r w])
                                                  (gen/stagger (/ 10000))
                                                  (gen/nemesis [(gen/sleep 0.3)
                                                                {:type :info, :f :start}
                                                                ])
                                                  (gen/time-limit 0.5))
                                             (gen/nemesis [{:type :info, :f :stop} (gen/sleep 1)])
                                             (gen/once check)))
          }
         opts))


(defn -main
  "I don't do a whole lot."
  [& args]
  (cli/run! (merge (cli/single-test-cmd {:test-fn redis-lease-test})
                   (cli/serve-cmd))
            args))
