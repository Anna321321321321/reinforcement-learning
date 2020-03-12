package a2;

import robocode.Robot;

import java.awt.*;   
import java.awt.geom.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import robocode.*; 
import a2.*;
       

public class myRobot extends AdvancedRobot{   
	public static final double PI = Math.PI;   
	private Target target;   
	private LUT table;   
	private Learner learner;   
	private PrintStream write=null;
	private double reward = 0.0;   
	private double firePower;   
	private int direction = 1;   
	private int isHitWall = 0;   
	private int isHitByBullet = 0;   
	private static int countForWin=0;
	private int winningFlag=0;
	double rewardForWin=100;
	double rewardForDeath=-100;//10
	double accumuReward=0.0;
	private static int count=0;
	private boolean found;

	public void run()   
	{   
		table = new LUT();
		loadData();   
		learner = new Learner(table);   
		target = new Target();   
		target.distance = 1000; 

		setColors(Color.red, Color.red, Color.red);   //green, Color.white, Color.blue
		setAdjustGunForRobotTurn(true);   
		setAdjustRadarForGunTurn(false); 
		setAdjustRadarForRobotTurn(true);
		turnRadarRightRadians(2 * PI);   
		
		while (true)   
		{   				
			if(getRoundNum()<getNumRounds()/5)//<getNumRounds()
				learner.explorationRate=0.5;//0.5
			else if(getRoundNum()<getNumRounds()/5)///3
				learner.explorationRate=0.2;//0.2
			else
				learner.explorationRate=0.0;
			robotMovement();  
			execute(); 
		}
	}   


