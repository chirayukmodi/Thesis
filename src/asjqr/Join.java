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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 *
 * @author palvali
 */
public class Join
{
     /*int hashcap=100;
    String doJoin1(String rela, String relb, int jattr1[], int jattr2[], int jop[]) throws FileNotFoundException, IOException
    {
        String outputfile=Misc.getFileName("temps");
        FileWriter w=new FileWriter(outputfile);
        BufferedWriter bw=new BufferedWriter(w);
        Vector<String> hasha[]=new Vector[hashcap];
        Vector<String> hashb[]=new Vector[hashcap];

        for(int i=0;i<100;i++)
        {
            hasha[i]=new Vector<String>();
            hashb[i]=new Vector<String>();
        }
        constructHash(rela,hasha);
        constructHash(relb,hashb);
        for(int i=0;i<hashcap;i++)
        {
            for(int j=0;j<hasha[i].size();j++)
            {
                String reca=hasha[i].elementAt(j);
                int elema=Integer.parseInt(new StringTokenizer(reca).nextToken());
                for(int k=0;k<hashb[i].size();k++)
                {
                    String recb=hashb[i].elementAt(k);
                    int elemb=Integer.parseInt(new StringTokenizer(recb).nextToken());
                    if(elema==elemb)
                    {
                        String record=reca.concat(recb.substring(recb.indexOf(' ')));
                        bw.write(record+"\n");
                    }
                    bw.flush();
                }
            }
        }
        bw.close();
        //return outputfile;
        return "src/asjqr/test/temps/temp100" ;
    }*/
    
    String doJoin(String file1, String file2, int jattr1[], int jattr2[], int jop[]) throws FileNotFoundException, IOException
    {
        String outputfile=Misc.getFileName("temps");
        FileWriter w=new FileWriter(outputfile);
        
        
        BufferedWriter bw =new BufferedWriter(w);
        
        ArrayList<ArrayList<Double>> rel1 = new ArrayList<ArrayList<Double>>();
        ArrayList<ArrayList<Double>> rel2 = new ArrayList<ArrayList<Double>>();
            

        constructHash(file1, rel1);
        constructHash(file2, rel2);
                
        
        for (Iterator<ArrayList<Double>> iterator = rel1.iterator(); iterator.hasNext();) {
			ArrayList<Double> row1 = (ArrayList<Double>) iterator.next();
			
			/**/
			
			for (Iterator<ArrayList<Double>> iterator2 = rel2.iterator(); iterator2.hasNext();) {
				ArrayList<Double> row2 = (ArrayList<Double>) iterator2.next();
				
				
				boolean flag = true;
				for (int i = 0; i < jop.length; i++) {
									
					flag &= compare(row1.get(jattr1[i]), row2.get(jattr2[i]), jop[i]);
				}
				
				if (flag) {
					StringBuffer str = new StringBuffer();
					for (Iterator<Double> iterator3 = row1.iterator(); iterator3.hasNext();) {
						Double column = (Double) iterator3.next();
						str.append(column.toString() + " ");
					}
					for (Iterator<Double> iterator4 = row2.iterator(); iterator4.hasNext();) {
						Double column = (Double) iterator4.next();
						str.append(column.toString() + " ");
					}
					
					String rec =  str.substring(0, str.length()-1) + "\n" ;
										
					bw.write(rec);
				}
			
			}			
			
		}
        
		bw.flush();     
        bw.close();
        //System.out.println(outputfile);
        return outputfile;
        //return "src/asjqr/test/temps/temp100" ;
    }

    //Added By Chirayu
    
