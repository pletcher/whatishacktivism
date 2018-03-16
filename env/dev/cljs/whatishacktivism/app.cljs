(ns ^:figwheel-no-load whatishacktivism.app
  (:require [whatishacktivism.core :as core]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(core/init!)
