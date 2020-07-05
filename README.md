# Magnolia

A pure Clojure structured application logger.

## Configuration

**Magnolia** uses pretty sane defaults that should be sufficient to get started, but if you want to change them, global initialization can be done using the helper methods exposed to the user. For example, **magnolia** logs to `stdout` by default and you might want to change that to log to a file.

``` clojure
user> (use '[magnolia.core :as log]')
user> (log/toggle-pretty-print)                  ; will log strings nicer to grok than json
user> (log/set-file "/var/log/clojure-app.log")  ; log to file
user> (log/disable-stdout)                       ; don't log to stdout
```

## Usage

A basic info log:

``` clojure
user> (use '[magnolia.core :as log])
user> (log/info "info log")
{"message":"info log","level":"info","timestamp":1592967099025}
nil
```

Log additional data as a `kv` map:

``` clojure
user> (log/info "info log" :kv {:count 1 :flag false})
{"message":"info log","level":"info","timestamp":1592967213412,"json":{"count":1,"flag":false}}
nil
```

Log an error:

``` clojure
user> (log/error "oh no!" :err (Exception. "wut"))
{"message":"oh no!","level":"error","timestamp":1592967303609,"error":"java.lang.Exception: wut","stacktrace":"user$eval8937.invokeStatic(NO_SOURCE_FILE:488)\nuser$eval8937.invoke(NO_SOURCE_FILE:488)\nclojure.lang.Compiler.eval(Compiler.java:7177)\nclojure.lang.Compiler.eval(Compiler.java:7132)\nclojure.core$eval.invokeStatic(core.clj:3214)\nclojure.core$eval.invoke(core.clj:3210)\nnrepl.middleware.interruptible_eval$evaluate$fn__959$fn__960.invoke(interruptible_eval.clj:82)\nclojure.lang.AFn.applyToHelper(AFn.java:152)\nclojure.lang.AFn.applyTo(AFn.java:144)\nclojure.core$apply.invokeStatic(core.clj:665)\nclojure.core$with_bindings_STAR_.invokeStatic(core.clj:1973)\nclojure.core$with_bindings_STAR_.doInvoke(core.clj:1973)\nclojure.lang.RestFn.invoke(RestFn.java:425)\nnrepl.middleware.interruptible_eval$evaluate$fn__959.invoke(interruptible_eval.clj:82)\nclojure.main$repl$read_eval_print__9086$fn__9089.invoke(main.clj:437)\nclojure.main$repl$read_eval_print__9086.invoke(main.clj:437)\nclojure.main$repl$fn__9095.invoke(main.clj:458)\nclojure.main$repl.invokeStatic(main.clj:458)\nclojure.main$repl.doInvoke(main.clj:368)\nclojure.lang.RestFn.invoke(RestFn.java:1523)\nnrepl.middleware.interruptible_eval$evaluate.invokeStatic(interruptible_eval.clj:79)\nnrepl.middleware.interruptible_eval$evaluate.invoke(interruptible_eval.clj:56)\nnrepl.middleware.interruptible_eval$interruptible_eval$fn__990$fn__994.invoke(interruptible_eval.clj:145)\nclojure.lang.AFn.run(AFn.java:22)\nnrepl.middleware.session$session_exec$main_loop__1057$fn__1061.invoke(session.clj:202)\nnrepl.middleware.session$session_exec$main_loop__1057.invoke(session.clj:201)\nclojure.lang.AFn.run(AFn.java:22)\njava.base\/java.lang.Thread.run(Thread.java:832)"}
nil
```

## TODO

- [ ] Configurable log level output
- [ ] Use a buffered output writer for writing the log
- [ ] Add a file appender
- [ ] Write some basic tests
- [ ] Look into wrapping any underlying java logging to format that as well?
