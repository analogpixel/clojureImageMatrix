<div id="table-of-contents">
<h2>Table of Contents</h2>
<div id="text-table-of-contents">
<ul>
<li><a href="#sec-1">1. Image Manipulation with clojure</a>
<ul>
<li><a href="#sec-1-1">1.1. Clojure namespace</a></li>
<li><a href="#sec-1-2">1.2. random stuff</a></li>
<li><a href="#sec-1-3">1.3. 32bit RGBA values</a></li>
<li><a href="#sec-1-4">1.4. Return a color channel as a matrix</a></li>
<li><a href="#sec-1-5">1.5. Test code</a>
<ul>
<li><a href="#sec-1-5-1">1.5.1. Links to helpful places</a></li>
</ul>
</li>
</ul>
</li>
</ul>
</div>
</div>

# Image Manipulation with clojure<a id="sec-1" name="sec-1"></a>

    (defproject image "0.1.0-SNAPSHOT"
      :description "FIXME: write description"
      :url "http://example.com/FIXME"
      :license {:name "Eclipse Public License"
                :url "http://www.eclipse.org/legal/epl-v10.html"}
      :main image.core
      :dependencies [[org.clojure/clojure "1.6.0"]
                     [net.mikera/core.matrix "0.32.1"]
                     [net.mikera/vectorz-clj "0.28.0"]
                     ])

## Clojure namespace<a id="sec-1-1" name="sec-1-1"></a>

    (ns image.core
      (require [clojure.core.matrix :as mx] [clojure.core.matrix.operators :as mxop])
    )

## random stuff<a id="sec-1-2" name="sec-1-2"></a>

    (mx/set-current-implementation :vectorz)
    (import 'java.io.File)
    (import 'java.io.FileInputStream)
    (import 'javax.imageio.ImageIO)
    (import 'java.awt.image.BufferedImage)

## 32bit RGBA values<a id="sec-1-3" name="sec-1-3"></a>

given a 32bit value, extract the RGBA values from it

[AAAAAAAARRRRRRRRGGGGGGGGBBBBBBBB]

To get the Alpha value A from a 32bit binary value, you would shift off the RGB values, so
move everything to the right 24 times so those values slide off and you are just left with
AAAAAAAA

to get the Red value R from a 32bit binary value, you would shift off the GB values, so
move everyhing to the right 16 times to remove all the G and B bits, and you are left with
AAAAAAAARRRRRRRR.  You then  do a binary and of 0000000011111111 and have that remove the
first 8 bits if they exist.

To get the Green value G from a 32bit binary value, you would shift off the B values,
and then do a binary and of 000000000000000011111111 to remove the A and R values.

to get the Blue value B from a 32bit binary value, you would shift off nothing, and
then do a binary and of 00000000000000000000000011111111 to get just the blue value

    (defn getrgb [rgba]
      (let [r (bit-and (bit-shift-right rgba 16) 0xFF)
            g (bit-and (bit-shift-right rgba 8) 0xFF)
            b (bit-and (bit-shift-right rgba 0) 0xFF)
            a (bit-and (bit-shift-right rgba 24) 0xFF)
            ]

      [r g b a]
      )
    )

To explore binary conversion in clojure, you can call the (Integer/toString <number> <base>) function
to print out number in base.  So if you have the integer 982044636 and you wanted to see what
the binary value looked like you could run:

    (Integer/toString 982044636 2)

and get: "111010100010001100111111011100".  Now if you wanted to shift some values you would run:

    (Integer/toString (bit-shift-right 982044636 16) 2)

to get: "11101010001000" which is the above number with the 16 right most bits removed.

## Return a color channel as a matrix<a id="sec-1-4" name="sec-1-4"></a>

    ;; 16 red
    ;; 8 green
    ;; 0 blue
    ;; return a matrix of the color value
    (defn colorMatrix [img bits w h]
      (mx/reshape (int-array  (map #(bit-and (bit-shift-right % bits) 0xFF) img)) [h w])
    )

    (defn imageMatrix [img w h]
       (mx/reshape img [h w])
    )

## Test code<a id="sec-1-5" name="sec-1-5"></a>

### Links to helpful places<a id="sec-1-5-1" name="sec-1-5-1"></a>

-   [Java BufferedImage class docs](http://docs.oracle.com/javase/7/docs/api/java/awt/image/BufferedImage.html)
-   [Getting RGB value of buffeeredImage](http://stackoverflow.com/questions/10880083/get-rgb-of-a-bufferedimage)
-   [Why amap is running slow](http://stackoverflow.com/questions/19202082/clojure-amap-is-very-slow)
-   [Core.matrix presentation](http://www.slideshare.net/mikeranderson/2013-1114-enter-thematrix)

The main test program

    (defn -main
    [& args]

    ;; load an image a java bufferedImage
    (def img  (ImageIO/read (FileInputStream. (File. "c:/data/circ.png"))))
    (def w (.getWidth img))
    (def h (.getHeight img))

    ;; convert the buffered image into an int array
    (def imgIntArray (.getRGB img 0 0 w h nil 0, w ))

    ;; create a core.matrix with just the blue channel
    (def imgMatrix (colorMatrix imgIntArray 8 w h ))

    ;; create a core.matrix with all RGBA
    (def imgMatrix (imageMatrix imgIntArray w h))

    ;; convert it back to an int Array
    (def newImage  (mx/eseq imgMatrix))

    ;; It is returned as a lazy seq
    (type newImage)

    ;; make it an int array
    (def newImgIntArray (int-array (doall newImage)))

    ;; create a buffered image
    (def bufImg  (BufferedImage. w h BufferedImage/TYPE_INT_ARGB))

    ;; write the pixel data to it
    (.setRGB bufImg 0 0 w h newImgIntArray 0 w)

    ;; save it to disk
    (ImageIO/write bufImg "png" (File. "c:/data/newcirc.png"))

    ;; (def b (into [] a))


    (def newImage ( mx/coerce  :double-array ((mx/reshape (colorMatrix a 16 (.getWidth img) (.getHeight img)) [1 (* (.getHeight img) (.getWidth img) )]) 0)))

    (.setRGB img 0 0 (int-array newImage 0))
    )