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

![alt text](https://github.com/jrlepere/ACLAHEwDGC/blob/master/imgs/Cut_Histo.jpg)

4) Local Histogram Equalization: Local histogram equalization function mappings are calculated for each block. This creates a function that maps each gray value to the histogram equalized gray value with respect to each block. This is done by calculating T for each block with the following equations:

![alt text](https://github.com/jrlepere/ACLAHEwDGC/blob/master/imgs/Histo_Equal_Calc.png)

5) Bilinear Interpolation: Bilinear interpolation is performed with respect to neighboring blocks to calculate the final image.

![alt text](https://github.com/jrlepere/ACLAHEwDGC/blob/master/imgs/Bilinear.jpg)

# ACLAHEwDGC2
L1 in the ACLAHEwDGC calculation has the ability to exceed the maximum gray value because lmax and cdf(l) have a maximum of 255 and 1, respectively. Therefore, to keep L1 in [0,255], Wen must be in [0.0, 1.0]. However, this is not always true. Therefore, a modified version of ACLAHEwDGC, appropriately names ACLAHEwDGC2, takes W'en equal to the sigmoid of the previous Wen calculation. This constrains the range of Wen to [0.5, 1.0].

![alt text](https://github.com/jrlepere/ACLAHEwDGC/blob/master/imgs/Wen2.png)

# Running the Program
The src files can be compiled and ran by 

> cd src

> javac Main.java

> java Main

Alternatively, the program can be executed by downloading and running the attached jar file.

# Navigating the Program
Loading Images
- There are 7 images provided with the application. These can be loaded by selecting File > Images followed by the name of the desired image to load.

Transforming Images
- Above each of the image panels, there is a combo box for the user to select the desired transformation. Those available are 
   - Original Image := no transformation done
   - Histogram Equalization := traditional histogram equalization
   - CLAHE := contrast limited histogram equalization
   - ACLAHE := automatic contrast limited histogram equalization
   - ACLAHEwDGC := automatic contrast limited histogram equalization with dual gamma correction
   - ACLAHEwDGC2 := automatic contrast limited histogram equalization with dual gamma correction with normalized Wen

Modifying Transformation Parameters
- A panel is available below each image for the user to modify the parameters of the transformation. If no parameters are available for the transformation, none will be displayed.

![alt text](https://github.com/jrlepere/ACLAHEwDGC/blob/master/imgs/ACLAHEwDGC.png)
