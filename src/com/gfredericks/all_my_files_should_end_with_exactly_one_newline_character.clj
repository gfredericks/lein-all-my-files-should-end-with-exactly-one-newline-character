(ns com.gfredericks.all-my-files-should-end-with-exactly-one-newline-character
  "Lint clojure files for file-ending newlines.")

(defn ^:private scan
  [files expected-newline-count]
  {:pre [(#{0 1} expected-newline-count)]}
  (for [file files
        :let [contents (slurp file)
              [_ everything-but final-newlines] (re-matches #"(?s)(.*?)(\n*)"
                                                            contents)
              final-newline-count (count final-newlines)]
        :when (not= final-newline-count expected-newline-count)]
    {:file file
     :newline-count final-newline-count
     :fixed (str everything-but (when (= 1 expected-newline-count)
                                  "\n"))}))

(defn but-do-they?
  [files & {:keys [expected-newline-count]
            :or {expected-newline-count 1}}]
  {:pre [(#{0 1} expected-newline-count)]}
  (if-let [bad-files (seq (scan files expected-newline-count))]
    (do
      (binding [*out* *err*]
        (if (= 1 expected-newline-count)
          (println "No, your files don't all end with exactly one newline character:")
          (println "No, your files don't all end without a newline character:"))
        (doseq [{:keys [file newline-count]} bad-files]
          (printf "  - %s ends in %d newline characters\n" file newline-count))
        (flush))
      (count bad-files))
    (do
      (if (= 1 expected-newline-count)
        (println "Yes, all your files end with exactly one newline character.")
        (println "Yes, all your files end without a newline character."))
      0)))

(defn so-fix-them
  [files & {:keys [expected-newline-count]
            :or {expected-newline-count 1}}]
  {:pre [(#{0 1} expected-newline-count)]}
  (if-let [bad-files (seq (scan files expected-newline-count))]
    (do
      (doseq [{:keys [file fixed]} (seq (scan files expected-newline-count))]
        (spit file fixed)
        (printf "Fixed newlines in %s.\n" file))
      (flush))
    (println "All newlines are good, nothing to fix.")))
