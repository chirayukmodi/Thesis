/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package asjqr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 *
 * @author palvali
 */
public class Misc
{
    //static String pwd="/home/palvali/NetBeansProjects/ASJQr/src/asjqr/test/";
//	static String pwd="C:/Users/Hare Krishna/workspace/pasjq/src/asjqr/test/";
	static String pwd="src/asjqr/test/";
	static String temps="temps";
	static String dom="dom";
    static String outputs="outputs";
    static Random r=new Random();
    static long starttime;
    static long stoptime;
    static long totaltime=0;
    static double probGlobal=0;
    static void executeShellCommand(String cmd) throws IOException, InterruptedException
    {
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
        Process shell = pb.start();
        InputStream shellIn = shell.getInputStream(); // this captures the output from the command
        shell.waitFor();
        shellIn.close();
        shell.getErrorStream().close();
        shell.destroy();
    }
    static int getRandom()
    {
        return r.nextInt(10000000);
    }
    static String getFileName(String dir)
    {
    	String filename=pwd.concat(dir+"/")+"tempfile"+getRandom();
        while(new File(filename).exists())
            filename=pwd.concat(dir+"/")+"tempfile"+getRandom();
        return filename;
    }
    static void start()
    {
        starttime=System.currentTimeMillis();
    }
    static void stop()
    {
        stoptime=System.currentTimeMillis();
        totaltime+=(stoptime-starttime);
    }
    static String addKey(String input) throws FileNotFoundException, IOException
    {
        BufferedReader br=new BufferedReader(new FileReader(input));
        String output=getFileName(Misc.temps);
        BufferedWriter bw=new BufferedWriter(new FileWriter(output));
        String str;
        int nr=1;
        while((str=br.readLine())!=null)
        {
            bw.write(str+" "+nr+"\n");
            nr++;
        }
        bw.flush();
        bw.close();
        br.close();
        return output;
    }
}