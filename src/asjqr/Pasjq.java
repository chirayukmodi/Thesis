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
import java.util.Vector;

/**
 *
 * @author palvali
 */
class Pasjq
{
	Join join=new Join();
	Aggregate agg=new Aggregate();
	HashMap<Integer,ArrayList<Integer>> Htemp;
	HashMap<Integer,ArrayList<Integer>> H=new HashMap<Integer, ArrayList<Integer>>();
	//Added by Chirayu
	void PruneInput(Query query) throws FileNotFoundException, IOException, InterruptedException
	{
		Pskyline sk=new Pskyline();
		for(int i=0;i<query.n;i++)
		{
			int attr[]=new int[query.rel[i].nl+query.gop.length];
			int pref[]=new int[query.rel[i].nl+query.gop.length];
			
				int j=0;
				for(int k=0;k<query.rel[i].nl;k++)
				{
					attr[j]=query.rel[i].lattr[k];
					pref[j]=query.rel[i].lpref[k];
					j++;
				}
				for(int k=0;k<query.gop.length;k++)
				{
					attr[j]=query.rel[i].gattr[k];
					pref[j]=query.rel[i].gpref[k];
					j++;
				}
				
				
				Htemp = sk.processSkyline_SingleRelation(query,query.rel[i].name, "", attr, pref, query.p, 1);
				H.putAll(Htemp);
		}
		
	}
	//End


	String bruteforce(Query query) throws FileNotFoundException, IOException, InterruptedException
	{
		Pskyline sk=new Pskyline();
		int attr[],pref[];
		//Added by Chirayu
		PruneInput(query);
		//End
		//Skyline sk = new Skyline();
		
		//Commented by Chirayu
		//String j=join.doJoin(query.rel[0].name, query.rel[1].name, query.rel[0].jattr,query.rel[1].jattr,query.jop);
		//End
		
		String j=join.doJoin(query.rel[0].name, query.rel[1].name, query.rel[0].jattr,query.rel[1].jattr,query.jop,H);
	
		System.out.println("Join File: " + j);

		int gattr[]=new int[query.rel[1].gattr.length];

		for(int i=0;i<gattr.length;i++)
		{
			//gattr[i]=query.rel[1].gattr[i]+qubruteforceery.rel[0].nl+query.rel[0].ng; //set the indices of aggregate columns of 2nd relation 
			gattr[i]=query.rel[1].gattr[i]+query.rel[0].numattr;
		}            
		String aj=agg.doAggregate(j,query.rel[0].gattr,gattr,query.gop);
		System.out.println("Aggregate File: " + aj);

		attr=new int[query.rel[0].nl+query.rel[1].nl+query.gop.length];
		pref=new int[query.rel[0].nl+query.rel[1].nl+query.gop.length];
		int totalNumAttr = query.rel[0].numattr + query.rel[1].numattr;
		int totalNumLocalAttr = query.rel[0].nl + query.rel[1].nl;
		for(int i=0;i<attr.length;i++)
		{
			//attr[i]=i+query.jop.length; //skipping join attr while setting index 
			if(i<query.rel[0].nl)
			{
				attr[i] = query.rel[0].lattr[i];
				pref[i] = query.rel[0].lpref[i];
			}
			//pref[i]=query.rel[0].attr[attr[i]].pref; //setting preference of local attr of 1st rel
			else if(i<totalNumLocalAttr)
			{
				attr[i] = query.rel[1].lattr[i-query.rel[0].nl] + query.rel[0].numattr;
				pref[i] = query.rel[1].lpref[i-query.rel[0].nl];
			}//pref[i]=query.rel[1].attr[i-query.rel[0].nl+query.jop.length].pref; //same of 2nd rel
			else
			{
				attr[i] = totalNumAttr + i-totalNumLocalAttr;
				pref[i] = query.rel[1].gpref[i-totalNumLocalAttr];
			}    
			System.out.println("("+attr[i] +","+pref[i]+")");
		}
		//String pasjq="";
		String pasjq=sk.processSkyline(query,aj,"",attr,pref,query.p,1);
		//sk.processSkyline(query,aj,"",attr,pref,query.p,1);
		//String asjq[] = {"success"};*/
		return pasjq;
	}

