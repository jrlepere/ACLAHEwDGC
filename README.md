# Introduction 
Implementation of Chang, Jung, Ke, Song and Hwang's paper "Automatic Contrast Limited Adaptive Histogram Equalization with Dual Gamma Correction" for fulfillment of California Polytechnic State University, Pomona's CS555 Image Processing final project.

# ACLAHEwDGC
Automatic Contrast Limited Adaptive Histogram Equalization with Dual Gamma Correction (ACLAHEwDGC) is an image processing technique for contrast enhancement. This method extends upon the Contrast Limited Adaptive Histogram Equalization (CLAHE) method by introducing an automatic clip point as well as two levels of gamma correction. Following is a brief description for each step in ACLAHEwDGC:
1) Image Decomposition: The image is segmented into equal sized blocks.

![alt text](https://github.com/jrlepere/ACLAHEwDGC/blob/master/imgs/Buzz_Cut.jpg)

2) Local Histogram Calculations: The histograms of each block are calculated.

![alt text](https://github.com/jrlepere/ACLAHEwDGC/blob/master/imgs/Buzz_Cut_Histo.jpg)

3) Local Histogram Redistribution: The histograms of each block are clipped and redistributed. The clipping point is calculated as follows:

![alt text](https://github.com/jrlepere/ACLAHEwDGC/blob/master/imgs/Clipping_Calculation.png)

Each histogram is trimmed based on its respective clipping point and the trimmed values are redistributed throughout the histogram.

![alt text](https://github.com/jrlepere/ACLAHEwDGC/blob/master/imgs/Cut_Histo.png)

4) Local Histogram Equalization: Local histogram equalization function mappings are calculated for each block. This creates a function that maps each gray value to the histogram equalized gray value with respect to each block.
5) Bilinear Interpolation: Bilinear interpolation is performed with respect to neighboring blocks to calculate the final image.
