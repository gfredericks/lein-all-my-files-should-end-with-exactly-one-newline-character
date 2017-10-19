(ns leiningen.all-my-files-should-end-with-exactly-one-newline-character
  "Lint clojure files for file-ending newlines."
  (:require
   [clojure.java.io     :as io]
   [leiningen.core.main :as main]))

(defn ^:private scan
  [files]
  (for [file files
        :let [contents (slurp file)
              [_ everything-but final-newlines] (re-matches #"(?s)(.*?)(\n*)"
                                                            contents)]
        :when (not= 1 (count final-newlines))]
    {:file file
     :newline-count (count final-newlines)
     :fixed (str everything-but "\n")}))

(defn ^:private but-do-they?
  [files]
  (if-let [bad-files (seq (scan files))]
    (do
      (binding [*out* *err*]
        (println "No, your files don't all end with exactly one newline character:")
        (doseq [{:keys [file newline-count]} bad-files]
          (printf "  - %s ends in %d newline characters\n" file newline-count))
        (flush))
      (main/exit (count bad-files)))
    (println "Yes, all your files end with exactly one newline character.")))

(defn ^:private so-fix-them
  [files]
  (if-let [bad-files (seq (scan files))]
    (do
      (doseq [{:keys [file fixed]} (seq (scan files))]
        (spit file fixed)
        (printf "Fixed newlines in %s.\n" file))
      (flush))
    (println "All newlines are good, nothing to fix.")))

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
  "Lint clojure file-ending newmlines.

USAGE:

  # prints file-ending-newline problems; alias \"check\"
  lein all-my-files-should-end-with-exactly-one-newline-character but-do-they?

  # corrects file-ending-newline problems; alias \"fix\"
  lein all-my-files-should-end-with-exactly-one-newline-character so-fix-them"
  [project & args]
  (let [all-files (all-files project)]
    (case (first args)
      ("but-do-they?" "check") (but-do-they? all-files)
      ("so-fix-them"  "fix")   (so-fix-them  all-files)
      (do
        (println usage)
        (main/exit 1)))))