	String msc(Query query) throws IOException, InterruptedException
	{
		Skyline sk=new Skyline();
		Pskyline psk=new Pskyline();
		HashMap<Integer,HashMap<Integer,Double>>OutputMap = new HashMap<Integer, HashMap<Integer, Double>>();
		String tempre[][]=new String[query.n][2];
		String temprel[][]=new String[query.n][3];
		for(int i=0;i<query.n;i++)
		{
			// find full skyline
			{
				int attr[]=new int[query.rel[i].numattr];
				int pref[]=new int[query.rel[i].numattr];
				for(int j=0;j<query.rel[i].numattr;j++)
				{
					attr[j]=j;
					pref[j]=query.rel[i].attr[j].pref;
				}
				tempre[i]=sk.processSkyline(query.rel[i].name,"",attr,pref); //Af, Af'
				temprel[i][2]=tempre[i][1]; //stores Af'
			}
			tempre[i]=sk.processSkyline(tempre[i][0],"",query.rel[i].lattr,query.rel[i].lpref);
			temprel[i][0]=tempre[i][0]; //Afl
			temprel[i][1]=tempre[i][1]; //Afl'
		}

		// join all the subsets
		String asj1=join.doJoin(temprel[0][0],temprel[1][0],query.rel[0].jattr,query.rel[1].jattr,query.jop); //Afl X Bfl
		String asj2=join.doJoin(temprel[0][0],temprel[1][1],query.rel[0].jattr,query.rel[1].jattr,query.jop); //Afl X Bfl'
		String asj3=join.doJoin(temprel[0][1],temprel[1][0],query.rel[0].jattr,query.rel[1].jattr,query.jop); //Afl' X Bfl
		String asj4=join.doJoin(temprel[0][1],temprel[1][1],query.rel[0].jattr,query.rel[1].jattr,query.jop); //Afl' X Bfl'
		String asj5=join.doJoin(temprel[0][0],temprel[1][2],query.rel[0].jattr,query.rel[1].jattr,query.jop); //Afl X Bf'
		String asj6=join.doJoin(temprel[0][1],temprel[1][2],query.rel[0].jattr,query.rel[1].jattr,query.jop); //Afl' X Bf'
		String asj7=join.doJoin(temprel[0][2],temprel[1][0],query.rel[0].jattr,query.rel[1].jattr,query.jop); //Af' X Bfl
		String asj8=join.doJoin(temprel[0][2],temprel[1][1],query.rel[0].jattr,query.rel[1].jattr,query.jop); //Af' X Bfl'
		String asj9=join.doJoin(temprel[0][2],temprel[1][2],query.rel[0].jattr,query.rel[1].jattr,query.jop); //Af' X Bf'

		int gattr[]=new int[query.rel[1].gattr.length];
		for(int i=0;i<gattr.length;i++)
			gattr[i]=query.rel[1].gattr[i]+query.rel[0].numattr; //gattr[i]=query.rel[1].gattr[i]+query.rel[0].nl+query.rel[0].ng;

		String join1=agg.doUnion(asj1,asj2,asj3);													//J1 ← T 1 ∪ T 2 ∪ T 3
		String sky1[] = agg.threshAggregate(query,join1,query.rel[0].gattr,gattr,query.gop,true); //(S1, R1) ← ThreshAggregate(J1, p)
		
		//Added By Chirayu
		BufferedReader br=new BufferedReader(new FileReader(sky1[0]));
		String temp;
		while((temp=br.readLine())!=null)
		{
			String FullSkyline[] = temp.split(" ");
			//System.out.println(temp);
			Double prob1,prob2;
			prob1 = Double.parseDouble(FullSkyline[query.rel[0].pattr]);
			prob2 = Double.parseDouble(FullSkyline[query.rel[1].pattr+query.rel[0].numattr]);
			if(query.p >= prob1 && query.p>=prob2)
			{
				HashMap<Integer,Double>tempMap = new HashMap<Integer,Double>();
				tempMap.put((int)Double.parseDouble(FullSkyline[query.rel[1].iattr + query.rel[0].numattr]), prob1*prob2);
				OutputMap.put((int)Double.parseDouble(FullSkyline[query.rel[0].iattr]), tempMap);
			}
		}
		br.close();
		//End
		String result2 = agg.doAggregate(asj5,query.rel[0].gattr,gattr,query.gop);				//R2 ← Aggregate(T 5)
		String join2=agg.doUnion(asj1,asj2,asj5);												//T ← Aggregate(T 1 ∪ T 2 ∪ T 5)
		String target = agg.doAggregate(join2,query.rel[0].gattr,gattr,query.gop);
		int attr[]=new int[query.rel[0].nl+query.rel[1].nl+query.gop.length];
		int pref[]=new int[query.rel[0].nl+query.rel[1].nl+query.gop.length];
		int totalNumAttr = query.rel[0].numattr + query.rel[1].numattr;
		int totalNumLocalAttr = query.rel[0].nl + query.rel[1].nl;
		for(int i=0;i<attr.length;i++)
		{
			if(i<query.rel[0].nl)
			{
				attr[i] = query.rel[0].lattr[i];
				pref[i] = query.rel[0].lpref[i];
			}
			else if(i<totalNumLocalAttr)
			{
				attr[i] = query.rel[1].lattr[i-query.rel[0].nl] + query.rel[0].numattr;
				pref[i] = query.rel[1].lpref[i-query.rel[0].nl];
			}
			else
			{
				attr[i] = totalNumAttr + i-totalNumLocalAttr;
				pref[i] = query.rel[1].gpref[i-totalNumLocalAttr];
			}    
			System.out.println("("+attr[i] +","+pref[i]+")");
		}
		String sky2=psk.processSkyline(query,result2,target,attr,pref,query.p,0);		//S2 ← ComputeSkylineUsingTargetSets(R2, T, p)

		String result3 = agg.doAggregate(asj7,query.rel[0].gattr,gattr,query.gop);	//R3 ← Aggregate(T 7)
		String join3=agg.doUnion(asj1,asj3,asj7);									//T ← Aggregate(T 1 ∪ T 3 ∪ T 7)
		target = agg.doAggregate(join3,query.rel[0].gattr,gattr,query.gop);
		String sky3=psk.processSkyline(query,result3,target,attr,pref,query.p,0); 	//S3 ← ComputeSkylineUsingTargetSets(R3, T, p)

		String join4=agg.doUnion(asj4,asj6,asj8,asj9);									//J4 ← T 4 ∪ T 6 ∪ T 8 ∪ T 9
		String result4 = agg.doAggregate(join4,query.rel[0].gattr,gattr,query.gop); 	//R4 ← Aggregate(J4)
		target = agg.doUnion(sky1[1],result2,result3,result4);							// T ← R1 ∪ R2 ∪ R3 ∪ R4 
		String sky4 = psk.processSkyline(query,result4,target,attr,pref,query.p,0);		// S4 ← ComputeSkylineUsingTargetSets(R4, T, p)
		String sky = agg.doUnion(sky1[0],sky2,sky3,sky4);								// S ← S1 ∪ S2 ∪ S3 ∪ S4
		return sky;
	}

