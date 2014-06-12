package tutorials.LoadingScreen;

import game.engine.GameEngine;

public class LoadingScreen extends GameEngine {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LoadingScreen(){
		gameStart(1024,768,32);
		
	}
	
	public static void main(String[] args){
		new LoadingScreen();
	}
	
	protected boolean buildAssetManager(){
		assetManager.loadAssetsFromFile(this.getClass().getResource("images/LoadingScreenAssets.txt"));
		return true;
	}
	protected boolean buildInitialGameLayers(){
		
		LoadingScreenLayer loaderLayer = new LoadingScreenLayer(this);
		addGameLayer(loaderLayer);
		
		return true;
	}
	

}
