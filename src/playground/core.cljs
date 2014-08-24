(ns playground.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async :refer [>! <! put! chan alts!]]
            [goog.events :as events]
            [goog.dom.classes :as classes]
            [goog.fx :as fx])
  (:import [goog.events EventType]
           [goog.fx DragDrop]))

(enable-console-print!)

;; =============================================================================
;; Utilities
(defn by-id
  "Helper function for document.getElementById(id)"
  [id]
  (.getElementById js/document id))

(defn events->chan
  "Given a target DOM element and event type return a channel of
  observed events. Can supply the channel to receive events as third
  optional argument."
  ([el event-type] (events->chan el event-type (chan)))
  ([el event-type c]
   (events/listen el event-type (fn [e] (put! c e))) c))

(defn show!
  "Replace the content of given element with the specified message."
  [id msg]
  (set! (.-innerHTML (by-id id)) msg))

(defn ex1 []
  (let [clicks (events->chan (by-id "button") EventType.CLICK)
        show! (partial show! "msg")]
    (go
      (show! "Waiting for a click ...")
      (<! clicks)
      (show! "Got a click!"))))

(ex1)

(defn ex2 []
  (let [clicks (events->chan (by-id "button2") EventType.CLICK)
        show! (partial show! "msg2")]
    (go
      (show! "Waiting for a click ...")
      (loop [n 0]
        (<! clicks)
        (when (< n 10)
          (do
            (show! (str "Click received! " (inc n)))
            (recur (inc n))))))))

(ex2)

(defn drag-drop []
  (let [source (fx/DragDrop. "drag")
        target (fx/DragDrop. "drop")
        drag-start (events->chan source EventType.DRAGSTART)
        dropped (events->chan target EventType.DROP)
        show! (partial show! "msg3")]
    (.addTarget source target)
    (.init source)
    (.init target)
    (go
      (show! "Waiting for drag ...")
      (loop []
        (let [[action chan] (alts! [drag-start dropped])]
          (condp = chan
            drag-start (show! "Drag start!")
            dropped    (show! "Dropped!")))
        (recur)))))

(drag-drop)
