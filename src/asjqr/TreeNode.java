package asjqr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;


public class TreeNode 
{
	//Added - ChirayuKM
	private int ide;
	//End
	private int tupleNo;
	private boolean flag;
	private double prob;
	private TreeNode left, right;

	public TreeNode(int id,int tupleNo, double prob, boolean flag)
	{
		//TreeNode t = new TreeNode();
		this.tupleNo = tupleNo;
		this.left = null;
		this.right = null;
		this.prob = prob;
		this.flag = flag;

		//Added - ChirayuKM
		this.ide=id;
		//End
		//return t;		
	}

	public void addLeft(TreeNode root)
	{
		left = root;
	}

//	TreeNode getl()
//	{
//		return left;
//	}
//	TreeNode getr()
//	{
//		return right;
//	}
//	double getp()
//	{
//		return prob;
//	}
	public void addJoinedTupleToTree (int idx,int idy,int tupleNoXj, int tupleNoYj, double probX, double probY)
	{
		if(tupleNoXj == tupleNo && flag && idx==ide)
		{
			if(left != null)
			{
				if(left.tupleNo == tupleNoYj && left.flag && left.ide==idy)
				{
					left = null;
				}
				else
				{
					left.addSingleTupleToTree(idy,tupleNoYj,probY);
				}					
				if(right != null)
				{
					if(right.tupleNo == tupleNoYj && right.flag && idy==right.ide)
					{
						right = null;
					}
					else
					{
						right.addSingleTupleToTree(idy,tupleNoYj,probY);
					}	
				}
			}
			
			//Added - ChirayuKM
			else if(right != null)
			{
				if(right.tupleNo == tupleNoYj && right.flag && idy==right.ide)
				{
					right = null;
				}
				else
				{
					right.addSingleTupleToTree(idy,tupleNoYj,probY);
				}	
			}
			else
				this.addSingleTupleToTree(idy,tupleNoYj,probY);
			//End
		}
		else if(tupleNoYj == tupleNo && flag && idy==ide)
		{
			if(left != null)
			{
				if(left.tupleNo == tupleNoXj && left.flag && left.ide==idx)
				{
					left = null;
				}
				else
				{                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           
					left.addSingleTupleToTree(idx,tupleNoXj,probX);
				}					
				if(right != null)
				{
					if(right.tupleNo == tupleNoXj && right.flag && right.ide==idx)
					{
						right = null;
					}
					else
					{
						right.addSingleTupleToTree(idx,tupleNoXj,probX);
					}	
				}				
			}
			//Added - ChirayuKM
			else if(right!=null)
			{
				if(right.tupleNo == tupleNoXj && right.flag && right.ide==idx)
				{
					right = null;
				}
				else
				{
					right.addSingleTupleToTree(idx,tupleNoXj,probX);
				}	
			}
			else
				this.addSingleTupleToTree(idx,tupleNoXj,probX);
			//end
		}
		else if(tupleNoXj != tupleNo && tupleNoYj != tupleNo)
		{
			if(left!= null)
			{
				left.addJoinedTupleToTree(idx,idy,tupleNoXj, tupleNoYj, probX, probY);
				if(right != null)
					right.addJoinedTupleToTree(idx,idy,tupleNoXj, tupleNoYj, probX, probY);
			}	
			//Added - ChirayuKM
			else if(right != null)
			{
				right.addJoinedTupleToTree(idx,idy,tupleNoXj, tupleNoYj, probX, probY);
			}
			//End
			else
			{
				TreeNode node1 = new TreeNode(idx,tupleNoXj, probX, true);
				TreeNode node2 = new TreeNode(idx,tupleNoXj, 1.0 - probX, false);
				TreeNode node3 = new TreeNode(idy,tupleNoYj, 1.0 - probY, false);
				left = node1;
				right = node2;
				node1.left = node3;
			}			
		}
		//Added - ChirayuKM
		else if(tupleNoXj == tupleNo && idx!=ide) 
		{

			if(left!= null)
			{
				left.addJoinedTupleToTree(idx,idy,tupleNoXj,tupleNoYj, probX, probY);
				if(right != null)
					right.addJoinedTupleToTree(idx,idy,tupleNoXj, tupleNoYj, probX, probY);
			}
			else if(right != null)
			{
				right.addJoinedTupleToTree(idx,idy,tupleNoXj, tupleNoYj, probX, probY);
			}
			else
			{

				TreeNode node1 = new TreeNode(idx,tupleNoXj, probX, true);
				TreeNode node2 = new TreeNode(idx,tupleNoXj, 1.0 - probX, false);
				TreeNode node3 = new TreeNode(idy,tupleNoYj, 1.0 - probY, false);
				left = node1;
				right = node2;
				node1.left = node3;
			}
		}

		else if(tupleNoYj == tupleNo && idy!=ide) 
		{

			if(left!= null)
			{
				left.addJoinedTupleToTree(idx,idy,tupleNoXj, tupleNoYj, probX, probY);
				if(right != null)
					right.addJoinedTupleToTree(idx,idy,tupleNoXj, tupleNoYj, probX, probY);
			}
			else if(right != null)
			{
				right.addJoinedTupleToTree(idx,idy,tupleNoXj, tupleNoYj, probX, probY);
			}
			else
			{
				TreeNode node1 = new TreeNode(idx,tupleNoXj, probX, true);
				TreeNode node2 = new TreeNode(idx,tupleNoXj, 1.0 - probX, false);
				TreeNode node3 = new TreeNode(idy,tupleNoYj, 1.0 - probY, false);
				left = node1;
				right = node2;
				node1.left = node3;
			}
		}
		//End
	}

