(defproject org.clojars.ertucetin/procedure.async "0.1.0"
  :description "Async procedures for Clojure"
  :url "https://github.com/ertugrulcetin/procedure.async"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.11.1" :scope "provided"]
                 [manifold "0.2.4"]
                 [weavejester/dependency "0.2.1"]]
  :global-vars {*warn-on-reflection* true}
  :jvm-opts ^:replace ["-server"
                       "-XX:-OmitStackTraceInFastThrow"
                       "-Xmx2g"
                       "-XX:NewSize=1g"])
