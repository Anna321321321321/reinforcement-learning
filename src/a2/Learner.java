package a2;


import java.util.Random;


public class Learner   
{   
  public static final double LearningRate =0.3;  // 0.1
  public static final double DiscountRate =0.9 ;   //0.9
  public static double explorationRate ;  //0.5 
  private int lastState;   
  private int lastAction;   
  private boolean first = true;   
  private LUT table;   
   
  public Learner(LUT table)   
  {   
    this.table = table;   
  }   
   
  public void learn(int state, int action, double reward)   
  {   
    if (first)   
      first = false;   
    else   
    {   
      double oldQValue = table.getQValue(lastState, lastAction);   
      double newQValue = (1 - LearningRate) * oldQValue + LearningRate * (reward + DiscountRate * table.max_Q(state));    
      table.setQValue(lastState, lastAction, newQValue);  
      System.out.println(newQValue+" ,action: "+action);
      if(newQValue==0) {
    	  System.out.println("newQValue:"+newQValue+" oldQValue:"+oldQValue+" reward:"+reward+" table.max_Q(state):"+table.max_Q(state));
      }
      if(Math.abs(newQValue)<0.001) {
    	  System.out.println("newQValue:"+newQValue+" oldQValue:"+oldQValue+" reward:"+reward+" table.max_Q(state):"+table.max_Q(state));
      }
    }   
    lastState = state;   
    lastAction = action;       
  }   
  
  public void learnSARSA(int state, int action, double reward) {
		if (first)
			first = false;
		else {
			double oldQValue = table.getQValue(lastState, lastAction);
			
			double newQValue = (1 - LearningRate) * oldQValue
					+ LearningRate * (reward + DiscountRate * table.getQValue(state, action));
			
			table.setQValue(lastState, lastAction, newQValue);
		}
		lastState = state;
		lastAction = action;
	}
   
  public int selectAction(int state)
  {

		double thres = Math.random();
		
		int actionIndex = 0;
		
		if (thres<explorationRate)
		{//randomly select one action //from action(0,1,2)
			//Random ran = new Random();
			actionIndex = (int)(Math.random()*(Action.numActions-1 - 0 + 1));
		}
		else
		{//e-greedy
			actionIndex=table.getBestAction(state);
		}
		return actionIndex;
	}
 
}  
