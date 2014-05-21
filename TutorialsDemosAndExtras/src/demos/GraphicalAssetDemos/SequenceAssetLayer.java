package demos.GraphicalAssetDemos;

import game.engine.*;

import java.awt.*;
import java.awt.event.KeyEvent;

public class SequenceAssetLayer extends GameLayer {

    public SequenceAssetLayer( GameEngine gameEngine ) {
        super( "SequenceAssetLayer", gameEngine );
        
        width = gameEngine.screenWidth;
        height = gameEngine.screenHeight;
        
        createRoad();
    }
    
    private void createRoad() {
        GameObject road = new GameObject( this, width/2, 450, 0 );
        road.setRealisationAndGeometry( "Road" );
        addGameObject( road );
    }
    
    @Override
    public void update() {
        if( inputEvent.keyTyped( KeyEvent.VK_1 ) ) {
            // Add more builders if requested
            for( int builderIdx = 0; builderIdx <= 5; builderIdx++ ) {
                BuilderSprite builder 
                        = new BuilderSprite( this, 0, 300 + builderIdx * 65 );
                queueGameObjectToAdd(builder);
            }
        }
        
        // Update each object
        for( GameObject gameObject : this.gameObjects.values() )
            gameObject.update();
        
        super.update();
    }
    
    @Override
    public void draw( Graphics2D graphics2D ) {        
        Color originalColour = graphics2D.getColor();
        graphics2D.setColor( Color.black );
        graphics2D.fillRect( 0, 0, gameEngine.screenWidth, gameEngine.screenHeight );
        graphics2D.setColor( originalColour );
        
        super.draw( graphics2D );
    }
}