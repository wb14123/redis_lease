(ns jepsen.redis.db
  (:require
    [clojure.tools.logging :refer :all]
    [jepsen.db :as db]
    [jepsen.control.util :as cu]
    [jepsen.control :as c]))

(def dir "/redis")
(def binary "redis-server")
(def logfile "/redis.log")
(def pidfile "/redis.pid")
(def port "6379")

(defn install-redis
  [node]
  (info node "Installing Redis ...")
  (cu/install-archive! "https://download.redis.io/releases/redis-6.0.9.tar.gz" dir)
  (c/cd dir (c/exec :make)))

(defn start-redis
  [node]
  (info node "Starting Redis ...")
  (cu/start-daemon!
    {:logfile logfile
     :pidfile pidfile
     :chdir (str dir "/src")}
    binary
    :--bind "0.0.0.0"
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
      (install-redis node)
      (start-redis node))
    (teardown! [_ test node]
      (stop-redis node))))

