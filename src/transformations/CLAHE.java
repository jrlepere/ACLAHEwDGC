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
 * Contrast Limited Adaptive Histogram Equalization transformation algorithm.
 * @author JLepere2
 * @date 05/08/2018
 */
public class CLAHE extends ATransformation {

	/**
	 * Creates object for CLAHE transformation. 
	 * @param hsbImage the original image in hsb format
	 * @param imageLabel the image label to modify for displaying the transformed image
	 */
	public CLAHE(float[][][] hsbImage, JLabel imageLabel) {
		super(hsbImage, imageLabel);
		
		// initial parameters
		blockSize = 4;
		alpha = 100;
		Smax = 1;
		
		// main panel initialization
		parameterPanel.setLayout(new GridLayout(3, 1));
		
		// block size input
		SliderPanel blockSizePanel = new SliderPanel("Block Size", new int[]{1, 2, 4, 8, 16, 32}, 1);
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
		
		// Smax size input
		SliderPanel SmaxPanel = new SliderPanel("Smax", IntStream.rangeClosed(1, 40).toArray(), 0);
		SmaxPanel.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Smax = SmaxPanel.getCurrentValue();
				transform();
			}
		});
		
		// add components to main panel
		parameterPanel.add(blockSizePanel);
		parameterPanel.add(alphaPanel);
		parameterPanel.add(SmaxPanel);
		
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
		
		// Initialize an array for holding the minimum and maximum value of each block, for future use
		int[][] minPerBlock = new int[numBlocksCol][numBlocksRow];
		int[][] maxPerBlock = new int[numBlocksCol][numBlocksRow];
		for (int blockC = 0; blockC < numBlocksCol; blockC ++) {
			for (int blockR = 0; blockR < numBlocksRow; blockR ++) {
				minPerBlock[blockC][blockR] = Integer.MAX_VALUE;
				maxPerBlock[blockC][blockR] = Integer.MIN_VALUE;
			}
		}
		
		// set the histogram for each block
		for (int blockC = 0; blockC < numBlocksCol; blockC ++) {
			for (int blockR = 0; blockR < numBlocksRow; blockR ++) {
				for (int pixelC = 0; pixelC < pixelsPerBlockCol; pixelC ++) {
					for (int pixelR = 0; pixelR < pixelsPerBlockRow; pixelR ++) {
						// get the brightness value
						int brightness = cutImage[blockC][blockR][pixelC][pixelR];
						
						// increment the brightness value index of the histogram for the block
						histogramsPerBlock[blockC][blockR][brightness] += 1;
						
						// update min and max values per block
						if (brightness < minPerBlock[blockC][blockR]) minPerBlock[blockC][blockR] = brightness;
						if (brightness > maxPerBlock[blockC][blockR]) maxPerBlock[blockC][blockR] = brightness;
					}
				}
			}
		}
		
		
		// -- STEP 3 : Histogram Clipping Redistribution -- //
		/*
		 * Clips the histogram for each block w/ the following clipping threshold β, where:
		    β = (M/N)*(1+(α/100)*Smax)
		    M = # pixels in each block
		    N = dynamic range of the block
		    α & Smax are passed parameters
		 */
		double M = pixelsPerBlockCol * pixelsPerBlockRow;
		
		// redistribute the histogram
		for (int blockC = 0; blockC < numBlocksCol; blockC ++) {
			for (int blockR = 0; blockR < numBlocksRow; blockR ++) {
				// calculate the clipping point for this block
				int N = maxPerBlock[blockC][blockR] - minPerBlock[blockC][blockR];
				int B = (int) ((M/N)*(1.0+(alpha/100.0)*Smax));
				
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
				// calculate running histogram
				for (int brightness = 1; brightness < hsbBrightnessMaxIntValue+1; brightness ++) {
					histogramsPerBlock[blockC][blockR][brightness] += histogramsPerBlock[blockC][blockR][brightness-1];
				}
				
				// the maximum value for this block
				int maxValueInBlock = maxPerBlock[blockC][blockR];
				
				// the histogram equalization factor
				double heFactor = ((double) maxValueInBlock) / pixelsPerBock;
				
				// calculate mapping function w/ histogram equalization
				for (int brightness = 0; brightness < hsbBrightnessMaxIntValue+1; brightness ++) {
					histogramsPerBlock[blockC][blockR][brightness] = (int) (heFactor * histogramsPerBlock[blockC][blockR][brightness]);
				}
			}
		}
		
		
		// STEP 5 - Bilinear Interpolation -- //
		
		// the transformed brightness values
		int[][] hsbNewBrightnessValues = new int[Utilities.IMAGE_SIZE][Utilities.IMAGE_SIZE];
		
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
	            hsbNewBrightnessValues[c][r] =(int) (m * (n * Ta + (1 - n) * Tb) + (1 - m) * (n * Tc + (1 - n) * Td));
	            
			}
		}
		
		// set transformed image
		imageLabel.setIcon(new ImageIcon(Utilities.createBufferedImage(hsbImage, hsbNewBrightnessValues, hsbBrightnessMaxIntValue)));
		
	}
	
	public String toString() {
		return "CLAHE";
	}
	
	private int blockSize;
	private int alpha;
	private int Smax;
	
}
