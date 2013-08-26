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
	 * smooth the gray image ,output an int array
	 */
	public int[] SMOOTH(FloatImage image){
		
		float[] data = image.getData();
		int[] rgb = new int[image.getWidth() * image.getHeight()];
		float[] pels_smooth= new float[image.getWidth() * image.getHeight()];
		float min=1.0f;
	    float max=-1.0f;
	    int _h=image.getHeight();
	    int _w=image.getWidth();
	    try{
	        
	        for(int i=0;i<_h;i++){
	        for(int j=0;j<_w ;j++){
	         if(i==0 || i==1 || i==_h-1 || i==_h-2 
	            ||j==0 || j==1 || j==_w-1 || j==_w-2){
	        	 pels_smooth[i*_w+j]=data[i*_w+j];
	          }
	          else{
	           float average;
	             //nine pixels in the middle
	              average=(data[i*_w+j]+data[i*_w+j-1]+data[i*_w+j+1]
	                      +data[(i-1)*_w+j]+data[(i-1)*_w+j-1]+data[(i-1)*_w+j+1]
	                      +data[(i+1)*_w+j]+data[(i+1)*_w+j-1]+data[(i+1)*_w+j+1])/9;
	              pels_smooth[i*_w+j]=average;
	          }       
	         if(pels_smooth[i*_w+j]<min)
	           min=pels_smooth[i*_w+j];
	          if(data[i*_w+j]>max)
	           max=pels_smooth[i*_w+j];
	         }
	         }
	        for(int i=0;i<_w*_h;i++){
	        	pels_smooth[i]=(pels_smooth[i]-min)/(max-min);
	          }
	     
	     }
	     catch (Exception e) 
	     {
	             e.printStackTrace();
	             //throw new Exception(e);
	         } 
		for (int i = 0; i < image.getWidth() * image.getHeight(); i++)
		{
			int r = Math.min(Math.max((int)(pels_smooth[ i ] * 255), 0), 255);
			int g = Math.min(Math.max((int)(pels_smooth[ i ] * 255), 0), 255);
			int b = Math.min(Math.max((int)(pels_smooth[ i ] * 255), 0), 255);
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
