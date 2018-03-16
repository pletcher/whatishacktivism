(ns whatishacktivism.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [whatishacktivism.core-test]))

(doo-tests 'whatishacktivism.core-test)

