(defproject jepsen.redis.lease "0.1.0-SNAPSHOT"
  :description "Jepsen test for redis lease scripts"
  :url "https://github.com/wb14123/redis_lease"
  :main jepsen.redis.lease
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [jepsen "0.2.1-SNAPSHOT"]
                 [redis.clients/jedis "3.3.0"]
                 ]
  :source-paths      ["src/clojure"]
  :java-source-paths ["src/java"]
  :repl-options {:init-ns jepsen.redis.lease})
