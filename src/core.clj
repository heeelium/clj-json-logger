(ns clj-json-logger.core
  (:import java.util.Date)
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.string :as str]))

(def- level-mapping
  {:debug 10
   :info  20
   :warn  30
   :error 40
   :fatal 50})

;; Dynamic configuration options

(def ^:dynamic *pretty* false)
(defn toggle-pretty-print []
  (alter-var-root #'*pretty* (constantly true)))

(def ^:dynamic *stdout* true)
(defn disable-stdout []
  (alter-var-root #'*stdout* (constantly false)))

(def ^:dynamic *level* :debug)
(defn set-log-level [level]
  (let [levels #{:debug :info :warn :error :fatal}]
    (if (contains? levels level)
      (alter-var-root #'*level* (constantly level))
      (throw (Exception. "incorrect log level")))))

(def ^:dynamic *file*)
(defn set-file [filename]
  (do
    (unless (.exists (io/file filename)) (create-file filename))
    (alter-var-root #'*file* (constantly filename))))

;; Library API exposed to the user

(defmacro debug [message & args] `(log :debug ~message ~@args))
(defmacro info  [message & args] `(log :info  ~message ~@args))
(defmacro warn  [message & args] `(log :warn  ~message ~@args))
(defmacro error [message & args] `(log :error ~message ~@args))
(defmacro fatal [message & args] `(log :fatal ~message ~@args))

(def- create-file [filename]
  (print "foo"))

(defn- convert-if-keyword [key]
  (if (keyword key)
    (name key)
    key))

(defn- pretty-formatter [log]
  (apply str [(pprint/cl-format true "level=~a namespace=~a message=\"~a\""
                                (name (log :level))
                                (log :namespace)
                                (log :message))
              (doseq [[k v] (log :kv)]
                (pprint/cl-format true " ~a=~a"
                                  (convert-if-keyword k)
                                  (convert-if-keyword v)))]))

(defn- current-epoch-time []
  (.getTime (java.util.Date.)))

(defn- convert-to-string [log]
  (if *pretty*
    (pretty-formatter log)
    (json/write-str log)))

(defn- write-to-stdout [log]
  (.write *out* (str log "\n")))

(defn- write-log [log]
  (when *stdout*
    (->> log
         convert-to-string
         write-to-stdout)))

(defn log [level message & {:keys [kv error]}]
  (write-log (into {}
                   [[:message message]
                    [:level level]
                    [:namespace (ns-name *ns*)]
                    [:level_number (level-mapping level)]
                    [:timestamp (current-epoch-time)]
                    (when error
                      {:error      (.toString error)
                       :stacktrace (str/join "\n" (.getStackTrace error))})
                    (when kv [:kv kv])])))
