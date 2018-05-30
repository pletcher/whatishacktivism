(ns whatishacktivism.util
  (:require [ajax.core :refer [GET]]
            [clojure.string :as string]))


(def vowels ["a" "e" "i" "o" "u" "y"])
(def double-cs ["bb" "dd" "ff" "gg" "mm" "nn" "pp" "rr" "tt"])
(def li-endings ["c" "d" "e" "g" "h" "k" "m" "n" "r" "t"])

(def vowels-re (re-pattern (string/join "|" vowels)))
(def word #"[^a-zA-Z0-9_\+\-/]")
(def sentence #"[.!?,;:\t\\\"\(\)\']|\s[\-|—]\s")

(defn stop-words [f]
  (GET "/stop-words" {:handler f
                      :response-type :transit}))

(defn replace-suffix [s suffix replacement]
  (if-let [i (string/last-index-of s suffix)]
    (str (subs s 0 i) replacement)
    s))

(defn contains-vowel? [s]
  (not (nil? (re-find vowels-re s))))

;; M.F. Porter, 1980, An algorithm for suffix stripping, Program, 14(3) pp 130−137.
(defn _measure
  "'A consonant will be denoted by c, a vowel by v. A list
   ccc... of length greater than 0 will be denoted by C, and a
   list vvv... of length greater than 0 will be denoted by V.
   Any word, or part of a word, therefore has one of the four
   forms:
       CVCV ... C
       CVCV ... V
       VCVC ... C
       VCVC ... V
   These may all be represented by the single form
       [C]VCVC ... [V]
   where the square brackets denote arbitrary presence of their
   contents. Using (VC){m} to denote VC repeated m times, this
   may again be written as
       [C](VC){m}[V].
   m will be called the \measure\ of any word or word part when
   represented in this form. The case m = 0 covers the null
   word. Here are some examples:
       m=0    TR,  EE,  TREE,  Y,  BY.
       m=1    TROUBLE,  OATS,  TREES,  IVY.
       m=2    TROUBLES,  PRIVATE,  OATEN,  ORRERY.' (Porter 1980: 134)")

(defn _step0 [s]
  (-> s
      (replace-suffix "'s'" "")
      (replace-suffix "'s" "")
      (replace-suffix "'" "")))

(defn _step1a [s]
  (let [f #(if (= 4 (count s)) "ie" "i")]
    (cond
        (string/ends-with? s "sses") (replace-suffix s "sses" "")
        (string/ends-with? s "ied")  (replace-suffix s "ied" (f))
        (string/ends-with? s "ies")  (replace-suffix s "ies" (f))
        (string/ends-with? s "ss")   s
        (string/ends-with? s "us")   s
        (string/ends-with? s "s")    (if (contains-vowel? (subs s 0 (- (count s) 2)))
                                       (replace-suffix s "s" "")
                                       s)
        :else s)))

(defn _step1b [s]
  (cond
    (string/ends-with? s "eed") (replace-suffix s "eed" "ee")))

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
