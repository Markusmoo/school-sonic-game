package tutorials.topDownScroller;

import game.assets.ImageAssetSequence;
import game.engine.GameLayer;
import game.physics.Body;

public class Projectile extends Body{

	protected enum ProjectileState{Move,Hit,Disappear}
	protected ProjectileState projectileState = ProjectileState.Move;
	
	protected double projectileDamage;
	
	private double projectileAcceleration;
	private double projectileMaxSpeed;
	private long projectileMaxTime;
	
	private long projectileTriggerTime;
	
	private String hitAsset;
	private String disappearAsset;
	
	public Projectile(String projectileType, double x, double y, double rotation, GameLayer gameLayer){
		super(gameLayer,x,y);
		
		this.rotation = rotation;
		
		defineWeaponType(projectileType);
	}
	
	private void defineWeaponType(String weaponType){
		if(weaponType.equals("Bullet")){
			projectileMaxSpeed = 1000.0;
			projectileAcceleration = 100.0;
			projectileDamage = 100.0;
			
			projectileMaxTime = 2000;
			projectileTriggerTime = System.nanoTime();
			
			setRealisationAndGeometry("EnergyBall");
			hitAsset = "EnergyBallExplode";
			disappearAsset = "EnergyBallDisappear";
			
			setMass(5.0);
			restitution = 1.0;
		}
	}
	
	public void triggerProjectileHit(){
		projectileState = ProjectileState.Hit;
		
		setRealisation(hitAsset);
		
		velocityx = 0.0;
		velocityy = 0.0;
		
		canIntersectOtherGraphicalObjects = false;
		
		projectileTriggerTime = System.nanoTime();
	}
	
	private void triggerProjectDisappear(){
		projectileState = ProjectileState.Disappear;
		
		setRealisation(disappearAsset);
		
		canIntersectOtherGraphicalObjects = false;
		
		projectileTriggerTime = System.nanoTime();
	}
	
	public void update(){
		if(getRealisation(0) instanceof ImageAssetSequence)
			getRealisation(0).update();
		
		if(projectileState != ProjectileState.Move){
			if((System.nanoTime() - projectileTriggerTime)/1000000L > ((ImageAssetSequence)getRealisation(0)).getAnimationPeriod())
					gameLayer.queueGameObjectToRemove(this);
		}
		
		if(projectileState != ProjectileState.Hit){
			if(velocityx*velocityx + velocityy*velocityy < projectileMaxSpeed*projectileMaxSpeed){
				velocityx += projectileAcceleration * Math.sin(rotation);
				velocityy -= projectileAcceleration * Math.cos(rotation);
			}
		
			if(projectileState == ProjectileState.Move)
				if((System.nanoTime() - projectileTriggerTime)/1000000L > projectileMaxTime)
					triggerProjectDisappear();
		}
	}
}
