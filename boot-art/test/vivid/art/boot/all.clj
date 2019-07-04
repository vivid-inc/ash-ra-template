; Copyright 2019 Vivid Inc.

(ns vivid.art.boot.all
  (:require
    [boot.core :as boot :refer [deftask]]
    [boot.test :refer :all]
    [clojure.java.io :as io]
    [clojure.string :as string]
    [vivid.boot-art :refer [art]])
  (:use
    [clojure.test :only [is testing]]))

(deftask all
         "Run art tests"
         [f file VALUE str "Template files"]
         (let [args (cond-> []
                            file (concat ["--file" file])
                            :always vec)]))
