(ns status-im.ui.screens.communities.profile
  (:require [quo.core :as quo]
            [status-im.utils.handlers :refer [>evt <sub]]
            [status-im.ui.components.profile-header.view :as profile-header]
            [status-im.i18n.i18n :as i18n]
            [reagent.core :as reagent]
            [status-im.communities.core :as communities]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.constants :as constants]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.unviewed-indicator :as unviewed-indicator]
            [quo.react-native :as rn]))

(defn management [route]
  (let [{:keys [community-id]}      (get-in route [:route :params])
        requests-to-join (<sub [:communities/requests-to-join-for-community community-id])
        community (<sub [:communities/community community-id])

        {:keys [color
                members
                description
                name admin]} community
        roles                       false
        notifications               false
        members-count               (count members)]

    [:<>
     [quo/animated-header {:left-accessories  [{:icon                :main-icons/arrow-left
                                                :accessibility-label :back-button
                                                :on-press            #(>evt [:navigate-back])}]
                           :right-accessories [{:icon                :main-icons/share
                                                :accessibility-label :invite-button
                                                :on-press            #(>evt [::communities/invite-people-pressed community-id])}]
                           :extended-header   (profile-header/extended-header
                                               {:title    name
                                                :color    (or color (rand-nth colors/chat-colors))
                                                :photo    (if (= community-id constants/status-community-id)
                                                            (:uri
                                                             (rn/resolve-asset-source
                                                              (resources/get-image :status-logo)))
                                                            (get-in community [:images :large :uri]))
                                                :subtitle (i18n/label-pluralize members-count :t/community-members {:count members-count})})
                           :use-insets        true}
      [:<>
       [quo/list-footer {:color :main}
        (get-in description [:identity :description])]
       [quo/separator {:style {:margin-vertical 8}}]
       [quo/list-item {:chevron        true
                       :accessory
                       [react/view {:flex-direction :row}
                        (when (pos? members-count)
                          [quo/text {:color :secondary} (str members-count)])
                        [unviewed-indicator/unviewed-indicator (count requests-to-join)]]
                       :on-press       #(>evt [:navigate-to :community-members {:community-id community-id}])
                       :title          (i18n/label :t/members-label)
                       :icon           :main-icons/group-chat}]
       (when (and admin roles)
         [quo/list-item {:chevron true
                         :title   (i18n/label :t/commonuity-role)
                         :icon    :main-icons/objects}])
       (when notifications
         [quo/list-item {:chevron true
                         :title   (i18n/label :t/chat-notification-preferences)
                         :icon    :main-icons/notification}])
       [quo/separator {:style {:margin-vertical 8}}]
       ;; Disable as not implemented yet
       (when false
         [quo/list-item {:theme    :accent
                         :icon     :main-icons/edit
                         :title    (i18n/label :t/edit-community)
                         :on-press #(>evt [::communities/open-edit-community community-id])}])
       [quo/list-item {:theme    :accent
                       :icon     :main-icons/arrow-left
                       :title    (i18n/label :t/leave-community)
                       :on-press #(>evt [::communities/leave community-id])}]
       (when admin
         [quo/list-item {:theme    :negative
                         :icon     :main-icons/delete
                         :title    (i18n/label :t/delete)
                         :on-press #(>evt [::communities/delete-community community-id])}])]]]))

(defn management-container [route]
  (reagent/create-class
   {:display-name "community-profile-view"
    :component-did-mount (fn []
                           (communities/fetch-requests-to-join! (get-in route [:route :params :community-id])))
    :reagent-render management}))



