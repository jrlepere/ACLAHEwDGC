package transformations;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import utils.Utilities;

/**
 * General histogram equalization transformation.
 * @author JLepere2
 * @date 05/08/2018
 */
public class HistogramEqualization extends ATransformation {

	/**
	 * Object for executing histogram equalization.
	 * @param hsbImage the original image in hsb format
	 * @param imageLabel the image label to modify for displaying the transformed image
	 */
	public HistogramEqualization(float[][][] hsbImage, JLabel imageLabel) {
		super(hsbImage, imageLabel);
	}

	public void transform() {
		
		// the matrix for the new brightness values
		int[][] hsbBrightnessNewValues = new int[Utilities.IMAGE_SIZE][Utilities.IMAGE_SIZE];
		
		// array for holding the histogram of the image
		int[] histogram = new int[hsbBrightnessMaxIntValue + 1];
		
		// initialize the histogram
		for (int c = 0; c < Utilities.IMAGE_SIZE; c ++) {
			for (int r = 0; r < Utilities.IMAGE_SIZE; r ++) {
				// increment the histogram index at the brightness value
				histogram[this.hsbBrightnessValues[c][r]] ++;
			}
		}
		
		// calculate the running histogram
		for (int i = 1; i < histogram.length; i ++) {
			histogram[i] += histogram[i-1];
		}
		
		// histogram equalization multiplication factor
		double heFactor = ((float) (hsbBrightnessMaxIntValue)) / (Utilities.IMAGE_SIZE * Utilities.IMAGE_SIZE);
		
		// set the new image by performing histogram equalization
		for (int c = 0; c < Utilities.IMAGE_SIZE; c ++) {
			for (int r = 0; r < Utilities.IMAGE_SIZE; r ++) {
				// get the original hsb brightness value
				int gv = hsbBrightnessValues[c][r];

				// calculate and set the new brightness value
				hsbBrightnessNewValues[c][r] = (int) (heFactor * histogram[gv]);
			}
		}
		
		// set the new transformed image
		imageLabel.setIcon(new ImageIcon(Utilities.createBufferedImage(hsbImage, hsbBrightnessNewValues, hsbBrightnessMaxIntValue)));
		
	}
	
	public String toString() {
		return "Histogram Equalization";
	}
	
}
