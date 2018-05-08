package transformations;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import utils.Utilities;

/**
 * Automatic caContrast Limited Adaptive Histogram Equalization transformation algorithm.
 * @author JLepere2
 * @date 05/08/2018
 */
public class ACLAHE extends ATransformation {

	/**
	 * Creates object for ACLAHE transformation. 
	 * @param image the default image to transform
	 * @param imageLabel the image label to modify to display the transformed image
	 */
	public ACLAHE(int[][] image, JLabel imageLabel) {
		super(image, imageLabel);
		
		// initial parameters
		blockSize = 4;
		alpha = 100;
		P = 1;
		
		// main panel initialization
		parameterPanel.setLayout(new BorderLayout());
		
		// create transformation specific parameter selection panel
		JPanel paramInputPanel = new JPanel(new GridLayout(3, 2));
		
		// block size input
		paramInputPanel.add(getParameterLabel(" Block Size: "));
		JTextField blockSizeTextField = new JTextField(""+blockSize);
		paramInputPanel.add(blockSizeTextField);
		
		// alpha size input
		paramInputPanel.add(getParameterLabel(" Alpha: "));
		JTextField alphaTextField = new JTextField(""+alpha);
		paramInputPanel.add(alphaTextField);
		
		// Smax size input
		paramInputPanel.add(getParameterLabel(" P: "));
		JTextField PTextField = new JTextField(""+P);
		paramInputPanel.add(PTextField);
		
		// execute button
		JButton executeButton = new JButton("Execute");
		executeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// get and test new block size
					int newBlockSize = Integer.parseInt(blockSizeTextField.getText().trim());
					if (Utilities.IMAGE_SIZE % newBlockSize != 0 || newBlockSize <= 0) {
						JOptionPane.showMessageDialog(null, "Enter a block size such that:\n512 % block size = 0\n(2, 4, 8, 16, ...)", "Block Size Error", JOptionPane.ERROR_MESSAGE);
					}
					
					// get and test new alpha
					int newAlpha = Integer.parseInt(alphaTextField.getText().trim());
					if (newAlpha < 0) {
						JOptionPane.showMessageDialog(null, "Enter an alpha >= 0", "Alpha Error", JOptionPane.ERROR_MESSAGE);
					}
					
					// get and test new P
					int newP = Integer.parseInt(PTextField.getText().trim());
					if (newP < 0) {
						JOptionPane.showMessageDialog(null, "Enter a P >= 0", "P Error", JOptionPane.ERROR_MESSAGE);
					}
					
					// All passed, set new parameters and transform image
					blockSize = newBlockSize;
					alpha = newAlpha;
					P = newP;
					transform();
					
				} catch (Exception err) {
					JOptionPane.showMessageDialog(null, "There was a parse int error.\nMake sure all inputs are integers.", "Parse Int Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		// add components to main panel
		parameterPanel.add(paramInputPanel, BorderLayout.CENTER);
		parameterPanel.add(executeButton, BorderLayout.SOUTH);
	}
	
	/**
	 * Gets a label to specify what parameter to modify for organization.
	 * @param text the text description
	 * @return a label for the parameter.
	 */
	private JLabel getParameterLabel(String text) {
		JLabel parameterLabel = new JLabel(text);
		parameterLabel.setHorizontalTextPosition(JLabel.CENTER);
		parameterLabel.setHorizontalAlignment(JLabel.CENTER);
		parameterLabel.setPreferredSize(new Dimension(150, 25));
		return parameterLabel;
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
				cutImage[blockCol][blockRow][pixelCol][pixelRow] = image[c][r];
			}
		}
		
		
		// -- STEP 2 : Histogram Calculations -- //
		
		// Initializes an array for holding the histogram of EACH block 
		int[][][] histogramsPerBlock = new int[numBlocksCol][numBlocksRow][256];
		
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
		
