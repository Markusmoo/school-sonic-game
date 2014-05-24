package tutorials.isometricMapTutorial;

import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.StringTokenizer;

import game.assets.GraphicalAsset;
import game.engine.GameLayer;
import game.engine.GameObject;
import game.geometry.Box;

public class IsometricTileMap extends GameObject{
	
	public int mapWidth, mapHeight;
	public int tileWidth, tileHeight;
	
	public Tile tiles[][];
	
	public HashMap<Integer, Unit> units = new HashMap<Integer, Unit>();
	public Unit focalUnit = null;
	public GameLayer gameLayer;
	
	public IsometricTileMap(GameLayer gameLayer, String mapFile){
		super(gameLayer);
		this.gameLayer = gameLayer;
		constructMap(mapFile);
		addUnits();
	}
	
	public int convertPositionToHash(int mapX, int mapY){
		return mapX * 65536 + mapY;
	}
	
	public int getMapXFromHash(int hash){
		return hash / 65536;
	}
	
	public int getMapYFromHash(int hash){
		return hash % 65536;
	}
	
	public void constructMap(String mapName){
		try{
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.getClass().getResource(mapName).openStream()));
			
			tileWidth = Integer.parseInt(bufferedReader.readLine());
			tileHeight = Integer.parseInt(bufferedReader.readLine());
			
			mapWidth = Integer.parseInt(bufferedReader.readLine());
			mapHeight = Integer.parseInt(bufferedReader.readLine());
			
			tiles = new Tile[mapWidth][mapHeight];
			
			for(int rowIdx = 0; rowIdx < mapHeight; rowIdx++){
				String rowData = bufferedReader.readLine();
				StringTokenizer tokenizer = new StringTokenizer(rowData);
				
				for(int colIdx = 0; colIdx < mapWidth; colIdx++)
					tiles[colIdx][rowIdx] = new Tile(tokenizer.nextToken(), this);
			}
			
			while(bufferedReader.ready()){
				StringTokenizer tokenizer = new StringTokenizer(bufferedReader.readLine());
				
				int colIdx = Integer.parseInt(tokenizer.nextToken());
				int rowIdx = Integer.parseInt(tokenizer.nextToken());
				
				String tileOverlayType = tokenizer.nextToken();
				
				tiles[colIdx][rowIdx].addTileOverlay(tileOverlayType);
			}
		}catch (IOException e){
			System.out.println("Error: Can't read from: "+mapName);
		}
		
		setGeometry(new Box(0,0,mapWidth*tileWidth+tileWidth/2,mapHeight*tileHeight/2+tileHeight/2));
	}
	
	private void addUnits(){
		//TODO Fix addition of multiple units, causes this error:
		/*
		Exception in thread "Thread-4" java.lang.ArrayIndexOutOfBoundsException: -1
		at tutorials.isometricMapTutorial.Animal.update(Animal.java:46)
		at tutorials.isometricMapTutorial.IsometricTileMap.update(IsometricTileMap.java:108)
		at tutorials.isometricMapTutorial.TileMapLayer.update(TileMapLayer.java:35)
		at game.engine.GameEngine.gameUpdate(GameEngine.java:684)
		at game.engine.GameEngine.run(GameEngine.java:576)
		at java.lang.Thread.run(Unknown Source)*/
		
		
		//Unit archer = new Unit("Archer", 5, 5, this); //TODO off for debug
		Animal a1 = new Animal(this);
		//Animal a2 = new Animal(this);
		//Animal a3 = new Animal(this);
		//Animal a4 = new Animal(this);
		//Animal a5 = new Animal(this);
		
		//units.put(convertPositionToHash(5,5), archer); //TODO off for debug
		
		units.put(convertPositionToHash(a1.getX(),a1.getY()), a1);
		//units.put(convertPositionToHash(a2.getX(),a2.getY()), a2);
		//units.put(convertPositionToHash(a3.getX(),a3.getY()), a3);
		//units.put(convertPositionToHash(a4.getX(),a4.getY()), a4);
		//units.put(convertPositionToHash(a5.getX(),a5.getY()), a5);
		
		//focalUnit = archer; //TODO off for debug
		focalUnit = a1; //TODO on for debug
	}
	
	@Override
	public void update(){
		for(Integer positionHash : units.keySet())
			units.get(positionHash).update();
		
		if(focalUnit!=null){
			gameLayer.centerViewportOnPosition((this.x - this.width/2)+focalUnit.mapX * tileWidth, (this.y - this.height/2)+focalUnit.mapY*tileHeight/2, gameLayer.gameEngine.screenWidth/4, gameLayer.gameEngine.screenHeight/4);
		}
	}
	
	public void moveUnit(Unit unit, int oldMapX, int oldMapY, int newMapX, int newMapY){
		units.remove(convertPositionToHash(oldMapX,oldMapY));
		units.put(convertPositionToHash(newMapX,newMapY),unit);
	}
	
	@Override
	public void draw(Graphics2D g, int drawX, int drawY){
		for(int rowIdx = 0; rowIdx < mapHeight; rowIdx++)
			for(int colIdx = 0; colIdx < mapWidth; colIdx++){
				
				int tileDrawX = drawX + colIdx * tileWidth - (int)width/2;
				if(rowIdx%2==1)
					tileDrawX += tileWidth/2;
				
				int tileDrawY = drawY + rowIdx * tileHeight/2 - (int)height/2;
				
				int tileDrawYDrop = 0;
				if(tiles[colIdx][rowIdx].tileHeight > tileHeight)
					tileDrawYDrop = (int)((tiles[colIdx][rowIdx].tileHeight - tileHeight));
				
				GraphicalAsset[] tileImages = tiles[colIdx][rowIdx].tileImages;
				for(int imageIdx = 0; imageIdx < tileImages.length; imageIdx++){
					if((tileDrawX + tileImages[imageIdx].width/2) >= 0
							&& (tileDrawX - tileImages[imageIdx].width < gameEngine.screenWidth)
							&& (tileDrawY + tileImages[imageIdx].height/2 >= 0)
							&& (tileDrawY - tileImages[imageIdx].height/2 < gameEngine.screenWidth))
						tileImages[imageIdx].draw(g, tileDrawX+(int)tileImages[imageIdx].offsetX,tileDrawY+(int)tileImages[imageIdx].offsetY - tileDrawYDrop);
				}
				
				int positionHash = convertPositionToHash(colIdx, rowIdx);
				if(units.containsKey(positionHash)){
					Unit unit = units.get(positionHash);
					if((tileDrawX + unit.image.width/2) >= 0
							&& (tileDrawX - unit.image.width < gameEngine.screenWidth)
							&& (tileDrawY + unit.image.height/2 >= 0)
							&& (tileDrawY - unit.image.height/2 < gameEngine.screenWidth))
						unit.image.draw(g, tileDrawX+tileWidth/2-(int)unit.width/2, tileDrawY-tileHeight/3);
						
				}
			}
	}
}
