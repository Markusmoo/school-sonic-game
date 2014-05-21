package game.engine;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferStrategy;
import javax.swing.JFrame;
import java.util.*;
import game.assets.*;

/**
 * GameEngine provides the core mechanism for managing a game FSEM update/render cycle
 * <P>
 * GameEngine is based on a JFrame running in full screen exclusive mode (FSEM), i.e.
 * this is output device used by the game. The GameEngine also contains: an
 * <B>AssetManager</B> instance providing asset storage and retrieval functionality;
 * a <B>GameInputEventManager</B> instance providing game keyboard and mouse input;
 * a collection of <B>GameLayer</B> instances providing the different layers within 
 * the game. A <B>GameStatisticsRecorder</B> instance providing performance 
 * measurement functionality. Other than the above, the GameEngine provides performance 
 * management for an update/render cycle based upon consideration of game input, update 
 * of active game layers and display of visible game layers.
 * <P>
 * It is intended that an instance of this class will be constructed and provided with
 * a number of GameLayer instances containing the relevant update/render processes.
 * When the game is ready to run, a call to gameStart will permit this class to
 * run in FSEM and commence the update/render cycle.
 * <P>
 * Note: the update/render cycle implemented within this class is closely based upon the
 * that available within Davison's 'Killer Game Programming in Java'.
 *
 * @author <A HREF="mailto:P.Hanna@qub.ac.uk">Philip Hanna</A>
 * @version $Revision: 1.1 $ $Date: 2007/08 $
 *
 * @see AssetManager
 * @see GameInputEventManager
 * @see GameLayer
 * @see GameStatisticsRecorder
 */
public class GameEngine extends JFrame implements Runnable {

    ///////////////////////////////////////////////////////////////////////////
    // Class data: Update/Render Control Parameters                          //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Constant variable used to determine the number of page buffers requested in FSEM
     */
    private static final int FSEM_NUM_PAGE_BUFFERS = 2;

    /**
     * Constant variable used to determine the default target number of update triggers
     * in a one second interval. A game render will be considered for each update
     * trigger, e.g. an a target update of 50 per second will provide a target of
     * 50 renders per second. The number of renders per second will be reduced as
     * necessary in order to maintain the target number of updates per second.
     */
    private static final int DEFAULT_TARGET_UPDATES_PER_SECOND = 60;

    /**
     * MAX_NUM_ITERATIONS_WITHOUT_SLEEP defines the maximum number of update
     * iterations that can be successively performed without the game update/render
     * loop yielding to other thread processes. MAX_NUM_UPDATES_WITHOUT_RENDER
     * defines the maximum number of game updates that can be performed without
     * a render. These variables can be used to determine how the game should
     * perform under heavy loading (either in terms of providing responsiveness
     * to other threads, including the event dispatch thread, or forcing each
     * update to be followed by a render). Note: by default the 
     * MAX_NUM_UPDATES_WITHOUT_RENDER is set to zero, i.e. renders will not be
     * skipped. This value should be increased if it is important that the 
     * number of updates per second remain constaint.
     */
    protected static int MAX_NUM_ITERATIONS_WITHOUT_SLEEP = 8;
    protected static int MAX_NUM_UPDATES_WITHOUT_RENDER = 0;

    /**
     * Boolean flag which, if set, will force the next render following the 
     * current update tick to be displayed. 
     */
    private boolean doNotSkipNextRender = false;

    /**
     * Variable holding the duration (in ns) of the target game update period. The
     * update period will be set within the constructor to provide the default
     * target number of game updates per second (although this can be changed at
     * runtime).
     */
    protected long gameUpdatePeriod;

    /**
     * Update counter, initially set to zero and increasing by 1 per update
     * iteration.
     */
    public static long updateCounter = 0;


    ///////////////////////////////////////////////////////////////////////////
    // Class data: Update/Render Process                                     //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Thread process on which the render/update process will be run.
     */
    private Thread updateRenderProcess;

