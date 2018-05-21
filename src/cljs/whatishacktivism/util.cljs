(ns whatishacktivism.util
  (:require [ajax.core :refer [GET]]
            [clojure.string :as string]))


(def vowels ["a" "e" "i" "o" "u" "y"])
(def double-cs ["bb" "dd" "ff" "gg" "mm" "nn" "pp" "rr" "tt"])
(def li-endings ["c" "d" "e" "g" "h" "k" "m" "n" "r" "t"])

(def word #"[^a-zA-Z0-9_\+\-/]")
(def sentence #"[.!?,;:\t\\\"\(\)\']|\s[\-|—]\s")

(defn stop-words [f]
  (GET "/stop-words" {:handler f
                      :response-type :transit}))

(defn replace-suffix [s suffix replacement]
  (if-let [i (string/last-index-of s suffix)]
    (str (subs s 0 i) replacement)
    s))

;; FIXME: these methods can't use string/replace
;; for obvious reasons

(defn _step0 [s]
  (-> s
      (replace-suffix "'s'" "")
      (replace-suffix "'s" "")
      (replace-suffix "'" "")))

(defn _step1a [s]
  (let [f #(if (>= (string/last-index-of s %) 2) "ie" "i")]
    (cond
        (string/ends-with? s "sses") (replace-suffix s "sses" "")
        (string/ends-with? s "ied")  (replace-suffix s "ied" (f "ied"))
        (string/ends-with? s "ies")  (replace-suffix s "ies" (f "ies"))
        (string/ends-with? s "ss")   s
        (string/ends-with? s "us")   s
        (string/ends-with? s "s")    (if-not (nil?
                                               (re-find (re-pattern (string/join "|" vowels))
                                                        (subs s 0 (- (count s) 2))))
                                       (replace-suffix s "s" "")
                                       s)
        :else s)))

(defn porter2 [s]
  (-> s _step0 _step1a))

(defn tokenize [s]
  (let [lowered (string/lower-case s)]
    (try (-> lowered
             string/trim
             (string/split word))
         (catch js/Error e lowered))))

#_(defn freqs [s]
  (frequencies (map str (-> s
                            tokenize
                            stem))))
