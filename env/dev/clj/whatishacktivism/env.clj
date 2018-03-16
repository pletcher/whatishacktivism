(ns whatishacktivism.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [whatishacktivism.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[whatishacktivism started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[whatishacktivism has shut down successfully]=-"))
   :middleware wrap-dev})
