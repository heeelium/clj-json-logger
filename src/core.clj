(ns clj-json-logger.core
  (:import java.util.Date)
  (:require [clojure.string :as str]
            [clojure.data.json :as json]))

(def config
  {:level  :debug ; one of [:debug, :info, :warn, :error, :fatal]
   :stdout true   ; log to stdout by default
   :file   nil    ; path to file if logging to file, nil means stdout only
   })

(def log-lvl-mapping
  {:debug 10
   :info  20
   :warn  30
   :error 40
   :fatal 50})

(defmacro unless
  [test & branches]
  (conj (reverse branches) test 'if))

(defn current-epoch-time []
  (.getTime (java.util.Date.)))

(defn to-json [log]
  (json/write-str log))

(defn write-to-stdout [log]
  (.write *out* (str log "\n")))

(defn write-log [log]
  (->> log
       to-json
       write-to-stdout))

(defn log [lvl msg & [kv]]
  (write-log (into {}
                   [[:msg msg]
                   [:lvl lvl]
                   [:ts (current-epoch-time)]
                   (unless kv [:kv kv])])))

(defmacro debug [msg & kv] `(log :debug ~msg ~@kv))
(defmacro info [msg & kv] `(log :info ~msg ~@kv))
(defmacro warn [msg & kv] `(log :warn ~msg ~@kv))
(defmacro error [msg & kv] `(log :error ~msg ~@kv))
(defmacro fatal  [msg & kv] `(log :fatal ~msg ~@kv))
