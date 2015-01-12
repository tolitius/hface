(ns hface.util.transit
  (:import (java.io ByteArrayOutputStream))
  (:require [cognitect.transit :as transit]))

;; a piece from "https://github.com/jalehman/ring-transit"
(defn write [x t opts]
  (let [baos (ByteArrayOutputStream.)
        w    (transit/writer baos t opts)
        _    (transit/write w x)
        ret  (.toString baos)]
    (.reset baos)
    ret))
