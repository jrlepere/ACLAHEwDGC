package transformations;
import javax.swing.JPanel;

/**
 * Interface for the transformations.
 * @author JLepere2
 * @date 05/07/2018
 */
public interface ITransformation {
	
	/**
	 * Provide the transformation with a new hsb image.
	 * @param image the new image to transform.
	 */
	public void newImage(float[][][] hsbImage);
	
	/**
	 * Transforms the current image in the transformation objects buffer.
	 */
	public void transform();
	
	/**
	 * Gets the parameter panel specific to this transformation.
	 * @return the transformation specific parameter selection panel.
	 */
	public JPanel getParameterPanel();
	
}
