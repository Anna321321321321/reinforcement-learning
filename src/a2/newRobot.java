package a2;


import java.awt.*;   
import java.awt.geom.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import robocode.*;   

public class newRobot extends AdvancedRobot   
{   
	public static final double PI = Math.PI;   
	private Target target;   
	private LUT table;   
	private Learner learner; 
	double change;
	int action;
	private boolean found=false;  
	private double reward = 0.0;   
	private double firePower;   
	private int direction = 1;   
	private int winFlag = 0; 
	private static int countForWin=0;
	double rewardForWin=100;//100
	double rewardForDeath=-20;//-20
	double accumuReward=0.0;
	private static int count=0;	

	public void run()   
	{   
		
		table = new LUT();   
		loadData();   
		learner = new Learner(table);   
		target = new Target();   
		target.distance = 1000;   


		setColors(Color.red, Color.red, Color.red);   
		setAdjustGunForRobotTurn(true); //false  
		setAdjustRadarForGunTurn(true); 
		//setAdjustRadarForRobotTurn(true);  
		turnRadarRightRadians(2 * PI);   
		while (true)   
		{ 
			if(getRoundNum()<300)//()<getNumRounds()/10
			learner.explorationRate=0.0;//0.5
//			else if(getRoundNum()<getNumRounds()/5)///3
//				learner.explorationRate=0.0;//0.2
			else
				learner.explorationRate=0;
			robotMovement(); 
		}   
	}   


	private void robotMovement()   
	{   
		int state = getState();   
		int action = learner.selectAction(state); 

		learner.learn(state, action, reward);  
		reward = 0.0;   

		switch (action)   
		{   
		case Action.ahead:   
			setAhead(Action.aheadDistance);   
			break;   
		case Action.back:   
			setBack(Action.aheadDistance);   
			break;   
		case Action.aheadLeft:   
			setAhead(Action.aheadDistance);
			setTurnLeft(Action.turnDegree);      
			break;   
		case Action.aheadRight: 
			setAhead(Action.aheadDistance);
			setTurnRight(Action.turnDegree);    
			break; 
		case Action.fireOne:  
			findTargetFire();	
			if (getGunHeat() == 0) {   
				ahead(0);
				turnLeft(0);
				setFire(1);   
			}
			break;		
		case Action.fireTwo:  
			findTargetFire();	
			if (getGunHeat() == 0) {   
				ahead(0);
				turnLeft(0);
				setFire(2);   
			}
			break;	
		case Action.fireThree:  
			findTargetFire();	
			if (getGunHeat() == 0) {   
				ahead(0);
				turnLeft(0);
				setFire(3);   
			}
			break;	
		}
		execute();   
		learner.learn(state, action, change);
}    
	private void findTargetFire() {
		found=false;
		while(!found) {
			setTurnRadarLeft(360);
			execute();
		}
		double gunOffset=(getGunHeading()-getHeading())/360*2*PI-target.bearing;
		setTurnGunLeftRadians(NormaliseBearing(gunOffset));
		execute();			
	}
	
	
	private int getState()   
	{   
		int heading = State.getHeading(getHeading());  
		int XPosition = State.getXPosition(getX());
		int YPosition = State.getYPosition(getY());		
		int targetDistance = State.getTargetDistance(target.distance);   
		int targetBearing = State.getTargetBearing(target.bearing);  
		int energy = State.getEnergy(getEnergy()); 
		int state = State.Mapping[heading][targetDistance][targetBearing][XPosition][YPosition];   //[isHitWall]
		return state;   
	}   

	
	//bearing is within the -pi to pi range   
	double NormaliseBearing(double ang){

		if (ang > PI)   
			ang -= 2*PI;   
		if (ang < -PI)    
			ang += 2*PI;   
		return ang;   
	}   

	//heading within the 0 to 2pi range   
	double NormaliseHeading(double ang) {   
		if (ang > 2*PI)   
			ang -= 2*PI;   
		if (ang < 0)   
			ang += 2*PI;   
		return ang;   
	}   

	//returns the distance between two x,y coordinates   
	public double getrange( double x1,double y1, double x2,double y2 )   
	{   
		double xo = x2-x1;   
		double yo = y2-y1;   
		double h = Math.sqrt( xo*xo + yo*yo );   
		return h;   
	}   

