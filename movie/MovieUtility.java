package movie;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;


/**
 * This class is meant to facilitate the creation of uncompressed movies.
 *
 * Much of the code here is derived from code that is availible on SourceForge in the "BmpSeq"
 * project (a GUI Tool).  That code itself has a number of drawbacks.
 * - You must load .bmp files.
 * - Your .bmp files must be formated a specific way
 * - The codebase did not allow you to easily insert its "main purpose" into additional programs
 * - The codebase is cluttered with clumbsy code that "finds the next frame"
 * - The codebase is named in a very confusing way
 * - The codebase is poorly commented
 *
 * However, as promised, that code does produce nice uncompressed .avi files.  This addition seeks
 * to make supporting movie output much easier.
 */
public class MovieUtility {
	
	/** Disallow creation of this class. */
	private MovieUtility() {}
	
		
	/**
	 * Initalize a new .avi movie file.  This method returns an object that enables you to add
	 * frames to the movie using the MovieUtility.addFrame(MovieInfo , BufferedImage) method.  Once
	 * you are done adding frames you must call MovieUtility.closeMovie(MovieInfo)
	 *
	 * @param baseFrame - An image that will determine many properties of the finished movie.  This 
	 * image will determine the finished movies, width, height, image buffer size, etc. etc.  This 
	 * image will be converted to a 24-bet bitmap (BufferedImage.TYPE_3BYTE_BGR).  While this 
	 * conversion may waste time because the input was already in this format, the conversion allows
	 * you to create movies from other types of images, for instance, .jpegs.
	 *
	 * @param movieFileName - The name of the resulting movie.  A ".avi" suffix will be added to the 
	 * movie's name if this string does not end with that suffix.
	 * @param frameRate - How many frames per second must be in the range 2-30 (inclusive).
	 * @param numFrames - How many frames will be in the movie.
	 *
	 * @return - MovieInfo - An object that contains infomation needed to input frames into the movie file
	 * that was created here.  The returned MovieInfo object contains a BufferedOutputStream to that newly 
	 * created movie file.  You can now add frames to this movie by calling "addFrame"
	 */
	public static Movie initMovie(BufferedImage baseFrame , String movieFileName , int frameRate , int numFrames) {
		
		///////////////////
		//  Check Input  //
		///////////////////
		if(frameRate > 30 || frameRate < 2) {
			throw new IllegalArgumentException("frameRate must be in range (2-30) :: " + frameRate);
		}		
		//append ".avi" to the outputFileName if necessary
		String trueFileName;
		if(movieFileName.endsWith(".avi")) {
			trueFileName = movieFileName;
		} else {
			trueFileName = movieFileName + ".avi";
		}
		
		
		BufferedImage correctedImage = repackageImage(baseFrame);		
						
		/////////////////////////
		//  Collect some data  //
		/////////////////////////
		
		//read in variables from the 1st frame
		Movie info = null;
		try {
			///////////////////////////////////////
			//  Create .bmp file from 1st frame  //
			///////////////////////////////////////
			File tempFile = new File("tempFile.bmp");
			ImageIO.write(correctedImage , "bmp" , tempFile);			
			
			FileInputStream fis = new FileInputStream(tempFile);
			info = new Movie(fis , frameRate , numFrames);
								
			fis.close();
			tempFile.delete();	
			
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			System.out.println("Failed");
			return null;
		} catch (IOException ex) {
			ex.printStackTrace();
			System.out.println("Failed");
			return null;
		}	
		
		///////////////////////////
		//  Start Writing Movie  //
		///////////////////////////		
		System.out.println("Beginning to create movie " + trueFileName);
		
		FileOutputStream outputFileHandle = null;
		BufferedOutputStream stream = null;
		try {
			outputFileHandle = new FileOutputStream(trueFileName);
			stream = new BufferedOutputStream(outputFileHandle);
			info.setBufferedOutputStream(stream);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			return null;
		}
		
		
		writeMovieHeader(info);
		
		return info;
	}
	
	
	/**
	 * Add a frame to a movie.
	 * 
	 * @param mov - A helper file that was returned when we initalized this movie.
	 * @param frame - An image that you want to become a frame of the movie.
	 */
	public static void addFrame(Movie mov , BufferedImage frame) {
						
		BufferedImage correctedImage = repackageImage(frame);	
		File tempFile = null;
		FileInputStream bmpFileHandle = null;		
		
		try {
			///////////////////////////////////
			//  Create .bmp file from frame  //
			///////////////////////////////////
			tempFile = File.createTempFile("tempFile" , "tmp");
			ImageIO.write(correctedImage , "bmp" , tempFile);
						
			bmpFileHandle = new FileInputStream(tempFile);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ioex) {
			ioex.printStackTrace();
		}
		
		if (!ReadBmp.readBmp(
			bmpFileHandle ,
			mov.width ,
			mov.height ,
			mov.bitCount ,
			mov.image.length ,
			mov.image)) {
			
			tempFile.delete();
			return;
		}
		mov.writeForm("00db");
		mov.writeInteger(mov.image.length);
		mov.writeBuffer(mov.image, mov.image.length);
		
		
		try {
			tempFile.delete();
			bmpFileHandle.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
	}
	
	
	/**
	 * 
	 * Flush and then close internal Datastream. 
	 * 
	 * 
	 * @param mov -  A helper file that was returned when we initalized this movie. Add the "end 
	 * of file" information to the movie file that this object helps.  Then flush and close the 
	 * streams to this movie file.
	 */
	public static void endMovie(Movie mov) {
				
		mov.writeForm("idx1");
		mov.writeInteger(mov.getNumFrames() * 16); // chunk size
		
		int offset = 4;
		for (int idx = 0 ; idx < mov.getNumFrames() ; idx++) {
			mov.writeForm("00db");
			mov.writeInteger(16); // flag
			mov.writeInteger(offset);
			mov.writeInteger(mov.image.length); // length
			offset = offset + mov.image.length + 8;
		}
		
		mov.close();
	}
	
	
	/**
	 * 
	 * Write the beginning part of a .avi file.  Only called by MovieUtility.initMovie
	 * 
	 * 
	 * @param mov - The MovieInfo object that will be returned once MovieUtility.initMovie
	 * is finished.
	 */
	private static void writeMovieHeader(Movie mov) {
	
		int imageWidth = mov.width;
		int imageHeight = mov.height;
		int bitCount = mov.bitCount;
		byte[] colormap = mov.colormap;
		
		BufferedOutputStream stream = mov.getStream();
		
		int imageBufferSize = mov.initImageBuffer(imageWidth, imageHeight);
		
		int nColors;
		if (bitCount == 8) {
			nColors = 256;
		} else {
			// 1, 24 bits
			nColors = 0;
		}
		
		int colormapSize = mov.getColormapSize(bitCount);
		
		int avihMicroSecPerFrame = 500000; // frame display rate
		int avihMaxBytesPerSec = 32; // max. transfer rate
		int avihPaddingGranularity = 0; // pad to multiples of this
		int avihFlags = 0x00000810; // ever-present flags
		int avihTotalFrames = mov.getNumFrames(); // # frames in file
		int avihInitialFrames = 0;
		int avihStreams = 1; // just the video stream
		int avihSuggestedBufferSize = imageBufferSize;
		int avihWidth = imageWidth;
		int avihHeight = imageHeight;
		int avihReserved0 = 0;
		int avihReserved1 = 0;
		int avihReserved2 = 0;
		int avihReserved3 = 0;
		
		//int strhfccType;
		int strhfccHandler = 0;
		int strhFlags = 0; // Contains AVITF_* flags
		short strhwPriority = 0;
		short strhwLanguage = 0;
		int strhInitialFrames = 0;
		int strhScale = 1;
		int strhRate = mov.getFrameRate(); // dwRate / dwScale == samples/second
		int strhStart = 0;
		int strhLength = mov.getNumFrames(); // In units above...
		int strhSuggestedBufferSize = imageBufferSize;
		int strhQuality = -1;
		int strhSampleSize = 0;
		int strhDummy = 0;
		
		int junkSize = 1816;
		
		mov.writeForm("RIFF");
		mov.writeInteger(232 + junkSize + mov.getNumFrames() *16 + mov.getNumFrames() *(8 + imageBufferSize));
		mov.writeForm("AVI ");
		mov.writeForm("LIST");
		mov.writeInteger(192 + colormapSize); // chunk size
		mov.writeForm("hdrl");
		mov.writeForm("avih");
		mov.writeInteger(56); // chunk size
		mov.writeInteger(avihMicroSecPerFrame);
		mov.writeInteger(avihMaxBytesPerSec);
		mov.writeInteger(avihPaddingGranularity);
		mov.writeInteger(avihFlags);
		mov.writeInteger(avihTotalFrames);
		mov.writeInteger(avihInitialFrames);
		mov.writeInteger(avihStreams);
		mov.writeInteger(avihSuggestedBufferSize);
		mov.writeInteger(avihWidth);
		mov.writeInteger(avihHeight);
		mov.writeInteger(avihReserved0);
		mov.writeInteger(avihReserved1);
		mov.writeInteger(avihReserved2);
		mov.writeInteger(avihReserved3);
		mov.writeForm("LIST");
		mov.writeInteger(116 + colormapSize); // chunk size
		mov.writeForm("strl");
		mov.writeForm("strh");
		mov.writeInteger(56); // chunk size
		mov.writeForm("vids");
		mov.writeInteger(strhfccHandler);
		mov.writeInteger(strhFlags);
		mov.writeShort(strhwPriority);
		mov.writeShort(strhwLanguage);
		mov.writeInteger(strhInitialFrames);
		mov.writeInteger(strhScale);
		mov.writeInteger(strhRate);
		mov.writeInteger(strhStart);
		mov.writeInteger(strhLength);
		mov.writeInteger(strhSuggestedBufferSize);
		mov.writeInteger(strhQuality);
		mov.writeInteger(strhSampleSize);
		mov.writeInteger(strhDummy);
		mov.writeInteger(strhDummy);
		mov.writeForm("strf");
		mov.writeInteger(40 + colormapSize); // chunk size
		mov.writeInteger(40); // bitmapinfo header size
		mov.writeInteger(imageWidth);
		mov.writeInteger(imageHeight);
		mov.writeShort((short)1); // bitplanes
		int writeBitCount = bitCount;
		if (writeBitCount == 1) {
			writeBitCount = 24;
		}
		mov.writeShort((short)writeBitCount); // bits per pixel
		mov.writeInteger(0); // clr used
		mov.writeInteger(imageBufferSize);
		mov.writeInteger(0); // don't need
		mov.writeInteger(0); // don't need
		mov.writeInteger(nColors); // colors
		mov.writeInteger(nColors); // important colors
		mov.writeBuffer(colormap, colormapSize);
		mov.writeForm("JUNK");
		mov.writeInteger(junkSize - colormapSize);

		// junk padding here
		int junkIdx;
		for (junkIdx = 0; junkIdx < (junkSize - colormapSize)/4; junkIdx++) {
			mov.writeInteger(0);
		}
		
		mov.writeForm("LIST");
		// chunk size
		mov.writeInteger(
			4 + mov.getNumFrames() *(8 + imageBufferSize)
			);
		mov.writeForm("movi");
		
		try {			
			stream.flush();		
		} catch (IOException ex) {
			ex.printStackTrace();
		}				
	}
	
	
	/**
	 * When creating .avi movies the images you use as frames of the movie must subscribe to certain
	 * constraints (i.e. Bitmap images specified using the correct number of bits).  This method will
	 * take any input image and create a correctly specified image File from it.  Running all images 
	 * through this filter will allow you to create movies using many more image types.
	 *
	 * @param inputImage - An image
	 * @result - A BufferedImage from which you can create a correctly specified .bmp file (that holds
	 * a version of the input image) by calling <p>
	 * "ImageIO.write(RETURNED_IMAGE , ".bmp" , new File("tempFile"));"
	 */
	private static BufferedImage repackageImage(BufferedImage inputImage) {
		
		int width = inputImage.getWidth();
		int height = inputImage.getHeight();
				
		BufferedImage outImage = new BufferedImage( 
			width ,
			height ,
			BufferedImage.TYPE_3BYTE_BGR
			);
		
		//copy the pixel color to the image with the correct internal format
		for (int i = 0 ; i < width ; i++) {
			for (int j = 0 ; j < height ; j++) {
				outImage.setRGB(i , j , inputImage.getRGB(i , j));				
			}
		}
		
		
		return outImage;		
	}
	

	/**
	 * Test package - Ensure that building a movie works
	 *
	 * @param args - ignored
	 */
	public static void main(String[] args) {
		System.out.println("Testing MovieUtility");
							
//		File f;
//		
//		f = new File("Day" + 1000 + ".bmp");
//		
//		MovieInfo info = MovieUtility.initMovie( f , "movie.avi" , 2 , 30);
//		
//		for(int i = 0 ; i < 30 ; i++) {
//			f = new File("Day" + (1000 + 5 * i) + ".bmp");
//			MovieUtility.addFrame(info , f);
//		}
		
		try {			
			//create a movie from .jpg files
			BufferedImage image = ImageIO.read(new File("0.JPG"));		
			Movie info = MovieUtility.initMovie( image , "movie.avi" , 2 , 4);
			
			MovieUtility.addFrame(info , image);
			MovieUtility.addFrame(info , ImageIO.read(new File("1.JPG")));
			MovieUtility.addFrame(info , ImageIO.read(new File("2.JPG")));
			MovieUtility.addFrame(info , ImageIO.read(new File("3.JPG")));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}

