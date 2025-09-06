(ns phosphor.core
  (:require ["fs" :as fs]
            ["@phosphor-icons/core" :as phosphor-icons]
            [clojure.string :as str]
            [cljs.pprint :as pp]
            [camel-snake-kebab.core :as csk]
            [taipei-404.html :refer [html->hiccup]]))

(defn- number->version-str [n]
  (if (int? n)
    (str n ".0")
    (str n)))

(def icons
  (-> phosphor-icons/icons
      (js->clj :keywordize-keys true)
      (->> (mapv (fn [icon]
                   (-> icon
                       (update-keys csk/->kebab-case-keyword)
                       (dissoc :pascal-name :codepoint)
                       (update :published-in number->version-str)
                       (update :updated-in number->version-str)))))))

(defn main [target-directory]
  (when-not (str/blank? target-directory)
    ;; Export the metadata
    (fs/writeFileSync (str target-directory "/metadata.edn")
                      (with-out-str (pp/pprint icons)))

    ;; Export the SVG files to Hiccup format in EDN files.
    (doseq [icon-style ["bold" "duotone" "fill" "light" "regular" "thin"]]
      (let [icon-style-folder-path (str target-directory "/" icon-style)]
        ;; Ensure that the folder for this style exists
        (when-not (fs/existsSync icon-style-folder-path)
          (fs/mkdirSync icon-style-folder-path #js {:recursive true}))

        (doseq [{icon-name :name} icons]
          (let [src-svg-file-path (js/require.resolve (str "@phosphor-icons/core/assets/"
                                                           icon-style
                                                           "/"
                                                           (if (= icon-style "regular")
                                                             icon-name
                                                             (str icon-name "-" icon-style))
                                                           ".svg"))
                dst-hiccup-file-path (str target-directory "/" icon-style "/" icon-name ".edn")]
            (-> src-svg-file-path
                (fs/readFileSync "utf8")
                (html->hiccup)
                (as-> hiccup (with-out-str (pp/pprint hiccup)))
                (->> (fs/writeFileSync dst-hiccup-file-path)))))))

    (println (str "Export to directory " target-directory " is finished."))))
