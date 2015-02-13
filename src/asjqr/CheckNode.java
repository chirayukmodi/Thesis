package asjqr;

public class CheckNode 
{
	private boolean flag;
	private double prob;

	public CheckNode(boolean flag, double prob)
	{
		this.flag=flag;
		this.prob=prob;
	}
	public void setFlag(boolean flag)
	{
		this.flag=flag;
	}
	public boolean getFlag()
	{
		return flag;
	}
	public void setProb(double prob)
	{
		this.prob=prob;
	}
	public double getProb()
	{
		return prob;
	}

}

