(ns klangmeister.compile.macros
  (:require [clojure.tools.namespace.find :as find]
            [clojure.tools.namespace.file :as file]
            [clojure.java.io :as io]
            [clojure.java.classpath :as classpath])
  (:import (java.io File)))

(defn name-val [rdr]
  [(-> rdr file/read-file-ns-decl second str) (slurp rdr)])

(defn files []
  (->> (classpath/classpath-directories)
       (mapcat #(find/find-sources-in-dir % find/cljs))))

(defn jars []
  (->> (classpath/classpath-jarfiles)
       (mapcat #(find/sources-in-jar % find/cljs))
       (map io/resource)))

(defn collate [entries]
  (reduce conj {} entries))

(defn sources* [names]
  (let [in-names? (->> names (map str) set)
        relevant? (fn [[name _]] (in-names? name))]
    (->> (concat (jars) (files))
         (map name-val)
         (filter relevant?)
         collate)))

(defmacro sources [& names]
  (sources* names))

(defmacro text [filename]
  (slurp filename))
