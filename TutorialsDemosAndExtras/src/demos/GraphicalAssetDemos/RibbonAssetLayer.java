package demos.GraphicalAssetDemos;

import game.engine.*;
import game.assets.ImageAssetRibbon;

import java.awt.*;
import java.awt.event.KeyEvent;

public class RibbonAssetLayer extends GameLayer {
    public RibbonAssetLayer( GameEngine gameEngine ) {
        super( "ImageLayer", gameEngine );
        
        width = gameEngine.screenWidth;
        height = gameEngine.screenHeight;
    }
    
    private void createDesertSky() {
        GameObject desertSky = new GameObject( this, width/2, 400, 0 );
        desertSky.setName( "DesertSky" );
        desertSky.setRealisationAndGeometry( "DesertSky" );
        queueGameObjectToAdd( desertSky );
    }
    
    private void createDesertSun() {
        GameObject desertSun = new GameObject( this, width-200, 270, 1 );
        desertSun.setName( "DesertSun" );
        desertSun.setRealisationAndGeometry( "DesertSun" );
        queueGameObjectToAdd( desertSun );
    }
    
    private void createDesertDunes() {
        GameObject desertDunes = new GameObject( this, width/2, 544, 2 );
        desertDunes.setName( "DesertDunes" );
        desertDunes.setRealisationAndGeometry( "DesertDunes" );
        queueGameObjectToAdd( desertDunes );
    }
    
    private void createDesertClouds1() {
        GameObject desertCloudLayer1 = new GameObject( this, width/2, 260, 4 );
        desertCloudLayer1.setName( "DesertCloudLayer1" );
        desertCloudLayer1.setRealisationAndGeometry( "DesertCloudLayer1" );
        queueGameObjectToAdd( desertCloudLayer1 );
    }
    
    private void createDesertClouds2() {
        GameObject desertCloudLayer2 = new GameObject( this, width/2, 320, 3 );
        desertCloudLayer2 .setName( "DesertCloudLayer2" );
        desertCloudLayer2 .setRealisationAndGeometry( "DesertCloudLayer2" );
        queueGameObjectToAdd( desertCloudLayer2 );
    }
        
    @Override
    public void update() {
        if( inputEvent.keyTyped( KeyEvent.VK_1 ) ) {
            if( !attemptRemoveGameObject( "DesertSky") )
                createDesertSky();
        } else if( inputEvent.keyTyped( KeyEvent.VK_2 ) ) {
            if( !attemptRemoveGameObject( "DesertDunes") )
                createDesertDunes();
        } else if( inputEvent.keyTyped( KeyEvent.VK_3 ) ) {
            if( !attemptRemoveGameObject( "DesertCloudLayer1") )
                createDesertClouds1();
        } else if( inputEvent.keyTyped( KeyEvent.VK_4 ) ) {
            if( !attemptRemoveGameObject( "DesertCloudLayer2") )
                createDesertClouds2();
        } else if( gameEngine.inputEvent.keyTyped( KeyEvent.VK_5 ) ) {
            if( !attemptRemoveGameObject( "DesertSun") )
                createDesertSun();
        }
        
        GameObject gameObject = getGameObject( "DesertSky" );
        if( gameObject != null )
            ((ImageAssetRibbon)gameObject.graphicalRealisation[0]).moveViewPortRight( 1 );
        
        gameObject = getGameObject( "DesertDunes" );
        if( gameObject != null )
            ((ImageAssetRibbon)gameObject.graphicalRealisation[0]).moveViewPortRight( 2 );
        
        gameObject = getGameObject( "DesertCloudLayer1" );
        if( gameObject != null )
            ((ImageAssetRibbon)gameObject.graphicalRealisation[0]).moveViewPortRight( 3 );
        
        gameObject = getGameObject( "DesertCloudLayer2" );
        if( gameObject != null )
            ((ImageAssetRibbon)gameObject.graphicalRealisation[0]).moveViewPortRight( 2 );

        // It's important to always call the super.update method if queued
        // game objects are to be removed or added
        super.update();
    }
    
    public boolean attemptRemoveGameObject( String gameObjectName ) {
        GameObject gameObject;
        if( (gameObject = getGameObject( gameObjectName )) == null )
            return false;
        else {
            queueGameObjectToRemove(gameObject);
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
