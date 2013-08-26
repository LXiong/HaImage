package hipi.xmu.cases.grayimagethreshold;

import hipi.experiments.mapreduce.JPEGFileInputFormat;
import hipi.experiments.mapreduce.JPEGSequenceFileInputFormat;
import hipi.image.FloatImage;
import hipi.image.ImageHeader;
import hipi.imagebundle.mapreduce.ImageBundleInputFormat;
import hipi.xmu.grayimage.GrayImageProcess;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class GrayImageThreshold extends Configured implements Tool{

	public static class MyMapper extends Mapper<ImageHeader, FloatImage, IntWritable, LongWritable>
	{
		private Path path;
		private FileSystem fileSystem;
		private Configuration conf;
		public void setup(Context jc) throws IOException
		{
			conf = jc.getConfiguration();
			fileSystem = FileSystem.get(conf);
			path = new Path( conf.get("GrayImageThreshold.outdir"));
			fileSystem.mkdirs(path);
		}
		public void map(ImageHeader key, FloatImage value, Context context) throws IOException, InterruptedException{
			if (value != null) {

				//initialize the class to process the image,which contains all methods to deal with grayimage
				GrayImageProcess graytool=GrayImageProcess.getInstance();
				
				//convet from rgb to gray
				FloatImage gray = graytool.RGB2GRAY(value,key);
				
				//threshold by 100
				int[] rgb =graytool.THRESHOLD(gray,100);
	
				//note: images may have the same hash code
				context.write(new IntWritable(1), new LongWritable(gray.hashCode()));
				
			
				//a neccessary step to avoid files with duplicate hash values
				Path outpath = new Path(path + "/" + value.hashCode() + ".jpg");
				while(fileSystem.exists(outpath)){
					String temp = outpath.toString();
					outpath = new Path(temp.substring(0,temp.lastIndexOf('.')) + "1.jpg"); 
				}
				
				
				FSDataOutputStream os = fileSystem.create(outpath);
				
				//output gray image to os
				graytool.GrayencodeImage(gray, rgb, os);
				os.flush();
				os.close();
				
				 
			}
			else
				context.write(new IntWritable(0), new LongWritable(0));
		}
	}
	public static class MyReducer extends Reducer<IntWritable, LongWritable, IntWritable, LongWritable> {
		// Just the basic indentity reducer... no extra functionality needed at this time
		public void reduce(IntWritable key, Iterable<LongWritable> values, Context context) 
		throws IOException, InterruptedException
		{

			System.out.println("REDUCING");
			for (LongWritable temp_hash : values) {
				{	    
					context.write(key, temp_hash);
				}

			}
		}
	}
		public int run(String[] args) throws Exception
		{	

			// Read in the configurations
			if (args.length < 3)
			{
				System.out.println("Usage: GrayImageThreshold <inputdir> <outputdir> <input type: hib, har, sequence, small_files>");
				System.exit(0);
			}


			// Setup configuration
			Configuration conf = new Configuration();

			// set the dir to output the jpegs to
			String outputPath = args[1];
			String input_file_type = args[2];
			conf.setStrings("GrayImageThreshold.outdir", outputPath);
			conf.setStrings("GrayImageThreshold.filetype", input_file_type);

			Job job = new Job(conf, "GrayImageThreshold");
			job.setJarByClass(GrayImageThreshold.class);
			job.setMapperClass(MyMapper.class);
			job.setReducerClass(MyReducer.class);

			// Set formats
			job.setOutputKeyClass(IntWritable.class);
			job.setOutputValueClass(LongWritable.class);       
			
			// Set out/in paths

			removeDir(outputPath, conf);
			FileOutputFormat.setOutputPath(job, new Path(outputPath));

			/*
			Path qualifiedPath = new Path("har://", new Path(args[0]).toUri().getPath());
			JPEGFileInputFormat.addInputPath(job, qualifiedPath);	
			*/
			
			JPEGFileInputFormat.addInputPath(job, new Path(args[0]));
			
			if(input_file_type.equals("hib"))
				job.setInputFormatClass(ImageBundleInputFormat.class);
			else if(input_file_type.equals("har"))
				job.setInputFormatClass(JPEGFileInputFormat.class);
			else if(input_file_type.equals("small_files"))
				job.setInputFormatClass(JPEGFileInputFormat.class);
			else if (input_file_type.equals("sequence"))
				job.setInputFormatClass(JPEGSequenceFileInputFormat.class);
			else{
				System.out.println("Usage: GrayImageThreshold <inputdir> <outputdir> <input type: hib, har, sequence, small_files>");
				System.exit(0);			
			}

			//conf.set("mapred.job.tracker", "local");
			job.setNumReduceTasks(1);
			System.exit(job.waitForCompletion(true) ? 0 : 1);
			return 0;
		}
		public static void removeDir(String path, Configuration conf) throws IOException {
			Path output_path = new Path(path);

			FileSystem fs = FileSystem.get(conf);

			if (fs.exists(output_path)) {
				fs.delete(output_path, true);
			}
		}
		public static void main(String[] args) throws Exception {
			int res = ToolRunner.run(new GrayImageThreshold(), args);
			System.exit(res);
		}
	}

