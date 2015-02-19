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
import java.util.StringTokenizer;
import java.util.Vector;

/**
 *
 * @author palvali
 */
public class Skyline
{
    String sortOnEntropy(String inputFile, int attr[], int pref[]) throws IOException, InterruptedException, FileNotFoundException, IOException
    {
        String inputTemp=Misc.getFileName(Misc.temps);
        String sortedInputTemp=Misc.getFileName(Misc.temps);
        
        BufferedReader br=new BufferedReader(new FileReader(inputFile));
        BufferedWriter bwt=new BufferedWriter(new FileWriter(inputTemp));
        String str;
        int numattr=0;
        while((str=br.readLine())!=null)
        {
            //System.out.println(pwd+""+inputFile);
            StringTokenizer st=new StringTokenizer(str);
            if(st.countTokens()==0) break;
            double entropy=1;
            double a[]=new double[st.countTokens()+1];
            int i=0;
            while(st.hasMoreTokens())
            {
                a[i]=Double.parseDouble(st.nextToken());
                i++;
            }
            for(int j=0;j<attr.length;j++)
            {
            	if(pref[j]==1)
            		entropy *= a[attr[j]];
            	else if(pref[j]==2)
            		entropy *= (1/a[attr[j]]);
            	//else
            		//System.out.println("Invalid preference "+pref[j]+" to "+attr[j]);
            }
            entropy = Math.log(entropy);
            a[i]=entropy;
            String record="";
            for(int j=0;j<a.length-1;j++)
                record += a[j]+" ";
            record += a[a.length-1];
            bwt.write(record+"\n");
            numattr=i+1;
        }
        bwt.flush();
        bwt.close();
        br.close();
        String tempfile=Misc.getFileName(Misc.temps);
        Misc.executeShellCommand("sort -t' ' -k"+numattr+" -n "+inputTemp+" >"+tempfile);
        Misc.executeShellCommand("cut -d' ' -f1-"+(numattr-1)+" "+tempfile+" >"+sortedInputTemp); // take care of delimiter give it using -d' '..
        return sortedInputTemp;
        // sort the records in the file based on its corresponding entropy.. sortedInputTemp contains that..
    }
    String removeData(String newnode, String curnode) throws IOException, InterruptedException // newnode-curnode
    {
        String outputfile=Misc.getFileName(Misc.temps);
        Misc.executeShellCommand("sort "+newnode+" > "+newnode+".sort");
        Misc.executeShellCommand("sort "+curnode+" > "+curnode+".sort");
        Misc.executeShellCommand("comm -23 "+newnode+".sort "+curnode+".sort > "+outputfile);
        Misc.executeShellCommand("rm "+newnode+".sort "+curnode+".sort");
        return outputfile;
    }
    String[] processSkyline(Query query,String inputfile, String base, int attr[], int pref[]) throws IOException, InterruptedException
    {
        String sortedBase="";
        String truncInput=Misc.getFileName(Misc.temps);
        	Misc.executeShellCommand("sed -e 's/^ *//' -e 's/ *$//' "+inputfile+" > "+truncInput); //removes spaces from start and end of input file
        if(!base.isEmpty())
        {
            sortedBase=sortOnEntropy(base, attr, pref);
            truncInput=removeData(inputfile, base);
        }
        String sortedInput=sortOnEntropy(truncInput,attr,pref); //entropy = log of multiplication of all attributes and then sort all rows based on it 
        String outputfile[]=findSkyline(query,sortedInput,sortedBase,attr,pref); // read input from sorted input file and write skyline rows to outputfile[0] and non skyline rows to outputfile[1]..*/
        return outputfile;
    }
    String[] findSkyline(Query query,String inputfile, String base, int attr[],int pref[]) throws IOException
    {
        BufferedReader br=new BufferedReader(new FileReader(inputfile));
        String outputfile[]=new String[2];
        BufferedWriter bw[]=new BufferedWriter[2];
        for(int i=0;i<2;i++)
        {
            outputfile[i]=Misc.getFileName(Misc.temps);
            bw[i]=new BufferedWriter(new FileWriter(outputfile[i]));
        }
        String tempWindow=Misc.getFileName(Misc.temps);
        String anotherTempWindow=tempWindow;
        FileWriter wt=new FileWriter(tempWindow);
        BufferedWriter bwt=new BufferedWriter(wt);
        Vector<String> window=new Vector<String>();
        String str;
        int windowLimit=100000;

        // load window with base
        if(!base.isEmpty())
        {
            BufferedReader brw=new BufferedReader(new FileReader(base));
            while((str=brw.readLine())!=null)
                window.add(str);
            brw.close();
        }

        boolean windowFull=false,bufferEmpty=true;
        while(bufferEmpty)
        {
            while((str=br.readLine())!=null)
            {
            	//Added By Chirayu
            	String temp[]=str.split(" ");
            	int tupleNo1 = (int)Double.parseDouble(temp[query.rel[0].iattr]);
            	//End
                int code=0;
                for(int i=0;i<window.size();i++)
                {
                    String record=window.elementAt(i);
                    //Added By Chirayu
                    temp = record.split(" ");
                    int tupleNo2 = (int)Double.parseDouble(temp[query.rel[0].iattr]);
                    
                    if(tupleNo1==tupleNo2)
                    	continue;
                    //End
                    code=dominates(str,record,attr,pref); // 1 : str dominates record 2: record dominates str 3: neither dominates each other
                    if(code==3)
                        continue;
                    if(code==2)
                    {
                        bw[1].write(str+"\n");
                        break;
                    }
                    //System.out.println(str+" dominates "+record);
                    bw[1].write(window.elementAt(i)+"\n");
                    window.removeElementAt(i);
                    i--;
                }
                if(code==2)
                    continue;
                if(window.size()==windowLimit)
                {
                    windowFull=true;
                    bufferEmpty=false;
                }
                if(!windowFull)
                    window.add(str);
                else
                    bwt.write(str);
            }
            bwt.flush();
            //System.out.println(window.size()); // Skyline Size
            for(int i=0;i<window.size();i++)
               bw[0].write(window.elementAt(i)+"\n");
            bw[0].flush();
            bw[1].flush();
            window.removeAllElements();
            windowFull=false;
            if(!bufferEmpty)
            {
                if(!tempWindow.equalsIgnoreCase(anotherTempWindow))
                    new File(tempWindow).delete();
                tempWindow=anotherTempWindow;
                br.close();
                br=new BufferedReader(new FileReader(tempWindow));
                bufferEmpty=true;
                bwt.close();
                anotherTempWindow=Misc.getFileName(Misc.temps);
                bwt=new BufferedWriter(new FileWriter(anotherTempWindow));
            }
            else
                bufferEmpty=false; // to end the while loop
        }
        new File(tempWindow).delete();
        br.close();
        bw[0].close();
        bw[1].close();
        bwt.close();
        return outputfile;
    }
    int dominates(String str,String record,int attr[],int pref[])
    {
        StringTokenizer st1=new StringTokenizer(str);
        StringTokenizer st2=new StringTokenizer(record);
        boolean strdom=true, recdom=true;
        int i=0,j=0;
        //Arrays.sort(attr);
        while(st1.hasMoreTokens()&&st2.hasMoreTokens())
        {
            //System.out.println(str+"$"+record);
            double el1=Double.parseDouble(st1.nextToken());
            double el2=Double.parseDouble(st2.nextToken());
            if(j==attr.length)
                break;
            if(i!=attr[j])
            {
                i++;
                continue;
            }
            i++;
            if(pref[j]==0) // If pref is "=", dominance should be compared only if the elements are equal
            {
                if(el1!=el2)
                    return 3;
                j++;
                continue;
            }

            //if(el1<el2)
            if(isDominating(el1,el2,pref[j]))
                recdom=false;
            //else if(el2<el1)
            else if(isDominating(el2,el1,pref[j]))
                strdom=false;
            j++;
        }
   //     if(strdom&&recdom)
    //        return 3;
        if(strdom)
            return 1;
        else if(recdom)
            return 2;
        return 3;
    }
    boolean isDominating(double el1, double el2, int pref)
    {
        switch(pref)
        {
            case 1: if(el1<el2) return true; break;
            case 2: if(el1>el2) return true; break;
            case 3: if(el1<=el2) return true; break;
            case 4: if(el1>=el2) return true; break;
        }
        return false;
    }
    String[] processSkyline_iter(String inputfile, String base, int attr[], int pref[]) throws IOException, InterruptedException
    {
        String truncInput=Misc.getFileName(Misc.temps);
        Misc.executeShellCommand("cp "+inputfile+" "+truncInput);
        String sortedInput=sortOnEntropy(truncInput,attr,pref);
        String outputfile[]=findSkyline_iter(sortedInput,base,attr,pref); // read input from br and write output to bw..*/
        return outputfile;
    }
    String[] findSkyline_iter(String inputfile, String base, int attr[],int pref[]) throws IOException
    {
        BufferedReader br=new BufferedReader(new FileReader(inputfile));
        String outputfile[]=new String[2];
        BufferedWriter bw[]=new BufferedWriter[2];
        for(int i=0;i<2;i++)
        {
            outputfile[i]=Misc.getFileName(Misc.temps);
            bw[i]=new BufferedWriter(new FileWriter(outputfile[i]));
        }
        String tempWindow=Misc.getFileName(Misc.temps);
        String anotherTempWindow=tempWindow;
        FileWriter wt=new FileWriter(tempWindow);
        BufferedWriter bwt=new BufferedWriter(wt);
        Vector<String> window=new Vector<String>();
        String str;
        int windowLimit=10000;

        // load window with base
        if(!base.isEmpty())
        {
            BufferedReader brw=new BufferedReader(new FileReader(base));
            while((str=brw.readLine())!=null)
                window.add(str);
            brw.close();
        }

        boolean windowFull=false,bufferEmpty=true;
        while(bufferEmpty)
        {
            int basesize=window.size();
            while((str=br.readLine())!=null)
            {
                int code=0;
                for(int i=0;i<window.size();i++)
                {
                    String record=window.elementAt(i);
                    code=dominates(str,record,attr,pref); // 1 : str dominates record 2: record dominates str 3: neither dominates each other
                    if(code==3)
                        continue;
                    if(code==2)
                    {
                        bw[1].write(str+"\n");
                        break;
                    }

                    bw[1].write(window.elementAt(i)+"\n");
                    window.removeElementAt(i);
                    i--;
                }
                if(code==2)
                    continue;
                if(window.size()==windowLimit)
                {
                    windowFull=true;
                    bufferEmpty=false;
                }
                if(!windowFull)
                    window.add(str);
                else
                    bwt.write(str);
            }
            bwt.flush();
            //System.out.println(window.size()); // Skyline Size
            for(int i=basesize;i<window.size();i++)
                bw[0].write(window.elementAt(i)+"\n");
            bw[0].flush();
            bw[1].flush();
            window.removeAllElements();
            windowFull=false;
            if(!bufferEmpty)
            {
                if(!tempWindow.equalsIgnoreCase(anotherTempWindow))
                    new File(tempWindow).delete();
                tempWindow=anotherTempWindow;
                br.close();
                br=new BufferedReader(new FileReader(tempWindow));
                bufferEmpty=true;
                bwt.close();
                anotherTempWindow=Misc.getFileName(Misc.temps);
                bwt=new BufferedWriter(new FileWriter(anotherTempWindow));
            }
            else
                bufferEmpty=false; // to end the while loop
        }
        new File(tempWindow).delete();
        br.close();
        bw[0].close();
        bw[1].close();
        bwt.close();
        return outputfile;
    }
	
}
