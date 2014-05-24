package tutorials.isometricMapTutorial;

import java.awt.Color;
import java.awt.Graphics2D;

import game.engine.GameEngine;
import game.engine.GameLayer;

public class TileMapLayer extends GameLayer{

	public TileMapLayer(GameEngine gameEngine, String mapFile){
		super("TileMapLayer", gameEngine);
		createMap(mapFile);
	}

	private void createMap(String mapFile) {
		IsometricTileMap isometricTileMap = new IsometricTileMap(this, mapFile);
		isometricTileMap.setName("TileMap");
		
		double mapWidth = isometricTileMap.width;
		double mapHeight = isometricTileMap.height;
		
		if(width < mapWidth) width = mapWidth;
		if(height < mapHeight) height = mapHeight;
		
		isometricTileMap.x = mapWidth/2;
		isometricTileMap.y = mapHeight/2;
		
		addGameObject(isometricTileMap);
	}
	
	@Override
	public void update(){
		IsometricTileMap isometricTileMap = (IsometricTileMap)getGameObject("TileMap");
		IsometricTileMap.update();
	}
	
	@Override
	public void draw(Graphics2D g){
		Color orgColour = g.getColor();
		g.setColor(Color.black);
		g.fillRect(0, 0, gameEngine.screenWidth, gameEngine.screenHeight);
		g.setColor(orgColour);
		
		super.draw(g);
	}

}
