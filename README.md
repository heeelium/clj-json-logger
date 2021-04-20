# Magnolia

<img alt="GitHub" src="https://img.shields.io/github/license/rgrmrts/magnolia">

🪓 A pure Clojure structured (JSON) application logger.

## Configuration

**Magnolia** uses pretty sane defaults that should be sufficient to get started, but if you want to change them, global initialization can be done using the helper methods exposed to the user. For example, **magnolia** logs to `stdout` by default and you might want to change that to log to a file.

All available configuration options:

``` clojure
;; Toggles pretty printing for development work.
(magnolia.core/toggle-pretty-print)

;; Disable logging to stdout, useful if logging only to files in production environments.
(magnolia.core/toggle-stdout)

;; Set the minimum log level for log statements, this in inclusive so :debug is the most permissive, :warn will only log warnings and higher (see level-mapping).
(magnolia.core/set-log-level :debug)

;; This sets the file to log to, will create the path and file if they don't already exist.
(magnolia.core/set-file "/path/to/filename")
```

Example:

``` clojure
user> (use '[magnolia.core :as log]')
user> (log/set-file "/var/log/clojure-app.log")  ; log to file
```

## Usage

A basic info log:

``` clojure
user> (use '[magnolia.core :as log])
user> (log/info "info log")
{"message":"info log","level":"info","namespace":"user","level_number":20,"timestamp":"2020-07-21T23:13:35.990164Z"}
nil
```

Log additional data as a `kv` map:

``` clojure
user> (log/info "info log" :kv {:count 1 :flag false})
{"message":"info log","level":"info","namespace":"user","level_number":20,"timestamp":"2020-07-21T23:14:29.114702Z","kv":{"count":1,"flag":false}}
nil
```

Log an error:

``` clojure
user> (log/error "oh no!" :error (Exception. "wut"))
{"message":"oh no!","level":"error","namespace":"user","level_number":40,"timestamp":"2020-07-21T23:15:05.447581Z","error":"java.lang.Exception: wut","stacktrace":"user$eval11942.invokeStatic(NO_SOURCE_FILE:55)\n...java.base\/java.lang.Thread.run(Thread.java:832)"}
nil
```

## License

MPL-2.0, See [LICENSE](LICENSE).

## TODO

- [x] Configurable log level output
- [ ] Use a buffered output writer for writing the log
- [ ] Add a file appender
- [ ] Write some basic tests
- [ ] Look into wrapping any underlying java logging to format that as well?
