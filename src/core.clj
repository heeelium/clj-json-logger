(ns clj-json-logger.core
  (:import java.util.Date)
  (:require [clojure.string :as str]
            [clojure.data.json :as json]
            [clojure.pprint :as pprint]))

(def config
  {:level  :debug ; one of [:debug, :info, :warn, :error, :fatal]
   :stdout true   ; log to stdout by default
   :file   nil    ; path to file if logging to file, nil means stdout only
   :pretty true  ; whether or not we want to pretty print logs (useful for dev)
   })

(def level-mapping
  {:debug 10
   :info  20
   :warn  30
   :error 40
   :fatal 50})

(defn current-epoch-time []
  (.getTime (java.util.Date.)))

(defn pretty-formatter [log]
  ;; (apply str [(pprint/cl-format nil "level=~a namespace=~a message=\"~a\""
  ;;                               (name (log :level))
  ;;                               (log :namespace)
  ;;                               (log :message))
  ;;             (doseq [[k v] (log :kv)]
  ;;               (pprint/cl-format nil " ~a=~a " k v))]))
  ;; TODO: figure out why this doseq is returning a nil
  (pprint/cl-format nil"level=~a namespace=~a message=\"~a\""
                    (name (log :level))
                    (log :namespace)
                    (log :message)))

(defn convert-to-string [log]
  (if (config :pretty)
    (pretty-formatter log)
    (json/write-str log)))

(defn write-to-stdout [log]
  (.write *out* (str log "\n")))

(defn- write-log [log]
  (when (config :stdout)
    (->> log
         convert-to-string
         write-to-stdout)))

(defn log [level message & {:keys [kv error]}]
  (write-log (into {}
                   [[:message message]
                    [:level level]
                    [:namespace (str *ns*)]
                    [:level_number (level-mapping level)]
                    [:timestamp (current-epoch-time)]
                    (when error
                      {:error      (.toString error)
                       :stacktrace (str/join "\n" (.getStackTrace error))})
                    (when kv [:kv kv])])))

(defmacro debug [message & args] `(log :debug ~message ~@args))
(defmacro info  [message & args] `(log :info  ~message ~@args))
(defmacro warn  [message & args] `(log :warn  ~message ~@args))
(defmacro error [message & args] `(log :error ~message ~@args))
(defmacro fatal [message & args] `(log :fatal ~message ~@args))
