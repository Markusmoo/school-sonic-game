package tutorials.topDownScroller;
import game.engine.*;
import game.physics.*;
import game.geometry.*;

import java.awt.event.KeyEvent;
import java.util.*;

public class Plane extends Body {
	

	public enum ForwardAcceleration{Positive, Negative, None }
	protected ForwardAcceleration
	forwardAccelerationType = ForwardAcceleration.None;
	
	public enum SidewayAcceleration{Left, Right, None }
	protected SidewayAcceleration
	SidewayAccelerationType = SidewayAcceleration.None;
	
	protected double forwardAcceleration;
	protected double maxForwardVelocity;
	
	protected double backwardAcceleration;
	protected double maxBackwardVelocity;
	
	protected double sidewayAcceleration;
	protected double maxSidewaysVelocity;
	
	protected double dampeningVelocity;
	
	
	public class Weapon{
		double xOffset, yOffset;
		double rotation;
		long fireDelay = 1000, lastFireTime = 0;
		String projectileType;
		
		public Weapon(String type, double xOffset, double yOffset, double rotation){
			this.projectileType = type;
			this.xOffset = xOffset;
			this.yOffset = yOffset;
			this.rotation = rotation;
		}
	}
	
	protected ArrayList<Weapon> planeWeapon = new ArrayList<Weapon>();
	
	protected int initialHitPoints;
	protected int currentHitPoints;
	
	public Plane(GameLayer gameLayer){
		super(gameLayer);
		definePlane();
	}
	
	protected void definePlane(){
		setRealisationAndGeometry("Plane");
		setGeometry(new Shape[]{
				new Box(0,0,38,140), new Box(0,0,117,31)});
		
		maxForwardVelocity= 500.0;
		maxBackwardVelocity=500.0;
		maxSidewaysVelocity=500.0;
		
		forwardAcceleration = 30.0;
		backwardAcceleration = 30.0;
		sidewayAcceleration=30.0;
		
		dampeningVelocity=10.0;
		
		currentHitPoints = initialHitPoints = 1000;
		
		planeWeapons.add(new Weapon("Bullet",-50,-90,-0.3));
		planeWeapons.add(new Weapon("Bullet",-40,-95,0.0));
		planeWeapons.add(new Weapon("Bullet",40,-95,0.));
		planeWeapons.add(new Weapon("Bullet",50,-90,0.3));
		
		setMass(100.0);
		
		
		
	}
	
	public void setForwardAcceleration(ForwardAcceleration forwardAcclerationType){
	this.forwardAccelerationType = forwardAccelerationType;
	
	}
	
	public void setSidewayAcceleration(
			SidewayAcceleration sidewayAccelerationType){
		this.sidewayAccelerationType = sidewayAccelerationType;
	}
	
	public void updatePlayerInput(){
		if(inputEvent.keyPressed[KeyEvent.VK_UP]
				&& !inputEvent.keyPressed[KeyEvent.VK_DOWN])
			setForwardAcceleration(Plane.ForwardAcceleration.Positive);
		else if(inputEvent.keyPressed[KeyEvent.VK_LEFT]
				&& !inputEvent.keyPressed[KeyEvent.VK_RIGHT])
			setSidewayAcceleration(Plane.SidewayAcceleration.Left);
		else 
			setSidewayAcceleration(Plane.SidewayAcceleration.None);
		
		if(inputEvent.keyPressed[KeyEvent.VK_SPACE]== true)
			considerFiring();
	}
}
