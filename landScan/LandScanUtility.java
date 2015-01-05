/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package landScan;

import util.ImageUtility;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Misc. functions that are useful throughout the landscan package.
 */
public class LandScanUtility {
	
	
	/**
	 * Draw a Map of the Grid, just for reference.
	 * 
	 * @param includeWhenBiggerThanThis - Only map grid squares with populations bigger than this num.
	 */
	public static void drawGrid(int[][] grid, int includeWhenBiggerThanThis, String fileName) {
		
		int width = grid[0].length;		//aka numCol in grid
		int height = grid.length;		//aka numRows in grid

		//create the image
		BufferedImage image = new BufferedImage(
			width,
			height,
			BufferedImage.TYPE_3BYTE_BGR);
		Graphics g = image.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (grid[j][i] > includeWhenBiggerThanThis) {
					image.setRGB(i, j, ImageUtility.getRGB(0, 0, 0));
				}
			}
		}
		try {
			ImageIO.write(image, "bmp", new File(fileName));
		} catch (IOException ex) {
			Logger.getLogger(RawLandScanReader.class.getName()).log(Level.SEVERE, null, ex);
		}
	}


	/** Serialize an object to the file "fileName". */
	public static void serialize(Serializable ser, String fileName) {
		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			ObjectOutputStream out = new ObjectOutputStream(fos);

			out.writeObject(ser);

			out.close();
			fos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Serilization Failure\n");
		}
	}
}
