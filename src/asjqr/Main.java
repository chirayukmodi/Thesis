/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package asjqr;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 *
 * @author palvali
 */
public class Main
{
	public static void main(String[] args) throws IOException, FileNotFoundException, InterruptedException
	{
		String query=Misc.pwd+"queries/query";
		AcceptQuery a=new AcceptQuery(query);
		a.acceptquery();
	}
}
class Query
{
	int n; // no. of relations
	double p; //threshold probability
	RelationProps rel[]; // properties of each relation, size: no. of relations
	int jop[]; // join operation, size: no. of join operations, 0 = ; 1 < ; 2 > ; 3 <= ; 4 >= ; 5 !=
	int gop[]; // aggregate operation, size: no. of aggregate operations, 0 + ; 1 max ; 2 avg ; 3 min
}
class RelationProps
{
	String name; // name of the dataset
	int numattr; // no. of attributes
	int nj; // no. of join attributes
	int nl; // no. of local attributes
	int ng; // no. of aggregate attributes
	int iattr; //index of identity attribute
	int pattr; //index of probability attribute

	int id; //index of identifier attribute - ChirayuKM

	int jattr[]; // indices of join attributes, size: no. of join attributes
	int lattr[]; // indices of local attributes, size: no. of local attributes
	int gattr[]; // indices of aggregate attributes, size: no. of aggregate attributes
	int lpref[];
	int gpref[];
	int jpref[];
	AttributeProps attr[]; // properties of each attribute, size: no. of attributes
}
class AttributeProps
{
	int category; // category of attribute, 0: join 1: local 2: aggregate
	int pref; // preference of attribute, 0 = ; 1 < ; 2 > ; 3 <= ; 4 >= ; 5 !=
}
class AcceptQuery
{
	Pasjq pasjq=new Pasjq();
	Query query=new Query();
	String resultset;
	FileReader r;
	BufferedReader br;
	String filename;
	int algo;

	public AcceptQuery(String input)
	{
		filename=input;

		/* INPUT FORMAT
		 * ------------
		 * <Number of Relations> <Relation Name> <Number of attributes> <Number of Join attributes>
		 * <Number of Local attributes> <Number of Aggregate attributes> <cat1> <pref1> <cat2> <pref2> ....
		 * <Relation Name> <Number of attributes> <Number of Join attributes>
		 * <Number of Local attributes> <Number of Aggregate attributes> <cat1> <pref1> <cat2> <pref2> ....
		 * <Join operation1> .... <Aggregate operation1> .... <threshold probability> <Algorithm>
		 *
		 * Description
		 * ------------
		 * category: 0-Join attribute ; 1-Local attribute ; 2-Aggregate attribute
		 * preference: 0:= ; 1:< ; 2:> ; 3:<= ; 4:>= ; 5:!=
		 * Join operation: 0:= ; 1:< ; 2:> ; 3:<= ; 4:>= ; 5:!=
		 * Aggregate operation: 0: + ; 1: avg ; 2 max ; 3 min  //shri : swapped 1 and 2 according to code
		 */
	}

