(ns jepsen.redis.client
  (:require [clojure.tools.logging :refer :all]
            [jepsen.client :as client])
  (:import (jepsen.redis.internal JepsenClient)))


(defrecord Client [conn]
  client/Client

  (open! [this test node]
    (assoc this :conn (if (:lease test)
                        (JepsenClient/singleLeaseCacheClient "redis-cache" 6379 "redis-db" 6379 "/jepsen/scripts")
                        (JepsenClient/singleRawCacheClient "redis-cache" 6379 "redis-db" 6379)
                        )))

  (setup! [this test])

  (invoke! [_ test op]
    (case (:f op)
      :read (assoc op :type :ok, :value (.get conn "foo"))
      :write (assoc op :type :ok, :value (.set conn "foo" (:value op)))
      :check (assoc op :type :ok, :value {:db-value (.getDB conn "foo"),
                                          :cache-value (.getCache conn "foo")})
      )
    )

  (teardown! [this test])

  (close! [_ test]
    (.close conn))
  )


