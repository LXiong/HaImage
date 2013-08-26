package hipi.xmu.grayimage;

import hipi.image.FloatImage;
import hipi.image.ImageHeader;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;



public class GrayImageProcess {
	
private static final GrayImageProcess static_object = new GrayImageProcess();
	
	public static GrayImageProcess getInstance() {
		return static_object;
	}
	
	/*
	 * convert image form rgb to gray.store the image into FloatImage
	 */
	
	public FloatImage RGB2GRAY(FloatImage image,ImageHeader header){
		
		int height = image.getHeight();
		int width  = image.getWidth();
		int bands  = image.getBands();
		float[] pels = image.getData();
		
		float[] result = new float[height*width];
		
		for (int i = 0; i < width * height; i++)
			result[i] = pels[i * bands] * 0.30f + pels[i * bands + 1] * 0.59f + pels[i * bands + 2] * 0.11f;
		
		return new FloatImage(width, height, 1, result);
		
	}
		
	/*
	 * In order to output the gray image.return int array
	 */
	public int[] RGB2GRAYOUT(FloatImage image )
				throws IOException {
		
			float[] data = image.getData();
			int[] rgb = new int[image.getWidth() * image.getHeight()];
			for (int i = 0; i < image.getWidth() * image.getHeight(); i++)
			{
				int r = Math.min(Math.max((int)(data[ i ] * 255), 0), 255);
				int g = Math.min(Math.max((int)(data[ i ] * 255), 0), 255);
				int b = Math.min(Math.max((int)(data[ i ] * 255), 0), 255);
				rgb[i] = r << 16 | g << 8 | b;
			}
			return rgb;

		}
			
	/*
	 * input:gray image with FloatImage type,int array of processed data,outputstream
	 * 
	 * output the gray image into filesystem.
	 */
	public void GrayencodeImage(FloatImage image,int[] rgb, OutputStream os)
				throws IOException {
		
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(os);
			BufferedImage bufferedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
			bufferedImage.setRGB(0, 0, image.getWidth(), image.getHeight(), rgb, 0, image.getWidth());
			JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bufferedImage);
			encoder.encode(bufferedImage, param);
		}

}
