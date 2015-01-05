package util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;


/**
 * A Collection of methods that should be helpful when manually creating Images
 */
public class ImageUtility {
	
	/** Return an integer that holds all three 0-255 RGB values.  */
	public static int getRGB(int red, int green, int blue) {
		//error check input
		if (red < 0 || red > 255) {
			throw new IllegalArgumentException("red must be between 0 and 255 :: " + red);
		}
		if (green < 0 || green > 255) {
			throw new IllegalArgumentException("green must be between 0 and 255 :: " + green);
		}
		if (blue < 0 || blue > 255) {
			throw new IllegalArgumentException("blue must be between 0 and 255 :: " + blue);
		}

		return red << 16 |
			green << 8
				| blue;
	}


	/** Resize the supplied image into the dimensions provided. */
	public static BufferedImage resize(BufferedImage img, int newWidth, int newHeight) {
		int w = img.getWidth();
		int h = img.getHeight();
		BufferedImage dimg = dimg = new BufferedImage(newWidth, newHeight, img.getType());
		Graphics2D g = dimg.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(img, 0, 0, newWidth, newHeight, 0, 0, w, h, null);
		g.dispose();
		return dimg;
	}


	/** Stretch the image (maintain the aspect ratio) until either the width or height hits its ceiling. */
	public static BufferedImage stretch(BufferedImage img, int maxW, int maxH) {

		double w = img.getWidth();
		double h = img.getHeight();
		double growthFrac = Math.min(maxW / w, maxH / h);

		int newW = (int) (w * growthFrac);
		int newH = (int) (h * growthFrac);

		BufferedImage dimg = dimg = new BufferedImage(newW, newH, img.getType());
		Graphics2D g = dimg.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(img, 0, 0, newW, newH, 0, 0, (int)w, (int)h, null);
		g.dispose();
		return dimg;
	}
}
