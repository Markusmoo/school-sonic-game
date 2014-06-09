package tutorials.LoadingScreen;

import game.engine.*;
import game.components.*;
import java.awt.Graphics2D;
import java.awt.Color;

public class LoadingScreenLayer extends GameLayer {

	private int loaderProgress = 0;
	
	public LoadingScreenLayer(GmaeEngine gameEngine){
		super("LoadingScreenLayer", gameEngine);
		
		buildLoaderMenu();
	}
	
	private void buildLoaderMenu(){
		GameObject loaderTitle = new GameObject(this, gameEngine.screenWidth/2, gameEngine.screenHeight/2, 0);
		loaderTitle.setRealisation(assetManager.retrieveGraphicalAsset("LoaderBackground"));
		addGameObject(loaderTitle);
		
		Bar progressBar = new Bar(this, "LoaderProgressBar","LoadProgressBarBorder", "LoadProgressBarInner",6,gameEngine.scrreenWidth/2 + 50, gameEngine.screenHeight/2 + 310,2);
		progressBar.setPoints(0);
		progressBar.setInnerAssetOffset(-6.0,-8.0);
		addGameObject(progressBar);
	}
}
