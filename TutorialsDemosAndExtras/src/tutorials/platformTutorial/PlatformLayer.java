/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tutorials.platformTutorial;
import game.assets.*; import game.engine.*;
import game.physics.*; import game.geometry.*;

/**
 *
 * @author Michael
 */
public class PlatformLayer extends CollisionSpace {
	
    public static final double GRAVITY_STRENGTH = 800.0;
	
    public PlatformLayer(GameEngine gameEngine){
        super("PlatformLayer", gameEngine);
        
        width = 10000;
        height = gameEngine.screenHeight;
        
        setGravity(0, GRAVITY_STRENGTH);
        
        addGameObjectCollection("Platforms");
        addGameObjectCollection("Balls");
        
        createBackground();
        createPlatforms();
        createCharacter();
        
    }
    private void createBackground(){
        GameLayer backgroundLayer = new GameLayer("BackgroundLayer", this.gameEngine);
        backgroundLayer.width = gameEngine.screenWidth;
        backgroundLayer.height = gameEngine.screenHeight;
        
        GameObject background  = new GameObject(backgroundLayer);
        background.setName("Background");
        background.setPosition(backgroundLayer.width/2, backgroundLayer.height/2);
        
        ImageAssetRibbon backgroundAsset = (ImageAssetRibbon)assetManager.retrieveGraphicalAsset("Background");
        background.setRealisation(backgroundAsset);
        background.setGeometry(new Box (0, 0, 1024, 768));
        
        backgroundLayer.addGameObject(background);
        gameEngine.addGameLayer(backgroundLayer);
        
        setDrawOrder(backgroundLayer.getDrawOrder()+1);
    }
    
    private void createPlatforms(){
        double groundOffset = 0;
        while(groundOffset < this.width){
            createPlatform(groundOffset, this.height - assetManager.retrieveGraphicalAssetArchetype("Platform").height);
            groundOffset += assetManager.retrieveGraphicalAssetArchetype("Platform").width;
        }
        for(int idx = 0; idx < 8; idx++)
            createPlatform(300+150 * idx, this.height-50*(idx+1));
        for(int idx = 0; idx < 8; idx++)
            createPlatform(2500-150 * idx, this.height-50*(idx+1));
        for(int idx = 0; idx < 5; idx++)
            createPlatform(3500-400* (idx%2), this.height-100*(idx+1));
        for(int idx = 0; idx < 10; idx++)
            createPlatform(4500+150* idx, this.height-50*(idx+1));
        for(int idx = 0; idx < 12; idx++)
            createPlatform(6000, this.height-50*(idx+1));
        for(int idx = 0; idx < 12; idx++)
            createPlatform(7200, this.height-50*(idx+1));
    }
    private void createPlatform(double x, double y){
        Body platform = new Body(this);
        platform.setRealisationAndGeometry("Platform");
        platform.setPosition(x,y);
        platform.setMass(Double.MAX_VALUE);
        
        addGameObject(platform, "Platforms");
    }
    private void createCharacter(){
        SonicSprite sonic = new SonicSprite(this,1);
        SonicSprite sonic2 = new SonicSprite(this,2);
        sonic.setPosition(0, gameEngine.screenHeight - sonic.height - 200);
        sonic2.setPosition(10, gameEngine.screenHeight - sonic.height - 200);
        addGameObject(sonic);
        addGameObject(sonic2);
    }
    public void update(){
        super.update();
        
        updateViewPort();
        updateGameObjects();
    }
    private void updateViewPort(){
        GameObject sonic = getGameObject("Sonic1");
        GameObject sonic2 = getGameObject("Sonic2");
        
        centerViewportOnGameObject(sonic, 0.0, 0.0, gameEngine.screenWidth/2.0, gameEngine.screenHeight/2.0);
        centerViewportOnGameObject(sonic2, 0.0, 0.0, gameEngine.screenWidth/2.0, gameEngine.screenHeight/2.0);
        
        GameObject background = gameEngine.getGameObjectFromLayer("Background","BackgroundLayer");
        ((ImageAssetRibbon) background.getRealisation(0)).setViewPort(
        (int)viewPortLayerX,0);
        background.getRealisation(0).update();
    }
    
    private void keepOnScreen(GameObject[] objectArray){
    	GameObject background = gameEngine.getGameObjectFromLayer("Background","BackgroundLayer");
    	int viewX = ((ImageAssetRibbon) background.getRealisation(0)).getViewPortX();
    	for(GameObject object : objectArray){
    		if(object.x > viewX+gameEngine.screenWidth/2){
    			object.setPosition(viewX+gameEngine.screenWidth/2, object.y);
    		}else if(object.x < viewX-gameEngine.screenWidth/2){
    			object.setPosition(viewX-gameEngine.screenWidth/2, object.y);
    		}
    	}
    }
    
    private void updateGameObjects(){
        GameObject sonic = getGameObject("Sonic1");
        GameObject sonic2 = getGameObject("Sonic2");
        sonic.update();
        sonic2.update();
        keepOnScreen(new GameObject[]{sonic2,sonic});
        GameObjectUtilities.reboundIfGameLayerExited(sonic);
        GameObjectUtilities.reboundIfGameLayerExited(sonic2);
    }
}
