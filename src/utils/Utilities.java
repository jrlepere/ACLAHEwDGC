package utils;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Implements utility functions for the application
 * @author JLepere2
 * @date 05/07/2019
 */
public class Utilities {

	/**
	 * Gets a buffered image from the modified brightness values
	 * @param hsbImage the original hsb image
	 * @param newBrightnessValues the new brightness values
	 * @param hsbBrightnessMaxIntValue the maximum brightness value used to normalize brightness between 0.0 and 1.0
	 * @return
	 */
	public static BufferedImage createBufferedImage(float[][][] hsbImage, int[][] newBrightnessValues, int hsbBrightnessMaxIntValue) {
		
		// buffered image for the image
		BufferedImage buffImage = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_INT_ARGB);
		
		for (int y = 0; y < IMAGE_SIZE; y ++) {
			for (int x = 0; x < IMAGE_SIZE; x ++) {
				
				// get original hsb
				float[] hsb = hsbImage[y][x];
				
				// hue, saturation and brightness values
				float hue = hsb[0];
				float saturation = hsb[1];
				float brightness = (newBrightnessValues[y][x] / ((float) (hsbBrightnessMaxIntValue)));
				
				// get rgb from hsb
				int rgb = Color.HSBtoRGB(hue, saturation, brightness);
				
				// set buffered image
				buffImage.setRGB(x, y, rgb);
				
			}
		}
		
		return buffImage;
	}
	
	/**
	 * Gets an HSB matrix from the image
	 * @param imageName the name of the image to load
	 * @return an HSB matrix of the default image
	 */
	public static float[][][] getImage(String imageName) {
		
		// matrix to hold the the hsb values
		float[][][] hsbMatrix = new float[IMAGE_SIZE][IMAGE_SIZE][3];
		
		// buffered image from IO read
		BufferedImage image;
		try {
			
			// load the default image
			image = ImageIO.read(Utilities.class.getResourceAsStream(imageName));
			
			// image parameters
			int loadedImageHeight = image.getHeight();
			int loadedImageWidth = image.getWidth();
			
			// converts the image to rgb
			BufferedImage loadedImage = new BufferedImage(loadedImageWidth, loadedImageHeight, BufferedImage.TYPE_INT_RGB);
			ColorConvertOp op = new ColorConvertOp(image.getColorModel().getColorSpace(), loadedImage.getColorModel().getColorSpace(), null);
		    op.filter(image, loadedImage);
		   
		    // the ratio to multiple to the loaded image to get the new image spatial coordinates
		 	double heightRatio = ((double) loadedImageHeight) / IMAGE_SIZE;
		 	double widthRatio = ((double) loadedImageWidth) / IMAGE_SIZE;
		 	
		 	// set the hsb matrix of the image
			for (int y = 0; y < IMAGE_SIZE; y ++) {
				for (int x = 0; x < IMAGE_SIZE; x ++) {
					
					// get the rgb values
					int rgb = loadedImage.getRGB((int) (x * widthRatio), (int) (y * heightRatio));
					int r = (rgb >> 16) & 0xFF;
					int g = (rgb >> 8) & 0xFF;
					int b = rgb & 0xFF;
					
					// convert rgb to hsv
					float[] hsb = Color.RGBtoHSB(r, g, b, null);
					
					// set the matrix
					hsbMatrix[y][x] = hsb;
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return hsbMatrix;
		
	}
	
	/**
	 * Extracts only the brightness from an HSB image matrix.
	 * @param hsbImage the HSB image matrix
	 * @param hsbBrightnessMaxIntValue the maximum HSB as integer for histogram equalization 
	 * @return the HSB brightness for each pixel
	 */
	public static int[][] hsbBrightnessExtractor(float[][][] hsbImage, int hsbBrightnessMaxIntValue) {
		
		// matrix for holding brightness values
		int[][] hsbBrightnessImage = new int[IMAGE_SIZE][IMAGE_SIZE];
		
		// extract and set brightnesses for each pixel
		for (int c = 0; c < IMAGE_SIZE; c ++) {
			for (int r = 0; r < IMAGE_SIZE; r ++) {
				hsbBrightnessImage[c][r] = (int) (hsbImage[c][r][2] * hsbBrightnessMaxIntValue);
			}
		}
	
		// return image with only brightness
		return hsbBrightnessImage;
		
	}
	
	public static final String[] images = new String[] {
		"Buzz.jpg",
		"Castle.jpg",
		"Dog.jpg",
		"Seattle.jpg",
		"Waterfall.jpg",
		"Beach.jpg"
	};
	
	public static final int IMAGE_SIZE = 512;
	
}