	//gets the absolute bearing between to x,y coordinates   
	public double absbearing( double x1,double y1, double x2,double y2 )   
	{  
		double xo = x2-x1;   
		double yo = y2-y1;   
		double h = getrange( x1,y1, x2,y2 );   
		if( xo > 0 && yo > 0 )   
		{   
			return Math.asin( xo / h );   
		}   
		if( xo > 0 && yo < 0 )   
		{   
			return Math.PI - Math.asin( xo / h );   
		}   
		if( xo < 0 && yo < 0 )   
		{   
			return Math.PI + Math.asin( -xo / h );   
		}   
		if( xo < 0 && yo > 0 )   
		{   
			return 2.0*Math.PI - Math.asin( -xo / h );   
		}   
		return 0;   
	}   



	public void onBulletHit(BulletHitEvent e)   
	{  		
		change = e.getBullet().getPower() * 9;   
		out.println("Bullet Hit: " + change);   
		accumuReward += change;  
		int state = getState();   
		//int action = learner.selectAction(state);  
		//learner.learn(state, action, change); 		   
	}   


	public void onBulletMissed(BulletMissedEvent e)   
	{   
		change = -e.getBullet().getPower()*9; //no *9  
		out.println("Bullet Missed: " + change);   

		accumuReward += change;  
		int state = getState();   
		//int action = learner.selectAction(state);  
		//learner.learn(state, action, change);  
	}   

	public void onHitByBullet(HitByBulletEvent e)   
	{   
		  
		double power = e.getBullet().getPower();   
		change = -9 * power;//-(4 * power + 2 * (power - 1));   
		out.println("Hit By Bullet: " + change);   
		accumuReward += change; 
		int state = getState();   
		//int action = learner.selectAction(state);  
		//learner.learn(state, action, change);  
		 
	}   

	public void onHitRobot(HitRobotEvent e)   
	{   
		   
		change = -6.0;   
		out.println("Hit Robot: " + change);   
		accumuReward += change;   
		int state = getState();   
		//int action = learner.selectAction(state);  
		//learner.learn(state, action, change);    
	}   

	public void onHitWall(HitWallEvent e)   
	{   

		change = - 6  ;//(Math.abs(getVelocity()) * 0.5 );   
		out.println("Hit Wall: " + change);   
		accumuReward += change;    
		int state = getState();   
		//int action = learner.selectAction(state);  
		//learner.learn(state, action, change);  
	}   

	public void onScannedRobot(ScannedRobotEvent e)   
	{   		  
		found=true;
		target.bearing = e.getBearingRadians();   
		target.head = e.getHeadingRadians();   
		target.ctime = getTime();             //game time at which this scan was produced   
		target.speed = e.getVelocity();   
		target.distance = e.getDistance();   
		target.energy = e.getEnergy();  
	}   

	public void onRobotDeath(RobotDeathEvent e)   
	{   

		if (e.getName() == target.name)   
		{
			target.distance = 1000; 
		}

	}   

	public void onWin(WinEvent event)   
	{   
		winFlag=1;
		accumuReward+=rewardForWin;
		int state = getState();   
		//int action = learner.selectAction(state);  
		learner.learn(state, action, rewardForWin);  
		saveData(); 
		saveResult2(winFlag);
	}   
	
	public void onDeath(DeathEvent event)   
	{   
		winFlag=0;
		accumuReward+=rewardForDeath;
		count++;
		int state = getState();   
		//int action = learner.selectAction(state);  
		learner.learn(state, action, rewardForDeath);  

		saveData();   
		saveResult2(winFlag);
	}  
	
	public void saveResult2(int winFlag) {
		if(true) {
			File file = getDataFile("saveResult1.dat");
			PrintStream w = null; 
			try 
			{ 
				w = new PrintStream(new RobocodeFileOutputStream(file.getAbsolutePath(), true)); 
				w.println(winFlag); 
				if (w.checkError()) 
					System.out.println("Could not save the data!");  //setTurnLeft(180 - (target.bearing + 90 - 30));
				w.close(); 
			}

			catch (IOException e1) 
			{ 
				System.out.println("IOException trying to write: " + e1); 
			} 
			finally 
			{ 
				try 
				{ 
					if (w != null) 
						w.close(); 
				} 
				catch (Exception e2) 
				{ 
					System.out.println("Exception trying to close witer: " + e2); 
				}
			} 
		}
	}
	 

	public void loadData()   
	{   
		try   
		{   
			table.load(getDataFile("movement1.dat"));   
		}   
		catch (Exception e)   
		{   
		}   
	}   

	public void saveData()   
	{   
		try   
		{   
			table.save(getDataFile("movement1.dat"));   
		}   
		catch (Exception e)   
		{   
			out.println("Exception trying to write: " + e);   
		}   
	}   
}  
