
(ns image.core
  (require [clojure.core.matrix :as mx] [clojure.core.matrix.operators :as mxop])
)

(mx/set-current-implementation :vectorz)
(import 'java.io.File)
(import 'java.io.FileInputStream)
(import 'javax.imageio.ImageIO)
(import 'java.awt.image.BufferedImage)

(defn unpackrgba [rgba]
  (let [r (bit-and (bit-shift-right rgba 16) 0xFF)
        g (bit-and (bit-shift-right rgba 8) 0xFF)
        b (bit-and (bit-shift-right rgba 0) 0xFF)
        a (bit-and (bit-shift-right rgba 24) 0xFF)
        ]

  [r g b a]
  )
)

(defn packrgba [r g b a]
  (bit-or
  (bit-shift-left r 16)
  (bit-shift-left g 8)
  (bit-shift-left b 0)
  (bit-shift-left a 24)
  )
  )

(defn reduceColor [rgba n]
  (let    [c (unpackrgba rgba)
           rr (* (int (/ (c 0) n)) n)
           rg (* (int (/ (c 1) n)) n)
           rb (* (int (/ (c 2) n)) n)
          ]
    (packrgba rr rg rb (c 3))
    )
  )

(defn loadImageMatrix [filename]
  (def img  (ImageIO/read (FileInputStream. (File. filename))))
  (def w (.getWidth img))
  (def h (.getHeight img))

  ;; convert the buffered image into an int array
  (def imgIntArray (.getRGB img 0 0 w h nil 0, w ))

  ;; create a core.matrix with all RGBA
  (mx/reshape imgIntArray [w h])
  )

(defn saveImageMatrix [imgMatrix type filename]
  ;; convert it back to an int Array
  (def newImage  (mx/eseq imgMatrix))

  ;; make it an int array
  (def newImgIntArray (int-array (doall newImage)))

  ;; create a buffered image
  (def bufImg  (BufferedImage. w h BufferedImage/TYPE_INT_ARGB))

  ;; write the pixel data to it
  (.setRGB bufImg 0 0 w h newImgIntArray 0 w)

  ;; save it to disk
  (ImageIO/write bufImg type (File. filename))

  )

;; 16 red
;; 8 green
;; 0 blue
;; return a matrix of the color value
(defn colorMatrix [img bits w h]
  (mx/reshape (int-array  (map #(bit-and (bit-shift-right % bits) 0xFF) img)) [h w])
)

(defn -main
[& args]

(loadImageMatrix "c:/data/circ.png")

(saveImageMatrix
 (mx/emap #(reduceColor % 5) (loadImageMatrix "c:/data/circ.png") )
 "jpg"
 "c:/data/test2.jpg")

)
