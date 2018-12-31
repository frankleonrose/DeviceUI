(ns mock-arduino-ui.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as gdom]
            [reagent.core :as r]
            [rc-slider]
            [cljs.core.async :as a :refer [<! >! alts! put!]]
            [haslett.client :as ws]
            [haslett.format :as fmt]))

(def slider (r/adapt-react-class rc-slider))

(defn digital-input
  ([sink-chan pin]
   (digital-input sink-chan pin 0))
  ([sink-chan pin start-value]
   (js/console.log (str "Digital input: " sink-chan ", " pin ", " start-value))
   (let [current-value (r/atom start-value)]
     (fn []
      [:div
        "The atom " [:code "current-value"] " has value: " @current-value ". "
        [:input {:type "button" :value "Toggle"
                 :on-click (fn [evt]
                            ; (js/console.log (str "Swapping value: " @current-value))
                            (swap! current-value (fn [x] (- 1 x)))
                            (if (not (put! sink-chan 
                                           (str "{\"op\":\"pinState\",\"pin\":" pin
                                            ",\"value\":" @current-value "}")
                                           #(js/console.log (str "put! to sink-chan " %))))
                              (js/console.log "sink-chan is closed!")))}]]))))

(defn send_slider [sink-chan pin value]
  (put! sink-chan (str "{\"op\":\"pinState\",\"pin\":" pin ",\"value\":" value "}")))

(defn simple-component [sink-chan]
  [:div
   [:label "Power: "]
   [slider {:min 0 :max 5000 :step 100 :onChange (partial send_slider sink-chan 101)}] ; :value val, :onChange fn, :min, :max, :step, https://github.com/react-component/slider
   [:label "Nothing: "]
   [digital-input sink-chan 13 1]
   [:p.someclass
    "I have " [:strong "bold"]
    [:span {:style {:color "red"}} " and red "] "text."]])


(go (let [stream (<! (ws/connect "ws://0.0.0.0:4000"))
          close-chan (:close-status stream)
          sink-chan (:sink stream)
          source-chan (:source stream)]
      (>! sink-chan "{\"op\":\"init\"}")
      (r/render [simple-component sink-chan] (gdom/getElement "app"))
      (loop []
        (let [[val port] (alts! [source-chan close-chan])]
          (when (= port source-chan)
            (js/console.log (str "From Arduino: " val))
            (recur))))
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
