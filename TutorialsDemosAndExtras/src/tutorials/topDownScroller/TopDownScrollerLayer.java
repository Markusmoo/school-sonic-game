package tutorials.topDownScroller;

import game.assets.ImageAssetTile;
import game.engine.GameEngine;
import game.engine.GameLayer;
import game.engine.GameObject;
import game.engine.GameObjectCollection;
import game.engine.GameObjectCollider;
import game.geometry.Box;
import game.physics.Body;
import game.physics.CollisionSpace;

public class TopDownScrollerLayer extends CollisionSpace {
	
	private static final double PARALLAX_VALUE_ISLANDS = 0.4;
	private static final double PARALLAX_VALUE_CLOUDS = 0.8;
	private static final double PARALLAX_VALUE_GAMEOBJECTS = 1.0;
	
	private static final double GAME_HEIGHT = 20000;
	private static final double GAME_SCROLLSPEED = 4.0;
	
	private double gamePosition = 0;
	
	private double flyRegionOffsetX = 0;
	private double flyRegionOffsetY = 0;
	private double flyRegionWidth = gameEngine.screenWidth - 120;
	private double flyRegionHeight = gameEngine.screenHeight - 150;
	
	public TopDownScrollerLayer(GameEngine gameEngine){
		super("TopDownScrollerLayer", gameEngine);
		
		width = gameEngine.screenWidth;
		height = GAME_HEIGHT;
		
		viewPortLayerX = width/2;
		viewPortLayerY = height - gameEngine.screenHeight/2;
		
		addGameObjectCollection("Projectiles");
		addGameObjectCollection("Obstacles");
		
		createBackground();
		createObstacles();
		createPlayerPlane();
	}
	
	private void createBackground(){
		int NUM_ISLANDS = 20;
		int NUM_CLOUDS = 150;
		
		GameLayer waterLayer = new GameLayer("WaterLayer", this.gameEngine);
		waterLayer.width = gameEngine.screenWidth;
		waterLayer.height = gameEngine.screenHeight;
		
		GameObject backgroundWater = new GameObject(waterLayer);
		backgroundWater.setName("BackgroundWater");
		backgroundWater.setRealisation("BackgroundWater");
		backgroundWater.setGeometry(new Box(0,0,gameEngine.screenWidth, gameEngine.screenHeight));
		backgroundWater.setPosition(waterLayer.width/2, waterLayer.height/2);
		waterLayer.addGameObject(backgroundWater);
		
		gameEngine.addGameLayer(waterLayer);
		
		GameLayer islandLayer = new GameLayer("IslandLayer", this.gameEngine);
		islandLayer.width = gameEngine.screenWidth;
		islandLayer.height = GAME_HEIGHT * PARALLAX_VALUE_ISLANDS;
		islandLayer.viewPortLayerY = islandLayer.height - gameEngine.screenHeight/2;
		
		for(int idx = 0; idx < NUM_ISLANDS; idx++){
			GameObject island = new GameObject(islandLayer);
			island.setRealisationAndGeometry("Island");
			island.setPosition(island.width/2 + gameEngine.randomiser.nextInt((int)(islandLayer.width - island.width/2)), island.height/2 + gameEngine.randomiser.nextInt((int)(islandLayer.height - island.height/2)));
			islandLayer.addGameObject(island);
		}
		
		gameEngine.addGameLayer(islandLayer);
		islandLayer.setDrawOrder(waterLayer.getDrawOrder()+1);
		
		GameLayer cloudLayer = new GameLayer("CloudLayer", this.gameEngine);
		cloudLayer.width = gameEngine.screenWidth;
		cloudLayer.height = GAME_HEIGHT * PARALLAX_VALUE_CLOUDS;
		cloudLayer.viewPortLayerY = cloudLayer.height - gameEngine.screenHeight/2;
		
		for(int idx = 0; idx < NUM_CLOUDS; idx++){
			GameObject cloud = new GameObject(cloudLayer);
			cloud.setRealisationAndGeometry("Cloud"+(gameEngine.randomiser.nextInt(2)+1));
			cloud.setPosition(cloud.width/2 + gameEngine.randomiser.nextInt((int)(cloudLayer.width - cloud.width/2)), cloud.height/2 + gameEngine.randomiser.nextInt((int)(cloudLayer.height - cloud.height/2)));
			cloudLayer.addGameObject(cloud);
		}
		
		gameEngine.addGameLayer(cloudLayer);
		cloudLayer.setDrawOrder(islandLayer.getDrawOrder()+1);
		this.setDrawOrder(cloudLayer.getDrawOrder()+1);
	}

	 
	private void createObstacles(){
		int NUM_OBSTACLES = 40;
		
		for(int idx = 0; idx < NUM_OBSTACLES; idx++){
			Body obstacle = new Body(this);
			obstacle.setRealisationAndGeometry("BarrageBalloon");
			obstacle.restitution = 0.25;
			
			obstacle.setPosition(obstacle.width/2 + gameEngine.randomiser.nextInt((int)(this.width - obstacle.width/2)), obstacle.height/2 + gameEngine.randomiser.nextInt((int)(this.height - obstacle.height/2)));
			this.addGameObject(obstacle, "Obstacles");
		}
	}
	
