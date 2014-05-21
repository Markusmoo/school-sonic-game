package demos.GraphicalAssetDemos;

import game.engine.GameEngine;
import java.awt.*;

public class RibbonAssetTest extends GameEngine {

    private static final int SCREEN_WIDTH = 1024;
    private static final int SCREEN_HEIGHT = 768;
    
    public RibbonAssetTest() {        
        gameStart( SCREEN_WIDTH, SCREEN_HEIGHT, 32 );
    }
    
    @Override
    protected boolean buildAssetManager() {
        assetManager.loadAssetsFromFile(
            getClass().getResource( "images/RibbonAssetTestAssets.txt" ) );
        
        return true;
    }
    
    @Override
    protected boolean buildInitialGameLayers() {
        RibbonAssetLayer ribbonLayer = new RibbonAssetLayer( this );
        addGameLayer( ribbonLayer );
        
        return true;
    }
    
    @Override    
    protected void gameRender( Graphics2D graphics2D ) {
        super.gameRender( graphics2D );
        
        graphics2D.setFont( new Font( "MONOSPACED", Font.BOLD, 12 ) );
        graphics2D.setColor( Color.white );
        
        graphics2D.drawString( "ESC - Exit", 10, 10 );
        graphics2D.drawString( "1 - Toggle sky", 10, 25 );
        graphics2D.drawString( "2 - Toggle sand", 10, 40 );
        graphics2D.drawString( "3 - Toggle cloud 1", 10, 55 );
        graphics2D.drawString( "4 - Toggle cloud 2", 10, 70 );
        graphics2D.drawString( "5 - Toggle sun", 10, 85 );
    }
    
    public static void main(String[] args) {
        RibbonAssetTest instance = new RibbonAssetTest();
    }
}