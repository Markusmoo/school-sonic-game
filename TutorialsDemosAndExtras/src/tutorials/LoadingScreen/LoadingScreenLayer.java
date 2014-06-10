package tutorials.LoadingScreen;

import game.engine.*;
import game.components.*;
import java.awt.Graphics2D;
import java.awt.Color;

public class LoadingScreenLayer extends GameLayer {

	private int loaderProgress = 0;
	
	public LoadingScreenLayer(GameEngine gameEngine){
		super("LoadingScreenLayer", gameEngine);
		
		buildLoaderMenu();
	}
	
	private void buildLoaderMenu(){
		GameObject loaderTitle = new GameObject(this, gameEngine.screenWidth/2, gameEngine.screenHeight/2, 0);
		loaderTitle.setRealisation(assetManager.retrieveGraphicalAsset("LoaderBackground"));
		addGameObject(loaderTitle);
		
		Bar progressBar = new Bar(this, "LoaderProgressBar","LoadProgressBarBorder", "LoadProgressBarInner",6,gameEngine.screenWidth/2 + 50, gameEngine.screenHeight/2 + 310,2);
		progressBar.setPoints(0);
		progressBar.setInnerAssetOffset(-6.0,-8.0);
		addGameObject(progressBar);
	}
	
	public void update(){
		super.update();
		switch (loaderProgress){
		case 0:
			break;
		case 1:
			assetManager.loadAssetsFromFile(this.getClass().getResource("images/CommonAssets.txt"));
			break;
		case 2:
			assetManager.loadAssetsFromFile(this.getClass().getResource("images/SpaceAssets.txt"));
		break;
		case 3:
			assetManager.loadAssetsFromFile(this.getClass().getResource("images/DecorativeAssets.txt"));
			break;
		case 4:
			assetManager.loadAssetsFromFile(this.getClass().getResource("images/AnimalAssets.txt"));
			break;
		case 5:
			assetManager.loadAssetsFromFile(this.getClass().getResource("images/SceneryAssets.txt"));
			break;
		case 6:
			assetManager.loadAssetsFromFile(this.getClass().getResource("images/EnemyAssets.txt"));
			break;
		case 7:
			assetManager.loadAssetsFromFile(this.getClass().getResource("images/MenuAssets.txt"));
			break;
		case 8:
			assetManager.loadAssetsFromFile(this.getClass().getResource("sound/SoundAssets.txt"));
			break;
		case 9:
			LoadingScreenGameLayer loadingScreenGameLayer = new LoadingScreenGameLayer(gameEngine);
			gameEngine.addGameLayer(loadingScreenGameLayer);
			gameEngine.removeGameLayer(this.gameLayerName);
			break;
		}
		Bar progressBar = (Bar) getGameObject("LoaderProgressBar");
		if(loaderProgress <= 6) progressBar.setPoints(loaderProgress);
		progressBar.update();
		gameEngine.doNotSkipNextRender();
		
		loaderProgress++;
	}
	
	public void draw(Graphics2D graphics2D){
		Color originalColour = graphics2D.getColor();
		graphics2D.setColor(new Color(200, 200, 0, 255));
		graphics2D.fillRect(0, 0, gameEngine.screenWidth,  gameEngine.screenHeight);
		graphics2D.setColor(originalColour);
		
		super.draw(graphics2D);
	}
}
