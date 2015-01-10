
(ns image.core
  (require [clojure.core.matrix :as mx] [clojure.core.matrix.operators :as mxop])
)

(mx/set-current-implementation :vectorz)
(import 'java.io.File)
(import 'java.io.FileInputStream)
(import 'javax.imageio.ImageIO)
(import 'java.awt.image.BufferedImage)

(defn getrgb [rgba]
  (let [r (bit-and (bit-shift-right (rgba 1) 16) 0xFF)
        g (bit-and (bit-shift-right (rgba 1) 8) 0xFF)
        b (bit-and (bit-shift-right (rgba 1) 0) 0xFF)
        a (bit-and (bit-shift-right (rgba 1) 24) 0xFF)
        ]

  [r g b a]
  )
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

;; http://docs.oracle.com/javase/7/docs/api/java/awt/image/BufferedImage.html
;; http://stackoverflow.com/questions/10880083/get-rgb-of-a-bufferedimage
;; http://stackoverflow.com/questions/19202082/clojure-amap-is-very-slow
;; http://www.slideshare.net/mikeranderson/2013-1114-enter-thematrix

;; load an image a java bufferedImage
(def img  (ImageIO/read (FileInputStream. (File. "c:/data/circ.png"))))
(def w (.getWidth img))
(def h (.getHeight img))

;; convert the buffered image into an int array
(def imgIntArray (.getRGB img 0 0 w h nil 0, w ))
;; convert it to a core.matrix
(def imgMatrix (colorMatrix imgIntArray 8 w h ))
;; convert it back to an int Array
(def newImage  (mx/eseq imgMatrix))

;; It is returned as a lazy seq
(type newImage)

;; make it an int array
(def newImgIntArray (int-array (doall newImage)))

;; create a buffered image
(def bufImg  (BufferedImage. w h BufferedImage/TYPE_INT_RGB))

;; write the pixel data to it
(.setRGB bufImg 0 0 w h newImgIntArray 0 w)

;; save it to disk
(ImageIO/write bufImg "png" (File. "c:/data/newcirc.png"))

;; (def b (into [] a))


(def newImage ( mx/coerce  :double-array ((mx/reshape (colorMatrix a 16 (.getWidth img) (.getHeight img)) [1 (* (.getHeight img) (.getWidth img) )]) 0)))

(.setRGB img 0 0 (int-array newImage 0))
)
