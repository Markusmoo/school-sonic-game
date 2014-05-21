package demos.GraphicalAssetDemos;

import game.engine.GameEngine;
import java.awt.*;

public class ImageTypeTest extends GameEngine {

    private static final int SCREEN_WIDTH = 1024;
    private static final int SCREEN_HEIGHT = 768;
    
    public ImageTypeTest() {
        gameStart( SCREEN_WIDTH, SCREEN_HEIGHT, 32 );
    }
    
    @Override
    protected boolean buildAssetManager() {
        assetManager.loadAssetsFromFile(
                getClass().getResource( "images/ImageTypeTestAssets.txt" ) );
        
        return true;
    }
        
    @Override
    protected boolean buildInitialGameLayers() {
        ImageTypeLayer imageLayer = new ImageTypeLayer( this );
        addGameLayer(imageLayer);
        
        return true;
    }
    
    @Override    
    protected void gameRender( Graphics2D graphics2D ) {
        super.gameRender( graphics2D );
        
        graphics2D.setFont( new Font( "MONOSPACED", Font.BOLD, 12 ) );
        graphics2D.setColor( Color.white );
        
        graphics2D.drawString( "ESC - Exit :", 10, 10 );
    }
    
    public static void main(String[] args) {
        ImageTypeTest instance = new ImageTypeTest();
    }
}