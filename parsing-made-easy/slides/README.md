Parsing Made Easy
=================

* Slides for talk given [JugSummerCamp 2013](http://www.jugsummercamp.com/edition/4). 
* Completely stolen  from Bodil Stokke's [Programming, Only Better](http://github.com/bodil/only-better).

# Howto

* To take advantage of reveal.js, use node.js
* Install everything needed:

    ```
    npm install
    ```

* run slideshow with node:

    ```
    node run
    ```

* While slideshow is running, type `s` to open speaker notes window.

# Performance Graphs

Graphs were drawn from micro-benchmark tests run comparing the two parsers: Run 1000 times each parser for increasingly lengthy input, take the average.
Here is the raw data:

length | jparsec  | antlr
-------|----------|-------
6565   | 1.424    | 0.602
13130  | 3.508    | 1.271
19695  | 6.686    | 2.123
26260  | 11.176   | 3.243
32825  | 16.499   | 4.641
39390  | 23.342   | 6.381
45955  | 31.095   | 8.427
52520  | 40.213   | 10.794
59085  | 50.787   | 13.370
65650  | 63.090   | 16.292

To draw the graph, I used the following gnuplot script:

```
set ylabel 'ms'
set xlabel '# of chars'
set term 'svg'
set out 'parsing-performance.svg'
plot 'parsing-performance.data' u 1:2 t "jparsec parsing time" w lines, '' u 1:3 t "antlr parsing time" w lines
``` 

# Credits

- gasworks: http://www.cs.virginia.edu/~evans/pictures/2009_04_24-seattle/gasworks-IMG_3520.JPG 
- country road: http://islamicsunrays.com/wp-content/uploads/2010/10/country-road-missouri.jpg
- omg: http://image.funscrape.com/images/o/omg_wtf-12875.jpg
- simple: http://cobaltpm.com/wp-content/uploads/2013/02/keeping-it-simple-project-plan-from-point-a-to-point-b.jpg