	String dominator(Query query) throws IOException, InterruptedException
	{
		Skyline sk = new Skyline();
		PskylineDom psk=new PskylineDom();
		String tempre[][]=new String[query.n][2];
		String temprel[][]=new String[query.n][3];
		for(int i=0;i<query.n;i++)
		{
			// find full skyline
			{
				int attr[]=new int[query.rel[i].numattr];
				int pref[]=new int[query.rel[i].numattr];
				for(int j=0;j<query.rel[i].numattr;j++)
				{
					attr[j]=j;
					pref[j]=query.rel[i].attr[j].pref;
				}
				tempre[i]=sk.processSkyline(query.rel[i].name,"",attr,pref); //Af, Af'
				temprel[i][2]=tempre[i][1]; //stores Af'
			}
			tempre[i]=sk.processSkyline(tempre[i][0],"",query.rel[i].lattr,query.rel[i].lpref);
			temprel[i][0]=tempre[i][0]; //Afl
			temprel[i][1]=tempre[i][1]; //Afl'
		}


		//String dom[][]=new String[2][2]; // The dominator file should be filename_dom. which has to be same in processSkylineDom method.
		for(int i=0;i<2;i++)
		{
			System.out.println(temprel[i][0] + " ");
			System.out.println(temprel[i][1] + " ");
			System.out.println(temprel[i][2] + "\n");
			psk.findLocalDom(temprel[i][0],query.rel[i]); //Afl_dom
			psk.findLocalDom(temprel[i][1],query.rel[i]); //Afl'_dom
			psk.findLocalDom(temprel[i][2],query.rel[i]); //Af'_dom
		}

		String asj1=join.doJoin(temprel[0][0],temprel[1][0],query.rel[0].jattr,query.rel[1].jattr,query.jop); //Afl X Bfl
		String asj2=join.doJoin(temprel[0][0],temprel[1][1],query.rel[0].jattr,query.rel[1].jattr,query.jop); //Afl X Bfl'
		String asj3=join.doJoin(temprel[0][1],temprel[1][0],query.rel[0].jattr,query.rel[1].jattr,query.jop); //Afl' X Bfl
		String asj4=join.doJoin(temprel[0][1],temprel[1][1],query.rel[0].jattr,query.rel[1].jattr,query.jop); //Afl' X Bfl'
		String asj5=join.doJoin(temprel[0][0],temprel[1][2],query.rel[0].jattr,query.rel[1].jattr,query.jop); //Afl X Bf'
		String asj6=join.doJoin(temprel[0][1],temprel[1][2],query.rel[0].jattr,query.rel[1].jattr,query.jop); //Afl' X Bf'
		String asj7=join.doJoin(temprel[0][2],temprel[1][0],query.rel[0].jattr,query.rel[1].jattr,query.jop); //Af' X Bfl
		String asj8=join.doJoin(temprel[0][2],temprel[1][1],query.rel[0].jattr,query.rel[1].jattr,query.jop); //Af' X Bfl'
		String asj9=join.doJoin(temprel[0][2],temprel[1][2],query.rel[0].jattr,query.rel[1].jattr,query.jop); //Af' X Bf'

		int gattr[]=new int[query.rel[1].gattr.length];
		for(int i=0;i<gattr.length;i++)
			gattr[i]=query.rel[1].gattr[i]+query.rel[0].numattr; //gattr[i]=query.rel[1].gattr[i]+query.rel[0].nl+query.rel[0].ng;

		String join1=agg.doUnion(asj1,asj2,asj3);													//J1 ← T 1 ∪ T 2 ∪ T 3
		String sky1[] = agg.threshAggregate(query,join1,query.rel[0].gattr,gattr,query.gop,true); //(S1, R1) ← ThreshAggregate(J1, p)

		String thresh4[] = agg.threshAggregate(query,asj4,query.rel[0].gattr,gattr,query.gop,true);
		String thresh5[] = agg.threshAggregate(query,asj5,query.rel[0].gattr,gattr,query.gop,true);
		String thresh6[] = agg.threshAggregate(query,asj6,query.rel[0].gattr,gattr,query.gop,true);
		String thresh7[] = agg.threshAggregate(query,asj7,query.rel[0].gattr,gattr,query.gop,true);
		String thresh8[] = agg.threshAggregate(query,asj8,query.rel[0].gattr,gattr,query.gop,true);
		String thresh9[] = agg.threshAggregate(query,asj9,query.rel[0].gattr,gattr,query.gop,true);

		Misc.executeShellCommand("rm "+asj1+" "+asj2+" "+asj3+" "+asj4+" "+asj5+" "+asj6+" "+asj7+" "+asj8+" "+asj9);

		int attr[]=new int[query.rel[0].nl+query.rel[1].nl+query.gop.length];
		int pref[]=new int[query.rel[0].nl+query.rel[1].nl+query.gop.length];
		int totalNumAttr = query.rel[0].numattr + query.rel[1].numattr;
		int totalNumLocalAttr = query.rel[0].nl + query.rel[1].nl;
		for(int i=0;i<attr.length;i++)
		{
			if(i<query.rel[0].nl)
			{
				attr[i] = query.rel[0].lattr[i];
				pref[i] = query.rel[0].lpref[i];
			}
			else if(i<totalNumLocalAttr)
			{
				attr[i] = query.rel[1].lattr[i-query.rel[0].nl] + query.rel[0].numattr;
				pref[i] = query.rel[1].lpref[i-query.rel[0].nl];
			}
			else
			{
				attr[i] = totalNumAttr + i-totalNumLocalAttr;
				pref[i] = query.rel[1].gpref[i-totalNumLocalAttr];
			}    
			System.out.println("("+attr[i] +","+pref[i]+")");
		}
		String sky2=psk.processSkylineUsingDom(query,thresh4[0],attr,pref,0);	
		System.out.println("sky2: "+ thresh4[0]);
		Misc.executeShellCommand("rm "+thresh4[0]);
		String sky3=psk.processSkylineUsingDom(query,thresh5[0],attr,pref,1);
		System.out.println("sky3: "+ sky3);
		Misc.executeShellCommand("rm "+thresh5[0]);
		String sky4=psk.processSkylineUsingDom(query,thresh6[0],attr,pref,0);
		System.out.println("sky4: "+ sky4);
		Misc.executeShellCommand("rm "+thresh6[0]);
		String sky5=psk.processSkylineUsingDom(query,thresh7[0],attr,pref,2);
		System.out.println("sky5: "+ sky5);
		Misc.executeShellCommand("rm "+thresh7[0]);
		String sky6=psk.processSkylineUsingDom(query,thresh8[0],attr,pref,0);	
		System.out.println("sky6: "+ sky6);
		Misc.executeShellCommand("rm "+thresh8[0]);
		String sky7=psk.processSkylineUsingDom(query,thresh9[0],attr,pref,0);	
		System.out.println("sky7: "+ sky7);
		Misc.executeShellCommand("rm "+thresh9[0]);
		String outputfile=Misc.getFileName("outputs");
		// System.out.println(file1+" "+file2+" "+file3+" > "+outputfile);
		String command="cat "+sky1[0]+" "+sky2+" "+sky3+" "+sky4+" "+sky5+" "+sky6+" "+sky7+" > "+outputfile;
		//String command="cat "+sky1[0]+" "+sky2+" "+sky3+" "+sky4+" "+" "+sky6+" "+" > "+outputfile;
		agg.executeShellCommand(command);
		Misc.executeShellCommand("rm "+sky1[0]+" "+sky2+" "+sky3+" "+sky4+" "+sky5+" "+sky6+" "+sky7);
		return outputfile;
	}

