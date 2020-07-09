(ns magnolia.core
  (:import java.util.Date)
  (:require [clojure.data.json :as json]
            [clojure.java.io   :as io]
            [clojure.pprint    :as pprint]
            [clojure.string    :as str]))

(def level-mapping
  {:debug 10
   :info  20
   :warn  30
   :error 40
   :fatal 50})

;; Dynamic configuration options

(def ^:dynamic *pretty* false)
(defn toggle-pretty-print []
  (alter-var-root #'*pretty* (constantly (not *pretty*))))

(def ^:dynamic *stdout* true)
(defn toggle-stdout []
  (alter-var-root #'*stdout* (constantly (not *stdout*))))

(def ^:dynamic *level* :debug)
(defn set-log-level [level]
  (let [levels #{:debug :info :warn :error :fatal}]
    (if (contains? levels level)
      (alter-var-root #'*level* (constantly level))
      (throw (Exception. "invalid log level")))))

(defn- create-file [filename]
  ;; TODO: create file if it doesn't exist already and any parent
  ;;       directories in the path
  (print "foo"))

(def ^:dynamic *filename*)
(defn set-file [filename]
  (do
    (when-not (.exists (io/file filename)) (create-file filename))
    (alter-var-root #'*filename* (constantly filename))))

;; Library API exposed to the user

(defmacro debug [message & args] `(log :debug ~message ~@args))
(defmacro info  [message & args] `(log :info  ~message ~@args))
(defmacro warn  [message & args] `(log :warn  ~message ~@args))
(defmacro error [message & args] `(log :error ~message ~@args))
(defmacro fatal [message & args] `(log :fatal ~message ~@args))

(defn- convert-if-keyword [key]
  (if (keyword? key)
    (name key)
    key))

(defn- pretty-formatter [log]
  (apply str [(pprint/cl-format true "level=~a namespace=~a message=\"~a\""
                                (name (log :level))
                                (log :namespace)
                                (log :message))
              (when (log :error)
                (let [error (log :error)]
                (pprint/cl-format true " error=\"~a\"" (.toString error))))
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

(defn- stacktrace-to-string [error]
  (str/join "\n" (.getStackTrace error)))

(defn- write-to-stdout [log]
  ;; TODO: use a buffered writer to stdout instead
  (.write *out* (str log "\n")))

(defn- write-to-file [log]
  ;; TODO: implement a file appender
  (print "foo"))

(defn- write-log [log]
  (do
    (when *stdout*
      (->> log
           convert-to-string
           write-to-stdout))
    (when *filename*
      (->> log
           convert-to-string
           write-to-file))))

(defn log [level message & {:keys [kv error]}]
  (write-log (into {}
                   [[:message      message]
                    [:level        level]
                    [:namespace    (ns-name *ns*)]
                    [:level_number (level-mapping level)]
                    [:timestamp    (current-epoch-time)]
                    (when error
                      {:error      (.toString error)
                       :stacktrace (stacktrace-to-string error)})
                    (when kv [:kv kv])])))
