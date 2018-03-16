(ns whatishacktivism.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[whatishacktivism started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[whatishacktivism has shut down successfully]=-"))
   :middleware identity})