	public void addSingleTupleToTree(int id,int tupleNo, double prob)
	{
		if(tupleNo != this.tupleNo)
		{
			if(left != null)
			{
				if(left.tupleNo == tupleNo && left.flag && left.ide==id)
				{
					left = null;
				}
				else
				{
					left.addSingleTupleToTree(id,tupleNo,prob);
				}
				if(right != null)
				{
					if(right.tupleNo == tupleNo && right.flag && right.ide==id)
					{
						right = null;
					}
					else
					{
						right.addSingleTupleToTree(id,tupleNo,prob);
					}	
				}
			}
			//Added - ChirayuKM
			else if(right != null)
			{
				if(right.tupleNo == tupleNo && right.flag && right.ide==id)
				{
					right = null;
				}
				else
				{
					right.addSingleTupleToTree(id,tupleNo,prob);
				}	
			}	
			//End
			else
			{
				TreeNode node1 = new TreeNode(id,tupleNo, 1.0 - prob, false);
				left = node1;
			}
		}
		//Chirayu Added
		else if(tupleNo == this.tupleNo && ide!=id)
		{
			if(left != null)
			{
				if(left.tupleNo == tupleNo && left.flag && left.ide==id)
				{
					left = null;
				}
				else
				{
					left.addSingleTupleToTree(id,tupleNo,prob);
				}
				if(right != null)
				{
					if(right.tupleNo == tupleNo && right.flag && right.ide==id)
					{
						right = null;
					}
					else
					{
						right.addSingleTupleToTree(id,tupleNo,prob);
					}	
				}
			}
			else if(right != null)
			{
				if(right.tupleNo == tupleNo && right.flag && right.ide==id)
				{;
					right = null;
				}
				else
				{
					right.addSingleTupleToTree(id,tupleNo,prob);
				}	
			}				
			else
			{
				TreeNode node1 = new TreeNode(id,tupleNo, 1.0 - prob, false);
				left = node1;
			}
		}//End
	}

	//Added - ChirayuKM
	public void computeSkylineProbability(HashMap<Integer, ArrayList<CheckNode>> hm)
	{
		if(!hm.containsKey(this.tupleNo))
		{
			ArrayList<CheckNode> Al = new ArrayList<CheckNode>();
			Al.add(new CheckNode(this.flag,1-this.prob));
			hm.put(this.tupleNo,Al);
		}
		else
		{
			ArrayList<CheckNode> temp=hm.get(this.tupleNo);
			CheckNode ck = temp.get(temp.size()-1);
			if(ck.getFlag()==false && this.flag==false)
				temp.add(new CheckNode(this.flag,ck.getProb()+(1-this.prob)));
			else if(ck.getFlag()==false && this.flag==true)
				temp.add(new CheckNode(this.flag,1-this.prob));
			else if(ck.getFlag()==true && this.flag==true)
				temp.add(new CheckNode(this.flag,1));
		}

		if(left != null)
		{
			left.computeSkylineProbability(hm);
			if(right != null)
			{
				right.computeSkylineProbability(hm);
			}

			if(hm.get(this.tupleNo).size()==1)
				hm.remove(this.tupleNo);
			else if(hm.get(this.tupleNo).size()>1)
				hm.get(this.tupleNo).remove(hm.get(this.tupleNo).size()-1);
		}

		else if(right!=null)
		{
			right.computeSkylineProbability(hm);
			if(hm.get(this.tupleNo).size()==1)
				hm.remove(this.tupleNo);
			else if(hm.get(this.tupleNo).size()>1)
				hm.get(this.tupleNo).remove(hm.get(this.tupleNo).size()-1);
		}

		else
		{	
			double TempProb=1.0;
			for (Entry<Integer, ArrayList<CheckNode>> entry : hm.entrySet())
			{
				TempProb=TempProb * (1- entry.getValue().get(entry.getValue().size()-1).getProb());
			}
			Misc.probGlobal = Misc.probGlobal + TempProb;

			if(hm.get(this.tupleNo).size()==1)
				hm.remove(this.tupleNo);
			else if(hm.get(this.tupleNo).size()>1)
				hm.get(this.tupleNo).remove(hm.get(this.tupleNo).size()-1);
		}
	}
	//End
	
//	public void computeSkylineProbability(double prob)
//	{
//		prob = prob * this.prob;
//		if(left != null)
//		{
//			left.computeSkylineProbability(prob);
//			if(right != null)
//			{
//				right.computeSkylineProbability(prob);
//			}
//		}
//		//Added by Chirayu
//		else if(right!=null)
//			right.computeSkylineProbability(prob);
//		//End
//		else
//		{
//			Misc.probGlobal = Misc.probGlobal + prob;
//		}
//	}
}
