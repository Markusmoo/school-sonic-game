package demos.CPUandGPULoading;

import game.assets.AssetManager;
import game.engine.GameInputEventManager;

import java.awt.*;
import java.awt.image.BufferStrategy;
import javax.swing.*;

/**
 * ScreenManager provides a simple class that supports an update/render
 * loop using either a JPanel or FSEM
 * <P>
 * Note: This class is functional, but somewhat crude in terms of its
 * construction. It is largely based on a cut down version of the
 * GameEngine/GameLayer classes - although with dual support for
 * FSEM and JPanel modes.
 *
 * @author <A HREF="mailto:P.Hanna@qub.ac.uk">Philip Hanna</A>
 * @version $Revision: 1 $ $Date: 2006/08 $
 */
public class ScreenManager extends JFrame implements Runnable {
    
    ///////////////////////////////////////////////////////////////////////////
    // Class data                                                            //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Define the different modes we can run the screen manager in
     */
    public enum ScreenMode { FSEM, PanelMode };
    public ScreenMode screenMode;
    
    /**
     * Define various parameters of used when we are going into FSEM
     * mode.
     */
    private static final int FSEM_NUM_PAGE_BUFFERS = 2;
    protected GraphicsDevice graphicsDevice;
    protected BufferStrategy bufferStrategy;
    
    /**
     * Define the JPanel we will use if not running in FSEM and also the
     * image into which we will render when running in double buffered
     * mode.
     */
    protected JPanel renderPanel;
    protected Image panelImage;
    
    /**
     * Record the screen width, height and colour depth
     */
    public int screenWidth;
    public int screenHeight;
    public int screenColorDepth;
    
    /**
     * Create an asset manager and input event manager. Note: these are
     * artefacts from the GameEngine class, on which this particular class
     * is a hacked version
     */
    public AssetManager assetManager;
    public GameInputEventManager inputEvent;
    
    // Thread object to run the render/update process
    protected Thread updateRenderProcess;
    
    
    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Create a new screen manager
     */
    public ScreenManager() {
        assetManager = new AssetManager();
        inputEvent = new GameInputEventManager();
        
        this.addKeyListener( inputEvent );
        
        Runtime.getRuntime().addShutdownHook( new Thread() {
            @Override
            public void run() {
                restoreScreen();
            }
        });
    }
    
    
    ///////////////////////////////////////////////////////////////////////////
    // Methods: FSEM/JPanel/Update                                           //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Start FSEM at the specified resolution and colour depth
     *
     * @param screenWidth screen width
     * @param screenHeight screen height
     * @param screenColorDepth screen colour depth (incensive commentary, eh?)
     */
    public Boolean startFSEM( int screenWidth, int screenHeight, int screenColorDepth ) {
        this.screenMode = ScreenMode.FSEM;
        
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.screenColorDepth = screenColorDepth;
        
        try {
            if( initialiseFullScreen() == false ) {
                System.err.println( "startFSEM: Cannot initialise full screen mode." );
                restoreScreen();
                return false;
            }
            
            if( updateRenderProcess == null ) {
                updateRenderProcess = new Thread( this );
                updateRenderProcess.start();
            }
        } catch( Exception exception ) {
            restoreScreen();
            return false;
        }
        
        return true;
    }
    
    /**
     * Create a JPanel with the specified resolution
     *
     * @param screenWidth screen width
     * @param screenHeight screen height
     */
    public Boolean startPanelMode( int screenWidth, int screenHeight ) {
        this.screenMode = ScreenMode.PanelMode;
        
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        
        this.setSize( this.screenWidth, this.screenHeight );
        
        this.renderPanel = new JPanel();
        
        this.getContentPane().add( renderPanel );
        this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        this.pack();
        this.setSize( 1920, 1200 );
        this.setVisible( true );
        
        this.panelImage = createImage( screenWidth, screenHeight );
        
        try {
            if( updateRenderProcess == null ) {
                updateRenderProcess = new Thread( this );
                updateRenderProcess.start();
            }
        } catch( Exception exception ) {
            return false;
        }
        
        return true;
    }
    
    /**
     * run method - which will need to be extended in order to do anything useful
     */
    public void run() {
        
    }
    
    /**
     * Paint the screen. If we are in double buffered mode, then render to the
     * backbuffer and copy this to the screen. If in FSEM then use our buffer
     * strategy to do a page flip.
     */
    public void paintScreen() {
        if( this.screenMode == ScreenMode.PanelMode ) {
            Graphics2D graphics2D = (Graphics2D)this.panelImage.getGraphics();
            gameRender( graphics2D );
            graphics2D.dispose();
            
            graphics2D = (Graphics2D)this.renderPanel.getGraphics();
            graphics2D.drawImage( panelImage, 0, 0, null );
            graphics2D.dispose();
        } else {
            try {
                Graphics2D graphics2D = (Graphics2D)bufferStrategy.getDrawGraphics();
                
                gameRender( graphics2D );
                graphics2D.dispose();
                
                if ( bufferStrategy.contentsLost() == false )
                    bufferStrategy.show();
            } catch( Exception exception ) {
                System.err.println(
                        "ScreenManager.paintScreen: Error cannot paint screen:" 
                        + exception.toString() );
                exception.printStackTrace();
            }
        }
    }
    
