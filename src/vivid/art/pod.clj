; Copyright 2019 Vivid Inc.

(ns vivid.art.pod
  (:require
    [cemerick.pomegranate.aether :as aether]
    [clojure.java.io :as io]
    [clojure.set :refer [difference union intersection]]
    [clojure.string :as string]
    [dynapath.dynamic-classpath :as cp]
    [vivid.art.boot-backtick])
  (:import
    (java.net MalformedURLException URL)
    (org.eclipse.aether.transfer MetadataNotFoundException ArtifactNotFoundException)
    (vivid.art AddableClassLoader AddableClassLoader)))

(comment defn classpath []
         (println (seq (.getURLs (java.lang.ClassLoader/getSystemClassLoader)))))

; ---- boot/pod boot.kahnsort ----

(defn choose
  "Returns the pair [element, s'] where s' is set s with element removed."
  [s] {:pre [(not (empty? s))]}
  (let [item (first s)]
    [item (disj s item)]))

(defn no-incoming
  "Returns the set of nodes in graph g for which there are no incoming
  edges, where g is a map of nodes to sets of nodes."
  [g]
  (let [nodes (set (keys g))
        have-incoming (apply union (vals g))]
    (difference nodes have-incoming)))

(defn normalize
  "Returns g with empty outgoing edges added for nodes with incoming
  edges only.  Example: {:a #{:b}} => {:a #{:b}, :b #{}}"
  [g]
  (let [have-incoming (apply union (vals g))]
    (reduce #(if (% %2) % (assoc % %2 #{})) g have-incoming)))

(defn topo-sort
  "Proposes a topological sort for directed graph g using Kahn's
   algorithm, where g is a map of nodes to sets of nodes. If g is
   cyclic, returns nil."
  ([g]
   (topo-sort (normalize g) [] (no-incoming g)))
  ([g l s]
   (if (empty? s)
     (if (every? empty? (vals g)) l)
     (let [[n s'] (choose s)
           m (g n)
           g' (reduce #(update-in % [n] disj %2) g m)]
       (recur g' (conj l n) (union s' (intersection (no-incoming g') m)))))))

; ---- boot/pod boot.util ----

(defmacro guard
  "Evaluates expr within a try/catch and returns default (or nil if default is
  not given) if an exception is thrown, otherwise returns the result."
  [expr & [default]]
  `(try ~expr (catch Throwable _# ~default)))

; ---- boot/aether boot.aether ----

(def offline? (atom false))
(def update? (atom :daily))
(def local-repo (atom nil))
(def default-repositories (delay [["clojars" {:url "https://repo.clojars.org/"}]
                                  ["maven-central" {:url "https://repo1.maven.org/maven2/"}]]))
(def default-mirrors (delay (let [c (comment "BOOT_CLOJARS_MIRROR")
                                  m (comment "BOOT_MAVEN_CENTRAL_MIRROR")
                                  f #(when %1 {%2 {:name (str %2 " mirror") :url %1}})]
                              (merge {} (f c "clojars") (f m "maven-central")))))

(defn ^{:boot/from :technomancy/leiningen} build-url
  "Creates java.net.URL from string"
  [url]
  (try (URL. url)
       (catch MalformedURLException _
         (URL. (str "http://" url)))))

(defn ^{:boot/from :technomancy/leiningen} get-non-proxy-hosts
  []
  (let [system-no-proxy (System/getenv "no_proxy")]
    (if (not-empty system-no-proxy)
      (->> (string/split system-no-proxy #",")
           (map #(str "*" %))
           (string/join "|")))))

(defn ^{:boot/from :technomancy/leiningen} get-proxy-settings
  "Returns a map of the JVM proxy settings"
  ([] (get-proxy-settings "http_proxy"))
  ([key]
   (if-let [proxy (System/getenv key)]
     (let [url (build-url proxy)
           user-info (.getUserInfo url)
           [username password] (and user-info (.split user-info ":"))]
       {:host            (.getHost url)
        :port            (.getPort url)
        :username        username
        :password        password
        :non-proxy-hosts (get-non-proxy-hosts)}))))

(defn- dep->path [dep] (->> dep meta :file .getPath))

(defmulti on-transfer (fn [info] (:type info)))

(defmethod on-transfer :started
  [_]
  (comment letfn [(->k [size]
                       (when-not (neg? size)
                         (format " (%sk)" (Math/round (double (max 1 (/ size 1024)))))))]
           ; TODO (util/info "%s %s %s %s%s\n"
           ;           (case method :get "Retrieving" :put "Sending")
           ;           (.getName (io/file name))
           ;           (case method :get "from" :put "to")
           ;           repository
           ;           (str (->k size)))
           ))

(defmethod on-transfer :succeeded
  [_])

(defmethod on-transfer :corrupted
  [{:keys [error]}]
  (when error
    ; TODO (util/fail "%s\n" (.getMessage error))
    ))

(defmethod on-transfer :failed
  [{:keys [error]}]
  (when (and error
             (not (instance? MetadataNotFoundException error))
             (not (instance? ArtifactNotFoundException error)))
    ; TODO (util/fail "%s\n" (.getMessage error))
    ))

(defmethod on-transfer :default
  [_])

(defn transfer-listener
  [info]
  ; TODO (trace* "Aether: %s\n" (with-out-str (pprint/pprint info)))
  (on-transfer info))

(defn resolve-dependencies*
  [env]
  (try
    (aether/resolve-dependencies
      :managed-coordinates (:managed-dependencies env)
      :coordinates (:dependencies env)
      :repositories (->> (or (seq (:repositories env)) @default-repositories)
                         (map (juxt first (fn [[_ y]] (if (map? y) y {:url y}))))
                         (map (juxt first (fn [[_ y]] (update-in y [:update] #(or % @update?))))))
      :local-repo (or (:local-repo env) @local-repo nil)
      :offline? (or @offline? (:offline? env))
      :mirrors (merge @default-mirrors (:mirrors env))
      :proxy (or (:proxy env) (get-proxy-settings))
      :transfer-listener transfer-listener
      :repository-session-fn (if (= @update? :always)
                               #(doto (aether/repository-session %)
                                  (.setUpdatePolicy (aether/update-policies :always)))
                               aether/repository-session))
    (catch Exception e
      (let [root-cause (last (take-while identity (iterate (memfn getCause) e)))]
        (if-not (and (not @offline?) (instance? java.net.UnknownHostException root-cause))
          (throw e)
          (do (reset! offline? true)
              (resolve-dependencies* env)))))))

(def resolve-dependencies-memoized* (memoize resolve-dependencies*))

(defn resolve-dependencies
  "Given an env map, returns a list of maps of the form
     {:dep [foo/bar \"1.2.3\"], :jar \"file:...\"}
   corresponding to the resolved dependencies (including transitive deps)."
  [{:keys [checkouts] :as env}]
  (let [checkouts (set (map first checkouts))]
    (->> [:dependencies :repositories :local-repo :offline? :mirrors :proxy]
         (select-keys env)
         resolve-dependencies-memoized*
         topo-sort
         (keep (fn [[p :as x]] (when-not (checkouts p) {:dep x :jar (dep->path x)}))))))

; ---- boot/pod boot.pod ----

(def env
  "This pod's boot environment."
  nil)

(def this-pod
  "A WeakReference to this pod's shim instance."
  nil)

(def worker-pod
  "A reference to the boot worker pod. All pods share access to the worker
  pod singleton instance."
  nil)

(defn set-pods!         [x] (alter-var-root #'pods        (constantly x)))
(defn set-data!         [x] (alter-var-root #'data        (constantly x)))
(defn set-this-pod!     [x] (alter-var-root #'this-pod    (constantly x)))
(defn set-worker-pod! [x] (alter-var-root #'worker-pod (constantly x)))

(defn pod-name
  "Returns pod's name if called with one argument, sets pod's name to new-name
  and returns new-name if called with two arguments."
  ([pod]
   (.getName pod))
  ([pod new-name]
   (.setName pod new-name) new-name))

(defn extend-addable-classloader
  "Opens up the class loader used to create the shim for
  modification. Once seal-app-classloader is called, this will be the
  highest class loader user code can modify.

  This function is called during Boot's bootstrapping phase, and shouldn't
  be needed in client code under normal circumstances."
  []
  (extend AddableClassLoader
    cp/DynamicClasspath
    (assoc dynapath.dynamic-classpath/base-readable-addable-classpath
      :classpath-urls #(seq (.getURLs %))
      :add-classpath-url #(.addURL %1 %2))))

(def sealed-classloader-fns
  (assoc cp/base-readable-addable-classpath
    :classpath-urls #(seq (.getURLs %))
    :can-add? (constantly false)))

(defn seal-app-classloader
  "Implements the DynamicClasspath protocol to the AppClassLoader and
  boot's ParentClassLoader classes such that instances of those
  classes will refuse attempts at runtime modification by libraries
  that do so via dynapath[1]. The system class loader is of the type
  AppClassLoader under Java < 9, and the top-level class loader used
  by boot is a ParentClassLoader.

  The purpose of this is to ensure that Clojure libraries do not pollute the
  higher-level class loaders with classes and interfaces created dynamically
  in their Clojure runtime. This is essential for pods to work properly[2].

  This function is called during Boot's bootstrapping phase, and shouldn't
  be needed in client code under normal circumstances.

  [1]: https://github.com/tobias/dynapath
  [2]: https://github.com/clojure-emacs/cider-nrepl/blob/36333cae25fd510747321f86e2f0369fcb7b4afd/README.md#with-jboss-asjboss-eapwildfly"
  []
  (try
    ;; this import will fail if the user doesn't have a new enough boot.sh
    (import boot.bin.ParentClassLoader)
    (eval '(extend boot.bin.ParentClassLoader
             dynapath.dynamic-classpath/DynamicClasspath
             vivid.art.pod/sealed-classloader-fns))
    (catch Exception _))

  (try
    ;; this import will fail if the user is using Java 9
    (import sun.misc.Launcher$AppClassLoader)
    (eval '(extend sun.misc.Launcher$AppClassLoader
             dynapath.dynamic-classpath/DynamicClasspath
             vivid.art.pod/sealed-classloader-fns))
    (catch Exception _)))

(defmacro with-invoke-in
  "Given a pod, a fully-qualified symbol sym, and args which are Java objects,
  invokes the function denoted by sym with the given args. This is a low-level
  interface--args are not serialized before being passed to the pod and the
  result is not deserialized before being returned. Passing Clojure objects as
  arguments or returning Clojure objects from the pod will result in undefined
  behavior.

  This macro correctly handles the case where pod is the current pod without
  thread binding issues: in this case the invocation will be done in another
  thread."
  [pod [sym & args]]
  `(let [pod# ~pod]
     (if (not= pod# (.get this-pod))
       (.invoke pod# ~(str sym) ~@args)
       (deref (future (.invoke pod# ~(str sym) ~@args))))))

(defn eval-fn-call
  "Given an expression of the form (f & args), resolves f and applies f to args."
  [[f & args]]
  (when-let [ns (namespace f)] (require (symbol ns)))
  (if-let [f (resolve f)]
    (apply f args)
    (throw (Exception. (format "can't resolve symbol (%s)" f)))))

(defn call-in*
  "Low-level interface by which expressions are evaluated in other pods. The
  two-arity version is invoked in the caller with a pod instance and an expr
  form. The form is serialized and the one-arity version is invoked in the
  pod with the serialized expr, which is deserialized and evaluated. The result
  is then serialized and returned to the two-arity where it is deserialized
  and returned to the caller. The *print-meta* binding determines whether
  metadata is transmitted between pods.

  The expr is expected to be of the form (f & args). It is evaluated in the
  pod by resolving f and applying it to args.

  Note: Since forms must be serialized to pass from one pod to another it is
  not always appropriate to include metadata, as metadata may contain eg. File
  objects which are not printable/readable by Clojure."
  ([expr]
   (let [{:keys [meta? expr]} (read-string expr)]
     (binding [*print-meta* meta?]
       (pr-str (eval-fn-call expr)))))
  ([pod expr]
   (let [arg (pr-str {:meta? *print-meta* :expr expr})
         ret (with-invoke-in pod (vivid.art.pod/call-in* arg))]
     (guard (read-string ret)))))

(defmacro with-call-in
  "Given a pod and an expr of the form (f & args), resolves f in the pod,
  applies it to args, and returns the result to the caller. The expr may be a
  template containing the ~ (unquote) and ~@ (unquote-splicing) reader macros.
  These will be evaluated in the calling scope and substituted in the template
  like the ` (syntax-quote) reader macro.

  Note: Unlike syntax-quote, no name resolution is done on the template forms.

  Note2: The macro returned value will be nil unless it is
  printable/readable. For instance, returning File objects will not work
  as they are not printable/readable by Clojure."
  [pod expr]
  `(if-not ~pod
     (eval-fn-call (vivid.art.boot-backtick/template ~expr))
     (call-in* ~pod (vivid.art.boot-backtick/template ~expr))))

(defmacro with-call-worker
  "Like with-call-in, evaluating expr in the worker pod."
  [expr]
  `(with-call-in worker-pod ~expr))

(defn resolve-dependencies
  "Returns a seq of maps of the form {:jar <path> :dep <dependency vector>}
  corresponding to the fully resolved dependency graph as specified in the
  env, where env is the boot environment (see boot.core/get-env). The seq of
  dependencies includes all transitive dependencies."
  [env]
  (with-call-worker (aether/resolve-dependencies ~env)))

(defn resolve-dependency-jars
  "Returns a seq of File objects corresponding to the jar files associated with
  the fully resolved dependency graph as specified in the env, where env is the
  boot environment (see boot.core/get-env). If ignore-clj? is specified Clojure
  will be excluded from the result (the clojure dependency is identified by the
  BOOT_CLOJURE_NAME environment setting, which defaults to org.clojure.clojure)."
  [env & [ignore-clj?]]
  (let [clj-dep (symbol "org.clojure.clojure")
        rm-clj (if-not ignore-clj?
                 identity
                 (partial remove #(and (= clj-dep (first (:dep %)))
                                       (not-any? #{[:classifier "sources"]} (partition 2 (:dep %))))))]
    (->> env resolve-dependencies rm-clj (map (comp io/file :jar)))))

(defn eval-in*
  "Low-level interface by which expressions are evaluated in other pods. The
  two-arity version is invoked in the caller with a pod instance and an expr
  form. The form is serialized and the one-arity version is invoked in the
  pod with the serialized expr, which is deserialized and evaluated. The result
  is then serialized and returned to the two-arity where it is deserialized
  and returned to the caller. The *print-meta* binding determines whether
  metadata is transmitted between pods.

  Unlike call-in*, expr can be any expression, without the restriction that it
  be of the form (f & args).

  Note: Since forms must be serialized to pass from one pod to another it is
  not always appropriate to include metadata, as metadata may contain eg. File
  objects which are not printable/readable by Clojure."
  ([expr]
   (let [{:keys [meta? expr]} (read-string expr)]
     (binding [*print-meta* meta?]
       (pr-str (eval expr)))))
  ([pod expr]
   (let [arg (pr-str {:meta? *print-meta* :expr expr})
         ret (with-invoke-in pod (vivid.art.pod/eval-in* arg))]
     (guard (read-string ret)))))

(defmacro with-eval-in
  "Given a pod and an expr, evaluates the body in the pod and returns the
  result to the caller. The body may be a template containing the ~ (unquote)
  and ~@ (unquote-splicing) reader macros. These will be evaluated in the
  calling scope and substituted in the template like the ` (syntax-quote)
  reader macro.

  Note: Unlike syntax-quote, no name resolution is done on the template
  forms.

  Note2: The macro returned value will be nil unless it is printable/readable.
  For instance, returning File objects will not work as they are not printable
  and readable by Clojure."
  [pod & body]
  `(if-not ~pod
     (eval (vivid.art.boot-backtick/template (do ~@body)))
     (eval-in* ~pod (vivid.art.boot-backtick/template (do ~@body)))))

(defn require-in
  "Evaluates (require 'ns) in the pod. Avoid this function."
  [pod ns]
  (doto pod
    (with-eval-in
      (require '~(symbol (str ns))))))

(defn- init-pod!
  [env pod]
  (doto pod
    (require-in "vivid.art.pod")
    (with-invoke-in (set-worker-pod! worker-pod))
    (with-eval-in
      (require 'vivid.art.pod)
      ;(reset! boot.util/*verbosity* ~(deref util/*verbosity*))
      (alter-var-root #'vivid.art.pod/env (constantly '~env))
      (create-ns 'pod)
      (dosync (alter @#'clojure.core/*loaded-libs* conj 'pod))
      (alter-var-root #'*ns* (constantly (the-ns 'pod)))
      (clojure.core/refer-clojure))))

(defn make-pod-cp
  "Returns a new pod with the given classpath. Classpath may be a collection
  of java.lang.String or java.io.File objects.

  The :name and :data options are the same as for boot.pod/make-pod.

  NB: The classpath must include Clojure (either clojure.jar or directories),
  but must not include Boot's pod.jar, Shimdandy's impl, or Dynapath. These
  are needed to bootstrap the pod, have no transitive dependencies, and are
  added automatically."
  [classpath & {:keys [name data]}]
  (doto (->> (assoc env :dependencies [['vivid/ash-ra-template "0.2.0"]])
             (resolve-dependency-jars)
             (into (map io/file classpath))
             (into-array java.io.File)
             (PodShim/newShim nil data)                     ; TODO (or data boot.pod/data)
             (init-pod! nil))
    (pod-name (or name "my-ns"))))
