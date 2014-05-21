package demos.GraphicalAssetDemos;

import game.engine.*;
import game.assets.ImageAssetTile;

import java.awt.*;
import java.awt.event.KeyEvent;

public class TiledAssetLayer extends GameLayer {

    // A ticker (incremented on each update) is maintained in order 
    // to control how the viewports of the various tiles are updated
    long ticker = 0;
    
    public TiledAssetLayer( GameEngine gameEngine ) {
        super( "ImageLayer", gameEngine );
        
        width = gameEngine.screenWidth;
        height = gameEngine.screenHeight;
    }
    
    
    private void createTile1() {
        GameObject tile1 = new GameObject( this, 260, 200, 0 );
        tile1.setName( "Tile1" );
        tile1.setRealisationAndGeometry( "Tile1" );
        queueGameObjectToAdd( tile1 );
    }
    
    private void createTile2() {
        GameObject tile2 = new GameObject( this, 764, 200, 0 );
        tile2.setName( "Tile2" );
        tile2.setRealisationAndGeometry( "Tile2" );
        queueGameObjectToAdd( tile2 );
    }
    
    private void createTile3() {
        GameObject tile3 = new GameObject( this, 260, 570, 0 );
        tile3.setName( "Tile3" );
        tile3.setRealisationAndGeometry( "Tile3" );
        queueGameObjectToAdd( tile3 );
    }
    
    private void createTile4() {
        GameObject tile4 = new GameObject( this, 764, 570, 0 );
        tile4.setName( "Tile4" );
        tile4.setRealisationAndGeometry( "Tile4" );
        queueGameObjectToAdd( tile4 );
    }
    
    
    @Override
    public void update() {
        if( inputEvent.keyTyped( KeyEvent.VK_1 ) ) {
            if( !attemptRemoveGameObject( "Tile1") )
                createTile1();
        } else if( inputEvent.keyTyped( KeyEvent.VK_2 ) ) {
            if( !attemptRemoveGameObject( "Tile2") )
                createTile2();
        } else if( inputEvent.keyTyped( KeyEvent.VK_3 ) ) {
            if( !attemptRemoveGameObject( "Tile3") )
                createTile3();
        } else if( inputEvent.keyTyped( KeyEvent.VK_4 ) ) {
            if( !attemptRemoveGameObject( "Tile4") )
                createTile4();
        }
        
        // Update the ticker
        ticker++;
        
        GameObject gameObject = getGameObject( "Tile1" );
        if( gameObject != null )
            ((ImageAssetTile)gameObject.graphicalRealisation[0]).moveViewPort( 1, 1 );
        
        gameObject = getGameObject( "Tile2" );
        if( gameObject != null )
            ((ImageAssetTile)gameObject.graphicalRealisation[0])
                .moveViewPortRight( (int)(Math.cos(Math.toRadians((double)ticker/2.0)) * 10.0) );
        
        gameObject = getGameObject( "Tile3" );
        if( gameObject != null )
            ((ImageAssetTile)gameObject.graphicalRealisation[0]).moveViewPortUp( (int)
                    (Math.cos(Math.toRadians((double)ticker))
                        * Math.sin(Math.toRadians((double)ticker)) * 10.0 ) );
        
        gameObject = getGameObject( "Tile4" );
        if( gameObject != null )
            ((ImageAssetTile)gameObject.graphicalRealisation[0]).moveViewPort(
                    (int)(Math.cos(Math.toRadians((double)ticker)) * 10.0),
                    (int)(Math.sin(Math.toRadians((double)ticker)) * 5.0) );
        
        super.update();
    }
        
    public boolean attemptRemoveGameObject( String gameObjectName ) {
        GameObject gameObject;
        if( (gameObject = getGameObject( gameObjectName )) == null )
            return false;
        else {
            queueGameObjectToRemove( gameObject );
            return true;
        }
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