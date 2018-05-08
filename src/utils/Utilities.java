package utils;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Utilities {

	/**
	 * Converts an array of pixels to a buffered image for viewing.
	 * @param image the pixel image.
	 * @return a buffered image of the pixel image.
	 */
	public static BufferedImage createBufferedImage(int[][] image) {
		
		// image dimensions
		int cols = image.length;
		int rows = image[0].length;
		
		// buffered image for the image
		BufferedImage buffImage = new BufferedImage(image.length, image[0].length, BufferedImage.TYPE_BYTE_GRAY);
		
		for (int c = 0; c < cols; c ++) {
			for (int r = 0; r < rows; r ++) {
				
				// get the gray pixel value value
				int gv = image[c][r] & 0xff;
				
				// convert the pixel value to rgb format for visualization
				// TODO test if we can change buffered image type.
				int rgbGV = 0xff000000 + (gv << 16) + (gv << 8) + gv;
				
				// set buffered image
				buffImage.setRGB(r, c, rgbGV);
			}
			
		}
		
		return buffImage;
	}
	
	public static int[][] getDefaultImage() {
		
		// matrix to hold the pixels
		int[][] pixelMatrix = new int[IMAGE_SIZE][IMAGE_SIZE];
		
		// buffered image from IO read
		BufferedImage image;
		try {
			// load the default image
			image = ImageIO.read(Utilities.class.getResourceAsStream(defaultImagePath));
			System.out.println(image.getType());
			
			// image parameters
			int loadedImageHeight = image.getHeight();
			int loadedImageWidth = image.getWidth();
			
			// converts the image to gray scale
			//BufferedImage loadedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
			BufferedImage loadedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
			ColorConvertOp op = new ColorConvertOp(image.getColorModel().getColorSpace(), loadedImage.getColorModel().getColorSpace(), null);
		    op.filter(image, loadedImage);
		   
		    // the ratio to multiple to the loaded image to get the new image spatial coordinates
		 	double heightRatio = ((double) loadedImageHeight) / IMAGE_SIZE;
		 	double widthRatio = ((double) loadedImageWidth) / IMAGE_SIZE;
		 	
		 	// set the pixel matrix of the image
			for (int y = 0; y < IMAGE_SIZE; y ++) {
				for (int x = 0; x < IMAGE_SIZE; x ++) {
					// get the closest pixel in the loaded image
					int gv = loadedImage.getRGB((int) (x * widthRatio), (int) (y * heightRatio));
					
					/*int bgr = loadedImage.getRGB((int) (x * widthRatio), (int) (y * heightRatio));
					int b = bgr & 0xFF0000;
					int g = bgr & 0x00FF00;
					int r = bgr & 0x0000FF;*/
					
					//float[] hsb = Color.RGBtoHSB(r, g, b, null)[2];
					
					// and to verify we are getting gray scale, last 8 bits (this should have no affect)
					//gv = gv & 0xff;
					
					// set the pixel matrix
					pixelMatrix[y][x] = gv;
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return pixelMatrix;
		
	}
	
	private static final String defaultImagePath = "Timmy_Wedding.jpg";
	
	// the size of the image that will be represented, may need to resize input image
	public static final int IMAGE_SIZE = 512;
	
}
