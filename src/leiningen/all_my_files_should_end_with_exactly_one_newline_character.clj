(ns leiningen.all-my-files-should-end-with-exactly-one-newline-character
  "Lint clojure files for file-ending newlines."
  (:require
   [clojure.java.io :as io]
   [com.gfredericks.all-my-files-should-end-with-exactly-one-newline-character :as core]
   [leiningen.core.main :as main]))

;; next four functions are pasted from cljfmt

(defn relative-path [dir file]
  (-> (.toURI dir)
      (.relativize (.toURI file))
      (.getPath)))

(defn grep [re dir]
  (filter #(re-find re (relative-path dir %)) (file-seq (io/file dir))))

(defn format-paths [project]
  (let [paths (concat (:source-paths project)
                      (:test-paths project))]
    (if (empty? paths)
      (main/abort "No source or test paths defined in project map")
      (->> (map io/file paths)
           (filter #(and (.exists %) (.isDirectory %)))))))

(defn find-files [project f]
  (let [f (io/file f)]
    (when-not (.exists f) (main/abort "No such file:" (str f)))
    (if (.isDirectory f)
      (grep #"\.clj[sc]?$" f)
      [f])))

(defn all-files
  [project]
  (->> (format-paths project)
       (mapcat #(find-files project %))))

(def usage
  "USAGE: lein all-my-files-should-end-with-exactly-one-newline-character [but-do-they? | so-fix-them]")

(defn all-my-files-should-end-with-exactly-one-newline-character
  "Lint clojure file-ending newlines.

USAGE:

  # prints file-ending-newline problems; alias \"check\"
  lein all-my-files-should-end-with-exactly-one-newline-character but-do-they?

  # corrects file-ending-newline problems; alias \"fix\"
  lein all-my-files-should-end-with-exactly-one-newline-character so-fix-them"
  [project & args]
  (let [all-files (all-files project)]
    (case (first args)
      ("but-do-they?" "check") (main/exit (core/but-do-they? all-files))
      ("so-fix-them"  "fix")   (core/so-fix-them all-files)
      "check-zero"             (main/exit (core/but-do-they? all-files :expected-newline-count 0))
      "fix-zero"               (core/so-fix-them all-files :expected-newline-count 0)
      (do
        (println usage)
        (main/exit 1)))))
