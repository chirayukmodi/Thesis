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
import java.util.StringTokenizer;
import java.util.Map.Entry;

//import asjqr.test.TreeNode;

/**
 *
 * @author palvali
 */
public class Pskyline
{
	HashMap<Integer,Double>HM = new HashMap<Integer,Double>();
	double T;
	String sortOnEntropy(String inputFile, int attr[], int pref[]) throws IOException, InterruptedException, FileNotFoundException, IOException
	{
		String inputTemp=Misc.getFileName(Misc.temps);

		BufferedReader br=new BufferedReader(new FileReader(inputFile));
		BufferedWriter bwt=new BufferedWriter(new FileWriter(inputTemp));
		String str;
		int numattr=0;
		while((str=br.readLine())!=null)
		{
			StringTokenizer st=new StringTokenizer(str, " ");
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
				/*else
            		System.out.println("Invalid preference "+pref[j]+" to "+attr[j]);*/
			}                
			entropy = Math.log(entropy);
			a[i]=entropy;
			String record="";
			for(int j=0;j<a.length-2;j++)
				record += a[j]+" "; //record += (int)a[j]+",";

			record = record+a[a.length-2]+" "+ a[a.length-1];
			bwt.write(record+"\n");
			numattr=i+1;
		}
		bwt.flush();
		bwt.close();
		br.close();
		String tempfile=Misc.getFileName(Misc.temps);
		Misc.executeShellCommand("sort -t' ' -k"+numattr+" -n "+inputTemp+" >"+tempfile);
		return tempfile;
	}

