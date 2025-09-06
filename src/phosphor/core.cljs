(ns phosphor.core
  (:require ["fs" :as fs]
            ["path" :as path]
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

(defn ensure-parent-dirs-exist [filename]
  (let [directory-path (path/dirname filename)]
    (when-not (fs/existsSync directory-path)
      (fs/mkdirSync directory-path #js {:recursive true}))))

(defn write-file-sync [filename content-str]
  (ensure-parent-dirs-exist filename)
  (fs/writeFileSync filename content-str))

(defn main [target-directory]
  (let [target-directory (if (str/blank? target-directory)
                           "data-out"
                           target-directory)]
    ;; Export the metadata
    (write-file-sync (str target-directory "/metadata.edn")
                     (with-out-str (pp/pprint icons)))

    ;; Export the SVG files to Hiccup format in EDN files.
    (doseq [icon-style ["bold" "duotone" "fill" "light" "regular" "thin"]
            {icon-name :name} icons]
      (let [src-svg-file-path (js/require.resolve (str "@phosphor-icons/core/assets/"
                                                       icon-style
                                                       "/"
                                                       (if (= icon-style "regular")
                                                         icon-name
                                                         (str icon-name "-" icon-style))
                                                       ".svg"))
            svg-content (-> src-svg-file-path
                            (fs/readFileSync "utf8"))]
        ;; Export the SVG file
        (write-file-sync (str target-directory "/" icon-style "/" icon-name ".svg")
                         svg-content)
        ;; Export the EDN file
        (write-file-sync (str target-directory "/" icon-style "/" icon-name ".edn")
                         (with-out-str (pp/pprint (first (html->hiccup svg-content)))))))

    (println (str "Export to directory " target-directory " is finished."))))

(comment

  (-> (js/require.resolve "@phosphor-icons/core/assets/bold/acorn-bold.svg")
      (fs/readFileSync "utf8")
      (html->hiccup))

  ,)