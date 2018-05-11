package transformations;

import java.awt.GridLayout;
import java.util.stream.IntStream;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import utils.SliderPanel;
import utils.Utilities;

/**
 * Automatic Contrast Limited Adaptive Histogram Equalization with Dual Gamma Correction and modified Wen transformation algorithm.
 * @author JLepere2
 * @date 05/08/2018
 */
public class ACLAHEwDGC2 extends ATransformation {

	/**
	 * Creates object for ACLAHE transformation. 
	 * @param hsbImage the original image in hsb format
	 * @param imageLabel the image label to modify for displaying the transformed image
	 */
	public ACLAHEwDGC2(float[][][] hsbImage, JLabel imageLabel) {
		super(hsbImage, imageLabel);
		
		// initial parameters
		blockSize = 2;
		alpha = 100;
		P = 1;
		D = 50;
		
		// main panel initialization
		parameterPanel.setLayout(new GridLayout(4, 1));
		
		// block size input
		SliderPanel blockSizePanel = new SliderPanel("Block Size", new int[]{1, 2, 4, 8, 16}, 1);
		blockSizePanel.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				blockSize = blockSizePanel.getCurrentValue();
				transform();
			}
		});
		
		// alpha input
		SliderPanel alphaPanel = new SliderPanel("Alpha", IntStream.rangeClosed(0, 500).toArray(), 100);
		alphaPanel.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				alpha = alphaPanel.getCurrentValue();
				transform();
			}
		});
		
		// P size input
		SliderPanel PPanel = new SliderPanel("P", IntStream.rangeClosed(1, 40).toArray(), 0);
		PPanel.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				P = PPanel.getCurrentValue();
				transform();
			}
		});
		
		// P size input
		SliderPanel DPanel = new SliderPanel("D", IntStream.rangeClosed(0, 100).toArray(), 50);
		DPanel.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				D = DPanel.getCurrentValue();
				transform();
			}
		});
		
		// add components to main panel
		parameterPanel.add(blockSizePanel);
		parameterPanel.add(alphaPanel);
		parameterPanel.add(PPanel);
		parameterPanel.add(DPanel);
	}

	public void transform() {
		
		/*
		 * 5 Steps
		 * 1) Image Decomposition
		 * 2) Histogram Calculations
		 * 3) Histogram Clipping Redistribution
		 * 4) Histogram Equalization Function Mapping
		 * 5) Bilinear Interpolation
		 */
		
		
		// -- STEP 1 : Image Decomposition -- //
		
		// image block parameters
		int numBlocksCol = blockSize;
		int numBlocksRow = blockSize;
		int pixelsPerBlockCol = Utilities.IMAGE_SIZE / numBlocksCol;
		int pixelsPerBlockRow = Utilities.IMAGE_SIZE / numBlocksRow;
		int pixelsPerBock = pixelsPerBlockCol * pixelsPerBlockRow;
		
		/*
		 *  Creates an array for storing the cut image where index:
		 *  0 := block column
		 *  1 := block row
		 *  2 := pixel column
		 *  3 := pixel row
		 */
		int[][][][] cutImage = new int[numBlocksCol][numBlocksRow][pixelsPerBlockCol][pixelsPerBlockRow];
		
		// initialize the cut image
		for (int c = 0; c < Utilities.IMAGE_SIZE; c ++) {
			for (int r = 0; r < Utilities.IMAGE_SIZE; r ++) {
				// get the index for the cut image
				int blockCol = c / pixelsPerBlockCol;
				int blockRow = r / pixelsPerBlockRow;
				int pixelCol = c % pixelsPerBlockCol;
				int pixelRow = r % pixelsPerBlockRow;
				
				// set the cut image pixel
				cutImage[blockCol][blockRow][pixelCol][pixelRow] = hsbBrightnessValues[c][r];
			}
		}
		
		
		// -- STEP 2 : Histogram Calculations -- //
		
		// Initializes an array for holding the histogram of EACH block 
		int[][][] histogramsPerBlock = new int[numBlocksCol][numBlocksRow][hsbBrightnessMaxIntValue+1];
		
		// Initialize an array for holding the minimum, maximum, and average value of each block, for future use
		int[][] minPerBlock = new int[numBlocksCol][numBlocksRow];
		int[][] maxPerBlock = new int[numBlocksCol][numBlocksRow];
		int[][] avgPerBlock = new int[numBlocksCol][numBlocksRow];
		for (int blockC = 0; blockC < numBlocksCol; blockC ++) {
			for (int blockR = 0; blockR < numBlocksRow; blockR ++) {
				minPerBlock[blockC][blockR] = Integer.MAX_VALUE;
				maxPerBlock[blockC][blockR] = Integer.MIN_VALUE;
			}
		}
		
		// get max value and array for holding the histogram globally for 0.75 cdf
		int Lmax = Integer.MIN_VALUE;
		int[] globalHistogram = new int[hsbBrightnessMaxIntValue+1];
		
		// set the histogram for each block
		for (int blockC = 0; blockC < numBlocksCol; blockC ++) {
			for (int blockR = 0; blockR < numBlocksRow; blockR ++) {
				for (int pixelC = 0; pixelC < pixelsPerBlockCol; pixelC ++) {
					for (int pixelR = 0; pixelR < pixelsPerBlockRow; pixelR ++) {
						// get the brightness value
						int brightness = cutImage[blockC][blockR][pixelC][pixelR];
						
						// increment the brightness value index of the histogram for the block
						histogramsPerBlock[blockC][blockR][brightness] += 1;
						
						// update min, max and avg count values per block
						if (brightness < minPerBlock[blockC][blockR]) minPerBlock[blockC][blockR] = brightness;
						if (brightness > maxPerBlock[blockC][blockR]) maxPerBlock[blockC][blockR] = brightness;
						avgPerBlock[blockC][blockR] += brightness;
						
						// update global max
						if (brightness > Lmax) Lmax = brightness;
						
						// update global histogram
						globalHistogram[brightness] += 1;
					}
				}
				// calculate average per block
				avgPerBlock[blockC][blockR] /= pixelsPerBock;
			}
		}
		
		// gets Lalpha such that cdf(Lalpha) = 0.75
		int cdfTarget = (int) ((pixelsPerBock * numBlocksCol * numBlocksRow) * 0.75);
		int LalphaDiff = Integer.MAX_VALUE;
		int Lalpha = 0;
		for (int i = 0; i < globalHistogram.length; i ++) {
			if (i > 0) globalHistogram[i] += globalHistogram[i-1];
			int diff = Math.abs(globalHistogram[i] - cdfTarget);
			if (diff < LalphaDiff) {
				LalphaDiff = diff;
				Lalpha = i;
			}
		}
		
		
		// standard deviation for each block
		double[][] stdPerBlock = new double[numBlocksCol][numBlocksRow];
		
		// calculate the standard deviation for each block
		for (int blockC = 0; blockC < numBlocksCol; blockC ++) {
			for (int blockR = 0; blockR < numBlocksRow; blockR ++) {
				// get the average for this block
				int avg = avgPerBlock[blockC][blockR];
				
				// update stdev
				for (int pixelC = 0; pixelC < pixelsPerBlockCol; pixelC ++) {
					for (int pixelR = 0; pixelR < pixelsPerBlockRow; pixelR ++) {
						stdPerBlock[blockC][blockR] += Math.pow(cutImage[blockC][blockR][pixelC][pixelR]-avg, 2);
					}
				}
				
				// divide by number of pixels in block
				stdPerBlock[blockC][blockR] /= pixelsPerBock;
				
				// take sqrt
				stdPerBlock[blockC][blockR] = Math.sqrt(stdPerBlock[blockC][blockR]);
			}
		}
		
		
		// -- STEP 3 : Histogram Clipping Redistribution -- //
		/*
		 * Clips the histogram for each block w/ the following clipping threshold β, where:
		    β = (M/N)*(1+P*(lmax/R)+(α/100)*(σ/(Avg+c)))
		    M = # pixels in each block
		    N = dynamic range of the block
		    lmax = max pixel value in each block
		    R = hsbBrightnessMaxIntValue
		    c = 0.0001
		    Avg = average pixel value
		    P, α = passed parameters
		 */
		double M = pixelsPerBlockCol * pixelsPerBlockRow;
		
		// redistribute the histogram
		for (int blockC = 0; blockC < numBlocksCol; blockC ++) {
			for (int blockR = 0; blockR < numBlocksRow; blockR ++) {
				// calculate the clipping point for this block
				int lmax = maxPerBlock[blockC][blockR];
				int N = lmax - minPerBlock[blockC][blockR];
				int avg = avgPerBlock[blockC][blockR];
				double std = stdPerBlock[blockC][blockR];
				int B = (int) ((M/N)*(1.0 + P*(lmax/((float) hsbBrightnessMaxIntValue)) + (alpha/100.0)*(std/(avg+0.0001))));
				
				// store the total clipped for redistribution
				int totalClipped = 0;
				
				// clip the histogram
				for (int brightness = 0; brightness < hsbBrightnessMaxIntValue+1; brightness ++) {
					// get the count for this brightness value and block
					int brightnessCount = histogramsPerBlock[blockC][blockR][brightness];
					if (brightnessCount > B) {
						// add amount clipped
						totalClipped += brightnessCount - B;
						// clip
						histogramsPerBlock[blockC][blockR][brightness] = B;
					}
				}
				
				// redistribute amount for each brightness
				int redistributionPerBrightness = totalClipped / (hsbBrightnessMaxIntValue+1);
				
				// redistribute
				for (int brightness = 0; brightness < hsbBrightnessMaxIntValue+1; brightness ++) {
					histogramsPerBlock[blockC][blockR][brightness] += redistributionPerBrightness;
				}
			}
		}
		
		
		// STEP 4 - Histogram Equalization Function Mapping -- //
		
		// Calculate mapped value for each brightness value per block w/ histogram equalization
		for (int blockC = 0; blockC < numBlocksCol; blockC ++) {
			for (int blockR = 0; blockR < numBlocksRow; blockR ++) {
				
				// store the histogram for pdf calculations
				int[] storedHisto = new int[hsbBrightnessMaxIntValue+1];
				storedHisto[0] = histogramsPerBlock[blockC][blockR][0];
				
				// min and max pdf
				int pdfMin = storedHisto[0];
				int pdfMax = storedHisto[0];
				
				// calculate running histogram
				for (int brightness = 1; brightness < hsbBrightnessMaxIntValue+1; brightness ++) {
					// update for pdf
					storedHisto[brightness] = histogramsPerBlock[blockC][blockR][brightness];
					// update min and max pdf
					if (storedHisto[brightness] < pdfMin) pdfMin = storedHisto[brightness];
					if (storedHisto[brightness] > pdfMax) pdfMax = storedHisto[brightness];
					// update for cdf
					histogramsPerBlock[blockC][blockR][brightness] += histogramsPerBlock[blockC][blockR][brightness-1];
				}
				
				// pdf weighted calculations
				int[] cumulativeHistoWeighted = new int[hsbBrightnessMaxIntValue+1];
				for (int brightness = 0; brightness < hsbBrightnessMaxIntValue+1; brightness ++) {
					cumulativeHistoWeighted[brightness] = pdfMax * ((storedHisto[brightness] - pdfMin) / (pdfMax - pdfMin));
					if (brightness > 0) cumulativeHistoWeighted[brightness] += cumulativeHistoWeighted[brightness-1];
				}
				
				// get sum of cumulatedHistoWeighted
				int pdfSum = cumulativeHistoWeighted[hsbBrightnessMaxIntValue];
				
				// the maximum value for this block
				int maxValueInBlock = maxPerBlock[blockC][blockR];
				
				// r: dynamic range of the block
				int r = maxPerBlock[blockC][blockR] - minPerBlock[blockC][blockR];
				
				// the histogram equalization factor
				double cdfFactor = (double) histogramsPerBlock[blockC][blockR][hsbBrightnessMaxIntValue];
				
				// calculate mapping function w/ histogram equalization
				for (int brightness = 0; brightness < hsbBrightnessMaxIntValue+1; brightness ++) {
					
					// get cdf of l
					double cdf = histogramsPerBlock[blockC][blockR][brightness] / cdfFactor;
					
					// weighted enhancement for gamma 1
					double Wen = 1.0 / (1 + Math.pow(Math.E, -1.0 * Math.pow(((double) Lmax) / Lalpha, 1.0 - (Math.log(Math.E + cdf) / 8))));
					
					// T1
					int T1 = (int) (maxValueInBlock * Wen * cdf);
					if (T1 > hsbBrightnessMaxIntValue) T1 = hsbBrightnessMaxIntValue;
					
					// Gamma calculation
					int Gamma = (int) (Lmax * Math.pow(((double) brightness)/Lmax, (1.0 + (cumulativeHistoWeighted[brightness]/pdfSum)) / 2.0));
					
					// Set L
					if (r > ((D*hsbBrightnessMaxIntValue)/100.0)) {
						histogramsPerBlock[blockC][blockR][brightness] = Math.max(T1, Gamma);
					} else {
						histogramsPerBlock[blockC][blockR][brightness] = Gamma;
					}
					
				}
			}
		}
		
		
		// STEP 5 - Bilinear Interpolation -- //
		
		// the transformed image
		int[][] hsbBrightnessNewValues = new int[Utilities.IMAGE_SIZE][Utilities.IMAGE_SIZE];
		
		// half of the block size col and row
		int halfPixelsPerBlockCol = pixelsPerBlockCol / 2;
		int halfPixelsPerBlockRow = pixelsPerBlockRow / 2;
		
		// set the new image
		for (int c = 0; c < Utilities.IMAGE_SIZE; c ++) {
			for (int r = 0; r < Utilities.IMAGE_SIZE; r ++) {
				// the original pixel value
				int p = hsbBrightnessValues[c][r];
				
				// block coordinates for this point
				int blockC = c / pixelsPerBlockCol;
				int blockR = r / pixelsPerBlockRow;
				
				// SPATIAL coordinates for the center of the block
				int centerBlockC = blockC * pixelsPerBlockCol + halfPixelsPerBlockCol;
				int centerBlockR = blockR * pixelsPerBlockRow + halfPixelsPerBlockRow;
				
				// get block column 1 and 2
				int bc1, bc2;
	            if (c < centerBlockC) {
	                bc1 = blockC - 1;
	                if (bc1 < 0) bc1 = 0;
	                bc2 = blockC;
	            } else {
	                bc1 = blockC;
	                bc2 = blockC + 1;
	                if (bc2 >= numBlocksCol) bc2 = blockC;
	            }
	            
	            // get block row 1 and 2
	            int br1, br2;
	            if (r < centerBlockR) {
	                br1 = blockR - 1;
	                if (br1 < 0) br1 = 0;
	                br2 = blockR;
	            } else {
	                br1 = blockR;
	                br2 = blockR + 1;
	                if (br2 >= numBlocksRow) br2 = blockR;
	            }
	            
	            // spatial coordinate of block centers
	            int c1 = bc1 * pixelsPerBlockCol + halfPixelsPerBlockCol;
	            int c2 = bc2 * pixelsPerBlockCol + halfPixelsPerBlockCol;
	            int r1 = br1 * pixelsPerBlockRow + halfPixelsPerBlockRow;
	            int r2 = br2 * pixelsPerBlockRow + halfPixelsPerBlockRow;
	            
	            // get m and n values
	            double m = (c2 - c1 == 0) ? 0 :  ((double) (c2 - c)) / (c2 - c1);
	            double n = (r2 - r1 == 0) ? 0 :  ((double) (r2 - r)) / (r2 - r1);

	            // histogram equalization function mapping values
	            int Ta = histogramsPerBlock[bc1][br1][p];
	            int Tb = histogramsPerBlock[bc1][br2][p];
	            int Tc = histogramsPerBlock[bc2][br1][p];
	            int Td = histogramsPerBlock[bc2][br2][p];
	            
	            // calculate and set transformed pixel value
	            hsbBrightnessNewValues[c][r] =(int) (m * (n * Ta + (1 - n) * Tb) + (1 - m) * (n * Tc + (1 - n) * Td));
	            
			}
		}
		
		// set transformed image
		imageLabel.setIcon(new ImageIcon(Utilities.createBufferedImage(hsbImage, hsbBrightnessNewValues, hsbBrightnessMaxIntValue)));
		
	}
	
	public String toString() {
		return "ACLAHEwDGC2";
	}
	
	private int blockSize;
	private int alpha;
	private int P;
	private int D;
	
}
