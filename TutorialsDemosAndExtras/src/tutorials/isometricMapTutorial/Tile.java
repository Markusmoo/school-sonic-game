package tutorials.isometricMapTutorial;

import game.assets.GraphicalAsset;

public class Tile {

	public boolean isPassable = true;
	public double tileHeight = 0.0, tileWidth = 0.0;
	public GraphicalAsset[] tileImages = new GraphicalAsset[0];
	private IsometricTileMap tileMap;
	
	public Tile(String baseTileType, IsometricTileMap tileMap){
		this.tileMap = tileMap;
		setBaseTile(baseTileType);
	}
	
	public void setBaseTile(String baseTileType){
		if(baseTileType.equals("DesertTile1")){
			isPassable = true;
			addTileGraphic("DesertTile1",0,0);
		}else if(baseTileType.equals("DesertTile2")){
			isPassable = true;
			addTileGraphic("DesertTile2",0,0);
		}else if(baseTileType.equals("DesertTile3")){
			isPassable = true;
			addTileGraphic("DesertTile3",0,0);
		}else if(baseTileType.equals("MountainTile")){
			isPassable = true;
			addTileGraphic("MountainTile",0,0);
		}
		
		tileWidth = tileImages[0].width;
		tileHeight = tileImages[0].height;
	}
	
	public void addTileOverlay(String tileOverlayType){
		if(tileOverlayType.equals("TreeOverlay")){
			isPassable = false;
			addTileGraphic("TreeOverlay",5,-20);
		}else if(tileOverlayType.equals("RocksOverlay")){
			addTileGraphic("RocksOverlay",0,0);
		}
	}
	
	private void addTileGraphic(String assetName, int assetXOffset, int assetYOffset){
		GraphicalAsset[] newRealisation = new GraphicalAsset[tileImages.length+1];
		System.arraycopy(tileImages, 0, newRealisation, 0, tileImages.length);
		
		newRealisation[tileImages.length] = tileMap.gameLayer.assetManager.retrieveGraphicalAsset(assetName);
		newRealisation[tileImages.length].offsetX = assetXOffset;
		newRealisation[tileImages.length].offsetY = assetYOffset;
		
		tileImages = newRealisation;
	}
}
