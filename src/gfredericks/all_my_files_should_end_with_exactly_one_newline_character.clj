(ns gfredericks.all-my-files-should-end-with-exactly-one-newline-character
  "Lint clojure files for file-ending newlines.")

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

(defn but-do-they?
  [files]
  (if-let [bad-files (seq (scan files))]
    (do
      (binding [*out* *err*]
        (println "No, your files don't all end with exactly one newline character:")
        (doseq [{:keys [file newline-count]} bad-files]
          (printf "  - %s ends in %d newline characters\n" file newline-count))
        (flush))
      (count bad-files))
    (do
      (println "Yes, all your files end with exactly one newline character.")
      0)))

(defn so-fix-them
  [files]
  (if-let [bad-files (seq (scan files))]
    (do
      (doseq [{:keys [file fixed]} (seq (scan files))]
        (spit file fixed)
        (printf "Fixed newlines in %s.\n" file))
      (flush))
    (println "All newlines are good, nothing to fix.")))
