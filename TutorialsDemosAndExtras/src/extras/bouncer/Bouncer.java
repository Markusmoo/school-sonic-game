package extras.bouncer;

import game.engine.*;
import java.awt.*;

public class Bouncer extends GameEngine 
{
    public Bouncer() {
        DisplayMode currentDisplayMode =
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDisplayMode();
        
        gameStart( currentDisplayMode.getWidth(),
                currentDisplayMode.getHeight(), currentDisplayMode.getBitDepth() );
    }

    @Override
    protected boolean buildAssetManager() {
        assetManager.loadAssetsFromFile( 
            getClass().getResource( "images/BouncerAssets.txt" ) );
        
        return true;
    } 
   
    @Override
    protected boolean buildInitialGameLayers() { 
        BouncerLayer bouncerLayer = new BouncerLayer( this );
        addGameLayer( bouncerLayer );             
   
        return true;     
    }   

    
    public static void main(String[] args) {
        Bouncer instance = new Bouncer();
    }         
}
