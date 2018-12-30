(ns mock-arduino-ui.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as gdom]
            [reagent.core :as r]
            [rc-slider]
            [cljs.core.async :as a :refer [<! >!]]
            [haslett.client :as ws]
            [haslett.format :as fmt]))

(def slider (r/adapt-react-class rc-slider))

(defn simple-component []
  [:div
   [slider] ; :value val, :onChange fn, :min, :max, :step, https://github.com/react-component/slider
   [:p "I am a component!"]
   [:p.someclass
    "I have " [:strong "bold"]
    [:span {:style {:color "red"}} " and red "] "text."]])

(r/render [simple-component] (gdom/getElement "app"))

(go (let [stream (<! (ws/connect "ws://0.0.0.0:4000"))]
      (>! (:sink stream) "Hello World")
      (js/console.log (str "From Arduino: " (<! (:source stream))))
      (ws/close stream)))

; (def ctx (-> js/document
;              (.getElementById "canvas")
;              (.getContext "2d")))

; (defn draw-shape [x y radius color]
;   (set! (.-fillStyle ctx) color)
;   (.beginPath ctx)
;   (.arc ctx x y radius 0 (* 2 Math/PI))
;   (.fill ctx))

; (draw-shape 150 150 100 "blue")
