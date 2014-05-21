package demos.CPUandGPULoading;

import java.net.URL;
import java.awt.*;

import game.engine.GameEngine;

public class PerformanceLoaderCPUCollision extends GameEngine {

    private static final int SCREEN_WIDTH = 1024;
    private static final int SCREEN_HEIGHT = 768;
    
    // The game engine will manage one layer to be used to update/render the balls
    private PerformanceLoaderCPUCollisionLayer collisionLayer;
    
    public PerformanceLoaderCPUCollision() {
        // The default behaviour of the game engine is not to skip
        // frame draws. In this test, this value is changed so that the
        // game engine will skip draws to maintain the target update 
        // frequency        
        MAX_NUM_UPDATES_WITHOUT_RENDER = 10;
        
        DisplayMode currentDisplayMode =
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDisplayMode();
        
        this.gameStart( 1024, 768, 32 );
//        this.gameStart( currentDisplayMode.getWidth(),
//                currentDisplayMode.getHeight(), currentDisplayMode.getBitDepth() );
    }
    
    @Override
    public boolean buildAssetManager() {
        URL imageURL;
        
        // Attempt to load the 5 differnet ball images that are used and add
        // each to the asset manager
        for( int ballIdx = 1; ballIdx <= 5; ballIdx++ ) {
            imageURL = getClass().getResource( "images/Ball" + ballIdx + ".png");
            assetManager.addImageAsset( "Ball" + ballIdx, imageURL );
        }
        
        return true;
    }
        
    @Override
    protected boolean buildInitialGameLayers() {
        collisionLayer = new PerformanceLoaderCPUCollisionLayer( this );
        addGameLayer( collisionLayer );
        
        return true;
    }
    
    @Override
    protected void gameRender( Graphics2D graphics2D ) {
        // Clear the screen to black
        Color originalColour = graphics2D.getColor();
        graphics2D.setColor( Color.black );
        graphics2D.fillRect( 0, 0, screenWidth, screenHeight );
        graphics2D.setColor( originalColour );
                
        // Call the inherited gameRender method which will draw all
        // visible game layers managed by this game engine
        super.gameRender( graphics2D );
        
        graphics2D.setFont( new Font( "MONOSPACED", Font.BOLD, 18 ) );
        graphics2D.setColor( Color.white );
        
        graphics2D.drawString( "ESC - Exit :", 10, 20 );
        graphics2D.drawString( "1 - Add 1 : 2 - Add 10 : 3 - Add 100", 10, 40  );
        graphics2D.drawString( "4 - Toggle collisons", 10, 60  );
        
        graphics2D.drawString( "FPS = "
                + gameStatisticsRecorder.getAverageFPS()
                + " (" + gameStatisticsRecorder.getAverageRenderTime() + "ms)"
                + " : UPS = " + gameStatisticsRecorder.getAverageUPS()
                + " (" + gameStatisticsRecorder.getAverageUpdateTime() + "ms)",
                SCREEN_WIDTH/2, 20  );
        graphics2D.drawString( "Active game objects = " +
                gameStatisticsRecorder.getNumActiveGameObjects(),
                SCREEN_WIDTH/2, 40 );
        graphics2D.drawString( "Average collisions) = " + 
                (collisionLayer.arbiters.size()
                * gameStatisticsRecorder.getAverageUPS())/1000 + "k/s" ,
                SCREEN_WIDTH/2, 60 );
        graphics2D.drawString( "Collision Checking = " 
                + collisionLayer.enableCollisionChecking,
                SCREEN_WIDTH/2, 80  );
    }
    
    public static void main(String[] args) {
        PerformanceLoaderCPUCollision instance = new PerformanceLoaderCPUCollision();
    }
}