//	void inorder(TreeNode r)
//	{
//		if(r==null)return;
//		inorder( r.getl() );
//		System.out.print(r.getp()+" ");
//		inorder( r.getr() );
//	}
//	void preorder(TreeNode r)
//	{
//		if(r==null)return;
//		System.out.print(r.getp()+" ");
//		preorder( r.getl() );
//		preorder( r.getr() );
//	}
	String processSkyline(Query query,String inputfile, String base, int attr[], int pref[], double p, int flag) throws IOException, InterruptedException
	{
		inputfile=sortOnEntropy(inputfile,attr,pref);
		System.out.println("Entropy File: " + inputfile);
		if(!base.isEmpty()) base=sortOnEntropy(base,attr,pref);
		String str;
		String baseRec;
		BufferedReader br1=new BufferedReader(new FileReader(inputfile));
		BufferedReader br2;
		String outputfile;
		if(flag==1) outputfile=Misc.getFileName(Misc.outputs);
		else outputfile=Misc.getFileName(Misc.temps);
		BufferedWriter bw=new BufferedWriter(new FileWriter(outputfile));
		int code=0;
		double entropy,tempProb1,tempProb2,tempProb=1;
		while((str=br1.readLine())!=null)
		{
			//System.out.println("Row1: " + str);
			String [] entryStr=str.split(" ");
			int l1 = entryStr.length;
			//Create Root Node
			int tupleNoX = (int)Double.parseDouble(entryStr[query.rel[0].iattr]);
			tempProb1 = Double.parseDouble(entryStr[query.rel[0].pattr]);
			//Added - ChirayuKM 
			int id1 = (int)Double.parseDouble(entryStr[query.rel[0].id]);
			//End
			//Updated - ChirayuKM 
			TreeNode root1 = new TreeNode(id1,tupleNoX, tempProb1, true);
			//End

			int tupleNoY = (int)Double.parseDouble(entryStr[query.rel[1].iattr + query.rel[0].numattr]);
			tempProb2 = Double.parseDouble(entryStr[query.rel[1].pattr + query.rel[0].numattr]);
			//Added - ChirayuKM
			int id2 = (int)Double.parseDouble(entryStr[query.rel[1].id + query.rel[0].numattr]);
			//End
			//Updated - ChirayuKM
			TreeNode root2 = new TreeNode(id2,tupleNoY, tempProb2, true);
			//End

			root1.addLeft(root2);
			tempProb = tempProb1*tempProb2;
		//	System.out.println(id1+","+id2+","+tupleNoX+","+tupleNoY+","+tempProb+" is dominated by :");
			entropy = Double.parseDouble(entryStr[l1-1]);
			if((tempProb1*tempProb2) > p){	
				if(!base.isEmpty())
					br2=new BufferedReader(new FileReader(base));				
				else
					br2=new BufferedReader(new FileReader(inputfile));
				while((baseRec=br2.readLine())!=null)
				{
					String [] entryRec=baseRec.split(" ");
					//Added - ChirayuKM
					if((int)Double.parseDouble(entryRec[query.rel[0].iattr])==(int)Double.parseDouble(entryStr[query.rel[0].iattr]) && (int)Double.parseDouble(entryRec[query.rel[1].iattr + query.rel[0].numattr]) == (int)Double.parseDouble(entryStr[query.rel[1].iattr + query.rel[0].numattr]))continue;
					if((int)Double.parseDouble(entryRec[query.rel[0].iattr])==(int)Double.parseDouble(entryStr[query.rel[0].iattr]) && (int)Double.parseDouble(entryRec[query.rel[0].id])!=(int)Double.parseDouble(entryStr[query.rel[0].id]))continue;
					if((int)Double.parseDouble(entryRec[query.rel[1].iattr + query.rel[0].numattr]) == (int)Double.parseDouble(entryStr[query.rel[1].iattr + query.rel[0].numattr]) && (int)Double.parseDouble(entryRec[query.rel[1].id + query.rel[0].numattr]) != (int)Double.parseDouble(entryStr[query.rel[1].id + query.rel[0].numattr]))continue;
					//End
					if(entropy < Double.parseDouble(entryRec[entryRec.length-1])) break; //record cannot dominate str
					code=dominates(str,baseRec,attr,pref); // 1 : str dominates record 2: record dominates str 3: neither dominates each other
					if (code==2)
					{
						//to do only need to make change here, add to tree and calc tempProb
						//Added - ChirayuKM
						HashMap<Integer, ArrayList<CheckNode>> hm = new HashMap<Integer, ArrayList<CheckNode>>();
						//End
						String entryStr1[] =baseRec.split(" ");
						l1 = entryStr1.length;
						tupleNoX = (int)Double.parseDouble(entryStr1[query.rel[0].iattr]);
						tempProb1 = Double.parseDouble(entryStr1[query.rel[0].pattr]);
						//Added - ChirayuKM
						id1=(int)Double.parseDouble(entryStr1[query.rel[0].id]);
						//End
						tupleNoY = (int)Double.parseDouble(entryStr1[query.rel[1].iattr + query.rel[0].numattr]);
						tempProb2 = Double.parseDouble(entryStr1[query.rel[1].pattr + query.rel[0].numattr]);
						//Added - ChirayuKM
						id2=(int)Double.parseDouble(entryStr1[query.rel[1].id + query.rel[0].numattr]);
						//End
						//System.out.println(tupleNoX+","+tupleNoY+","+tempProb1*tempProb2);
						//Chirayu Updated
						root1.addJoinedTupleToTree(id1,id2,tupleNoX,tupleNoY,tempProb1,tempProb2);
						//System.out.println(id1+","+id2+","+tupleNoX+","+tupleNoY+","+tempProb1+","+tempProb2);
						//inorder(root1);
						//System.out.println();
						//preorder(root1);
						//System.out.println();
						//End
						Misc.probGlobal = 0;
						//root1.computeSkylineProbability(1);
						
						//Added - ChirayuKM
						root1.computeSkylineProbability(hm);
						//End
						tempProb = Misc.probGlobal;
						//tempProb = tempProb*(1-Double.parseDouble(entryRec[entryRec.length-2]));
						if(tempProb <= p)
						break;
					}
				}
				br2.close();
				//System.out.println("Prob: "+tempProb);
			}
			if(tempProb > p){
				String newStr="";
				for(int i=0; i<l1-1; i++){
					newStr=newStr+entryStr[i]+" ";
				}	
				newStr=newStr+tempProb+"\n";
				bw.write(newStr);
			}
		}
		bw.flush();
		bw.close();
		br1.close();
		return outputfile;
	}
	
	//Added by Chirayu
	
	HashMap<Integer,ArrayList<Integer>> processSkyline_SingleRelation(Query query,String inputfile, String base, int attr[], int pref[], double p, int flag) throws IOException, InterruptedException
	{
		inputfile=sortOnEntropy(inputfile,attr,pref);
		//System.out.println("Entropy File: " + inputfile);
		if(!base.isEmpty()) base=sortOnEntropy(base,attr,pref);
		String str;
		String baseRec;
		BufferedReader br1=new BufferedReader(new FileReader(inputfile));
		BufferedReader br2;
		int code=0;
		double entropy,tempProb1,tempProb2=1;
		HashMap<Integer,ArrayList<Integer>>hm = new HashMap<Integer,ArrayList<Integer>>();
		while((str=br1.readLine())!=null)
		{
			ArrayList<Integer> temp;
			//System.out.println("Row1: " + str);
			String [] entryStr=str.split(" ");
			int l1 = entryStr.length;
			int tupleNoX = (int)Double.parseDouble(entryStr[query.rel[0].iattr]);
			int id1 = (int)Double.parseDouble(entryStr[query.rel[0].id]);
			tempProb1 = Double.parseDouble(entryStr[query.rel[0].pattr]);
			
			HashMap<Integer,Double>h=new HashMap<Integer,Double>();
			//System.out.println(id1+","+tupleNoX+","+tempProb1+"is dominated by :");
			entropy = Double.parseDouble(entryStr[l1-1]);
				if(!base.isEmpty())
					br2=new BufferedReader(new FileReader(base));				
				else
					br2=new BufferedReader(new FileReader(inputfile));
				while((baseRec=br2.readLine())!=null)
				{
					boolean flg = true;
					String [] entryRec=baseRec.split(" ");
					
					if((int)Double.parseDouble(entryRec[query.rel[0].iattr])==(int)Double.parseDouble(entryStr[query.rel[0].iattr]))continue;
					if(entropy < Double.parseDouble(entryRec[entryRec.length-1])) break; //record cannot dominate str
					
					for(int i=0;i<query.jop.length;i++)
					{
						if((int)Double.parseDouble(entryStr[query.rel[0].jattr[i]])!=(int)Double.parseDouble(entryRec[query.rel[0].jattr[i]]))
						{
							flg=false;
							break;
						}
					}
					
					if(!flg)continue;
					code=dominates(str,baseRec,attr,pref); // 1 : str dominates record 2: record dominates str 3: neither dominates each other
					if (code==2)
					{
						String entryStr1[] =baseRec.split(" ");
						l1 = entryStr1.length;
						int tupleNoY = (int)Double.parseDouble(entryStr1[query.rel[0].iattr]);
						tempProb2 = Double.parseDouble(entryStr1[query.rel[0].pattr]);
						
						if(h.containsKey(tupleNoY))
							h.put(tupleNoY,h.get(tupleNoY)+tempProb2);
						else
							h.put(tupleNoY,tempProb2);
					}
				}
				
				T = tempProb1;
				for (Entry<Integer, Double> entry : h.entrySet())
				{
					if(entry.getValue().intValue()==1)
					{
						if(hm.containsKey(tupleNoX))
						{
							temp= hm.get(tupleNoX);
							temp.add(id1);
						}
						else
						{
							temp = new ArrayList<Integer>();
							temp.add(id1);
							hm.put(tupleNoX,temp);
						}
						break;
					}
					else
						T=T*(1-tempProb2);			
				}
				
				
				
				
				br2.close();
		}
		br1.close();
//		
//		for ( Integer key : hm.keySet() ) {
//			System.out.print(key+":");
//			for(int i=0;i<hm.get(key).size();i++)
//			System.out.print(hm.get(key).get(i).intValue());
//			System.out.println();
//		}
		
		return hm;
	}
	//End
	int dominates(String str,String record,int attr[],int pref[])
	{
		StringTokenizer st1=new StringTokenizer(str," ");
		StringTokenizer st2=new StringTokenizer(record," ");
		boolean strdom=true, recdom=true;
		int i=0,j=0;
		//Arrays.sort(attr);
		while(st1.hasMoreTokens()&&st2.hasMoreTokens())
		{   
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
			if(el1 != el2 && isDominating(el1,el2,pref[j]))
				recdom=false;
			else if(el1 != el2 && isDominating(el2,el1,pref[j]))
				strdom=false;
			j++;
		}
		if(strdom && recdom)
			return 3;
		else if(strdom)
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

	String processSkyline_iter(String inputfile, String base, int attr[], int pref[], double p) throws IOException, InterruptedException
	{	
		inputfile=sortOnEntropy(inputfile,attr,pref);
		base=sortOnEntropy(base,attr,pref);
		BufferedReader br=new BufferedReader(new FileReader(inputfile));
		String outputfile=Misc.getFileName(Misc.temps);
		BufferedWriter bw=new BufferedWriter(new FileWriter(outputfile));
		BufferedReader br1;
		BufferedReader br2;
		int code=0;double entropy = 1;
		String str, baseRec;
		while((str=br.readLine())!=null){
			String [] entryStr=str.split(" ");
			int l1 = entryStr.length;
			double tempProb = Double.parseDouble(entryStr[l1-2]);
			entropy = Double.parseDouble(entryStr[l1-1]);
			if(!base.isEmpty() && tempProb >= p){	
				br1=new BufferedReader(new FileReader(base));
				while((baseRec=br1.readLine())!=null){
					String [] entryRec=baseRec.split(" ");
					if(entropy < Double.parseDouble(entryRec[entryRec.length-1])) break;
					code=dominates(str,baseRec,attr,pref); // 1 : str dominates record 2: record dominates str 3: neither dominates each other
					if (code==2){
						tempProb = tempProb*(1-Double.parseDouble(entryRec[entryRec.length-2]));
						if(tempProb < p)
							break;
					}
				}
				br1.close();
			}
			if(tempProb >=p ){
				br2=new BufferedReader(new FileReader(inputfile));
				while((baseRec=br2.readLine())!=null){
					String [] entryRec=baseRec.split(" ");
					if(entropy < Double.parseDouble(entryRec[entryRec.length-1])) break;
					code=dominates(str,baseRec,attr,pref); // 1 : str dominates record 2: record dominates str 3: neither dominates each other
					if (code==2){
						tempProb = tempProb*(1-Double.parseDouble(entryRec[entryRec.length-2]));
						if(tempProb < p)
							break;
					}
				}
				br2.close();
			}
			if(tempProb >= p){
				String newStr="";
				for(int i=0; i<l1-1; i++){
					newStr=newStr+entryStr[i]+" ";
				}	
				newStr=newStr+tempProb+"\n";
				bw.write(newStr);
			}
		}
		br.close();
		bw.flush();
		bw.close();
		return outputfile;
	}
	String processSkyline_iter(String inputfile, String base, String base1, int attr[], int pref[], double p, int flag) throws IOException, InterruptedException
	{	
		BufferedReader br=new BufferedReader(new FileReader(inputfile));
		String outputfile=Misc.getFileName(Misc.temps);
		BufferedWriter bw=new BufferedWriter(new FileWriter(outputfile));
		BufferedReader br1 = null;
		BufferedReader br2 = null;
		String str, baseRec;
		if(!base.isEmpty() && flag == 0){		
			br1=new BufferedReader(new FileReader(base));		
			while((baseRec=br1.readLine())!=null){
				String [] entryRec=baseRec.split(" ");			
				if(Double.parseDouble(entryRec[entryRec.length-1]) >= p)
					bw.write(baseRec+" "+entryRec[entryRec.length-1]+"\n");
			}
			br1.close();
		}
		base1 = sortOnEntropy(base1,attr,pref);
		base = sortOnEntropy(base,attr,pref);
		int code=0;double entropy = 1;
		while((str=br.readLine())!=null){
			String [] entryStr=str.split(" ");
			int l1 = entryStr.length;
			double tempProb = Double.parseDouble(entryStr[l1-1]);
			entropy = 1;
			for(int j=0;j<attr.length;j++)
				entropy *= Double.parseDouble(entryStr[attr[j]]);
			entropy = Math.log(entropy);
			if(!base.isEmpty() && tempProb >= p){	
				br1=new BufferedReader(new FileReader(base));
				while((baseRec=br1.readLine())!=null){
					String [] entryRec=baseRec.split(" ");
					if(entropy < Double.parseDouble(entryRec[entryRec.length-1])) break;
					code=dominates(str,baseRec,attr,pref); // 1 : str dominates record 2: record dominates str 3: neither dominates each other
					if (code==2){
						tempProb = tempProb*(1-Double.parseDouble(entryRec[entryRec.length-2]));
						if(tempProb < p)
							break;
					}
				}
				br1.close();
			}
			if(tempProb >=p ){
				br2=new BufferedReader(new FileReader(base1));
				while((baseRec=br2.readLine())!=null){
					String [] entryRec=baseRec.split(" ");
					if(entropy < Double.parseDouble(entryRec[entryRec.length-1])) break;
					code=dominates(str,baseRec,attr,pref); // 1 : str dominates record 2: record dominates str 3: neither dominates each other
					if (code==2){
						tempProb = tempProb*(1-Double.parseDouble(entryRec[entryRec.length-2]));
						if(tempProb < p)
							break;
					}
				}
				br2.close();
			}
			if(tempProb >= p){
				String newStr="";
				newStr=str+" "+tempProb+"\n";
				bw.write(newStr);
			}
		}
		br.close();
		bw.flush();
		bw.close();
		return outputfile;
	}

	
	
	
	
	//implement for string instead of input file
	//public void processSkylineForTuple(Query query, String input, String base,
	//		int[] attr, int[] pref, BufferedWriter bw) throws IOException, InterruptedException {
	//	String str=getEntropy(input,attr,pref);
	//	if(!base.isEmpty()) 
	//		base=sortOnEntropy(base,attr,pref);
	//	else 
	//	{
	//		bw.write(input);
	//		return;
	//	}
	//	String baseRec;
	//    BufferedReader br2;
	//	int code=0;
	//	double entropy,tempProb1,tempProb2,tempProb=1;
	//	String [] entryStr=str.split(" ");
	//	int l1 = entryStr.length;
	//	//Create Root Node
	//	int tupleNoX = (int)Double.parseDouble(entryStr[query.rel[0].iattr]);
	//	tempProb1 = Double.parseDouble(entryStr[query.rel[0].pattr]);
	//	TreeNode root1 = new TreeNode(tupleNoX, tempProb1, true);
	//		
	//	int tupleNoY = (int)Double.parseDouble(entryStr[query.rel[1].iattr + query.rel[0].numattr]);
	//	tempProb2 = Double.parseDouble(entryStr[query.rel[1].pattr + query.rel[0].numattr]);
	//	TreeNode root2 = new TreeNode(tupleNoY, tempProb2, true);
	//	
	//	root1.addLeft(root2);
	//	tempProb = tempProb1*tempProb2;
	//	entropy = Double.parseDouble(entryStr[l1-1]);
	//	if((tempProb1*tempProb2) > query.p){	
	//		br2=new BufferedReader(new FileReader(base));				
	//		while((baseRec=br2.readLine())!=null){
	//			String [] entryRec=baseRec.split(" ");
	//   			if(entropy < Double.parseDouble(entryRec[entryRec.length-1])) break; //record cannot dominate str
	//	        code=dominates(str,baseRec,attr,pref); // 1 : str dominates record 2: record dominates str 3: neither dominates each other
	//		    if (code==2){
	//		    	//to do only need to make change here, add to tree and calc tempProb
	//		    	entryStr=baseRec.split(" ");
	//				l1 = entryStr.length;
	//				tupleNoX = (int)Double.parseDouble(entryStr[query.rel[0].iattr]);
	//				tempProb1 = Double.parseDouble(entryStr[query.rel[0].pattr]); 
	//				tupleNoY = (int)Double.parseDouble(entryStr[query.rel[1].iattr + query.rel[0].numattr]);
	//				tempProb2 = Double.parseDouble(entryStr[query.rel[1].pattr + query.rel[0].numattr]);
	//		    	root1.addJoinedTupleToTree(tupleNoX,tupleNoY,tempProb1,tempProb2);
	//		    	Misc.probGlobal = 0;
	//		    	root1.computeSkylineProbability(1);
	//		    	tempProb = Misc.probGlobal;
	//				//tempProb = tempProb*(1-Double.parseDouble(entryRec[entryRec.length-2]));
	//				if(tempProb <= query.p)
	//					break;
	//		    }
	//		}
	//		br2.close();
	//	}
	//	if(tempProb > query.p){
	//		bw.write(input.substring(0, input.lastIndexOf(" ")) + tempProb + "\n");		
	//	}
	//}

//	private String getEntropy(String input, int[] attr, int[] pref) {
//		StringTokenizer st=new StringTokenizer(input, " ");
//		double entropy=1;
//		double a[]=new double[st.countTokens()+1];
//		int i=0;
//		while(st.hasMoreTokens())
//		{
//			a[i]=Double.parseDouble(st.nextToken());
//			i++;
//		}
//		for(int j=0;j<attr.length;j++)
//		{
//			if(pref[j]==1)
//				entropy *= a[attr[j]];
//			else if(pref[j]==2)
//				entropy *= (1/a[attr[j]]);
//			/*else
//    		System.out.println("Invalid preference "+pref[j]+" to "+attr[j]);*/
//		}                
//		entropy = Math.log(entropy);
//		a[i]=entropy;
//		String record="";
//		for(int j=0;j<a.length-2;j++)
//			record += a[j]+" "; //record += (int)a[j]+",";
//		record = record+a[a.length-2]+" "+ a[a.length-1];
//		return record;
//	}

}
