(ns status-im.ui.screens.routing.status-stack
  (:require [status-im.ui.screens.routing.core :as navigation]
            [status-im.ui.components.tabbar.styles :as tabbar.styles]
            [status-im.ui.screens.status.views :as status.views]
            [status-im.constants :as constants]))

(defonce stack (navigation/create-stack))

(defn status-stack []
  [stack {:initial-route-name :status
          :header-mode        :none}
   [{:name      :status
     :on-focus  [:load-messages constants/timeline-chat-id]
     :insets    {:top true}
     :style     {:padding-bottom tabbar.styles/tabs-diff}
     :component status.views/timeline}]])
