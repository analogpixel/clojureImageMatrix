
(ns image.core
  (:use clojure.core.matrix clojure.core.matrix.operators)
  (:require [clojure.core.matrix :refer :all]
            [clojure.core.matrix.operators :as mo])
  (:import (java.io File FileInputStream) javax.imageio.ImageIO java.awt.image.BufferedImage)
)

(defn makeMatrix [min mout w]
  (if (<= (count min) 0)
    mout
    (makeMatrix (drop w min) (conj mout (take w min)) w)
    )
  )

(defn loadImageMatrix [filename]

  (def img  (ImageIO/read (FileInputStream. (File. filename))))
  (def w  (.getWidth img))
  (def h (.getHeight img))

  (makeMatrix (.getRGB ^BufferedImage img 0 0 w h nil 0, w ) (vec []) w)
  )

(defn saveImageMatrix [imgMatrix imtype filename]
  (let [
        h (row-count imgMatrix)
        w (column-count imgMatrix)
        bufImg (BufferedImage. w h BufferedImage/TYPE_INT_ARGB)
        ]

    (dotimes [y h]
      (dotimes [x w]
        (.setRGB bufImg x y (mget imgMatrix y x))
        )
      )
    (ImageIO/write ^BufferedImage bufImg imtype  (File. filename))
    )
  )

(defn unpackrgba [^long rgba]
  (let [r (bit-and (bit-shift-right rgba 16) 0xFF)
        g (bit-and (bit-shift-right rgba 8) 0xFF)
        b (bit-and (bit-shift-right rgba 0) 0xFF)
        a (bit-and (bit-shift-right rgba 24) 0xFF)
        ]

  [r g b a]
  )
)

(defn packrgba [r g b a]
  (unchecked-int
  (bit-or
  (bit-shift-left r 16)
  (bit-shift-left g 8)
  (bit-shift-left b 0)
  (bit-shift-left a 24)
  )
  )
  )

(defn reduceColor [^long rgba n]
  (let    [n (int (/ 255 n))
           c (unpackrgba rgba)
           rr (* (int (/ (c 0) n)) n)
           rg (* (int (/ (c 1) n)) n)
           rb (* (int (/ (c 2) n)) n)
          ]
    (packrgba rr rg rb (c 3))
    )
  )

(defn bw [rgba n]
  (let    [c (unpackrgba rgba)
           rr (* (int (/ (c 0) n)) n)
           rg (* (int (/ (c 1) n)) n)
           rb (* (int (/ (c 2) n)) n)
          ]
    (packrgba rr rr rr (c 3))
    )
  )


;; if setting this is valid then set it and return it
;; if it is out of bounds just return the unchanged matrix
(defn validmset [m r c v]
  (if (and (>= r 0)
           (>= c 0)
           (< r (row-count m) )
           (< c (column-count m) ))
    (mset m r c v)
    m
    )

  )

;; given a row and col give all he neighbors defined in n
(defn getNeigh [m r c n]

   (if (> (count n) 0)
     (validmset (getNeigh m r c (rest n)) (+ r ((first n) 0)) (+ c ((first n) 1)) 1)
     (repeat (row-count m) (repeat (column-count m) 0 ))
     )
)

;; row col
;; y   x
(def neighFour [[-1 0] [0 -1] [0 1] [1 0]])
(def neighEight [[-1 -1] [-1 0] [-1 1] [0 -1] [0 1] [1 -1] [1 0] [1 1]])

(defn -main
  [& args]
  (set-current-implementation :vectorz)
  (set! *warn-on-reflection* true)

  (def m (loadImageMatrix "c:/data/1.png"))
  (def n (loadImageMatrix "c:/data/2.png"))

  (saveImageMatrix (mo/- m n) "png" "c:/data/yay.png")
  (saveImageMatrix (emap #(bw % 23) (mo/- m n)) "png" "c:/data/yay.png")
  )
