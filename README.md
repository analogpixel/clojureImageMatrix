<div id="table-of-contents">
<h2>Table of Contents</h2>
<div id="text-table-of-contents">
<ul>
<li><a href="#sec-1">1. Image Manipulation with clojure</a>
<ul>
<li><a href="#sec-1-1">1.1. Configure the project</a></li>
<li><a href="#sec-1-2">1.2. Clojure namespace</a>
<ul>
<li><a href="#sec-1-2-1">1.2.1. require</a></li>
<li><a href="#sec-1-2-2">1.2.2. use</a></li>
<li><a href="#sec-1-2-3">1.2.3. import</a></li>
</ul>
</li>
<li><a href="#sec-1-3">1.3. Loading an image into a matrix</a></li>
<li><a href="#sec-1-4">1.4. Saving a matrix into an image</a></li>
<li><a href="#sec-1-5">1.5. 32bit RGBA values</a></li>
<li><a href="#sec-1-6">1.6. Reducing the intensity levels of your image</a></li>
<li><a href="#sec-1-7">1.7. Converting to black and white</a></li>
<li><a href="#sec-1-8">1.8. Test code</a>
<ul>
<li><a href="#sec-1-8-1">1.8.1. Links to helpful places</a></li>
</ul>
</li>
</ul>
</li>
</ul>
</div>
</div>

# Image Manipulation with clojure<a id="sec-1" name="sec-1"></a>

## Configure the project<a id="sec-1-1" name="sec-1-1"></a>

Before you begin, you'll need to get [Leiningen](http://leiningen.org/) which is a project manager
for clojure.  Once you have it installed, you run:

    lein new image

to create a new project called image. Once the project is created open the project.clj
file add add a :main section to point to your main function (defn -main in your source)
and then add the core.matrix and clatrix dependencies.  Now from the command line you can
run lein deps from the image directory, and lein will go out and download all the libraries
you requested, and all their dependencies.

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

 If you are using emacs, and have [CIDER](https://github.com/clojure-emacs/cider) installed (`M-x package-install` cider)
you can now open image/project.clj from emacs and then type:
`M-x cider-jack-in` to connect to that project and edit it live in the REPL.
Now that you have a REPL running in emacs connected to your project, you can open
src/image/core.clj and start editing the program.

## Clojure namespace<a id="sec-1-2" name="sec-1-2"></a>

The begining of the program is the name space declaration.  The name space has
three main sections (besides the name)

### require<a id="sec-1-2-1" name="sec-1-2-1"></a>

load a clojure library from your class path and imports it, with the :as flag it will
alias it so yo don't need to type out the entire lib name each time you want
to use something from it.

### use<a id="sec-1-2-2" name="sec-1-2-2"></a>

loads an existing namespace and refers all the symbols from it into this namespace. So
by using clojure.core.matrix you have all the function available in your namespace.

### import<a id="sec-1-2-3" name="sec-1-2-3"></a>

Import is used to import java libraries into the clojure namespace. If you want to
load java.io.File and java.io.FileInputStream, you can use the notation:
(java.io File FileInputStream)  but if you just want to load javax.imageio.ImageIO,
placing () around it will actually break it and won't load it into the namespace
like you'd want.

    (ns image.core
      (:use clojure.core.matrix clojure.core.matrix.operators)
      (:require [clojure.core.matrix :refer :all]
                [clojure.core.matrix.operators :as mo])
      (:import (java.io File FileInputStream) javax.imageio.ImageIO java.awt.image.BufferedImage)
    )

## Loading an image into a matrix<a id="sec-1-3" name="sec-1-3"></a>

Images are loaded via the java BufferedImage class.  Once the image is loaded
it is converted into Matrix format and returned.  The makeMatrix format just takes
a long array [2 2 2 2 2 2 2 2 2 2] and converts it into [ [2 2] [2 2] [2 2]&#x2026;.]

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

## Saving a matrix into an image<a id="sec-1-4" name="sec-1-4"></a>

This function saves a matrix back into an image

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

## 32bit RGBA values<a id="sec-1-5" name="sec-1-5"></a>

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

^long in the decleration tells clojure that rgba is a long and not a double

    (defn unpackrgba [^long rgba]
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

To get RGBA values back into a single 32bit number.  I'm using unchecked-int since bufferedImage
is expecting to get an int back, and just int isn't big enough.

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

## Reducing the intensity levels of your image<a id="sec-1-6" name="sec-1-6"></a>

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

## Converting to black and white<a id="sec-1-7" name="sec-1-7"></a>

    (defn bw [rgba n]
      (let    [c (unpackrgba rgba)
               rr (* (int (/ (c 0) n)) n)
               rg (* (int (/ (c 1) n)) n)
               rb (* (int (/ (c 2) n)) n)
              ]
        (packrgba rr rr rr (c 3))
        )
      )

## Test code<a id="sec-1-8" name="sec-1-8"></a>

### Links to helpful places<a id="sec-1-8-1" name="sec-1-8-1"></a>

-   [Java BufferedImage class docs](http://docs.oracle.com/javase/7/docs/api/java/awt/image/BufferedImage.html)
-   [Getting RGB value of buffeeredImage](http://stackoverflow.com/questions/10880083/get-rgb-of-a-bufferedimage)
-   [Why amap is running slow](http://stackoverflow.com/questions/19202082/clojure-amap-is-very-slow)
-   [Core.matrix presentation](http://www.slideshare.net/mikeranderson/2013-1114-enter-thematrix)

The main test program

    (defn -main
    [& args]
      (set-current-implementation :vectorz)
      (set! *warn-on-reflection* true)

      (def m (loadImageMatrix "c:/data/1.png"))
      (def n (loadImageMatrix "c:/data/2.png"))

      (saveImageMatrix (mo/- m n) "png" "c:/data/yay.png")
      (saveImageMatrix (emap #(bw % 23) (mo/- m n)) "png" "c:/data/yay.png")
    )