import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) throws FileNotFoundException {

		String mode = args[0];//
		String inputFile = args[1];
		File image = new File(inputFile);
		Scanner console = new Scanner(image);
		String header = console.nextLine();//I took the first line of input.ppm to write it output files.
		int arrayRow = console.nextInt();//I took the number of row of input.ppm to write it output files.
		int arrayColumn = console.nextInt();//I took the number of column of input.ppm to write it output files.
		int range = console.nextInt();
		int[][][] image_array = new int[arrayRow][arrayColumn][3];//it creates my image array.

		printArray( arrayRow, arrayColumn, console, image_array);//this method fills the image array with input.ppm.
		if (mode.equals("0")) {//if mode is 0 program will create output ppm file and writes the input.ppm
			PrintStream output = new PrintStream("output.ppm");
			writeOutput(arrayRow, arrayColumn, output, image_array, header, range);
		}
		if (mode.equals("1")) {// if mode is 1 it prints black-white version of image to black and white ppm file. 
			PrintStream output = new PrintStream("black-and-white.ppm");
			black_white(arrayRow, arrayColumn, output, image_array, header, range);
		}
		if(mode.equals("2")) {//if mode is 2 it use a filter and print the result convolution.ppm file.
			PrintStream output=new PrintStream("convolution.ppm");
			
			String filter=args[2];//it takes the name of filter
			Scanner input=new Scanner(new File(filter));
			String line=input.nextLine();
			int filter_length=Integer.parseInt(line.substring(0,line.indexOf('x')));//it finds the length of filter
			int[][]filter_array=new int[filter_length][filter_length];// filter array is defined and filled with values at filter.ppm file
			for(int i=0;i<filter_length;i++) 
				for(int j=0;j<filter_length;j++)
					filter_array[i][j]=input.nextInt();
			
			convolution(arrayRow, arrayColumn, output, image_array, header, range, filter_array);
		}
		if(mode.equals("3")) {//the last mode 3 is to do quantization by using recursion.
			PrintStream output=new PrintStream(new File("quantized.ppm"));
			int rangeofquan=Integer.parseInt(args[2]);//it takes range of quantization.
			int [][][]image_array_copy=new int[arrayRow][arrayColumn][3];// I copied the image_array.
			for (int i = 0; i < arrayRow; i++) 
				for (int j = 0; j < arrayColumn; j++) 
					for (int k = 0; k < 3; k++) 
						image_array_copy[i][j][k] = image_array[i][j][k];
			
			boolean [][][]control= new boolean[arrayRow][arrayColumn][3];// this boolean will change when the values are quantized.
			
			for (int i = 0; i < arrayRow; i++) //for every member of image_array I call my quantization method.
				for (int j = 0; j < arrayColumn; j++) 
					for (int k = 0; k < 3; k++) {
						quantization(arrayRow, arrayColumn, output, image_array_copy, header, range, rangeofquan, control, i, j,k , image_array_copy[i][j][k]);		
					}
			writeOutput(arrayRow, arrayColumn, output, image_array_copy, header, range);
		}
	}

	public static int[][][] printArray( int arrayRow, int arrayColumn, Scanner console, int[][][] image_array) throws FileNotFoundException {
		for (int i = 0; i < arrayRow; i++) //this nested loops provide program to write each token to array
			for (int j = 0; j < arrayColumn; j++) 
				for (int k = 0; k < 3; k++) 
					image_array[i][j][k] = console.nextInt();
		return image_array;
	}

	public static void writeOutput(int arrayRow, int arrayColumn, PrintStream output, int[][][] image_array, String header, int range) {
		output.println(header);// I  first write the features of files and then like writeOutput method I write all tokens to output file.
		output.println(arrayRow + " " + arrayColumn);
		output.println(range);
		for (int i = 0; i < arrayRow; i++) {
			for (int j = 0; j < arrayColumn; j++) {
				for (int k = 0; k < 3; k++) 
					output.print(image_array[i][j][k] + " "); 
				output.print("\t");
			}
			output.println();
		}
	}

	public static void black_white(int arrayRow, int arrayColumn, PrintStream output, int[][][] image_array, String header, int range) {
		for (int i = 0; i < arrayRow; i++) {
			for (int j = 0; j < arrayColumn; j++) {
				int average = 0;// I defined a average integer to take average of three values.
				for (int k = 0; k < 3; k++) {
					average += image_array[i][j][k];
				}
				for (int m = 0; m < 3; m++) 
					image_array[i][j][m] = average/3;/// I changed the values of image array by average.	
			}
		}
		writeOutput(arrayRow, arrayColumn, output, image_array, header, range);//  I always use this method to print values to output file.
	} 
	public static void convolution(int arrayRow,int arrayColumn,PrintStream output,int[][][] image_array,String header,int range,int[][]filter) {
		int[][][]convolution_array=new int[arrayRow-(filter.length-1)][arrayColumn-filter.length+1][3];// I found the length of convolution array.
		for(int i=0;i<3;i++) {// first I defined the for which iterates channel 
			for(int j=0;j<=arrayRow-filter.length;j++) {// it iterates the rows.
				for(int k=0;k<=arrayColumn-filter.length;k++) {// it iterates the columns
					int sum=0;
					for(int m=0;m<filter.length;m++) 
						for(int n=0;n<filter.length;n++) // I multiply image_array with filter.
							sum+=(filter[m][n])*(image_array[k+m][j+n][i]);	
					if(sum<0)
						sum=0;
					if(sum>255)
						sum=255;
					convolution_array[k][j][i]=sum;// I implement results to convolution array.				
				}
			}
		}
		black_white(arrayRow-(filter.length-1), arrayColumn-(filter.length-1), output, convolution_array, header, range);// this method makes picture black white and then prints it.
			}
	public static void quantization(int arrayRow,int arrayColumn,PrintStream output,int[][][] image_array,String header,int range,int rangeofquan,boolean[][][]control,int row,int column,int channel,int value) {
		if(row>=arrayRow||column>=arrayColumn||row<0||column<0||channel>=3||channel<0) 
			return;// if row ,column or channel is out of bound method returns. 
		
		if(control[row][column][channel])// if I visited the value before control will be true then method will return.
			return;
		if(image_array[row][column][channel]>value+rangeofquan||image_array[row][column][channel]<value-rangeofquan) // if image_array[x][y][z] is not in range it will return.
			return;
		else {
			control[row][column][channel]=true;	// the value will be change so control also should be true to prevent another change.
			image_array[row][column][channel]=value;// I change the image_array[x][y][z] with first value that I keep.
			quantization(arrayRow, arrayColumn, output, image_array, header, range, rangeofquan, control, row+1, column, channel, value);//it goes to x+1
			quantization(arrayRow, arrayColumn, output, image_array, header, range, rangeofquan, control, row-1, column, channel, value);//it goes to x-1
			quantization(arrayRow, arrayColumn, output, image_array, header, range, rangeofquan, control, row, column+1, channel, value);//it goes to y+1
			quantization(arrayRow, arrayColumn, output, image_array, header, range, rangeofquan, control, row, column-1, channel, value);//it goes to y-1
			quantization(arrayRow, arrayColumn, output, image_array, header, range, rangeofquan, control, row, column, channel+1, value);// it goes to z+1
			quantization(arrayRow, arrayColumn, output, image_array, header, range, rangeofquan, control, row, column, channel-1, value);// it goes to z-1
		}
	}
}
