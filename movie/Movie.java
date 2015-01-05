/*
 * Movie.java
 *
 * Created on August 27, 2007, 12:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package movie;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * A utility class to helps keep track of resources needed to build a movie file. <p>
 * 
 * Some variables are extracted from a BMP file that will become the 1st frame of the movie.
 * The variables extracted from the BMP file are:
 * -width
 * -height
 * -bitCount
 * -colormap
 */
public class Movie {
	
	//variables extracted from a frame
	int width;
	int height;
	int bitCount;
	byte[] colormap;
	
	//variables assigned so help movie creation
	byte[] image;
	BufferedOutputStream stream;
	int frameRate;
	int numFrames;
	
	
	/**
	 * Create and initalize the resources needed to create a .avi movie file.
	 *
	 * @param bitMapFileHandle - A stream to a BMP file that we will extract information from.
	 */
	Movie(FileInputStream bitMapFileHandle , int frameRate , int numFrames) {
		
		this.frameRate = frameRate;
		this.numFrames = numFrames;
		
		BufferedInputStream stream = new BufferedInputStream(bitMapFileHandle);
		
		ReadBmp.loadByte(stream);
		ReadBmp.loadByte(stream);
		ReadBmp.loadInteger(stream);
		ReadBmp.loadShort(stream);
		ReadBmp.loadShort(stream);
		ReadBmp.loadShort(stream);
		ReadBmp.loadShort(stream);
		ReadBmp.loadInteger(stream);
		
		width = ReadBmp.loadInteger(stream);
		height = ReadBmp.loadInteger(stream);
		
		ReadBmp.loadShort(stream);
		
		bitCount = ReadBmp.loadShort(stream);

		if (!(bitCount == 1 || bitCount == 8 || bitCount == 24)) {
			throw new IllegalStateException("unsupported bit count: " + bitCount);
		}
		
		ReadBmp.loadInteger(stream);
		ReadBmp.loadInteger(stream);
		ReadBmp.loadInteger(stream);
		ReadBmp.loadInteger(stream);
		
		colormap = new byte[getColormapSize(bitCount)];
		//only required when bitcount == 8
		if(bitCount == 8) {			
			ReadBmp.loadBuffer(stream, colormap, 1024);
		}
		
	}
		
	int getColormapSize(int bitCount) {
		if (bitCount == 8) {
			return 1024;
		} else {
			//1, 24 bits
			return 0;
		}
	}
	
	
	int initImageBuffer(int w, int h) {
		int scanLinePadSize;
		scanLinePadSize = w % 4;
		
		int imageBufferSize = (w * 3 + scanLinePadSize) * h;		
		image = new byte[imageBufferSize];
		return imageBufferSize;

	}
	
	int getFrameRate() { return frameRate;}
	int getNumFrames() { return numFrames;}
	
	
	/**  Save a buffer that can store an entire frame of the movie.  - Chiefly need to remember length. */
	public void setImageBuffer(byte[] imageBuffer) {
		this.image = imageBuffer;
	}
	
	public byte[] getImageBuffer() {return image;}
	
	
	/**  Add access to the movie file. */
	public void setBufferedOutputStream(BufferedOutputStream bos) {
		this.stream = bos;
	}
	
	/** Return this movie file's associated BufferedOutputStream. */
	public BufferedOutputStream getStream() {
		return stream;
	}
	
	/** Flush and close the output stream to the movie file */
	public void close() {
		try {
			stream.flush();
			stream.close();		
		} catch (IOException ex) {
			System.out.println("Error flushing and closing stream to movie file");
			ex.printStackTrace();
		}		
	}
	
	
	public void writeByte(int value) {
		try {
			stream.write(value);
		} catch (IOException e) {
			System.out.println("I/O error writing byte");
		}
	}
	
	public void writeShort(short value) {
		writeByte((byte)(value & 0xff));
		writeByte((byte)((value >> 8) & 0xff));
	}
	
	public void writeInteger(int value) {
		writeByte((byte)(value & 0xff));
		writeByte((byte)((value >> 8) & 0xff));
		writeByte((byte)((value >> 16) & 0xff));
		writeByte((byte)((value >> 24) & 0xff));
	}
	
