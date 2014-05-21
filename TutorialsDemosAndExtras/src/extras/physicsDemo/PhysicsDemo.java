package extras.physicsDemo;

import game.engine.*;
import java.awt.*;

/**
 * This class provides some physics based demos exploring different physical
 * simulation aspects
 *
 * @author <A HREF="mailto:P.Hanna@qub.ac.uk">Philip Hanna</A>
 * @version $Revision: 1.0 $ $Date: 2007/08 $
 */

public class PhysicsDemo extends GameEngine {

    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Create a new physics demo using the current screen resolution
     */
    public PhysicsDemo() {
        // By default draw the FPS/UPS statistics
        drawGameStatistics = true;

        DisplayMode currentDisplayMode =
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDisplayMode();
        
        gameStart( currentDisplayMode.getWidth(),
                currentDisplayMode.getHeight(), currentDisplayMode.getBitDepth() );
    }
    
    
    ///////////////////////////////////////////////////////////////////////////
    // Methods:                                                              //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Load in graphical and sounds assets needed by the demo
     */
    @Override
    protected boolean buildAssetManager() {
        assetManager.loadAssetsFromFile(
                getClass().getResource( "images/PhysicsDemoAssets.txt" ) );
        
        return true;
    }
    
    /**
     * Construct a demo layer instance and add it to the game engine
     */
    @Override
    protected boolean buildInitialGameLayers() {
        PhysicsGameLayer physicsGameLayer = new PhysicsGameLayer( this );
        addGameLayer( physicsGameLayer );
        
        return true;
    }
    
    /**
     * Create a new demo instance
     */
    public static void main(String[] args) {
        PhysicsDemo instance = new PhysicsDemo();
    }    
}