    /**
     * Render the screen, i.e. whatever the benchmark wishes to show
     */
    protected void gameRender( Graphics2D graphics2D ) {};
    
    
    ///////////////////////////////////////////////////////////////////////////
    // Methods: Init FSEM                                                    //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Attempt to initialise FSEM.
     *
     * @return boolean true if FSEM can be initialised at the desired dimension
     *         and colour depth, otherwise false
     */
    private boolean initialiseFullScreen() {
        this.setUndecorated( true );
        this.setIgnoreRepaint( true );
        this.setResizable( false );
        
        this.graphicsDevice =
                GraphicsEnvironment.getLocalGraphicsEnvironment().
                    getDefaultScreenDevice();
        
        // Check to see if FSEM is supported and if the target display mode is available
        // and if the desired buffer strategy can be created.
        if( this.graphicsDevice.isFullScreenSupported() ) {
            this.graphicsDevice.setFullScreenWindow( this );
            if( this.setDisplayMode( 
                    this.screenWidth, this.screenHeight, this.screenColorDepth ) )
                if( this.setBufferStrategy() )
                    return true;
        }
        
        return false;
    }
    
    /**
     * Attempt to set the display mode to that specified.
     *
     * @param  screenWidth pixel width of the desired display mode
     * @param  screenHeight pixel height of the desired display mode
     * @param  screenColorDepth pixel colour depth of the desired display mode
     * @return boolean true if the display mode can be changed to the desired dimension
     *         and colour depth, otherwise false
     */
    private boolean setDisplayMode( 
            int screenWidth, int screenHeight, int screenColorDepth ) {
        if( this.graphicsDevice.isDisplayChangeSupported() == false
                || this.isDisplayModeAvailable(
                screenWidth, screenHeight, screenColorDepth) == false )
            return false;
        
        DisplayMode targetDisplayMode
                = new DisplayMode(
                    screenWidth, screenHeight, screenColorDepth, 
                        DisplayMode.REFRESH_RATE_UNKNOWN );
        try {
            this.graphicsDevice.setDisplayMode( targetDisplayMode );
        } catch( IllegalArgumentException exception ) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Determine if the specified display mode is available on the current graphics device.
     *
     * @param  screenWidth pixel width of the desired display mode
     * @param  screenHeight pixel height of the desired display mode
     * @param  screenColorDepth pixel colour depth of the desired display mode
     * @return boolean true if the display mode is available at the desired dimension
     *         and colour depth, otherwise false
     */
    private boolean isDisplayModeAvailable( 
            int screenWidth, int screenHeight, int screenColorDepth ) {
        DisplayMode[] displayModes = this.graphicsDevice.getDisplayModes();
        
        for( DisplayMode mode : displayModes ) {
            if( screenWidth == mode.getWidth() && screenHeight == mode.getHeight()
            && screenColorDepth == mode.getBitDepth() )
                return true;
        }
        
        return false;
    }
    
    /**
     * Attempt to create the desired FSEM buffer strategy for the current graphics
     * device (e.g. FSEM_NUM_PAGE_BUFFERS = 2 then two buffers will be created,
     * one a 'primary surface' for display and another a 'back buffer' for
     * screen rendering.
     *
     * Note: This method will also update the class bufferStrategy reference to
     * refer to the created buffer strategy.
     *
     * @return boolean true if the desired buffered strategy could be created,
     * otherwise false
     */
    private boolean setBufferStrategy() {
        try {
            // If this process is not running from the event dispatch thread
            // (which is desirable) then make use of invoke and wait to avoid
            // a potential deadlock situation. This may be an unncessary
            // precaution in Java 1.6
            if( EventQueue.isDispatchThread() == false ) {
                EventQueue.invokeAndWait( new Runnable() {
                    public void run() {
                        createBufferStrategy( FSEM_NUM_PAGE_BUFFERS );  }
                });
            } else
                this.createBufferStrategy( FSEM_NUM_PAGE_BUFFERS );
            
            // Sleep for a short interval to provide adequate time for the buffer
            // strategy to be created.
            Thread.sleep( 500 );
        } catch( Exception exception ) {
            return false;
        }
        
        // Store the created buffered strategy for use within the render cycle
        this.bufferStrategy = getBufferStrategy();
        
        return true;
    }
    
    /**
     * Exit from FSEM mode, restoring the previous display mode
     */
    protected void restoreScreen() {
        if( this.graphicsDevice == null )
            return;
        
        Window window = this.graphicsDevice.getFullScreenWindow();
        if( window != null )
            window.dispose();
        
        this.graphicsDevice.setFullScreenWindow( null );
    }
}