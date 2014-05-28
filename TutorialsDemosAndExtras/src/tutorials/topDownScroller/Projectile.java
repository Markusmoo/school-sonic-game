package tutorials.topDownScroller;

import game.engine.GameLayer;

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
			
			setRealisationAndGeometry("Energy")
		}
	}
	
	
}
