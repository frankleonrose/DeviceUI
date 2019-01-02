(ns mock-arduino-ui.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [goog.dom :as gdom]
            [reagent.core :as r]
            [rc-slider]
            [cljs.core.async :as a :refer [<! >! alts! put! chan]]
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
                            (swap! current-value (fn [x] (- 1 x)))
                            (if (not (put! sink-chan {:op :pinState :pin pin :value @current-value}))
                              (js/console.error "sink-chan is closed!")))}]]))))

(defn send_slider [sink-chan pin value]
  (put! sink-chan {:op :pinState :pin pin :value value}))

(def state (r/atom {}))

(defn light-strip []
  (fn []
    (let [pixels (:light-strip @state) 
          length (count pixels)]
      [:svg {:x 0 :y 0 :width (* length 10) :height 10}
        (map #(identity [:rect {:key % :x (* % 10) :y 0 :width 9 :height 9 :fill (nth pixels %)}]) (range length))])))

(defn simple-component [sink-chan]
  (fn []
    [:div
      [:label "Power sensor: "]
      [slider {:min 0 :max 5000 :step 500 :onChange (partial send_slider sink-chan 101)}] ; :value val, :onChange fn, :min, :max, :step, https://github.com/react-component/slider
      [:label "Open sensor: "]
      [slider {:min 0 :max 2000 :step 500 :onChange (partial send_slider sink-chan 100)}] ; :value val, :onChange fn, :min, :max, :step, https://github.com/react-component/slider
      [digital-input sink-chan 13 1]
      [:label "Light Strip: "]
      [light-strip]]))
      ; [:p.someclass
      ;   "I have " [:strong "bold"]
      ;   [:span {:style {:color "red"}} " and red "] "text."]]))

(defn handle-op [op]
  ; (js/console.log "From Arduino: " (clj->js op))
  (when (:light-strip op)
    ; (if (not= (:light-strip op) (:light-strip @state))
    ;   (js/console.log "New light-strip: " (clj->js (:light-strip op))))
    (swap! state assoc :light-strip (:light-strip op))))
  
(defn clj->json [clj]
  (.stringify js/JSON (clj->js clj)))

(defn json->clj [json]
  (js->clj (.parse js/JSON json) :keywordize-keys true))

(go (let [
          send-chan (chan 1 (map clj->json))
          receive-chan (chan 1 (map json->clj))
          stream (<! (ws/connect "ws://0.0.0.0:4000" {:sink send-chan :source receive-chan}))
          close-chan (:close-status stream)]
          ; send-chan (:sink stream)
          ; receive-chan (:source stream)]
      (>! send-chan {:op :connect})
      (r/render [simple-component send-chan] (gdom/getElement "app"))
      (loop []
        (let [[val port] (alts! [receive-chan close-chan])]
          (when (= port receive-chan)
            (handle-op val)
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
