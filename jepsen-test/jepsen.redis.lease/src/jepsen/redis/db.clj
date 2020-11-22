(ns jepsen.redis.db
  (:require
    [clojure.tools.logging :refer :all]
    [jepsen.db :as db]
    [jepsen.control.util :as cu]))

(def dir "/redis")
(def binary "redis-server")
(def logfile "/redis.log")
(def pidfile "/redis.pid")
(def port "6379")

(defn start-redis
  [node]
  (info node "Starting Redis ...")
  (cu/start-daemon!
    {:logfile logfile
     :pidfile pidfile
     :chdir dir}
    binary
    :--port port
    :--appendfilename (str node ".aof")
    :--dbfilename (str node ".rdb")
    )
  (info node "Started Redis."))

(defn stop-redis
  [node]
  (info node "Stopping Redis ...")
  (cu/stop-daemon! pidfile)
  (info node "Stopped Redis."))

(defn db
  []
  (reify db/DB
    (setup! [_ test node]
      (start-redis node))
    (teardown! [_ test node]
      (stop-redis node))))

