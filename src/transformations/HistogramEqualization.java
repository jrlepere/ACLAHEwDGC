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

	public HistogramEqualization(int[][] image, JLabel imageLabel) {
		super(image, imageLabel);
		
		parameterPanel.add(new JLabel("HistoEqual"));
	}

	public void transform() {
		
		// the matrix for the transformed image
		int[][] newImage = new int[Utilities.IMAGE_SIZE][Utilities.IMAGE_SIZE];
		
		// array for holding the histogram of the image
		int[] histogram = new int[256];
		
		// initialize the histogram
		for (int c = 0; c < Utilities.IMAGE_SIZE; c ++) {
			for (int r = 0; r < Utilities.IMAGE_SIZE; r ++) {
				// increment the histogram index at the pixel gray value
				histogram[this.image[c][r]] ++;
			}
		}
		
		// calculate the running histogram
		for (int i = 1; i < histogram.length; i ++) {
			histogram[i] += histogram[i-1];
		}
		
		// histogram equalization multiplication factor
		double heFactor = 255.0 / (Utilities.IMAGE_SIZE * Utilities.IMAGE_SIZE);
		
		// set the new image by performing histogram equalization
		for (int c = 0; c < Utilities.IMAGE_SIZE; c ++) {
			for (int r = 0; r < Utilities.IMAGE_SIZE; r ++) {
				// get the original gray value
				int gv = image[c][r];

				// calculate the new gray value and set the the new image matrix
				newImage[c][r] = (int) (heFactor * histogram[gv]);
			}
		}
		
		// set the transformed image on the panel
		imageLabel.setIcon(new ImageIcon(Utilities.createBufferedImage(newImage)));
		
	}
	
	public String toString() {
		return "Histogram Equalization";
	}
	
}