	public void writeForm(String name) {
		writeByte((byte)name.charAt(0));
		writeByte((byte)name.charAt(1));
		writeByte((byte)name.charAt(2));
		writeByte((byte)name.charAt(3));
	}
	
	public void writeBuffer(byte[] image, int imageBufferSize) {
		try {
			stream.write(image, 0, imageBufferSize);
		} catch (IOException e) {
			System.out.println("I/O error writing byte");
		}
	}	
}


/**
 * A collection of methods to extract certain variables from a BMP file.  This is a big part of 
 * the ugly code from the parent SourceForge package.
 */
class ReadBmp {
	
	static int loadByte(BufferedInputStream stream) {
		int b;
		try {
			b = stream.read();
		} catch (IOException e) {
			b = 0;
			e.printStackTrace();
			System.out.println("I/O error reading byte");
			System.exit(0); // panic
		}
		return b;
	}
	
	static int loadShort(BufferedInputStream stream) {
		return
			loadByte(stream) |
			loadByte(stream) << 8;
	}
	
	static int loadInteger(BufferedInputStream stream) {
		return
			loadByte(stream) |
			loadByte(stream) << 8 |
			loadByte(stream) << 16 |
			loadByte(stream) << 24;
	}
	
	static void loadBuffer(BufferedInputStream stream, byte[] buffer, int size) {
		int b;
		try {
			b = stream.read(buffer, 0, size);
		} catch (IOException e) {
			b = 0;
			System.out.println("I/O error reading byte");
			System.exit(0); // panic
		}
	}

	
	/**
	 * Given access to a file that contains a properly specified .bmp file write the image data to the supplied buffer.
	 */
	static boolean readBmp( FileInputStream handle, int inputWidth, int inputHeight, int bitCount, int imageBufferSize, byte[] sourceImage) {
		
		int colormapSize;
		if (bitCount == 1) {
			colormapSize = 8;
		} else
			if (bitCount == 8) {
			colormapSize = 1024;
			} else { // 24 bits
			colormapSize = 0;
			}
		
		byte[] colormap = new byte[colormapSize];
		
		/* file layout variables */
		
		char letterB;
		char letterM;
		int fileSize;
		int reserved1;
		int reserved2;
		int pixelArrayOffset;
		int reserved3;
		int structSize;
		int imageWidth;
		int imageHeight;
		int nPlanes;
		//  int bitCount; // input parm
		int compression;
		int imageSize;
		int XpixelsPerMeter;
		int YpixelsPerMeter;
		int colorsUsed;
		int colorsImportant;
		int heightIdx;
		int scanLinePadIdx;
		int scanLineLength;
		int scanLinePadSize;
		
		BufferedInputStream stream = new BufferedInputStream(handle);
		
		letterB = 'B';
		letterM = 'M';
		reserved1 = 0;
		reserved2 = 0;
		pixelArrayOffset = 54;
		reserved3 = 0;
		structSize = 40;
		imageWidth = inputWidth;
		imageHeight = inputHeight;
		nPlanes = 1;
		//  bitCount is input parm
		compression = 0;
		imageSize = 0;
		XpixelsPerMeter = 0;
		YpixelsPerMeter = 0;
		colorsUsed = 0;
		colorsImportant = 0;
		
		if ((char)loadByte(stream) != letterB)
			return false;
		if ((char)loadByte(stream) != letterM)
			return false;
		fileSize = loadInteger(stream);
		reserved1 = loadShort(stream);
		reserved2 = loadShort(stream);
		pixelArrayOffset = loadShort(stream);
		reserved3 = loadShort(stream);
		structSize = loadInteger(stream);
		imageWidth = loadInteger(stream);
		scanLinePadSize = imageWidth % 4;
		imageHeight = loadInteger(stream);
		nPlanes = loadShort(stream);
		bitCount = loadShort(stream);
		compression = loadInteger(stream);
		imageSize = loadInteger(stream);
		XpixelsPerMeter = loadInteger(stream);
		YpixelsPerMeter = loadInteger(stream);
		colorsUsed = loadInteger(stream);
		colorsImportant = loadInteger(stream);
		
		loadBuffer(stream, colormap, colormapSize); // skip over color map
		if (bitCount == 1)
			convert1BitTo24Bit(
				stream, colormap, sourceImage, imageBufferSize,
				imageWidth, imageHeight);
		else
			loadBuffer(stream, sourceImage, imageBufferSize);
		return true;
	}
	
