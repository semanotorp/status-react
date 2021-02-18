(ns status-im.ui.screens.chat.message.gap
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.utils.datetime :as datetime]
            [status-im.ui.screens.chat.styles.input.gap :as style]))

(defn on-press
  [ids first-gap? idx list-ref chat-id]
  (fn []
    (when (and list-ref @list-ref)
      (.scrollToIndex ^js @list-ref
                      #js {:index        (max 0 (dec idx))
                           :viewOffset   20
                           :viewPosition 0.5}))
    (if first-gap?
      (re-frame/dispatch [:chat.ui/fetch-more chat-id])
      (re-frame/dispatch [:chat.ui/fill-gaps ids chat-id]))))

(views/defview gap
  [{:keys [gaps first-gap?]} idx list-ref timeline chat-id]
  (views/letsubs [range [:chats/range chat-id]
                  {:keys [might-have-join-time-messages?]} [:chat-by-id chat-id]
                  in-progress? [:chats/fetching-gap-in-progress?
                                (if first-gap?
                                  [:first-gap]
                                  (:ids gaps))
                                chat-id]
                  connected?   [:mailserver/connected?]]
    (let [ids            (:ids gaps)]
      (when-not (and first-gap? might-have-join-time-messages?)
        [react/view {:style style/gap-container}
         [react/touchable-highlight
          {:on-press (when (and connected? (not in-progress?))
                       (on-press ids first-gap? idx list-ref chat-id))
           :style    style/touchable}
          [react/view {:style style/label-container}
           (if in-progress?
             [react/activity-indicator]
             [react/nested-text
              {:style (style/gap-text connected?)}
              (i18n/label (if first-gap?
                            (if timeline :t/load-more-timeline :t/load-more-messages)
                            (if timeline :t/fetch-timeline :t/fetch-messages)))
              (when first-gap?
                [{:style style/date}
                 (let [date (datetime/timestamp->long-date
                             (* 1000 (:lowest-request-from range)))]
                   (str
                    "\n"
                    (i18n/label :t/load-messages-before
                                {:date date})))])])]]]))))