    /**
     * The running boolean variable specifies if the update/render process is
     * currently running. Setting this flag to false will result in the termination
     * of a running update/render process.
     */
    protected volatile boolean running = false;


    ///////////////////////////////////////////////////////////////////////////
    // Class data: Graphical Device Parameters                               //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * A reference to the current graphics device, buffer strategy and graphics
     * object for drawing to the graphics device
     */
    private GraphicsDevice graphicsDevice;
    private BufferStrategy bufferStrategy;
    private Graphics2D graphics2D;

    /**
     * Stores the current screen width. The variable is declared as public to
     * permit direct (i.e. fast) access from game layer / game object instances.
     */
    public int screenWidth;

    /**
     * Stores the current screen height. The variable is declared as public to
     * permit direct (i.e. fast) access from game layer / game object instances.
     */

    public int screenHeight;

    /**
     * Stores the current screen colour depth. The variable is declared as public to
     * permit direct (i.e. fast) access from game layer / game object instances.
     */
    public int screenColorDepth;


    ///////////////////////////////////////////////////////////////////////////
    // Class data: Auxiliary Game Engine Objects                             //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * The statistics recorded can be used to measure the performance of the
     * update/render process (in terms of UPS, FPS, etc.). It will be 
     * automatically displayed in the top-left hand corner if the screen if the
     * drawGameStatistics flag is set.
     */
    protected GameStatisticsRecorder gameStatisticsRecorder;
    protected boolean drawGameStatistics = false;

    /**
     * A public AssetManager reference for ease of use by game object
     * instances. It is intended that prior to game commencement the AssetManager
     * will be populated with the various graphical and sound assets. It is
     * also intended that game objects will access the AssetManager in order to
     * retrieve the assets they will use.
     */
    public AssetManager assetManager;

    /**
     * A public GameInputEventManager reference for ease of use by
     * game layer and game object instances. It is intended the
     * GameInputEventManager will provide the primiary means of gaining
     * keyboard and mouse input.
     */
    public GameInputEventManager inputEvent;

    /**
     * A public Random reference for ease of use by game layer and 
     * game object instances, i.e. Random objects do not need to be
     * created on demand.
     */
    public Random randomiser;
        
    /**
     * A linked hashmap of GameLayer reference that comprise the
     * game. It is intended that each game will consist of a number of
     * differnet layers, with each layer consisting of different game
     * objects.
     */
    public LinkedHashMap<String, GameLayer> gameLayers;

    /**
     * An sorted list of game layer instances (mirroring those held within the
     * gameLayers map) sorted in ascending draw order, i.e. lower-level draws
     * are drawn first, followed by higher level layers.
     */
    protected ArrayList<GameLayer> gameLayersDrawOrderSorted;

