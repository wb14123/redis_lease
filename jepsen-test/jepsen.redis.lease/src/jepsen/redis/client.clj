(ns jepsen.redis.client
  (:require [clojure.tools.logging :refer :all]
            [clojure.string :as str]
            [jepsen [cli :as cli]
             [client :as client]
             [control :as c]
             [db :as db]
             [tests :as tests]]
            [jepsen.control.util :as cu]
            [jepsen.os.debian :as debian])
  (:import (com.lambdaworks.redis RedisURI RedisClient)))


(defrecord Client [conn]
  client/Client

  (open! [this test node]
    (assoc this :conn (.connect (new RedisClient
      (RedisURI/create (str "redis://" (name node) ":6379"))))))

  (setup! [this test])

  (invoke! [_ test op]
    (case (:f op)
      :read (assoc op :type :ok, :value (.get conn "foo"))
      :write (assoc op :type :ok, :value (.set conn "foo" (:value op)))
      )
    )

  (teardown! [this test])

  (close! [_ test]
    (.close conn))
  )


