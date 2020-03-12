package a2;

import static robocode.util.Utils.normalRelativeAngleDegrees;

import java.awt.Color;

import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;
import robocode.*;
public class hihi extends AdvancedRobot{
	public void run() {
		setColors(Color.green, Color.white, Color.blue);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		while(true) {			
			//ahead(100);
			//turnGunRight(360);
			//back(100);			
			//turnRadarRight(180);
			//turnGunRight(90);
			//turnRight(90);
			setAhead(10);
			setTurnRight(10);
			setTurnGunLeft(10);
			setFire(3);
			execute();			
		}
	}
	private void radarTurnRight(int i) {
		// TODO Auto-generated method stub
		
	}
	public void onScannedRobot(ScannedRobotEvent e) {
		double gunTurnAmt = normalRelativeAngleDegrees(e.getBearing() + (getHeading() - getRadarHeading()));
		turnGunRight(gunTurnAmt); // Try changing these to setTurnGunRight,
		fire(3);
	}

	/**
	 * onHitRobot:  If it's our fault, we'll stop turning and moving,
	 * so we need to turn again to keep spinning.
	 */
	public void onHitRobot(HitRobotEvent e) {
		if (e.getBearing() > -10 && e.getBearing() < 10) {
			fire(3);
		}
		if (e.isMyFault()) {
			turnRight(10);
		}
	}
}


