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

(defn log [lvl msg & [kv err]]
  (write-log (into {}
                   [[:message msg]
                    [:level lvl]
                    [:timestamp (current-epoch-time)]
                    (unless err
                            [:error      (.toString err)
                             :stacktrace (.getStackTrace err)])
                    (unless kv [:json kv])])))

(defmacro debug [msg & args] `(log :debug ~msg ~@args))
(defmacro info  [msg & args] `(log :info  ~msg ~@args))
(defmacro warn  [msg & args] `(log :warn  ~msg ~@args))
(defmacro error [msg & args] `(log :error ~msg ~@args))
(defmacro fatal [msg & args] `(log :fatal ~msg ~@args))