	private void robotMovement()   
	{   
		int state = getState();   
		int action = learner.selectAction(state);   

		learner.learn(state, action, reward);  
		//accumuReward+=reward;
		reward = 0.0;   
		isHitWall = 0;  
		isHitByBullet = 0;  
		setTurnRadarRight(360);  
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
			break;
		}   
	}    
	private void findTargetFire() {
		found=false;
		while(!found) {
			setTurnRadarLeft(360);
			execute();
		}
		turnGunLeft(getGunHeading()-getHeading()-target.bearing);
		firePower = 3000/target.distance;
		if (firePower > 3)   
			firePower = 3; 
		if (getGunHeat() == 0) {   
			setFire(firePower);   
		} 		
	}
	private int getState()   
	{   
		int heading = State.getHeading(getHeading());  
		int XPosition = State.getXPosition(getX());
		int YPosition = State.getYPosition(getY());		
		int targetDistance = State.getTargetDistance(target.distance);   
		int targetBearing = State.getTargetBearing(target.bearing);  
		int energy = State.getEnergy(getEnergy()); 
		//out.println("State(" + heading + ", " + targetDistance + ", " + targetBearing + ", " + numDistanceToWall +  ")");  //energy 
		int state = State.Mapping[heading][targetDistance][targetBearing][XPosition][YPosition];   //[isHitWall]
		return state;   
	}   
	

	private void radarMovement()   
	{   
		double radarOffset;   
		if (getTime() - target.ctime > 4) { //if we haven't seen anybody for a bit....   
			radarOffset = 4*PI;               //rotate the radar to find a target   
		} else {   

			//next is the amount we need to rotate the radar by to scan where the target is now   
			radarOffset = getRadarHeadingRadians() - (Math.PI/2 - Math.atan2(target.y - getY(),target.x - getX()));   
			//this adds or subtracts small amounts from the bearing for the radar to produce the wobbling   
			//and make sure we don't lose the target   
			radarOffset = NormaliseBearing(radarOffset);   
			if (radarOffset < 0)   
				radarOffset -= PI/10;   
			else   
				radarOffset += PI/10; 

		}   
		//turn the radar   
		setTurnRadarLeftRadians(radarOffset);   
	}   

	private void gunMovement()   
	{   /*
		long time;   
		long nextTime;   
		Point2D.Double p;   
		p = new Point2D.Double(target.x, target.y);   
		for (int i = 0; i < 20; i++)   
		{   
			nextTime = (int)Math.round((getrange(getX(),getY(),p.x,p.y)/(20-(3*firePower))));   
			time = getTime() + nextTime - 10;   
			p = target.guessPosition(time);   
		}   
		//offsets the gun by the angle to the next shot based on linear targeting provided by the enemy class   
		double gunOffset = getGunHeadingRadians() - (Math.PI/2 - Math.atan2(p.y - getY(),p.x -  getX()));   
		setTurnGunLeftRadians(NormaliseBearing(gunOffset));   */
		
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

		if (target.name == e.getName())   
		{     
			double change = e.getBullet().getPower() * 6;   //9 A12
			out.println("Bullet Hit: " + change);  			
			accumuReward += change;  
			int state = getState();   
			int action = learner.selectAction(state);  
			learner.learn(state, action, change);  
		}   
	}   


	public void onBulletMissed(BulletMissedEvent e)   
	{   
		double change = -e.getBullet().getPower();   
		out.println("Bullet Missed: " + change);   
		accumuReward += change;  
		int state = getState();   
		int action = learner.selectAction(state);  
		learner.learn(state, action, change);  
	}   

	public void onHitByBullet(HitByBulletEvent e)   
	{   
		if (target.name == e.getName())   
		{   
			double power = e.getBullet().getPower();   
			double change = -(4 * power + 2 * (power - 1));   
			out.println("Hit By Bullet: " + change); 
			accumuReward += change; 
			int state = getState();   
			int action = learner.selectAction(state);  
			learner.learn(state, action, change);  
		}   
		isHitByBullet = 1;   
	}   

	public void onHitRobot(HitRobotEvent e)   
	{   
		if (target.name == e.getName())   
		{   
			double change = -6.0;   
			out.println("Hit Robot: " + change);  
			accumuReward += change;   
			int state = getState();   
			int action = learner.selectAction(state);  
			learner.learn(state, action, change);  
		}
		if (e.getBearing() > -10 && e.getBearing() < 10) {
			fire(3);
		}
		if (e.isMyFault()) {
			turnRight(10);
		}
	}   

	public void onHitWall(HitWallEvent e)   
	{   

		//double change = -(Math.abs(getVelocity()) * 0.5 );   
		double change=-1;
		out.println("Hit Wall: " + change);   
		accumuReward += change;   
		isHitWall = 1;   
		int state = getState();   
		int action = learner.selectAction(state);  
		learner.learn(state, action, change);  
	}   

	public void onScannedRobot(ScannedRobotEvent e)   
	{   
			
		//if ((e.getDistance() < target.distance)||(target.name == e.getName()))   
		//{   
			//the next line gets the absolute bearing to the point where the bot is   
			double absbearing_rad = (getHeadingRadians()+e.getBearingRadians())%(2*PI);   
			//this section sets all the information about our target   
			target.name = e.getName();   
			double h = NormaliseBearing(e.getHeadingRadians() - target.head);   
			h = h/(getTime() - target.ctime);   
			target.changehead = h;   
			target.x = getX()+Math.sin(absbearing_rad)*e.getDistance(); //works out the x coordinate of where the target is   
			target.y = getY()+Math.cos(absbearing_rad)*e.getDistance(); //works out the y coordinate of where the target is 
			target.bearing = e.getBearing();   
			target.head = e.getHeadingRadians();   
			target.ctime = getTime();             //game time at which this scan was produced   
			target.speed = e.getVelocity();   
			target.distance = e.getDistance();   
			target.energy = e.getEnergy();
			found=true;
			//enemyLocked = true;
			// read raw sensor input upon enemy scanned
			/*
			double absBearing=e.getBearingRadians()+getHeadingRadians();//absolute bearing of enemies 
			double latVel=e.getVelocity() * Math.sin(e.getHeadingRadians() -absBearing);//later velocity of enemies

			setTurnRadarLeftRadians(getRadarTurnRemainingRadians());//lock on the radar	
			// turn gun to face enemy
			
			double gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing- getGunHeadingRadians()+latVel/22);//amount to turn our gun, lead just a little bit
			setTurnGunRightRadians(gunTurnAmt); //turn our gun	*/		
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
		accumuReward+=rewardForWin;
		int state = getState();   
		int action = learner.selectAction(state);  
		learner.learn(state, action, rewardForWin); 		
		winningFlag=1;
		saveData(); 
		saveResult(winningFlag);
	}   

	public void onDeath(DeathEvent event)   
	{   
		accumuReward+=rewardForDeath;
		count++;
		int state = getState();   
		int action = learner.selectAction(state);  
		learner.learn(state, action, rewardForDeath);
		winningFlag=0;
		saveData(); 				
		saveResult(winningFlag);
		
	}   

	public void loadData()   
	{   
		try   
		{   
			table.load(getDataFile("movement.dat"));
		}   
		catch (Exception e)   
		{   
		}   
	}   

	public void saveData()   
	{   
		try   
		{   
			table.save(getDataFile("movement.dat"));   
		}   
		catch (Exception e)   
		{   
			out.println("Exception trying to write: " + e);   
		} 
		finally {
		}
		 
	}  
	public void saveResult(int winningFlag) {
		try{ 
			PrintStream write = new PrintStream(new RobocodeFileOutputStream(getDataFile("saveResult.dat").getAbsolutePath(),true)); //.getAbsolutePath(),true
			System.out.println(winningFlag);
			write.println(winningFlag); 
			write.close();
		}		
		catch (IOException e){ 
			   System.out.println("IOException trying to write: " + e); 
		} 
		finally{ 
			try{ 
				if (write != null) 
					write.close(); 
			} 
			catch (Exception e){ 
				      System.out.println("Exception trying to close witer: " + e); 
			} 
		}
	}
}  