	@SuppressWarnings("unchecked")
	String iterative(Query query) throws IOException, InterruptedException
	{
		Skyline sk=new Skyline();
		Pskyline psk = new Pskyline();
		String temprel[][]=new String[query.n][2];
		String tempre[][]=new String[query.n][2];
		String nonFull[]=new String[query.n];
		String full[]=new String[query.n];
		Vector<String> rel[]=new Vector[2];

		String skyjoin[][];
		for(int i=0;i<query.n;i++)
		{
			// find full skyline
			{
				int attr[]=new int[query.rel[i].numattr];
				int pref[]=new int[query.rel[i].numattr];
				for(int j=0;j<query.rel[i].numattr;j++)
				{
					attr[j]=j;
					pref[j]=query.rel[i].attr[j].pref;
				}
				temprel[i]=sk.processSkyline(query.rel[i].name,"",attr,pref);
				full[i] = temprel[i][0];  //storing Af
				nonFull[i]= temprel[i][1]; //storing Af'
			}
			int THRESHOLD=2;
			rel[i]=new Vector<String>();
			boolean iter=false;int iteration=0;
			do
			{
				iteration++;
				if(iter)
					temprel[i][0]=temprel[i][1];
				//String temp1=temprel[i][0];
				temprel[i]=sk.processSkyline(temprel[i][0],"",query.rel[i].lattr,query.rel[i].lpref);
				rel[i].add(temprel[i][0]); //Afl and Afl'l
				iter=true;
			}while(iteration<THRESHOLD);
			rel[i].add(temprel[i][1]); //Afl'l'
			tempre[i]=sk.processSkyline(nonFull[i],"",query.rel[i].lattr,query.rel[i].lpref);
			rel[i].add(tempre[i][0]); //Af'l
			rel[i].add(tempre[i][1]); //Af'l'
		}
		int gattr[]=new int[query.rel[1].gattr.length];
		for(int i=0;i<gattr.length;i++)
			gattr[i]=query.rel[1].gattr[i]+query.rel[0].numattr; //gattr[i]=query.rel[1].gattr[i]+query.rel[0].nl+query.rel[0].ng;
		int attr[]=new int[query.rel[0].nl+query.rel[1].nl+query.gop.length];
		int pref[]=new int[query.rel[0].nl+query.rel[1].nl+query.gop.length];        
		int totalNumAttr = query.rel[0].numattr + query.rel[1].numattr;
		int totalNumLocalAttr = query.rel[0].nl + query.rel[1].nl;
		for(int i=0;i<attr.length;i++)
		{
			if(i<query.rel[0].nl)
			{
				attr[i] = query.rel[0].lattr[i];
				pref[i] = query.rel[0].lpref[i];
			}
			else if(i<totalNumLocalAttr)
			{
				attr[i] = query.rel[1].lattr[i-query.rel[0].nl] + query.rel[0].numattr;
				pref[i] = query.rel[1].lpref[i-query.rel[0].nl];
			}
			else
			{
				attr[i] = totalNumAttr + i-totalNumLocalAttr;
				pref[i] = query.rel[1].gpref[i-totalNumLocalAttr];
			}    
			System.out.println("("+attr[i] +","+pref[i]+")");
		}

		skyjoin=new String[rel[0].size()][rel[1].size()];

		String rela[][] = new String[query.n][5];
		for(int i=0;i<query.n;i++)
		{
			rela[i][0] = rel[i].elementAt(0); //Afl
			rela[i][1] = agg.doUnion(rel[i].elementAt(0), rel[i].elementAt(1),nonFull[i]); //Afl U Afl'l U Af'
			rela[i][2] = query.rel[i].name; //A
			rela[i][3] = agg.doUnion(full[i], rel[i].elementAt(3), ""); //Af U Af'l
			rela[i][4] = query.rel[i].name; //A
		}

		for(int i=0;i<rel[0].size();i++)
		{
			for(int j=0;j<rel[1].size();j++)
			{
				String temp = join.doJoin(rel[0].elementAt(i),rel[1].elementAt(j),query.rel[0].jattr,query.rel[1].jattr,query.jop);
				String pasjq[] = agg.threshAggregate(query, temp, query.rel[0].gattr,gattr,query.gop, true);
				if((i==0 && j<3) || (i<3 && j==0) )
				{
					skyjoin[i][j] = pasjq[0];
				}
				else
				{
					String base = join.doJoin(rela[0][i], rela[1][j], query.rel[0].jattr,query.rel[1].jattr,query.jop);
					base = agg.doAggregate(base, query.rel[0].gattr,gattr,query.gop);
					skyjoin[i][j]=psk.processSkyline(query, pasjq[0], base, attr, pref, query.p,0);
				}                
			}
		}
		String asjq=Misc.getFileName(Misc.outputs),files="";
		for(int i=0;i<rel[0].size();i++)
			for(int j=0;j<rel[1].size();j++)
				files += skyjoin[i][j]+" ";
		agg.executeShellCommand("cat "+files+" > "+asjq);
		return asjq;
	}
	}