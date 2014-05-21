package demos.CPUandGPULoading;

import game.assets.ImageAssetRibbon;
import game.engine.GameEngine;

import java.awt.*;
import java.awt.Graphics2D;
import java.net.URL;

public class PerformanceLoaderGfxScroller extends GameEngine {

    private static final int SCREEN_WIDTH = 1024;
    private static final int SCREEN_HEIGHT = 768;
    
    public PerformanceLoaderGfxScroller() {
        // The default behaviour of the game engine is not to skip
        // frame draws. In this test, this value is changed so that the
        // game engine will skip draws to maintain the target update 
        // frequency
        MAX_NUM_UPDATES_WITHOUT_RENDER = 10;
        
        // Start the game at the specified resolution
        gameStart( SCREEN_WIDTH, SCREEN_HEIGHT, 32 );
    }
    
    @Override
    public boolean buildAssetManager() {
        // Load the various images which will go together to form the
        // ribbon to be used
        URL imageURLs[] = new URL[4];
        for( int backgroundIdx = 1; backgroundIdx <= 4; backgroundIdx++ )
            imageURLs[backgroundIdx-1] = getClass().getResource(
                    "images/Back" + backgroundIdx + ".png" );
        
        // Create a ImageAssetRibbon using the loaded images and add
        // it to the asset manager
        assetManager.addImageAssetRibbon( "BackgroundRibbon", 0, 0, 500, 225, 
                ImageAssetRibbon.RibbonScrollDirection.HORIZONTAL, imageURLs);
        
        return true;
    }
    
    /**
     * Construct a demo layer instance and add it to the game engine
     */
    @Override
    protected boolean buildInitialGameLayers() {
        // Create a layer to hold and manage the various ribbons
        PerformanceLoaderGfxScrollerLayer scrollingLayer
                = new PerformanceLoaderGfxScrollerLayer( this  );

        // Add the game layer to this game engine to be managed
        addGameLayer( scrollingLayer );
        
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
        
        graphics2D.drawString( "FPS = " + gameStatisticsRecorder.getAverageFPS()
        + " : UPS = " + gameStatisticsRecorder.getAverageUPS(),
                this.SCREEN_WIDTH/2, 20  );
        graphics2D.drawString( "Video memory available = " +
                gameStatisticsRecorder.getAvailableVideoMemory() / 1024 + "(kb)",
                this.SCREEN_WIDTH/2, 40 );
        graphics2D.drawString( "Active game objects = " +
                this.gameStatisticsRecorder.getNumActiveGameObjects(),
                this.SCREEN_WIDTH/2, 60  );
    }
    
    
    public static void main(String[] args) {
        PerformanceLoaderGfxScroller instance = new PerformanceLoaderGfxScroller();
    }    
}

