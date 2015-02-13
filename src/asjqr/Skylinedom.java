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
public class Skylinedom
{
    String sortOnEntropy(String inputFile, int attr[]) throws IOException, InterruptedException, FileNotFoundException, IOException
    {
        String inputTemp=Misc.getFileName(Misc.temps);
        String sortedInputTemp=Misc.getFileName(Misc.temps);

        BufferedReader br=new BufferedReader(new FileReader(inputFile));
        BufferedWriter bwt=new BufferedWriter(new FileWriter(inputTemp));
        String str;
        int numattr=0;
        while((str=br.readLine())!=null)
        {
            StringTokenizer st=new StringTokenizer(str);
            if(st.countTokens()==0) break;
            double entropy=0;
            double a[]=new double[st.countTokens()+1];
            int i=0;
            while(st.hasMoreTokens())
            {
                a[i]=Double.parseDouble(st.nextToken());
                i++;
            }
            for(int j=0;j<attr.length;j++)
                entropy += Math.log(a[attr[j]]+1);
            a[i]=entropy;
            String record="";
            for(int j=0;j<a.length-1;j++)
                record += (int)a[j]+" ";
            record += (int)a[a.length-1];
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
    String[] processSkyline(String inputfile, int attr[], int pref[]) throws IOException, InterruptedException
    {
        String truncInput=Misc.getFileName(Misc.temps);
        Misc.executeShellCommand("sed -e 's/^ *//' -e 's/ *$//' "+inputfile+" > "+truncInput);
        String sortedInput=sortOnEntropy(truncInput,attr);
        String outputfile[]=findSkyline(sortedInput,attr,pref); // read input from br and write output to bw..*/
        return outputfile;
    }
    String[] findSkyline(String inputfile, int attr[],int pref[]) throws IOException
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

        boolean windowFull=false,bufferEmpty=true;
        while(bufferEmpty)
        {
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
        while(st1.hasMoreTokens()&&st2.hasMoreTokens())
        {
            int el1=(int)Double.parseDouble(st1.nextToken());
            int el2=(int)Double.parseDouble(st2.nextToken());
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

            if(isDominating(el1,el2,pref[j]))
                recdom=false;
            else if(isDominating(el2,el1,pref[j]))
                strdom=false;
            j++;
        }
        if(strdom)
            return 1;
        else if(recdom)
            return 2;
        return 3;
    }
    boolean isDominating(int el1, int el2, int pref)
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
    String[] processSkylinefinddom(String inputfile, int attr[], int pref[]) throws FileNotFoundException, IOException, InterruptedException
    {
        inputfile=sortOnEntropy(inputfile, attr);
        BufferedReader br1=new BufferedReader(new FileReader(inputfile));
        String outputfile[]=new String[2];
        BufferedWriter bw[]=new BufferedWriter[2];
        for(int i=0;i<2;i++)
        {
            outputfile[i]=Misc.getFileName(Misc.temps);
            bw[i]=new BufferedWriter(new FileWriter(outputfile[i]));
        }
        BufferedWriter bwd=new BufferedWriter(new FileWriter(outputfile[1]+"_dom"));
        String str;
        while((str=br1.readLine())!=null)
        {
            BufferedReader br2=new BufferedReader(new FileReader(inputfile));
            String filerecord;
            //TODO:use iattr instead of last column for primary key
            String dominators=str.substring(str.lastIndexOf(" ")+1)+" ";
            int code=0;
            boolean skylinepoint=true;
            while((filerecord=br2.readLine())!=null)
            {
                if(filerecord.equalsIgnoreCase(str))
                    break;
                code=dominates(str,filerecord,attr,pref); // 1 : str dominates filerecord 2: filerecord dominates str 3: neither dominates each other
                if(code==2)
                {
                    skylinepoint=false;
                    StringTokenizer st=new StringTokenizer(filerecord);
                    int pk=0;
                    while(st.hasMoreTokens())
                        pk=Integer.parseInt(st.nextToken()); //TODO:use iattr instead of last col which is primary key
                    dominators += pk+" ";
                }
            }
            if(skylinepoint)
                bw[0].write(str+"\n");
            else
            {
                bw[1].write(str+"\n");
                bwd.write(dominators+"\n");
            }
            br2.close();
        }
        bw[0].flush();
        bw[1].flush();
        bwd.flush();
        br1.close();
        bw[0].close();
        bw[1].close();
        bwd.close();
        return outputfile;
    }
    String processSkylineDomUsingBase(String inputfile, String base, String domfile[], int attr[], int pref[]) throws FileNotFoundException, IOException, InterruptedException // use inputfile_dom as dominator file
    {
        String inputkeys=inputfile+"_key";
        BufferedReader bri[]=new BufferedReader[2]; // Reader for input file and keys
        bri[0]=new BufferedReader(new FileReader(inputfile)); // Reader for input file
        bri[1]=new BufferedReader(new FileReader(inputkeys)); // Reader for input keys

        String outputfile=Misc.getFileName(Misc.temps);
        BufferedWriter bwt=new BufferedWriter(new FileWriter(outputfile));
        String rec;
        int len=0;
        while((rec=bri[0].readLine())!=null)
        {
            if(len==0)
                len=new StringTokenizer(rec).countTokens();
            String keys=bri[1].readLine(); //keys of input rec
            String dom[]=getDominators(keys,domfile);  //returns keys of dominators for tuple_1 in dom[0] and tuple_2 in dom[1]
            String basetemp=getTargetRecords2(dom[0],dom[1],base);
            boolean skylinerec=isSkylineRecord(rec, basetemp, attr, pref);
            if(skylinerec)
                bwt.write(rec+"\n");
        }
        bwt.flush();
        String skyline[]=processSkyline(outputfile, attr, pref);
        bri[0].close();
        String basetemp=cutArgs(base,len); //removes last 2 columns
        String asjq=Misc.getFileName(Misc.outputs);
        mergeFiles(basetemp,skyline[0],asjq); //vertical append
        bri[1].close();
        bwt.close();
        return asjq;
    }
    void mergeFiles(String basetemp, String skyline, String asjq) throws FileNotFoundException, IOException
    {
        BufferedReader br1=new BufferedReader(new FileReader(basetemp));
        BufferedReader br2=new BufferedReader(new FileReader(skyline));
        BufferedWriter bw=new BufferedWriter(new FileWriter(asjq));
        String str;
        while((str=br1.readLine())!=null)
            bw.write(str+"\n");
        while((str=br2.readLine())!=null)
            bw.write(str+"\n");
        bw.flush();
        br1.close();
        br2.close();
        bw.close();
    }
    String cutArgs(String base, int len) throws FileNotFoundException, IOException
    {
        String basetemp=Misc.getFileName(Misc.temps);
        BufferedReader br=new BufferedReader(new FileReader(base));
        BufferedWriter bw=new BufferedWriter(new FileWriter(basetemp));
        String str;
        while((str=br.readLine())!=null)
        {
            String rec=str.substring(0,str.substring(0, str.lastIndexOf(" ")).lastIndexOf(" "));
            bw.write(rec+"\n");
        }
        bw.flush();
        br.close();
        bw.close();
        return basetemp;
    }
    String[] getDominators(String keys, String domfile[]) throws FileNotFoundException, IOException
    {
        String dom[]=new String[2];
        BufferedReader brd[]=new BufferedReader[2]; // Reader for dominators of both relations
        brd[0]=new BufferedReader(new FileReader(domfile[0]));
        brd[1]=new BufferedReader(new FileReader(domfile[1]));
        String str;
        String[] keystok=keys.split(" ");
        for(int i=0;i<2;i++)
        {
            dom[i]="";
            while((str=brd[i].readLine())!=null)
            {
                String[] s=str.split(" ");
                if(keystok[i].equalsIgnoreCase(s[0]))
                {
                    for(int j=1;j<s.length;j++)
                        dom[i]+= s[j]+" ";
                    break;
                }
            }
        }
        brd[0].close();
        brd[1].close();
        return dom;
    }
    String getTargetKeys(String rec1, String rec2) throws IOException
    {
        String outputfile=Misc.getFileName(Misc.temps);
        BufferedWriter targetKeys=new BufferedWriter(new FileWriter(outputfile));
        StringTokenizer st[]=new StringTokenizer[2];
        st[0]=new StringTokenizer(rec1);
        st[1]=new StringTokenizer(rec2);
        int n1=st[0].countTokens();
        int n2=st[1].countTokens();
        //int k=0;
        for(int i=0;i<n1;i++)
        {
            String key1=st[0].nextToken();
            st[1]=new StringTokenizer(rec2);
            for(int j=0;j<n2;j++)
            {
                String key2=st[1].nextToken();
                targetKeys.write(key1+" "+key2+"\n");
            }
        }
        targetKeys.flush();
        targetKeys.close();
        return outputfile;
    }
    String getTargetRecords2(String rec1, String rec2, String base) throws IOException
    {
        String basetemp=Misc.getFileName(Misc.temps);
        BufferedWriter bw=new BufferedWriter(new FileWriter(basetemp));
        BufferedReader brb=new BufferedReader(new FileReader(base));
        String strb;
        while((strb=brb.readLine())!=null)
        {
            String[] s=strb.split(" ");
            if(rec1.indexOf(s[s.length-2])>=0 && rec2.indexOf(s[s.length-1])>=0) // tuple_1 key present in local dominator rec1 and tuple_2 key in dominator of rec2
                bw.write(strb+"\n");
        }
        bw.flush();
        brb.close();
        bw.close();
        return basetemp;
    }
    String getTargetRecords(String keys, String base) throws FileNotFoundException, IOException
    {
        String basetemp=base+"_temp"+(int)(Math.random()*100);
        BufferedWriter bw=new BufferedWriter(new FileWriter(basetemp));
        BufferedReader brk=new BufferedReader(new FileReader(keys));
        String strk;
        while((strk=brk.readLine())!=null)
        {
            BufferedReader brb=new BufferedReader(new FileReader(base));
            String strb;
            while((strb=brb.readLine())!=null)
            {
                if(strb.endsWith(strk))
                {
                    bw.write(strb+"\n");
                    break;
                }
            }
            brb.close();
        }
        bw.flush();
        bw.close();
        brk.close();
        return basetemp;
    }
    boolean isSkylineRecord(String str, String base, int attr[], int pref[]) throws FileNotFoundException, IOException
    {
        BufferedReader br=new BufferedReader(new FileReader(base));
        String record;
        while((record=br.readLine())!=null)
        {
            int code=dominates(str, record, attr, pref);
            if(code==2)
            {
                br.close();
                return false;
            }
        }
        br.close();
        return true;
    }
}