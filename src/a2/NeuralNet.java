package a2;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class NeuralNet implements NeuralNetInterface{
	int argNumInputs;
	int argNumHidden;
	double argLearningRate; 
	double argMomentumTerm;
	double argA;
	double argB;
	double[][][]weight;
	double [][][] weightChange;
	public NeuralNet ( 
		int argNumInputs,
		int argNumHidden,
		double argLearningRate, 
		double argMomentumTerm, 
		double argA,
		double argB ) {
		this.argNumInputs=argNumInputs;
		this.argNumHidden=argNumHidden;
		this.argLearningRate=argLearningRate;
		this.argMomentumTerm=argMomentumTerm; 
		this.argA=argA;
		this.argB=argB;
		this.weight=new double[2][][];
		this.weight[0]=new double[argNumInputs+1][argNumHidden+1];
		this.weight[1]=new double[argNumHidden+1][1];
		this.weightChange=new double[2][][];
		this.weightChange[0]=new double[argNumInputs+1][argNumHidden+1];
		this.weightChange[1]=new double[argNumHidden+1][1];
	}

	public double outputFor(double[] X) {		
		return 0;
	}
	
	public double train(double[] X, double argValue,String type) {
		double yhat=forward(double[] X, String type);
		if(type=="binary") {
			//yhat=sigmoid(sum);
			for(int i=0;i<argNumHidden+1;i++) {
				delta[i]=yhat*(1-yhat)*(argValue-yhat);				
				weight[1][i][0]+=argLearningRate*delta[i]*h[i]+argMomentumTerm*weightChange[1][i][0];
				weightChange[1][i][0]=argLearningRate*delta[i]*h[i]+argMomentumTerm*weightChange[1][i][0];
				System.out.println(weightChange[1][i][0]);
			}
			for(int i=0;i<argNumInputs+1;i++) {
				for(int j=0;j<argNumHidden+1;j++) {
					weight[0][i][j]+=argLearningRate*h[j]*(1-h[j])*delta[j]*weight[1][j][0]*input[i]+argMomentumTerm*weightChange[0][i][j];
					weightChange[0][i][j]=argLearningRate*h[j]*(1-h[j])*delta[j]*weight[1][j][0]*input[i]+argMomentumTerm*weightChange[0][i][j];
				}
			}
		}
		else {
			//yhat=customSigmoid(sum);//customSigmoid
			for(int i=0;i<argNumHidden+1;i++) {
				delta[i]=(argB-argA)*0.25*(1-Math.pow(yhat,2))*(argValue-yhat);   //0.5*(1-Math.pow(yhat,2))*(argValue-yhat);
				//System.out.println("delta[ "+i+"]:"+delta[i]);
				weight[1][i][0]+=argLearningRate*delta[i]*h[i]+argMomentumTerm*weightChange[1][i][0];
				weightChange[1][i][0]=argLearningRate*delta[i]*h[i]+argMomentumTerm*weightChange[1][i][0];
			}
			for(int i=0;i<argNumInputs+1;i++) {
				for(int j=0;j<argNumHidden;j++) {					
					weight[0][i][j]+=argLearningRate*0.5*(1-Math.pow(h[j],2))*delta[j]*weight[1][j][0]*input[i]+argMomentumTerm*weightChange[0][i][j];
					//System.out.println("weight[0]["+i+"]["+j+"]:"+argLearningRate*0.5*(1-Math.pow(h[j],2))*delta[j]*weight[1][j][0]*input[i]);
					weightChange[0][i][j]=argLearningRate*0.5*(1-Math.pow(h[j],2))*delta[j]*weight[1][j][0]*input[i]+argMomentumTerm*weightChange[0][i][j];
					//System.out.println("weightChange[0]["+i+"]["+j+"]:"+argLearningRate*0.5*(1-Math.pow(h[j],2))*delta[j]*weight[1][j][0]*input[i]);
				}
			}
		}
		/*
		for(int i=0;i<argNumInputs;i++) {
			System.out.println(Arrays.toString(weightChange[0][i])); 
		}
		for(int i=0;i<argNumHidden;i++) {
			System.out.println(Arrays.toString(weightChange[1][i])); 
		}*/
		return Math.pow(argValue-yhat, 2);//0.5*
	}
	public double forward(double[] X, String type){
		double []h =new double[argNumHidden+1],delta=new double[argNumHidden+1];
		
		double yhat=0,error=0;
		double[] input = Arrays.copyOf(X, X.length+1);//数组扩容
		input[input.length-1]=bias;  
		for(int j=0;j<this.argNumHidden;j++) {
			for(int i=0;i<this.argNumInputs+1;i++) {
				h[j]+=input[i]*weight[0][i][j];				
			}
			if(type=="binary")
				h[j]=sigmoid(h[j]);
			else
				h[j]=sigmoidBipolar(h[j]);//customSigmoid
		}		
		h[argNumHidden]=bias;
		double sum=0;
		for(int i=0;i<argNumHidden+1;i++) {			
			sum+=h[i]*weight[1][i][0];			
		}
		return customSigmoid(sum);
	}
	
	public void save(File argFile) {
		// TODO Auto-generated method stub		
	}

	
	public void load(String argFileName) throws IOException {
		// TODO Auto-generated method stub		
	}

	
	public double sigmoidBipolar(double x) {		
		return 2 / (1 + Math.exp(-x))-1;
	}
	public double sigmoid(double x) {
		return 1 / (1 + Math.exp(-x));
	}
	public double relu(double x) {
		return 1 / (1 + Math.exp(-x));
	}
	
	public double customSigmoid(double x) {
		return (argB-argA) / (1 + Math.exp(-x))+argA;
	}
	
	public void initializeWeights() {
		for(int i=0;i<=this.argNumInputs;i++) {
			for(int j=0;j<=this.argNumHidden;j++) {
				weight[0][i][j]=Math.random()-0.5;
			}
		}
		for(int i=0;i<=this.argNumHidden;i++) {
			weight[1][i][0]=Math.random()-0.5;
		}		
	}

	
	public void zeroWeights() {
		// TODO Auto-generated method stub		
	}
	pubilc int selectActionNN(int [] state,double reward){
		int action=0;
		double qmax=Double.NEGATIVE_INFINITY;
		for(int i=0;i<7;i++){			 
			double q=forward(state,i,"bipolar")
			if q>qmax{
				qmax=q;
				action=i;			
			}
		}
		double NN_Q_new=forward(input,"bipolar");//myNet[action].outputFor(NN_current_states);
    	double error_signal = 0;
    	double old_Q=newforward(NN_last_states,NN_last_action,"bipolar");
    	error_signal = NN_alpha*(reward + NN_lambda * NN_Q_new - old_Q); //myNet[NN_last_action].outputFor(NN_last_states));
    	  
    	  
		//newRobot1.total_error_in_one_round += error_signal*error_signal/2;
		double correct_old_Q = old_Q + error_signal;
		train(NN_last_states,NN_last_action,correct_old_Q)//myNet[NN_last_action].train(NN_last_states, correct_old_Q); 
		if(Math.random() < NN_epsilon)
		{
		  action = new Random().nextInt(Action.NumRobotActions);
		}

		for(int i=0; i<5; i++)
		{
		  NN_last_states[i] = NN_current_states[i];
		}
		NN_last_action=action;
		return action;
	}
	//getHeading()/180-1,target.distance/500-1,target.bearing/180-1, reward);		
}