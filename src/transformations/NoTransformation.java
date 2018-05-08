package transformations;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import utils.Utilities;

public class NoTransformation extends ATransformation {

	/**
	 * No transformation performed. Simply display the original image
	 * @param hsbImage the original image in hsb format
	 * @param imageLabel the image label to modify for displaying the transformed image
	 */
	public NoTransformation(float[][][] hsbImage, JLabel imageLabel) {
		super(hsbImage, imageLabel);
	}
	
	public void transform() {
		// no transformation is done, just set the image icon on the image label.
		imageLabel.setIcon(new ImageIcon(Utilities.createBufferedImage(hsbImage, hsbBrightnessValues, hsbBrightnessMaxIntValue)));
	}
	
	public String toString() {
		return "Original Image";
	}

}