	public static boolean isValidBmpFormat(FileInputStream handle) {
		char letterB = 'B';
		char letterM = 'M';
		
		BufferedInputStream stream = new BufferedInputStream(handle);
		if ((char)loadByte(stream) != letterB)
			return false;
		if ((char)loadByte(stream) != letterM)
			return false;
		return true;
	}
	
	public static int getImageWidth(FileInputStream handle) {
		BufferedInputStream stream = new BufferedInputStream(handle);
		loadByte(stream);
		loadByte(stream);
		loadInteger(stream);
		loadShort(stream);
		loadShort(stream);
		loadShort(stream);
		loadShort(stream);
		loadInteger(stream);
		return loadInteger(stream);
	}
	
	public static int getImageHeight(FileInputStream handle) {
		BufferedInputStream stream = new BufferedInputStream(handle);
		loadByte(stream);
		loadByte(stream);
		loadInteger(stream);
		loadShort(stream);
		loadShort(stream);
		loadShort(stream);
		loadShort(stream);
		loadInteger(stream);
		loadInteger(stream);
		return loadInteger(stream);
	}
	
	public static int getBitCount(FileInputStream handle) {
		BufferedInputStream stream = new BufferedInputStream(handle);
		loadByte(stream);
		loadByte(stream);
		loadInteger(stream);
		loadShort(stream);
		loadShort(stream);
		loadShort(stream);
		loadShort(stream);
		loadInteger(stream); //discard
		loadInteger(stream); //width
		loadInteger(stream); //height
		loadShort(stream);   //dicard
		return loadShort(stream);
	}
	
	public static void getColormap(FileInputStream handle, byte[] colormap, int bitCount) {
		if (bitCount == 8) {
			BufferedInputStream stream = new BufferedInputStream(handle);
			loadByte(stream);
			loadByte(stream);
			loadInteger(stream);
			loadShort(stream);
			loadShort(stream);
			loadShort(stream);
			loadShort(stream);
			loadInteger(stream);
			loadInteger(stream);
			loadInteger(stream);
			loadShort(stream);
			loadShort(stream);
			loadInteger(stream);
			loadInteger(stream);
			loadInteger(stream);
			loadInteger(stream);
			int bufferSize;
			loadBuffer(stream, colormap, 1024);
		}
		// else 1, 24 bits
	}
		
	private static void convert1BitTo24Bit(BufferedInputStream stream, byte[] colormap, byte[] sourceImage, int imageBufferSize, int imageWidth,  int imageHeight) {
		
		byte rgbQuad[][] = new byte[2][4];
		int rgbColorIdx, rgbIdx;
		
		int colormapIdx = 0;
		for (rgbColorIdx = 0; rgbColorIdx < 2; rgbColorIdx++)
			for (rgbIdx = 0; rgbIdx < 3; rgbIdx++)
				rgbQuad[rgbColorIdx][rgbIdx] = colormap[colormapIdx++];
		
		int heightIdx, widthIdx;
		int bitIdx;
		int dataByte;
		int scanLinePadSize = 3 - (((imageWidth - 1) % 32) / 8);
		int outputPadSize = imageWidth % 4;
		int imageIdx = 0;
		
		for (heightIdx = 0; heightIdx < imageHeight; heightIdx++) {
			widthIdx = 0;
			while (widthIdx < imageWidth) {
				if (widthIdx % 8 == 0) {
					dataByte = loadByte(stream);
					for (bitIdx = 0; bitIdx < 8; bitIdx++) {
						if ((dataByte & ((int)0x80 >> bitIdx)) == 0)
							rgbColorIdx = 0;
						else
							rgbColorIdx = 1;
						for (rgbIdx = 0; rgbIdx < 3; rgbIdx++)
							sourceImage[imageIdx++] = rgbQuad[rgbColorIdx][rgbIdx];
						widthIdx++;
						if (widthIdx == imageWidth)
							break;
					}
				}
			}
			for (int i = 0; i < scanLinePadSize; i++)
				loadByte(stream);
			for (int i = 0; i < outputPadSize; i++)
				sourceImage[imageIdx++] = (byte)0;
		}
	}
}
