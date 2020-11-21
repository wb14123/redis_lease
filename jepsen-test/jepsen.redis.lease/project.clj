(defproject jepsen.redis.lease "0.1.0-SNAPSHOT"
  :description "Jepsen test for redis lease scripts"
  :url "https://github.com/wb14123/redis_lease"
  :main jepsen.redis.lease
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [jepsen "0.2.1-SNAPSHOT"]
                 [biz.paluch.redis/lettuce "3.2.Final"]
                 ]
  :repl-options {:init-ns jepsen.redis.lease})
