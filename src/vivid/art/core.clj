(ns vivid.art.core
  (:require
    [eval-soup.core :as eval-soup]
    [reduce-fsm :as fsm]))

(defn lex [str]
  "Tokenizes the input stream"
  ; Note: I haven't figured out how to isolate both <% and <%= in the
  ; time I allotted myself. For the meantime, this regex splits <%=
  ; into <% followed by = .
  (clojure.string/split str #"(?=<%=?)|(?<=<%=?)|(?=%>)|(?<=%>)"))

(defn echo [acc val & _]
  "Echoes the value literal to the rendered output"
  (let [escaped (clojure.string/escape val {\" "\\\""})]
    (assoc acc :output
               (conj (acc :output)
                     (str "(emit \"" escaped "\")")))))

(defn echo-eval [acc expr & _]
  "Echoes the result of evaluating the expression to the rendered output"
  (assoc acc :output
             (conj (acc :output)
                   (str "(emit " expr " )"))))

(defn -eval [acc expr & _]
  "Evaluates the expression to the rendered output"
  (assoc acc :output
             (conj (acc :output)
                   expr)))

(fsm/defsm tokens->forms
           [[:echo
             "<%" -> :eval
             ;"<%=" -> :echo-eval
             _ -> {:action echo} :echo]
            [:eval
             "=" -> :echo-eval
             "%>" -> :echo
             _ -> {:action -eval} :eval]
            [:echo-eval
             "%>" -> :echo
             _ -> {:action echo-eval} :echo-eval]]
           :default-acc {:output []})

(defn parse [tokens]
  (let [fsm-result (tokens->forms tokens)]
    (fsm-result :output)))

(defn wrap-forms [forms]
  (concat ["(def ^StringBuilder __vt__art__sb__emit (new StringBuilder))"
           "(defn emit [val] (.append __vt__art__sb__emit val))"]
          forms
          ["(.toString __vt__art__sb__emit)"]))

(defn render [input]
  (println "INPUT:" input)
  (println "LEXED:" (lex input))
  (println "FORMS:" (parse (lex input)))
  (println "EVALD:" (last (eval-soup/code->results (wrap-forms (parse (lex input))))))
  (last (eval-soup/code->results (wrap-forms (parse (lex input))))))
