(ns guestbook.core
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [ajax.core :refer [GET POST]]))

(defn save-message! [fields errors]
  (POST "/message" {:format :json
                    :headers {"Accept" "application/transit+json"
                              "x-csrf-token" (->> "token"
                                                  (.getElementById js/document)
                                                  (.-value))}
                    :params @fields
                    :handler (fn [r]
                               (.log js/console (str "response: " r))
                               (reset! errors nil))
                    :error-handler (fn [e]
                                     (.error js.console (str "Error: " e))
                                     (reset! errors (-> e :response :errors))
                                     )
                                     }))

(defn error-notification [errors id]
  (when-let [error (id @errors)]
    [:div.notification.is-danger
     (clojure.string/join error)]))

(defn message-form []
  (let [fields (r/atom {:name "" :message "" })
        errors (r/atom nil)]
    (fn []
      [:div
       [error-notification errors :server-error]
       [:div.field
        [:label.label {:for :name} "Name"]
        [error-notification errors :name]
        [:div.control.has-icons-left
         [:input.input {:type :text
                        :placeholder "Alex Jefferson"
                        :name :name
                        :value (:name @fields)
                        :on-change #(swap! fields assoc :name (-> % .-target .-value))}]
         [:span.icon.is-small.is-left
          [:i.fa]]]]
       
       [:div.field
        [:label.label {:for :message} "Message"]
        [error-notification errors :message]
        [:textarea.textarea {:type :text
                             :placeholder "Hello World"
                             :name :message
                             :value (:message @fields)
                             :on-change #(swap! fields assoc :message (-> % .-target .-value))}]]
       [:div.field
        [:input.button.is-success {:type :submit
                                   :value "Comment"
                                   :on-click #(save-message! fields errors)
                                   }]]])))

(defn home []
  [:main
   [:div.columns.is-centered
    [:div.column.is-two-thirds
     [:p.title "Messages"]
;     [messages]
     [:div.columns
      [:div.column
       [message-form]]]]
    ]])

(rdom/render
 [home]
 (.getElementById js/document "content"))
