package transformations;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import utils.Utilities;

public class NoTransformation extends ATransformation {

	public NoTransformation(int[][] image, JLabel imageLabel) {
		super(image, imageLabel);
		
		parameterPanel.add(new JLabel("None"));
	}
	
	public void transform() {
		// no transformation is done, just set the image icon on the image label.
		imageLabel.setIcon(new ImageIcon(Utilities.createBufferedImage(image)));
	}
	
	public String toString() {
		return "Original Image";
	}

}