	protected void createPlayerPlane(){
		Plane playerPlane = new Plane(this);
		playerPlane.setName("PlayerPlane");
		playerPlane.setPosition(gameEngine.screenWidth/2, GAME_HEIGHT -gameEngine.screenHeight/2);
		addGameObject(playerPlane);
	}
	
	public void update(){
		updateViewPort();
		updatePlayerPlane();
		updateProjectiles();
		super.update();
	}
	
	private void updateViewPort(){
		if(gamePosition + gameEngine.screenHeight > GAME_HEIGHT)return;
		
		gamePosition += GAME_SCROLLSPEED;
		viewPortLayerY -= GAME_SCROLLSPEED;
		
		gameEngine.getGameLayer("IslandLayer").viewPortLayerY -= GAME_SCROLLSPEED * PARALLAX_VALUE_ISLANDS;
		gameEngine.getGameLayer("CloudLayer").viewPortLayerY -= GAME_SCROLLSPEED * PARALLAX_VALUE_CLOUDS;
		
		GameObject backgroundWater = gameEngine.getGameObjectFromLayer("BackgroundWater", "WaterLayer");
		((ImageAssetTile)backgroundWater.getRealisation(0)).setViewPort(gameEngine.screenWidth/2, (int)(viewPortLayerY*PARALLAX_VALUE_ISLANDS));
		backgroundWater.getRealisation(0).update();
	}
	
	private void updatePlayerPlane(){
		Plane playerPlane = (Plane)getGameObject("PlayerPlane");
		if(playerPlane == null)return;
		this.testForObstacleCollision(playerPlane); //TODO Checkout source code.
		playerPlane.update();
		
		if(playerPlane.x < viewPortLayerX + flyRegionOffsetX - flyRegionWidth/2)
			playerPlane.x = viewPortLayerX + flyRegionOffsetX - flyRegionWidth/2;
		else if(playerPlane.x > viewPortLayerX + flyRegionOffsetX + flyRegionWidth/2)
			playerPlane.x = viewPortLayerX + flyRegionOffsetX + flyRegionWidth/2;
		
		if(playerPlane.y < viewPortLayerY + flyRegionOffsetY - flyRegionHeight/2)
			playerPlane.y = viewPortLayerY + flyRegionOffsetY - flyRegionHeight/2;
		else if(playerPlane.y > viewPortLayerY + flyRegionOffsetY + flyRegionHeight/2)
			playerPlane.y = viewPortLayerY + flyRegionOffsetY + flyRegionHeight/2;
	}
	
	private void updateProjectiles(){
		GameObjectCollection projectiles = getGameObjectCollection("Projectiles");
		
		for(int idx=0; idx<projectiles.size; idx++){
			Projectile projectile = (Projectile)projectiles.gameObjects[idx];
			if(this.testForObstacleCollision(projectile))
				projectile.triggerProjectileHit();
			projectile.update();
		}
	}
	
	private boolean testForObstacleCollision(GameObject object){
		GameObjectCollection obstacles = getGameObjectCollection("Obstacles");
		for(int ido = 0; ido < obstacles.size; ido++){
			GameObject obstacle = obstacles.gameObjects[ido];
			if(GameObjectCollider.isIntersection(object,  obstacle)){
				this.queueGameObjectToRemove(obstacle);
				return true;
			}
		}
		return false;
	}
}