		// set the histogram for each block
		for (int blockC = 0; blockC < numBlocksCol; blockC ++) {
			for (int blockR = 0; blockR < numBlocksRow; blockR ++) {
				for (int pixelC = 0; pixelC < pixelsPerBlockCol; pixelC ++) {
					for (int pixelR = 0; pixelR < pixelsPerBlockRow; pixelR ++) {
						// get the gray value
						int gv = cutImage[blockC][blockR][pixelC][pixelR];
						
						// increment the gray value index of the histogram for the block
						histogramsPerBlock[blockC][blockR][gv] += 1;
						
						// update min, max and avg count values per block
						if (gv < minPerBlock[blockC][blockR]) minPerBlock[blockC][blockR] = gv;
						if (gv > maxPerBlock[blockC][blockR]) maxPerBlock[blockC][blockR] = gv;
						avgPerBlock[blockC][blockR] += gv;
					}
				}
				// calculate average per block
				avgPerBlock[blockC][blockR] /= pixelsPerBock;
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
		    R = 255
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
				int B = (int) ((M/N)*(1.0 + P*(lmax/255.0) + (alpha/100.0)*(std/(avg+0.0001))));
				
				// store the total clipped for redistribution
				int totalClipped = 0;
				
				// clip the histogram
				for (int gv = 0; gv < 256; gv ++) {
					// get the count for this gray value and block
					int gvCount = histogramsPerBlock[blockC][blockR][gv];
					if (gvCount > B) {
						// add amount clipped
						totalClipped += gvCount - B;
						// clip
						histogramsPerBlock[blockC][blockR][gv] = B;
					}
				}
				
				// redistribute amount for each gv
				int redistributionPerGv = totalClipped / 256;
				
				// redistribute
				for (int gv = 0; gv < 256; gv ++) {
					histogramsPerBlock[blockC][blockR][gv] += redistributionPerGv;
				}
			}
		}
		
		
		// STEP 4 - Histogram Equalization Function Mapping -- //
		
		// Calculate mapped value for each gray value per block w/ histogram equalization
		for (int blockC = 0; blockC < numBlocksCol; blockC ++) {
			for (int blockR = 0; blockR < numBlocksRow; blockR ++) {
				// calculate running histogram
				for (int gv = 1; gv < 256; gv ++) {
					histogramsPerBlock[blockC][blockR][gv] += histogramsPerBlock[blockC][blockR][gv-1];
				}
				
				// the maximum value for this block
				int maxValueInBlock = maxPerBlock[blockC][blockR];
				
				// the histogram equalization factor
				double heFactor = ((double) maxValueInBlock) / pixelsPerBock;
				
				// calculate mapping function w/ histogram equalization
				for (int gv = 0; gv < 256; gv ++) {
					histogramsPerBlock[blockC][blockR][gv] = (int) (heFactor * histogramsPerBlock[blockC][blockR][gv]);
				}
			}
		}
		
		
		// STEP 5 - Bilinear Interpolation -- //
		
		// the transformed image
		int[][] newImage = new int[Utilities.IMAGE_SIZE][Utilities.IMAGE_SIZE];
		
		// half of the block size col and row
		int halfPixelsPerBlockCol = pixelsPerBlockCol / 2;
		int halfPixelsPerBlockRow = pixelsPerBlockRow / 2;
		
		// set the new image
		for (int c = 0; c < Utilities.IMAGE_SIZE; c ++) {
			for (int r = 0; r < Utilities.IMAGE_SIZE; r ++) {
				// the original pixel value
				int p = image[c][r];
				
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
	            newImage[c][r] =(int) (m * (n * Ta + (1 - n) * Tb) + (1 - m) * (n * Tc + (1 - n) * Td));
	            
			}
		}
		
		// set transformed image
		imageLabel.setIcon(new ImageIcon(Utilities.createBufferedImage(newImage)));
		
	}
	
	public String toString() {
		return "ACLAHE";
	}
	
	private int blockSize;
	private int alpha;
	private int P;
	
}
