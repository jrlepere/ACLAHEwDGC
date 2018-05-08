package transformations;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Abstract Transformation class defining parameter selection panel, and others.
 * @author JLepere2
 * @date 05/07/2018
 */
public abstract class ATransformation implements ITransformation {

	/**
	 * Abstract class instantiation.
	 * @param image default image to transform
	 * @param imageLabel the label containing the image
	 */
	public ATransformation(int[][] image, JLabel imageLabel) {
		this.image = image;
		this.imageLabel = imageLabel;
		
		parameterPanel = new JPanel();
	}
	
	public void newImage(int[][] image) {
		this.image = image;
		transform();
	}
	
	public JPanel getParameterPanel() {
		return parameterPanel;
	}
	
	protected int[][] image;
	protected JPanel parameterPanel;
	protected JLabel imageLabel;
	
}
