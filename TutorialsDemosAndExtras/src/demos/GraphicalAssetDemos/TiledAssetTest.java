package demos.GraphicalAssetDemos;

import game.engine.GameEngine;
import java.awt.*;

public class TiledAssetTest extends GameEngine {

    private static final int SCREEN_WIDTH = 1024;
    private static final int SCREEN_HEIGHT = 768;
    
    public TiledAssetTest() {
        gameStart( SCREEN_WIDTH, SCREEN_HEIGHT, 32 );
    }
        
    @Override
    protected boolean buildAssetManager() {
        assetManager.loadAssetsFromFile(
            getClass().getResource( "images/TiledAssetTestAssets.txt" ) );
        
        return true;
    }
    
    @Override
    protected boolean buildInitialGameLayers() {
        TiledAssetLayer tiledLayer = new TiledAssetLayer( this );
        addGameLayer( tiledLayer );
        
        return true;
    }
    
    @Override
    protected void gameRender( Graphics2D graphics2D ) {
        super.gameRender( graphics2D );
        
        graphics2D.setFont( new Font( "MONOSPACED", Font.BOLD, 12 ) );
        graphics2D.setColor( Color.white );
        
        graphics2D.drawString( "ESC - Exit", 10, 10 );
        graphics2D.drawString( "1 - Toggle tile 1", 10, 25 );
        graphics2D.drawString( "2 - Toggle tile 2", 10, 40 );
        graphics2D.drawString( "3 - Toggle tile 3", 10, 55 );
        graphics2D.drawString( "4 - Toggle tile 4", 10, 70 );
    }
    
    public static void main(String[] args) {
        TiledAssetTest instance = new TiledAssetTest();
    }    
}