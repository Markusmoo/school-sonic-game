package tutorials.isometricMapTutorial;

import java.util.Random;

public class Animal extends Unit {
	
	private static Random rand = new Random();
	private static String[] types = {"Cow","Pig","Chicken"};

	public Animal(String type, int mapX, int mapY, IsometricTileMap tileMap) {
		super(type, mapX, mapY, tileMap);
	}
	
	public Animal(String type, IsometricTileMap tileMap) {
		super(type, rand.nextInt(tileMap.mapWidth-4)+2, rand.nextInt(tileMap.mapHeight-4)+2, tileMap);
	}
	
	public Animal(IsometricTileMap tileMap) {
		super(types[rand.nextInt(types.length)], rand.nextInt(tileMap.mapWidth-4)+2, rand.nextInt(tileMap.mapHeight-4)+2, tileMap);
	}

	public void setType(String type){
		if(type.equals("Cow")){
			image = tileMap.gameLayer.assetManager.retrieveGraphicalAsset("Cow");
		}else if(type.equals("Pig")){
			image = tileMap.gameLayer.assetManager.retrieveGraphicalAsset("Pig");
		}else if(type.equals("Chicken")){
			image = tileMap.gameLayer.assetManager.retrieveGraphicalAsset("Chicken");
		}
		super.setType(type);
	}
	
	public void update(){
		if(Math.random()>0.95){
			int newMapX = mapX, newMapY = mapY;
			int moveDir = rand.nextInt(4);
			
			switch (moveDir){
				case(0):
					if(tileMap.tiles[mapX+(mapY%2==1?1:0)][mapY-1].isPassable){
						newMapX += (mapY%2==1?1:0);
						newMapY--;
					}
					break;
				case(1):
					if(tileMap.tiles[mapX-(mapY%2==0?1:0)][mapY+1].isPassable){
						newMapX -= (mapY%2==0?1:0);
						newMapY++;
					}
					break;
				case(2):
					if(tileMap.tiles[mapX-(mapY%2==0?1:0)][mapY-1].isPassable){
						newMapX -= (mapY%2==0?1:0);
						newMapY--;
					}
					break;
				case(3):
					if(tileMap.tiles[mapX+(mapY%2==1?1:0)][mapY+1].isPassable){
						newMapX += (mapY%2==1?1:0);
						newMapY++;
					}
					break;
			}
			
			if(newMapX != mapX || newMapY != mapY){
				tileMap.moveUnit(this, mapX, mapY, newMapX, newMapY);
				mapX = newMapX;
				mapY = newMapY;
			}
		}
	}
	
	public int getX(){
		return this.mapX;
	}
	
	public int getY(){
		return this.mapY;
	}
}