	void acceptquery() throws IOException, FileNotFoundException, InterruptedException // Forms the query by accepting the details from user
	{
		r=new FileReader(filename);
		br=new BufferedReader(r);

		StringTokenizer st;
		String str;
		while((str=br.readLine())!=null)
		{
			st=new StringTokenizer(str);
			query.n=Integer.parseInt(st.nextToken()); 
			query.rel=new RelationProps[query.n];
			for(int i=0;i<query.n;i++)
			{
				query.rel[i]=new RelationProps();

				query.rel[i].name=Misc.pwd.concat("datasets/"+st.nextToken());
				query.rel[i].numattr=Integer.parseInt(st.nextToken());
				query.rel[i].nj=Integer.parseInt(st.nextToken());
				query.rel[i].nl=Integer.parseInt(st.nextToken());
				query.rel[i].ng=Integer.parseInt(st.nextToken());

				query.rel[i].attr=new AttributeProps[query.rel[i].numattr];
				query.rel[i].jattr=new int[query.rel[i].nj];
				query.rel[i].jpref=new int[query.rel[i].nj];
				query.rel[i].lattr=new int[query.rel[i].nl];
				query.rel[i].lpref=new int[query.rel[i].nl];
				query.rel[i].gattr=new int[query.rel[i].ng];
				query.rel[i].gpref=new int[query.rel[i].ng];

				int nj=0,nl=0,ng=0;
				for(int j=0;j<query.rel[i].numattr;j++)
				{
					query.rel[i].attr[j]=new AttributeProps();
					query.rel[i].attr[j].category=Integer.parseInt(st.nextToken());
					query.rel[i].attr[j].pref=Integer.parseInt(st.nextToken());

					if(query.rel[i].attr[j].category==0) //join attribute
					{
						query.rel[i].jattr[nj]=j;
						query.rel[i].jpref[nj]=query.rel[i].attr[j].pref;
						nj++;
					}
					else if(query.rel[i].attr[j].category==1) //local attribute
					{
						query.rel[i].lattr[nl]=j;
						query.rel[i].lpref[nl]=query.rel[i].attr[j].pref;
						nl++;
					}
					else if(query.rel[i].attr[j].category==2) //aggregate attribute
					{
						query.rel[i].gattr[ng]=j;
						query.rel[i].gpref[ng]=query.rel[i].attr[j].pref;
						ng++;
					}
					else if(query.rel[i].attr[j].category==3) //identity attribute
					{
						query.rel[i].iattr = j;
					}
					else if(query.rel[i].attr[j].category==4) //probability attribute
					{
						query.rel[i].pattr = j;
					}

					//Added - ChirayuKM 
					else if(query.rel[i].attr[j].category==5) //Id attribute
					{
						query.rel[i].id = j;
					}
					//End
				}
			}
			query.jop=new int[query.rel[0].nj];
			for(int i=0;i<query.jop.length;i++)
				query.jop[i]=Integer.parseInt(st.nextToken());
			query.gop=new int[query.rel[0].ng];
			for(int i=0;i<query.gop.length;i++)
				query.gop[i]=Integer.parseInt(st.nextToken());

			query.p = Double.parseDouble(st.nextToken());		//threshold probability
			algo=Integer.parseInt(st.nextToken());

			this.printQuery();
			this.sendQuery();
		}
		br.close();
	}
	void printQuery()
	{
		System.out.println("Algorithm: "+algo);
		System.out.println("Threshold Probability: "+ query.p);
		System.out.print("\nNumber of Relations: "+ query.n);
		System.out.print(" [ ");
		for(int i=0;i<query.n;i++)
		{
			System.out.print(query.rel[i].name.substring(query.rel[i].name.lastIndexOf("/")+1));
			System.out.print("(Join:"+query.rel[i].nj+", Local:"+query.rel[i].nl+",Aggregate:"+query.rel[i].ng+") ");
		}
		System.out.println("]");
		for(int i=0;i<query.n;i++)
		{
			System.out.print(query.rel[i].name.substring(query.rel[i].name.lastIndexOf("/")+1)+": ");
			//Added - ChirayuKM 
			System.out.print(" [ ");
			System.out.print("id attribute:("+query.rel[i].id+") ] [ ");
			//End
			System.out.print(" [ ");
			System.out.print("ID:("+query.rel[i].iattr+") ] [ ");
			for(int j=0;j<query.rel[i].nj;j++)
				System.out.print("Join:(col_index,pref) ("+query.rel[i].jattr[j]+","+query.rel[i].attr[query.rel[i].jattr[j]].pref+") ");
			System.out.print("] [ ");
			for(int j=0;j<query.rel[i].nl;j++)
				System.out.print("Local:(col_index,pref) ("+query.rel[i].lattr[j]+","+query.rel[i].attr[query.rel[i].lattr[j]].pref+") ");
			System.out.print("] [ ");
			for(int j=0;j<query.rel[i].ng;j++)
				System.out.print("Aggregate:(col_index,pref) ("+query.rel[i].gattr[j]+","+query.rel[i].attr[query.rel[i].gattr[j]].pref+") ");
			System.out.println("] [");
			System.out.print("Prob column: ("+query.rel[i].pattr+") ]");
		}
		System.out.println();
		System.out.print("Join Operations: ");
		for(int i=0;i<query.jop.length;i++)
			System.out.print(query.jop[i]+" ");
		System.out.println();
		System.out.print("Aggregate Operations: ");
		for(int i=0;i<query.gop.length;i++)
			System.out.print(query.gop[i]+" ");
		System.out.println();
	}

	private void sendQuery() throws FileNotFoundException, IOException, InterruptedException
	{
		switch(algo)
		{
		case 0:
			Misc.start();
			System.out.println("Resultset: "+pasjq.bruteforce(query));
			Misc.stop();
			System.out.println(Misc.totaltime);
			break;
		case 1:
			Misc.start();
			System.out.println("Resultset: "+pasjq.msc(query));
			Misc.stop();
			System.out.println(Misc.totaltime);
			break;
		case 2:
			Misc.start();
			System.out.println("Resultset: "+pasjq.dominator(query));
			Misc.stop();
			System.out.println(Misc.totaltime);
			break;
		case 3:
			Misc.start();
			System.out.println("Resultset: "+pasjq.iterative(query));
			Misc.stop();
			System.out.println(Misc.totaltime);
			break;
		default:
			Misc.start();
			System.out.println("Resultset: "+pasjq.msc(query));
			Misc.stop();
			System.out.println(Misc.totaltime);
			break;
		}
		//TODO: Remove comment
		//       Misc.executeShellCommand("sh "+Misc.pwd+"cleandata.sh");
	}
}