    /**
     * Game queues to add and remove layers. This is needed to ensure safe
     * addition and removal of game layers once the game is running (i.e.
     * it is important to avoid modification of the game layer hash map
     * whilst at the same time iterating over the collection).
     */
    private ArrayList<GameLayer> queuedGameLayersToAdd;
    private ArrayList<GameLayer> queuedGameLayersToRemove;

    
    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Create a new game engine. The game will not start until the gameStart
     * method is invoked. Prior to invoking gameStart the various game
     * layers comprising the game and associated graphical and sound assets
     * should be added to the game engine.
     */
    public GameEngine() {
        // Ensure that the JVM version is at least 1.6
        StringTokenizer tokenizer = 
                new StringTokenizer( System.getProperty("java.version"), ".-" );
        if( Integer.parseInt(tokenizer.nextToken()) == 1
                && Integer.parseInt(tokenizer.nextToken()) < 6) {
            System.out.println( "GameEngine.constructor: " +
                    "ERROR: Java version 6, or above, must be installed" );
            System.exit(-1);
        }
        
        // Define target game update period (in ns)
        gameUpdatePeriod = 1000000000 / DEFAULT_TARGET_UPDATES_PER_SECOND;

        assetManager = new AssetManager();
        gameLayers = new LinkedHashMap<String, GameLayer>();
        gameLayersDrawOrderSorted = new ArrayList<GameLayer>();
        inputEvent = new GameInputEventManager();
        randomiser = new Random();

        queuedGameLayersToAdd = new ArrayList<GameLayer>();
        queuedGameLayersToRemove = new ArrayList<GameLayer>();

        gameStatisticsRecorder = new GameStatisticsRecorder(this, this.gameUpdatePeriod);
        assetManager.registerGameStatisticsRecorder(gameStatisticsRecorder);

        // Add a shutdown hook to the thread that will attempt to perform game
        // shut down actions, including existing FSEM, in the event of a forced
        // termination.
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                running = false;
                gameShutdownActions();
            }
        });
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods: Game Start                                                   //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Start the game update/render cycle at the specified screen dimensions
     * and colour depth.
     *
     * @param  screenWidth pixel width of the desired FSEM mode
     * @param  screenHeight pixel height of the desired FSEM mode
     * @param  screenColorDepth pixel colour depth of the desired FSEM mode
     * @return boolean true if the game is now running at the specified screen
     *         dimensions and colour depth, otherwise false
     */
    public boolean gameStart(int screenWidth, int screenHeight, int screenColorDepth) {
        // Store the target screen dimensions and colour depth
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.screenColorDepth = screenColorDepth;

        // Enable the GameInputEventManager to handle key and mouse events from the game
        addKeyListener(inputEvent);
        addMouseListener(inputEvent);
        addMouseMotionListener(inputEvent);
        addMouseWheelListener(inputEvent);

        // By default initially turn the mouse pointer 'off'.
        setMouseCursor(null, 0, 0);

        // Display loading screen, build asset manager and construct initial layesr
        displaySplashScreen();
        if (buildAssetManager()) {
            removeSplashScreen();
            if (!buildInitialGameLayers()) {
                return false;
            }
        } else {
            return false;
        }
        
        try {
            // Attempt to initialise FSEM using the specified dimensions and depth
            if (initialiseFullScreen() == false) {
                System.err.println("GameEngine.gameStart: " +
                        "Cannot initialise full screen mode.");
                restoreScreen();
                return false;
            } else {
                gameStatisticsRecorder.recordInitialAvailableVideoMemory();
            }
            
            // Start the update/render process if not already running
            if (updateRenderProcess == null || running == false) {
                updateRenderProcess = new Thread(this);
                updateRenderProcess.start();
            }
        } catch (Exception exception) {
            // If any exceptions are thrown attempt to restore the screen
            restoreScreen();
            return false;
        }

        return true;
    }

    /**
     * If appropriate display an initial splash screen whilst the assets are
     * loaded. It is intended that method is overloaded and be used to
     * display a suitable introductory screen (if necessary).
     */
    protected void displaySplashScreen() {
    }

    /**
     * Attempt to load initial graphical and sound assets required within the game.
     * Loaded assets are added to the game engine's asset manager.
     *
     * @return boolean true if the initial assets could be loaded, otherwise false
     */
    protected boolean buildAssetManager() {
        return true;
    }

    /**
     * Remove any splash screen aspects once all the initial assets has been loaded
     */
    protected void removeSplashScreen() {
    }

    /**
     * Attempt to build the initial game layers required within the game.
     *
     * @return boolean true if the initial game layers could be created,
     *         otherwise false
     */
    protected boolean buildInitialGameLayers() {
        return true;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods: FSEM                                                         //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Attempt to initialise FSEM.
     *
     * @return boolean true if FSEM can be initialised at the desired dimension
     *         and colour depth, otherwise false
     */
    private final boolean initialiseFullScreen() {
        setUndecorated(true);
        setIgnoreRepaint(true);
        setResizable(false);

        graphicsDevice = GraphicsEnvironment.
                getLocalGraphicsEnvironment().getDefaultScreenDevice();

        // Check to see if FSEM is supported and if the target display mode is
        // available and if the desired buffer strategy can be created.
        if (graphicsDevice.isFullScreenSupported()) {
            graphicsDevice.setFullScreenWindow(this);
            if (setDisplayMode(screenWidth, screenHeight, screenColorDepth)) {
                if (setBufferStrategy()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Attempt to set the display mode to that specified.
     *
     * @param  screenWidth pixel width of the desired display mode
     * @param  screenHeight pixel height of the desired display mode
     * @param  screenColorDepth pixel colour depth of the desired display mode
     * @return boolean true if the display mode can be changed to the desired 
     *         dimension and colour depth, otherwise false
     */
    private final boolean setDisplayMode(
            int screenWidth, int screenHeight, int screenColorDepth) {
        if (graphicsDevice.isDisplayChangeSupported() == false) {
            return false;
        }
        boolean displayModeAvailable = false;
        DisplayMode[] displayModes = graphicsDevice.getDisplayModes();
        for (DisplayMode mode : displayModes) {
            if (screenWidth == mode.getWidth() && screenHeight == mode.getHeight() 
                    && screenColorDepth == mode.getBitDepth()) {
                displayModeAvailable = true;
            }
        }
        
        if (displayModeAvailable == false) {
            return false;
        }
        
        DisplayMode targetDisplayMode = 
                new DisplayMode(screenWidth, screenHeight, 
                    screenColorDepth, DisplayMode.REFRESH_RATE_UNKNOWN);
        try {
            graphicsDevice.setDisplayMode(targetDisplayMode);
        } catch (IllegalArgumentException exception) {
            return false;
        }

        return true;
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
    private final boolean setBufferStrategy() {
        try {
            // If this process is not running from the event dispatch thread
            // (which is desirable) then make use of invoke and wait to avoid
            // a potential deadlock situation. This may be an unncessary
            // precaution in Java 1.6
            if (EventQueue.isDispatchThread() == false) {
                EventQueue.invokeAndWait(new Runnable() {

                    public void run() {
                        createBufferStrategy(FSEM_NUM_PAGE_BUFFERS);
                    }
                });
            } else {
                createBufferStrategy(FSEM_NUM_PAGE_BUFFERS);
            }
            
            // Sleep for a short interval to provide adequate time for the buffer
            // strategy to be created.
            Thread.sleep(500);
        } catch (Exception exception) {
            return false;
        }

        // Store the created buffered strategy for use within the render cycle
        bufferStrategy = getBufferStrategy();

        return true;
    }

    /**
     * Exit from FSEM mode, restoring the previous display mode
     */
    private void restoreScreen() {
        if (graphicsDevice == null) {
            return;
        }
        Window window = graphicsDevice.getFullScreenWindow();
        if (window != null) {
            window.dispose();
        }
        graphicsDevice.setFullScreenWindow(null);
    }
    
    /**
     * Return the display modes that are supported on the screen device
     *
     * @return DisplayMode[] array of supported DisplayMode references
     */
    public final DisplayMode[] getAvailableDisplayModes() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDisplayModes();
    }

    /**
     * Change the display  mode to that specified.
     * <P>
     * Note: Care should be exercised when using this module to ensure that
     * it is only called with a supported screen mode.
     *
     * @return DisplayMode[] array of supported DisplayMode references
     */
    public final void changeDisplayMode(int width, int height, int colourDepth) {
        if (setDisplayMode(width, height, colourDepth)) {
            if (setBufferStrategy()) {
                screenWidth = width;
                screenHeight = height;
                screenColorDepth = colourDepth;
            }
        }
    }

    
    ///////////////////////////////////////////////////////////////////////////
    // Methods: Update/Render Cycle                                          //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Core update/render cycle thread run activity (which is initiated by calling
     * the gameStart method.
     * <P>
     * The update/render cycle will attempt to maintain the target number of
     * updates per second. Where necessary (depending MAX_NUM_UPDATES_WITHOUT_RENDER)
     * the number of renders per second may be reduced to maintain the target
     * number of updates per second.
     * <P>
     * During each cycle an opportunity is provided to consider the current input
     * status. This is then followed by an update of all active game layers.
     * Finally, and conditionally, each visible game layer will be rendered to the
     * current graphics device.
     */
    public final void run() {
        running = true;

        /**
         * Define variables which will be used to provide timing information to
         * enable precise control of the update/render cycle.
         *
         * The timeBefore and timeAfter variables respectively record the time
         * before and time immediately after a joint update/render step.
         *
         * sleepTime records how long the thread should sleep before it is
         * necessary to start on the next update/render cycle (this may be
         * a negative period - i.e. the update/render process took longer than
         * desired). overSleepTime records how much longer the thread sleep than
         * was originally requested (i.e. accounting for the unpredictable delay
         * in waking up the thread).
         *
         * renderTimeBeyondPeriod maintains a cumulative count of how much time
         * has been spent beyond that needed to maintain the target number of
         * update per second. If this period exceeds the target update period
         * then (conditionally) one, or more, renders may be skipped in an
         * attempt to maintain the target number of updates per second.
         *
         * numIterationsWithoutSleep records the total number of cycle which
         * have elapsed without this thread sleeping. Should this variable
         * exceed MAX_NUM_UPDATES_WITHOUT_RENDER then the update/render thread
         * will yield to provide other threads an opportunity to execute.
         */
        long timeBefore, timeMid, timeAfter;
        long sleepTime, overSleepTime = 0L;
        long renderTimeBeyondPeriod = 0L;
        int numIterationsWithoutSleep = 0;

        // Prepare to start recording update/render statistics
        gameStatisticsRecorder.startRecordingStatistics();

        try {
            while (running) {
                timeBefore = System.nanoTime();
                gameStatisticsRecorder.recordStartTime(timeBefore);

                // Consider global game input, then update all active layers,
                // render all visible layers and show the rendered scene
                considerInput();
                gameUpdate();
                gameStatisticsRecorder.recordUpdateTime(System.nanoTime());
                gameRender();
                gameStatisticsRecorder.recordRenderTime(System.nanoTime());
                showScreen();

                timeAfter = System.nanoTime();

                // Determine how long we need to sleep before the next update/render 
                // cycle is due. This may be a negative number.
                sleepTime = (gameUpdatePeriod - (timeAfter - timeBefore)) - overSleepTime;

                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime / 1000000L); // Covert ns into ms
                    } catch( InterruptedException exception ) { }

                    // Determine how much longer we slept than was originally requested
                    overSleepTime = (System.nanoTime() - timeAfter) - sleepTime;
                } else {
                    // Keep a track of the cumulative amount of time that the 
                    // update/render cycle has taken beyond that required to 
                    // maintain the target number of updates per second.
                    renderTimeBeyondPeriod -= sleepTime;
                    overSleepTime = 0L;

                    if (++numIterationsWithoutSleep >= MAX_NUM_ITERATIONS_WITHOUT_SLEEP) {
                        Thread.yield();
                        numIterationsWithoutSleep = 0;
                    }
                }

                // If necessary, skip one or more game renders in an attempt to 
                // save some time and get back onto the target number of updates 
                // per second.
                int renderSkips = 0;
                while (renderTimeBeyondPeriod > gameUpdatePeriod 
                        && renderSkips < MAX_NUM_UPDATES_WITHOUT_RENDER) {
                    if (doNotSkipNextRender) {
                        doNotSkipNextRender = false;
                        renderTimeBeyondPeriod = 0;
                    } else {
                        renderTimeBeyondPeriod -= gameUpdatePeriod;
                        gameUpdate();

                        renderSkips++;
                    }
                }

                // Record game statistics for this update/render
                gameStatisticsRecorder.storeStats(renderSkips);
            }
        } finally {
            // No matter how we exit form the run loop, always execute the game 
            // shutdown actions
            gameShutdownActions();
        }
    }

    /**
     * Consider the current game input state.
     * <P>
     * Note: By default this method will terminate the game if the ESCAPE key is 
     * pressed. Classes which extent GameEngine may override this functionality 
     * as desired. It is intended that this method will provide core, game-wide 
     * input event handling. It is not intended that this method will provide game 
     * layer specified event handling (which is better confined to the update 
     * method within each layer - i.e. if the layer is inactive then input events 
     * for that layer will not be triggered).
     */
    protected void considerInput() {
        if (inputEvent.keyTyped(KeyEvent.VK_ESCAPE)) {
            running = false;
        }
        
        if (inputEvent.keyPressed[KeyEvent.VK_CONTROL] 
                && inputEvent.keyTyped(KeyEvent.VK_B)) {
            boolean drawGameObjectDebugInformation = false;
            for (GameLayer gameLayer : gameLayers.values()) {
                if (gameLayer.drawGameObjectDebugInformation == true) {
                    drawGameObjectDebugInformation = true;
                }
            }
            for (GameLayer gameLayer : gameLayers.values()) {
                gameLayer.drawGameObjectDebugInformation 
                        = !drawGameObjectDebugInformation;
            }
        }

        if (inputEvent.keyPressed[KeyEvent.VK_CONTROL] 
                && inputEvent.keyTyped(KeyEvent.VK_I)) {
            drawGameStatistics = !drawGameStatistics;
        }
    }

    /**
     * Invoke the update method of each active game layer attached to this game 
     * engine.
     * <P>
     * Note: If any game layer addition and/or game layer removal requests have 
     * been queued they will be executed before the layer update commences.
     */
    protected final void gameUpdate() {
        // Increase the update conunter;
        updateCounter++;

        for (GameLayer gameLayer : gameLayers.values()) {
            if (gameLayer.getActivity() == GameLayer.GameLayerActivity.ACTIVE) {
                gameLayer.update();
            }
        }
        
        if (queuedGameLayersToRemove.size() > 0) {
            for (GameLayer gameLayer : queuedGameLayersToRemove) {
                gameLayersDrawOrderSorted.remove(gameLayer);
                gameLayers.remove(gameLayer.gameLayerName);
            }
            queuedGameLayersToRemove.clear();
        }

        if (queuedGameLayersToAdd.size() > 0) {
            for (GameLayer gameLayer : queuedGameLayersToAdd) {
                gameLayers.put(gameLayer.gameLayerName, gameLayer);
                gameLayersDrawOrderSorted.add(gameLayer);
                sortGameLayersOnDrawOrder();
            }
            queuedGameLayersToAdd.clear();
        }
    }

    /**
     * Render the next frame to a Graphics object obtained from the current
     * buffer strategy.
     */
    private void gameRender() {
        try {
            // Render each visible game layer to an availalbe graphics buffer
            graphics2D = (Graphics2D) bufferStrategy.getDrawGraphics();
            gameRender(graphics2D);

            if (drawGameStatistics) {
                gameStatisticsRecorder.drawStatistics(graphics2D, 0, 0);
            }
            
            graphics2D.dispose();
        } catch (Exception exception) {
            System.err.println("GameEngine.gameRender: " +
                    "Error cannot render screen:" + exception.toString());
            exception.printStackTrace();

            running = false;
        }
    }

    /**
     * Render the each visible game layer to an available graphics buffer
     * obtained from the bufferStrategy
     * <P>
     * Note: This particular render does not clear the background prior to
     * the render. If desired the following lines should be added before 
     * a call:
     * graphics2D.setColor( Color.black );
     * graphics2D.fillRect( 0, 0, this.screenWidth, this.screenHeight );
     * 
     * @param graphics2D Graphics2D object to perform the render
     */
    protected void gameRender(Graphics2D graphics2D) {
        for (int layerIdx = 0; layerIdx < gameLayersDrawOrderSorted.size(); layerIdx++) {
            GameLayer gameLayer = gameLayersDrawOrderSorted.get(layerIdx);
            if (gameLayer.getVisibility() == GameLayer.GameLayerVisibility.VISIBLE) {
                gameLayer.draw(graphics2D);
            }
        }
    }

    /**
     * Inform the game engine that the next render following the current update
     * should not be skipped.
     */
    public final void doNotSkipNextRender() {
        doNotSkipNextRender = true;
    }

    /**
     * Request that the buffer strategy show the rendered screen
     */
    private final void showScreen() {
        try {
            if (bufferStrategy.contentsLost() == false) {
                bufferStrategy.show();

                // Apparently on some platforms (e.g. maybe Linux) there may
                // be a need to sync the display. Uncomment if appropiate.
                //Toolkit.getDefaultToolkit().sync();
            }
        } catch (Exception exception) {
            System.err.println("GameEngine.showScreen: " +
                    "Error cannot show screen:" + exception.toString());
            exception.printStackTrace();

            running = false;
        }
    }

    /**
     * Execute all appropiate shutdown action upon termination of the update/render
     * cycle.
     *
     * Note: By default this method simply exits from FSEM and terminates any 
     * assets. Extending classes should overload this method as appropriate.
     */
    protected void gameShutdownActions() {
        restoreScreen();
        assetManager.performGameShutDownActions();
    }

    /**
     * Terminate the update/render cycle.
     */
    public final void terminateProcess() {
        running = false;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods: Add/Remove/Set/Get methods                                   //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Set the target number of game updates per second.
     *
     * @param targetNumberGameUpdatesPerSecond target number of game updates per second
     */
    public final void setTargetGameUpdatesPerSecond(
            int targetNumberGameUpdatesPerSecond) {
        gameUpdatePeriod = 1000000000 / targetNumberGameUpdatesPerSecond;
    }

    /**
     * Add the specified game layer to the game engine
     *
     * @param gameLayer GameLayer to add to the game engine
     */
    public final void addGameLayer(GameLayer gameLayer) {
        if (running == false) {
            gameLayers.put(gameLayer.gameLayerName, gameLayer);
            gameLayersDrawOrderSorted.add(gameLayer);
            sortGameLayersOnDrawOrder();
        } else {
            queuedGameLayersToAdd.add(gameLayer);
        }
    }

    /**
     * Remove the specified game layer from the game engine
     *
     * @param gameLayerName Name of the GameLayer to remove from the game engine
     *
     * @exception IllegalArgumentException if the specified game layer cannot be found
     */
    public final void removeGameLayer(String gameLayerName) {
        if (gameLayers.containsKey(gameLayerName) == false) {
            throw new IllegalArgumentException("GameEngine.removeGameLayer: " +
                    "Invalid game layer specified: " + gameLayerName);
        }
        
        if (running == false) {
            gameLayersDrawOrderSorted.remove(gameLayers.get(gameLayerName));
            gameLayers.remove(gameLayerName);
        } else {
            queuedGameLayersToRemove.add(getGameLayer(gameLayerName));
        }
    }

    /**
     * Obtain a reference to the specified game layer from the game engine
     *
     * @param gameLayerName String name of the game layer to obtain
     * @return GameLayer reference to the requested game layer
     *
     * @exception IllegalArgumentException if the specified game layer cannot be found
     */
    public final GameLayer getGameLayer(String gameLayerName) {
        if (gameLayers.containsKey(gameLayerName) == false) {
            throw new IllegalArgumentException("GameEngine.getGameLayer: " +
                    "Invalid game layer specified: " + gameLayerName);
        }
        
        return gameLayers.get(gameLayerName);
    }

    /**
     * Obtain a reference to the specified game object from the specified
     * game layer.
     *
     * @param gameObject String name of the game object to obtain
     * @param gameLayerName String name of the game layer to obtain
     * @return GameObject reference to the requested game object
     */
    public final GameObject getGameObjectFromLayer(
            String gameObject, String gameLayerName) {
        return gameLayers.get(gameLayerName).getGameObject(gameObject);
    }

    /**
     * Return the current game update period (in ns)
     *
     * @return long containing game update period in ns
     */
    public final long getGameUpdatePeriod() {
        return gameUpdatePeriod;
    }

    /**
     * Return the game statistics recorded used by this game engine
     *
     * @return GameStatisticsRecorder used by this game engine
     */
    public final GameStatisticsRecorder getGameStatisticsRecorder() {
        return gameStatisticsRecorder;
    }

    /**
     * Set the mouse cursor to the specified graphical asset, using the specified
     * mouse clickpoint offset. If a null mouseAssetName is specified then the
     * mouse cursor will be turned 'off'.
     *
     * @param mouseAssetName String name of graphical asset to be used as the pointer
     * @param pointX int x offset of mouse click hot point
     * @param pointY int y offset of mouse click hot point
     */
    public final void setMouseCursor(String mouseAssetName, int pointX, int pointY) {
        // If a null mouseAsset has been specified then 'blank' the mouse cursor
        if (mouseAssetName == null) {
            setCursor(getToolkit().createCustomCursor(
                    new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), 
                    new Point(0, 0), "null"));
        } else {
            BufferedImage mouseImage = ((ImageAsset) assetManager
                    .retrieveAsset( mouseAssetName )).getImageRealisation();
            setCursor(getToolkit().createCustomCursor(
                    mouseImage, new Point(pointX, pointY), "MousePointer"));
        }
    }

    /**
     * Do not display the mouse cursor
     */
    public final void setNullMouseCursor() {
        setMouseCursor(null, 0, 0);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods: Miscellaneous                                                //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Sort the available game layers into the correct draw order.
     * <P>
     * Note: This method uses a modified form of bubble sort to order the 
     * game layers into the correct sequence. This form of algorithm will 
     * work faster than other forms of sort algorithm, e.g. quick sort, 
     * merge sort, etc. if the data is nearly in sorted order and only a 
     * few elements need to be 'corrected' - which will be the case for 
     * adding a game layer to an already sorted set of game layers.
     */
    protected final void sortGameLayersOnDrawOrder() {
        GameLayer tempGameObject;
        for (int i = 0; i < gameLayersDrawOrderSorted.size() - 1; i++) {
            if (gameLayersDrawOrderSorted.get(i).drawOrder 
                    > gameLayersDrawOrderSorted.get(i + 1).drawOrder) {
                tempGameObject = gameLayersDrawOrderSorted.get(i);
                gameLayersDrawOrderSorted.set(i, gameLayersDrawOrderSorted.get(i + 1));
                gameLayersDrawOrderSorted.set(i + 1, tempGameObject);

                int j = i;
                while (j > 0 && gameLayersDrawOrderSorted.get(j).drawOrder 
                        < gameLayersDrawOrderSorted.get(j - 1).drawOrder) {
                    tempGameObject = gameLayersDrawOrderSorted.get(j);
                    gameLayersDrawOrderSorted.set(j, gameLayersDrawOrderSorted.get(j - 1));
                    gameLayersDrawOrderSorted.set(j - 1, tempGameObject);
                    j--;
                }

                j = i + 1;
                while (j < gameLayersDrawOrderSorted.size() - 1 && 
                        gameLayersDrawOrderSorted.get(j).drawOrder 
                            > gameLayersDrawOrderSorted.get(j + 1).drawOrder) {
                    tempGameObject = gameLayersDrawOrderSorted.get(j);
                    gameLayersDrawOrderSorted.set(j, gameLayersDrawOrderSorted.get(j + 1));
                    gameLayersDrawOrderSorted.set(j + 1, tempGameObject);
                    j++;
                }
            }
        }
    }
}