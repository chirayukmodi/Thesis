/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package asjqr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

/**
 *
 * @author palvali
 */
public class Aggregate
{
	void executeShellCommand(String cmd) throws IOException, InterruptedException
	{
		//System.out.println("Executing command: "+cmd);
		ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
		Process shell = pb.start();
		InputStream shellIn = shell.getInputStream(); // this captures the output from the command
		shell.waitFor();
		shellIn.close();
	}
	String[] threshAggregate(Query query,String inputfile, int attr1[], int attr2[], int gop[],boolean flag) throws FileNotFoundException, IOException
	{
		FileReader r=new FileReader(inputfile);
		BufferedReader br=new BufferedReader(r);
		String outputfile[] = new String[2];
		outputfile[0]=Misc.getFileName("temps");
		outputfile[1]=Misc.getFileName("temps");
		BufferedWriter bwt=new BufferedWriter(new FileWriter(outputfile[0])); // tuple with prob > p
		BufferedWriter bw=new BufferedWriter(new FileWriter(outputfile[1]));      //all tuples
		String str;
		while((str=br.readLine())!=null)
		{
			StringTokenizer st=new StringTokenizer(str);
			double record[]=new double[st.countTokens()];  //size of array = no. of columns
			for(int i=0;i<record.length;i++)
				record[i]=Double.parseDouble(st.nextToken());  //store all columns in record                     
			double gattr[]=new double[gop.length];
			for(int i=0;i<gop.length;i++)
			{
				gattr[i]=doOperation(record[attr1[i]],record[attr2[i]],gop[i]); //store aggregated attribute in gattr
			}
			String rec="";
			rec += str + " ";
			for(int i=0;i<gattr.length;i++)
				rec += gattr[i]+" ";	//appending the aggregate columns at last

			bw.write(rec+"\n");
			if(flag && (record[query.rel[0].pattr] * record[query.rel[0].numattr + query.rel[1].pattr]) >= query.p)  //if flag is true means thresh aggregate and next condition checks probability of joined tuple > p, else does not write to thresh file
			{
				bwt.write(rec+" "+(record[query.rel[0].pattr] * record[query.rel[0].numattr + query.rel[1].pattr])+"\n");
			}            	
		}
		bw.flush();
		bwt.flush();
		bw.close(); //-added by shri
		bwt.close();
		br.close(); //-added by shri
		return outputfile;
	}
	String doAggregate(String inputfile, int attr1[], int attr2[], int gop[]) throws FileNotFoundException, IOException
	{
		String outputfile[] = threshAggregate(null, inputfile, attr1, attr2, gop,false);
		return outputfile[1];
	}
	double doOperation(double elem1, double elem2, int gop)
	{
		switch(gop)
		{
		case 0: return elem1+elem2;
		case 1: return (elem1+elem2)/2;
		case 2: return (elem1>elem2)?elem1:elem2;
		case 3: return (elem1<elem2)?elem1:elem2;
		default: return 0;
		}        
	}
	String doUnion(String file1, String file2, String file3) throws IOException, InterruptedException
	{
		String outputfile=Misc.getFileName("temps");
		// System.out.println(file1+" "+file2+" "+file3+" > "+outputfile);
		String command="cat "+file1+" "+file2+" "+file3+" > "+outputfile;
		executeShellCommand(command);
		//executeShellCommand("rm "+file1+" "+file2+" "+file3);
		return outputfile;
	}
	String doUnion(String file1, String file2, String file3, String file4) throws IOException, InterruptedException
	{
		String outputfile=Misc.getFileName("outputs");
		// System.out.println(file1+" "+file2+" "+file3+" > "+outputfile);
		String command="cat "+file1+" "+file2+" "+file3+" "+file4+" > "+outputfile;
		executeShellCommand(command);
		//executeShellCommand("rm "+file1+" "+file2+" "+file3+" "+file4);
		return outputfile;
	}
	int findSize(String file) throws IOException, InterruptedException
	{
		ProcessBuilder pb = new ProcessBuilder("bash", "-c", "wc -l "+file);
		Process shell = pb.start();
		InputStream shellIn = shell.getInputStream(); // this captures the output from the command
		shell.waitFor();
		int size=shellIn.read();
		shellIn.close();
		return size;
	}
}
