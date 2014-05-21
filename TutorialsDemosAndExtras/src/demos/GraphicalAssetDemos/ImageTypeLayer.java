package demos.GraphicalAssetDemos;

import game.engine.*;
import java.awt.*;

public class ImageTypeLayer extends GameLayer {

    public ImageTypeLayer( GameEngine gameEngine ) {
        super( "ImageLayer", gameEngine );
        
        width = gameEngine.screenWidth;
        height = gameEngine.screenHeight;
        
        createImageObjects();
    }
    
    private void createImageObjects() {
        // Note the following block of code is tedious - an artefact of creating 
        // a number of individual objects
        
        // Background object
        GameObject background = new GameObject( this, width/2, 550, 0 );
        background.setRealisationAndGeometry( "Background" );
        background.setDrawOrder(0);
        addGameObject( background );
        
        
        // Left set of objects
        GameObject biPlaneOpaqueTop = new GameObject( this, 160, 100, 0 );
        biPlaneOpaqueTop.setRealisationAndGeometry( "BiplaneOpaque" );
        biPlaneOpaqueTop.setDrawOrder(1);
        addGameObject( biPlaneOpaqueTop );
        
        GameObject cloudOpaqueTop = new GameObject( this, 160, 280, 0 );
        cloudOpaqueTop.setRealisationAndGeometry( "CloudOpaque" );
        cloudOpaqueTop.setDrawOrder(1);
        addGameObject( cloudOpaqueTop );
        
        GameObject biPlaneOpaqueBottom = new GameObject( this, 140, 560, 0 );
        biPlaneOpaqueBottom.setRealisationAndGeometry( "BiplaneOpaque" );
        biPlaneOpaqueBottom.setDrawOrder(1);
        addGameObject( biPlaneOpaqueBottom );
        
        GameObject cloudOpaqueBottom1 = new GameObject( this, 200, 450, 0 );
        cloudOpaqueBottom1.setRealisationAndGeometry( "CloudOpaque" );
        cloudOpaqueBottom1.setDrawOrder(2);
        addGameObject( cloudOpaqueBottom1 );
        
        GameObject cloudOpaqueBottom2 = new GameObject( this, 250, 600, 0 );
        cloudOpaqueBottom2.setRealisationAndGeometry( "CloudOpaque" );
        cloudOpaqueBottom2.setDrawOrder(2);
        addGameObject( cloudOpaqueBottom2 );
        
               
        // Middle set of objects
        GameObject biplaneTransparentTop = new GameObject( this, 500, 100, 0 );
        biplaneTransparentTop.setRealisationAndGeometry( "BiplaneTransparent" );
        biplaneTransparentTop.setDrawOrder(1);
        addGameObject( biplaneTransparentTop );
        
        GameObject cloudTransparentTop = new GameObject( this, 500, 280, 0 );
        cloudTransparentTop.setRealisationAndGeometry( "CloudTransparent" );
        cloudTransparentTop.setDrawOrder(1);
        addGameObject( cloudTransparentTop );
        
        GameObject biplaneTransparentBottom1 = new GameObject( this, 480, 560, 0 );
        biplaneTransparentBottom1.setRealisationAndGeometry(  "BiplaneTransparent");
        biplaneTransparentBottom1.setDrawOrder(1);
        addGameObject( biplaneTransparentBottom1  );
        
        GameObject cloudTransparentBottom1 = new GameObject( this, 540, 450, 0 );
        cloudTransparentBottom1.setRealisationAndGeometry(  "CloudTransparent"  );
        cloudTransparentBottom1.setDrawOrder(2);
        addGameObject( cloudTransparentBottom1 );
        
        GameObject cloudTransparentBottom2 = new GameObject( this, 590, 600, 0 );
        cloudTransparentBottom2.setRealisationAndGeometry( "CloudTransparent"  );
        cloudTransparentBottom2.setDrawOrder(2);
        addGameObject( cloudTransparentBottom2 );
        
        
        // Right set of objects
        GameObject biplaneTransparentTop2 = new GameObject( this, 800, 100, 0 );
        biplaneTransparentTop2.setRealisationAndGeometry( "BiplaneTransparent" );
        biplaneTransparentTop2.setDrawOrder(1);
        addGameObject( biplaneTransparentTop2 );
        
        GameObject cloudTranslucentTop = new GameObject( this, 800, 280, 0 );
        cloudTranslucentTop.setRealisationAndGeometry(  "CloudTranslucent" );
        cloudTranslucentTop.setDrawOrder(1);
        addGameObject( cloudTranslucentTop );
        
        GameObject biplaneTransparentBottom2 = new GameObject( this, 780, 560, 0 );
        biplaneTransparentBottom2.setRealisationAndGeometry(  "BiplaneTransparent" );
        biplaneTransparentBottom2.setDrawOrder(1);
        addGameObject( biplaneTransparentBottom2  );
        
        GameObject cloudTranslucentBottom1 = new GameObject( this, 840, 450, 0 );
        cloudTranslucentBottom1 .setRealisationAndGeometry( "CloudTranslucent" );
        cloudTranslucentBottom1.setDrawOrder(2);
        addGameObject( cloudTranslucentBottom1 );
        
        GameObject cloudTranslucentBottom2 = new GameObject( this, 890, 600, 0 );
        cloudTranslucentBottom2.setRealisationAndGeometry(  "CloudTranslucent"  );
        cloudTranslucentBottom2.setDrawOrder(2);
        addGameObject( cloudTranslucentBottom2 );
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