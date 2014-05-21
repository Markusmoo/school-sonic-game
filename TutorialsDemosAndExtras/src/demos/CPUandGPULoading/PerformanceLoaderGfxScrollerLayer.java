package demos.CPUandGPULoading;

import game.engine.*;
import game.assets.ImageAssetRibbon;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;

public class PerformanceLoaderGfxScrollerLayer extends GameLayer {
    
    public PerformanceLoaderGfxScrollerLayer( GameEngine gameEngine ) {
        super( "SpaceLayer", gameEngine );
        
        width = gameEngine.screenWidth;
        height = gameEngine.screenHeight;
    }
    
    private void createRibbons( int numExtraRibbons ) {
        for( int ribbonIdx = 0; ribbonIdx < numExtraRibbons; ribbonIdx++ ) {
            // Retreive a new ImageAssetRibbon from the asset store
            ImageAssetRibbon newRibbon =
                    (ImageAssetRibbon)assetManager.retrieveAsset( "BackgroundRibbon" );
            
            // Randomly set the x viewport to somewhere within the ribbon
            newRibbon.setViewPort( (new Random()).nextInt( newRibbon.width ), 0 );
            
            // Create a new GameObjectGraphical to hold the ribbon (which will
            // be set as the object's realisation
            GameObject newGameObject = new GameObject( this );
            newGameObject.setRealisationAndGeometry( newRibbon );
            
            // Randomly place the game object on the screen
            newGameObject.x = (new Random()).nextInt( (int)width - newRibbon.width/2 );
            newGameObject.y = (new Random()).nextInt( (int)height - newRibbon.height/2 );
            
            // Add the newly created image to this layer to be managed
            addGameObject( newGameObject );
        }
    }
        
    @Override
    public void update() {
        // Consider player input and create more ribbons if requested
        if( inputEvent.keyTyped( KeyEvent.VK_1 ) )
            createRibbons( 1 );
        else if( inputEvent.keyTyped( KeyEvent.VK_2 ) )
            createRibbons( 10 );
        else if( inputEvent.keyTyped( KeyEvent.VK_3 ) )
            createRibbons( 100 );
        
        // Move the viewport on each ribbon (move some ribbons faster than others)
        int iOffsetCounter = 1;
        for( GameObject gameObject : this.gameObjects.values() ) {
            ((ImageAssetRibbon)gameObject.graphicalRealisation[0])
            .moveViewPortRight( (iOffsetCounter++)%5 + 1 );
        }
    }
}