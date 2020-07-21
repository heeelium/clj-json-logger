(ns magnolia.core
  (:require [clojure.data.json :as json]
            [clojure.java.io   :as io]
            [clojure.pprint    :as pprint]
            [clojure.string    :as str]
            [tick.alpha.api    :as t]))

(def level-mapping
  "Mapping from log level to a numeric representation, useful for analyzing
  structured logs and to simplify the implementation of log level thresholds."
  {:debug 10
   :info  20
   :warn  30
   :error 40
   :fatal 50})

;; Dynamic configuration options

(def ^:dynamic *pretty* false)
(defn toggle-pretty-print
  "Toggle function to enable/disable pretty-print for uses of this library, the
  default behavior is for pretty-printing to be disabled."
  []
  (alter-var-root #'*pretty* (constantly (not *pretty*))))

(def ^:dynamic *stdout* true)
(defn toggle-stdout
  "Toggle function to enable/disable writing logs to stdout, the default
  behavior is to log to stdout. Calling (toggle-stdout) will disable this
  behavior."
  []
  (alter-var-root #'*stdout* (constantly (not *stdout*))))

(def ^:dynamic *level* :debug)
(defn set-log-level
  "Sets the level threshold for logging. Default value :debug. If you'd like to
  log only :info and above, set the log level before using the logger:
  (set-log-level :info)
  This method will throw an exception if level is not one of the standard
  defined error levels: #{:debug :info :warn :error :fatal}"
  [level]
  (let [levels #{:debug :info :warn :error :fatal}]
    (if (contains? levels level)
      (alter-var-root #'*level* (constantly level))
      (throw (Exception. "invalid log level")))))

(defn- create-file
  "Create a file at the given location."
  [filename]
  ;; TODO: create file if it doesn't exist already and any parent
  ;;       directories in the path
  nil)

(def ^:dynamic *filename*)
(defn set-file
  "If logging to a file, use set-file to point to a file path. This function
  will create the file if it doesn't already exist, but will not create any
  directories in the path.
  Usage: (set-file '/path/to/filename')"
  [filename]
  (do
    (when-not (.exists (io/file filename)) (create-file filename))
    (alter-var-root #'*filename* (constantly filename))))

;; Library API exposed to the user

(defmacro debug [message & args] `(log :debug ~message ~@args))
(defmacro info  [message & args] `(log :info  ~message ~@args))
(defmacro warn  [message & args] `(log :warn  ~message ~@args))
(defmacro error [message & args] `(log :error ~message ~@args))
(defmacro fatal [message & args] `(log :fatal ~message ~@args))

(defn- convert-if-keyword
  "If the given key is a keyword return the string representation of that key.
  Example: :foo -> 'foo'"
  [key]
  (if (keyword? key)
    (name key)
    key))

(defn- pretty-formatter
  "Given the log, this function will convert it into a somewhat human readable
  format, enabled with (toggle-pretty-print)."
  [log-data]
  (str/join [(pprint/cl-format true "level=~a namespace=~a message=\"~a\""
                               (name (log-data :level))
                               (log-data :namespace)
                               (log-data :message))
             (when (log-data :error)
               (let [error (log-data :error)]
                 (pprint/cl-format true " error=\"~a\"" (str error))))
             (doseq [[k v] (log-data :kv)]
               (pprint/cl-format true " ~a=~a"
                                 (convert-if-keyword k)
                                 (convert-if-keyword v)))]))

(defn- current-time-as-string
  "Gets current UTC time and returns it as a string."
  []
  (str (t/now)))

(defn- convert-to-string
  "Takes log datum and returns it as either a json string or a custom formatted
  'pretty' string which can be useful for development purposes."
  [log]
  (if *pretty*
    (pretty-formatter log)
    (json/write-str log)))

(defn- stacktrace-to-string
  "Converts a Java error object's stacktrace into a string, formatted correctly
  with newline charactoers ('\n') for better viewing in observability tools."
  [error]
  (str/join "\n" (.getStackTrace error)))

(defn- write-to-stdout
  "Writes the string log to the stdout stream, along with a newline char \n."
  [log]
  ;; TODO: use a buffered writer to stdout instead
  (.write *out* (str log "\n")))

(defn- write-to-file
  "Appends log line to the file explicitly set with set-file."
  [log]
  ;; TODO: implement a file appender
  nil)

(defn- write-log
  "Internal function mostly for readability, takes a log, converts it to a
  string, and then writes it to the enabled targets (stdout, file, or both)."
  [log]
  (do
    (when *stdout*
      (->> log
           convert-to-string
           write-to-stdout))
    (when *filename*
      (->> log
           convert-to-string
           write-to-file))))

(defn log
  "Low level implementation for the logger, usage is wrapped by the macros
  exposed to the library user."
  [level message & {:keys [kv error]}]
  (write-log (into {}
                   [[:message      message]
                    [:level        level]
                    [:namespace    (ns-name *ns*)]
                    [:level_number (level-mapping level)]
                    [:timestamp    (current-time-as-string)]
                    (when error
                      {:error      (str error)
                       :stacktrace (stacktrace-to-string error)})
                    (when kv [:kv kv])])))
