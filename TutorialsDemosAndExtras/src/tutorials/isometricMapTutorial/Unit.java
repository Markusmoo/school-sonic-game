package tutorials.isometricMapTutorial;

import java.awt.event.KeyEvent;
import game.assets.GraphicalAsset;
import game.engine.GameInputEventManager;

public class Unit {

	public String type;
	public int mapX, mapY;
	public double width = 0.0, height = 0.0;
	
	public String currentType;
	
	public GraphicalAsset image;
	protected IsometricTileMap tileMap;
	
	public Unit(String type, int mapX, int mapY, IsometricTileMap tileMap){
		this.tileMap = tileMap;
		
		this.mapX = mapX;
		this.mapY = mapY;
		
		setType(type);
	}
	
	public void setType(String type){
		if(type.equals("Archer")){
			image = tileMap.gameLayer.assetManager.retrieveGraphicalAsset("Archer");
		}
		
		currentType = type;
		
		width = image.width;
		height = image.height;
	}
	
	public void update(){
		GameInputEventManager inputEvent = tileMap.gameLayer.inputEvent;
		
		int newMapX = mapX, newMapY = mapY;
		
		if(inputEvent.keyTyped(KeyEvent.VK_UP)){
			if(tileMap.tiles[mapX+(mapY%2==1?1:0)][mapY-1].isPassable){
				newMapX += (mapY%2==1?1:0);
				newMapY--;
			}
		}else if(inputEvent.keyTyped(KeyEvent.VK_DOWN)){
			if(tileMap.tiles[mapX-(mapY%2==0?1:0)][mapY+1].isPassable){
				newMapX -= (mapY%2==0?1:0);
				newMapY++;
			}
		}
		if(inputEvent.keyTyped(KeyEvent.VK_LEFT)){
			if(tileMap.tiles[mapX-(mapY%2==0?1:0)][mapY-1].isPassable){
				newMapX -= (mapY%2==0?1:0);
				newMapY--;
			}
		}else if(inputEvent.keyTyped(KeyEvent.VK_RIGHT)){
			if(tileMap.tiles[mapX+(mapY%2==1?1:0)][mapY+1].isPassable){
				newMapX += (mapY%2==1?1:0);
				newMapY++;
			}
		}
		
		if(newMapX != mapX || newMapY != mapY){
			tileMap.moveUnit(this, mapX, mapY, newMapX, newMapY);
			mapX = newMapX;
			mapY = newMapY;
		}
	}
}