    String doJoin(String file1, String file2, int jattr1[], int jattr2[], int jop[],HashMap<Integer,ArrayList<Integer>>H) throws FileNotFoundException, IOException
    {
        String outputfile=Misc.getFileName("temps");
        FileWriter w=new FileWriter(outputfile);
        boolean flg1,flg2;
        
        BufferedWriter bw =new BufferedWriter(w);
        
        ArrayList<ArrayList<Double>> rel1 = new ArrayList<ArrayList<Double>>();
        ArrayList<ArrayList<Double>> rel2 = new ArrayList<ArrayList<Double>>();
            

        constructHash(file1,rel1);
        constructHash(file2,rel2);
                
        
        for (Iterator<ArrayList<Double>> iterator = rel1.iterator(); iterator.hasNext();) {
			ArrayList<Double> row1 = (ArrayList<Double>) iterator.next();
			
			flg1 = false;
			if(H.containsKey(row1.get(1).intValue()))
			{
				for(int k=0;k<H.get(row1.get(1).intValue()).size();k++)
				{
					if(H.get(row1.get(1).intValue()).get(k)==row1.get(0).intValue())
					{
						flg1=true;
						break;
					}
				}
			}
			
			if(flg1)
				continue;
			/**/
			
			for (Iterator<ArrayList<Double>> iterator2 = rel2.iterator(); iterator2.hasNext();) {
				ArrayList<Double> row2 = (ArrayList<Double>) iterator2.next();
				
				flg2 = false;
				if(H.containsKey(row2.get(1).intValue()))
				{
					for(int k=0;k<H.get(row2.get(1).intValue()).size();k++)
					{
						if(H.get(row2.get(1).intValue()).get(k)==row2.get(0).intValue())
						{
							flg2=true;
								break;
						}
					}
				}
				
				if(flg2)
					continue;
				
				boolean flag = true;
				for (int i = 0; i < jop.length; i++) {
									
					flag &= compare(row1.get(jattr1[i]), row2.get(jattr2[i]), jop[i]);
				}
				
				if (flag) {
					StringBuffer str = new StringBuffer();
					for (Iterator<Double> iterator3 = row1.iterator(); iterator3.hasNext();) {
						Double column = (Double) iterator3.next();
						str.append(column.toString() + " ");
					}
					for (Iterator<Double> iterator4 = row2.iterator(); iterator4.hasNext();) {
						Double column = (Double) iterator4.next();
						str.append(column.toString() + " ");
					}
					
					String rec =  str.substring(0, str.length()-1) + "\n" ;
										
					bw.write(rec);
				}
			
			}			
			
		}
        
		bw.flush();     
        bw.close();
        //System.out.println(outputfile);
        return outputfile;
        //return "src/asjqr/test/temps/temp100" ;
    }

    //End
 // join operation, size: no. of join operations, 0 = ; 1 < ; 2 > ; 3 <= ; 4 >= ; 5 !=
    boolean compare(Double el1, Double el2, int operator) {    
    	double ele1 = el1.doubleValue();
    	double ele2 = el2.doubleValue();
    	
    	switch(operator)
        {
    		case 0: if(ele1 ==ele2) return true; break;
            case 1: if(ele1 < ele2) return true; break;
            case 2: if(ele1 > ele2) return true; break;
            case 3: if(ele1 <= ele2) return true; break;
            case 4: if(ele1 >= ele2) return true; break;
            case 5: if(ele1 != ele2) return true; break;
        }
        return false;
    }
    
    
    /*void constructHash1(String rel,Vector<String>[] hash) throws FileNotFoundException, IOException, FileNotFoundException
    {
        FileReader r=new FileReader(rel);
        BufferedReader br=new BufferedReader(r);
        String str;
        while((str=br.readLine())!=null)
        {
            StringTokenizer st=new StringTokenizer(str);
            int elem=Integer.parseInt(st.nextToken());
            int key=elem%hashcap;
            hash[key].add(str);
        }
        br.close();
    }*/
    
    void constructHash(String file, ArrayList<ArrayList<Double>> rel) throws FileNotFoundException, IOException
    {
        FileReader r = new FileReader(file);
        BufferedReader br=new BufferedReader(r);
        String str;
        int i = 0;
        while((str=br.readLine())!=null)
        {
        		
            StringTokenizer st=new StringTokenizer(str);
            rel.add(new ArrayList<Double>());            
            
            while(st.hasMoreTokens())
            {
            	rel.get(i).add(Double.parseDouble(st.nextToken()));
            }
            i++;
        }
        br.close();
    }
}
