package transformations;
import javax.swing.JLabel;
import javax.swing.JPanel;

import utils.Utilities;

/**
 * Abstract Transformation class defining parameter selection panel, and others.
 * @author JLepere2
 * @date 05/07/2018
 */
public abstract class ATransformation implements ITransformation {

	/**
	 * Abstract class instantiation.
	 * @param hsbImage the original image in hsb format
	 * @param imageLabel the image label to modify for displaying the transformed image
	 */
	public ATransformation(float[][][] hsbImage, JLabel imageLabel) {
		this.hsbImage = hsbImage;
		this.hsbBrightnessValues = Utilities.hsbBrightnessExtractor(this.hsbImage, hsbBrightnessMaxIntValue);
		this.imageLabel = imageLabel;
		
		parameterPanel = new JPanel();
	}
	
	public void newImage(float[][][] hsbImage) {
		this.hsbImage = hsbImage;
		this.hsbBrightnessValues = Utilities.hsbBrightnessExtractor(this.hsbImage, hsbBrightnessMaxIntValue);
	}
	
	public JPanel getParameterPanel() {
		return parameterPanel;
	}
	
	protected float[][][] hsbImage;
	protected int[][] hsbBrightnessValues;
	protected JPanel parameterPanel;
	protected JLabel imageLabel;
	
	// hsb brightness is a float between 0 and 1 so we make it between 0 and 1000 for histogram calculations
	protected static final int hsbBrightnessMaxIntValue = 1000;
	
}
