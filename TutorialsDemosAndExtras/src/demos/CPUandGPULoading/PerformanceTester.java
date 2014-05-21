package demos.CPUandGPULoading;

import game.assets.GraphicalAsset;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Random;

/**
 * PerformanceTester provides a somewhat crude graphical benchmark program.
 * It is mostly intended to high light the difference between FSEM and
 * 'normal' drawing methods and Java (as well as exploring performance
 * differences between Java versions - Strong hint: use Java 1.6 in order to
 * get decent speed).
 * <P>
 * Note: This class is functional, but somewhat crude in terms of its
 * construction. It is largely based on a cut down version of the
 * GameEngine/GameLayer classes - although with dual support for
 * FSEM and JPanel modes.
 *
 * @author <A HREF="mailto:P.Hanna@qub.ac.uk">Philip Hanna</A>
 * @version $Revision: 1 $ $Date: 2006/08 $
 */
public class PerformanceTester extends ScreenManager {
    
    ///////////////////////////////////////////////////////////////////////////
    // Class data                                                            //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Enumerated type used to define the different types of benchmark
     * test available and a variable recording the current test.
     */
    private enum PerformanceTest {
        SelectTest,
        OpaqueImageDrawRate,
        TransparentImageDrawRate,
        TranslucentImageDrawRate,
        ResizedImageDrawRate,
        FlippedImageDrawRate,
        FaddedImageDrawRate,
        RotatedImageDrawRate,
        BlurredImageDrawRate }

    private PerformanceTest performanceTest;
        
    /**
     * Screen/panel width and height
     */
    private static final int SCREEN_WIDTH = 1024;
    private static final int SCREEN_HEIGHT = 768;

    /**
     * Test duration - decreasing this will speed up how quickly the tests
     * terminate. Set to 10s by default - which appears to be long enough to
     * get consistent readings on successive tests.
     */
    private static final long TEST_TARGET_DURATION = 10000;

    /**
     * Batch size - this controls how many individual image tests will be
     * performed before a screen refresh is carried out. This has a significant
     * impact on the reported performance, e.g. when using JPanel the backbuffer
     * needs to be copied to video memory, if using FSEM with VSYNC enabled
     * then there will be a fixed limit as to the refresh time. Generally,
     * recommended to stick with a large batch size (i.e. 10,000) which will
     * report correct draw times, but return a figure not achievable in
     * practice as the cost of refreshing the display has been limited.
     */
    private static final int[] BATCH_SIZE = { 1, 10, 100, 1000, 10000 };

    /**
     * Separate batch sizes are retained for operations that involved image
     * transfer (possibly along with some form of compositing) compared to
     * those that require manipulation of the source image. In practice,
     * you can get away with keep the batch size at the default for both.
     */
    private int drawBatchSizeIdx = BATCH_SIZE.length -1;
    private int imageSFXBatchSizeIdx = BATCH_SIZE.length -1;

    /**
     * Variables recording the name of the test and, more usefully, the test
     * start time and end time.
     */
    private String testName = "";
    private long testStartTime;
    private long testEndTime;

    /**
     * This is the graphical asset (image) that will be used within the
     * tests. The size of the asset (in bytes) is also recorded (to be used
     * when working out the drawrate).
     */
    private GraphicalAsset drawAsset;
    private long drawAssetSize = 0;

    /**
     * A count is kept of the total number of images that were drawn
     * within the test.
     */
    private long drawAssetRenderCount = 0;

    /**
     * A Random instance is used to provide random draw coordinates.
     * A test counter (which is incremented is also maintained for any
     * test that need to make use of an increasing value, e.g. rotation
     */
    private Random randomiser;
    private int testCounter;

    /**
     * This class provides a number of core image effect algorithms, e.g.
     * stretching, blurring, etc. and is used within the tests.
     */
    private ImageSFXs imageSFXs;

    /**
     * When a test is run the screen will be cleared - this provides a
     * crude means of indicating that the test is running (otherwise with
     * a large batch size and a complex operation the user might think that
     * the test is not running - on account of the screen not changing).
     */
    private boolean needToClearScreenBeforeTest = false;


    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Create a new performance tester - both JPanel and FSEM modes are supported.
     *
     * @param  useFSEM If true, the tests will run in FSEM mode, otherwise the
     *         tests will run within a JPanel.
     */
    public PerformanceTester( boolean useFSEM ) {
        if( !buildAssetManager() ) {
            System.err.println( "ERROR: AssetManager cannot be built" );
            System.exit(-1);
        }

        this.screenHeight = SCREEN_HEIGHT;
        this.screenWidth = SCREEN_WIDTH;

        this.randomiser = new Random();
        this.imageSFXs = new ImageSFXs();

        if( useFSEM )
            startFSEM( SCREEN_WIDTH, SCREEN_HEIGHT, 32 );
        else
            startPanelMode( SCREEN_WIDTH, SCREEN_HEIGHT );
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods:                                                              //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Attempt to build the asset manager (loading an opaque,
     * transparent and translucent image to be used within the
     * tests).
     *
     * @return  true if succesful, otherwise false
     */
    public boolean buildAssetManager() {
        URL imageURL;

        imageURL = getClass().getResource( "images/MediumOpaque.png");
        assetManager.addImageAsset( "MediumOpaque", imageURL );

        imageURL = getClass().getResource( "images/MediumTransparent.png");
        assetManager.addImageAsset( "MediumTransparent", imageURL );

        imageURL = getClass().getResource( "images/MediumTranslucent.png");
        assetManager.addImageAsset( "MediumTranslucent", imageURL );

        return true;
    }

    /**
     * Core run method, which is executed once either the JPanel or FSEM
     * has been setup.
     */
    @Override
    public void run() {
        // Initially get the user to select which test they wish to run
        performanceTest = PerformanceTest.SelectTest;

        while( true ) {
            if( this.performanceTest == PerformanceTest.SelectTest ) {
                /**
                 * Termination options
                 */
                if( this.inputEvent.keyTyped( KeyEvent.VK_ESCAPE ) ) {
                    // Provide a means of quitting the program
                    this.restoreScreen();
                    return;
                }

                /**
                 * Batch size options
                 */
                else if( this.inputEvent.keyTyped( KeyEvent.VK_D ) ) {
                    // Toggle the draw image batch size
                    this.drawBatchSizeIdx = 
                            (this.drawBatchSizeIdx+1) % BATCH_SIZE.length;
                } else if( this.inputEvent.keyTyped( KeyEvent.VK_S ) ) {
                    // Toggle the image effect batch size
                    this.imageSFXBatchSizeIdx = 
                            (this.imageSFXBatchSizeIdx+1) % BATCH_SIZE.length;
                }

                /**
                 * Run test options
                 */
                else if( this.inputEvent.keyTyped( KeyEvent.VK_1 ) ) {
                    this.performanceTest = PerformanceTest.OpaqueImageDrawRate;
                    this.testName = "OpaqueImageDrawRate";
                    this.drawAsset = (GraphicalAsset)this.assetManager.retrieveAsset( "MediumOpaque" );
                    initialiseTest();
                } else if( this.inputEvent.keyTyped( KeyEvent.VK_2 ) ) {
                    this.performanceTest = PerformanceTest.TransparentImageDrawRate;
                    this.testName = "TransparentImageDrawRate";
                    this.drawAsset = (GraphicalAsset)this.assetManager.retrieveAsset( "MediumTransparent" );
                    initialiseTest();
                } else if( this.inputEvent.keyTyped( KeyEvent.VK_3 ) ) {
                    this.performanceTest = PerformanceTest.TranslucentImageDrawRate;
                    this.testName = "TranslucentImageDrawRate";
                    this.drawAsset = (GraphicalAsset)this.assetManager.retrieveAsset( "MediumTranslucent" );
                    initialiseTest();
                } else if( this.inputEvent.keyTyped( KeyEvent.VK_4 ) ) {
                    this.performanceTest = PerformanceTest.ResizedImageDrawRate;
                    this.testName = "ResizedImageDrawRate";
                    this.drawAsset = (GraphicalAsset)this.assetManager.retrieveAsset( "MediumTranslucent" );
                    initialiseTest();
                } else if( this.inputEvent.keyTyped( KeyEvent.VK_5 ) ) {
                    this.performanceTest = PerformanceTest.FlippedImageDrawRate;
                    this.testName = "FlippedImageDrawRate";
                    this.drawAsset = (GraphicalAsset)this.assetManager.retrieveAsset( "MediumTranslucent" );
                    initialiseTest();
                } else if( this.inputEvent.keyTyped( KeyEvent.VK_6 ) ) {
                    this.performanceTest = PerformanceTest.FaddedImageDrawRate;
                    this.testName = "FaddedImageDrawRate";
                    this.drawAsset = (GraphicalAsset)this.assetManager.retrieveAsset( "MediumTranslucent" );
                    initialiseTest();
                } else if( this.inputEvent.keyTyped( KeyEvent.VK_7 ) ) {
                    this.performanceTest = PerformanceTest.RotatedImageDrawRate;
                    this.testName = "RotatedImageDrawRate";
                    this.drawAsset = (GraphicalAsset)this.assetManager.retrieveAsset( "MediumTranslucent" );
                    initialiseTest();
                } else if( this.inputEvent.keyTyped( KeyEvent.VK_8 ) ) {
                    this.performanceTest = PerformanceTest.BlurredImageDrawRate;
                    this.testName = "BlurredImageDrawRate";
                    this.drawAsset = (GraphicalAsset)this.assetManager.retrieveAsset( "MediumTranslucent" );
                    initialiseTest();
                }
            }

            /**
             * Draw the screen - this will consist of either displaying the menu (if we
             * are not running a test, or display one complete batch run of an active
             * test).
             */
            this.paintScreen();

            /**
             * We should be nice and yield to other threads (certainly event dispatch ones).
             * Yielding is more of an issue when we wish to gather user input, we could
             * turn it off when running a benchmark if desired.
             */
            Thread.yield();
        }
    }

    /**
     * Prepare for a new test to run - i.e. record the start time, reset the
     * test counter, render count, etc.
     */
    private void initialiseTest() {
        this.testStartTime = System.nanoTime() / 1000000;

        this.drawAssetSize = drawAsset.width * drawAsset.height * 4;
        this.drawAssetRenderCount = 0 ;

        this.testCounter = 0;

        // Signify that we wish to clear the screen to indicate that the
        // benchmark is running
        this.needToClearScreenBeforeTest = true;
    }

    /**
     * Finalise the test, i.e. record the end time and go back into 'menu' mode
     */
    private void finaliseTest() {
        this.testEndTime = System.nanoTime() / 1000000;

        this.performanceTest = PerformanceTest.SelectTest;
    }

    /**
     * Perform a render - one of three different types of render are include:
     * A title page for when we wish the user to make a choice, a draw image
     * render and a special effect render.
     * <P>
     * Note: as the name for this method suggests, we are basing the code for
     * this on the GameEngine setup
     *
     * @param  graphics2D Location to which we are to render
     */
    @Override
    protected void gameRender( Graphics2D graphics2D ) {
        // Clear the screen once if requested
        if( this.needToClearScreenBeforeTest == true ) {
            graphics2D.setColor(Color.black);
            graphics2D.fillRect(0, 0, screenWidth, screenHeight);

            this.needToClearScreenBeforeTest = false;
            return;
        }

        if( this.performanceTest == PerformanceTest.SelectTest )
            this.renderTitleScreen( graphics2D );
        else if( performanceTest == PerformanceTest.OpaqueImageDrawRate
                || performanceTest == PerformanceTest.TransparentImageDrawRate
                || performanceTest == PerformanceTest.TranslucentImageDrawRate )
            this.renderDrawAsset( graphics2D );
        else if( performanceTest == PerformanceTest.ResizedImageDrawRate
                || performanceTest == PerformanceTest.FlippedImageDrawRate
                || performanceTest == PerformanceTest.FaddedImageDrawRate
                || performanceTest == PerformanceTest.RotatedImageDrawRate
                || performanceTest == PerformanceTest.BlurredImageDrawRate )
            this.renderImageSFX( graphics2D );
    }

    /**
     * Render the title screen - which include the menu options and also the
     * 'results' of the last benchmark run
     *
     * @param  graphics2D Location to which we are to render
     */
    private void renderTitleScreen( Graphics2D graphics2D ) {
        graphics2D.setColor(Color.black);
        graphics2D.fillRect(0, 0, screenWidth, screenHeight);

        graphics2D.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

        graphics2D.setFont( new Font( "SANS_SERIF", Font.PLAIN, 16 ) );
        this.drawOutlineText( graphics2D, "CSC217 - Games Programming", 30, 30, Color.blue, Color.white);
        graphics2D.setFont( new Font( "SANS_SERIF", Font.PLAIN, 22 ) );
        this.drawOutlineText( graphics2D, "Java Performance Measurement", 30, 55, Color.blue, Color.white);

        graphics2D.setFont( new Font( "SANS_SERIF", Font.BOLD, 16 ) );
        graphics2D.setColor( Color.white );
        graphics2D.drawString( "Performance Test Controls:", 30, 100 );

        graphics2D.setFont( new Font( "SANS_SERIF", Font.PLAIN, 16 ) );
        graphics2D.drawString( "1 - Opaque Image Draw Rate", 50, 120 );
        graphics2D.drawString( "2 - Transparent Image Draw Rate", 50, 140 );
        graphics2D.drawString( "3 - Translucent Image Draw Rate", 50, 160 );
        graphics2D.drawString( "4 - Resized Image Draw Rate", 50, 180 );
        graphics2D.drawString( "5 - Flipped Image Draw Rate", 50, 200 );
        graphics2D.drawString( "6 - Fadded Image Draw Rate", 50, 220 );
        graphics2D.drawString( "7 - Rotated Image Draw Rate", 50, 240 );
        graphics2D.drawString( "8 - Blurred Image Draw Rate", 50, 260 );

        graphics2D.drawString( "D - Change draw batch size (tests 1-3)", 50, 290 );
        graphics2D.drawString( "S - Change image sfx batch size (tests 4-8)", 50, 310 );

        graphics2D.setFont( new Font( "SANS_SERIF", Font.BOLD, 16 ) );
        graphics2D.setColor( Color.white );
        graphics2D.drawString( "Settings and Last Test Results", 350, 100 );

        graphics2D.setFont( new Font( "SANS_SERIF", Font.PLAIN, 16 ) );
        graphics2D.drawString( "Draw batch size = " + BATCH_SIZE[drawBatchSizeIdx], 370, 120 );
        graphics2D.drawString( "Image SFX batch size = " + BATCH_SIZE[imageSFXBatchSizeIdx], 370, 140 );

        graphics2D.drawString( "Last test: Name = " + this.testName, 370, 180 );
        graphics2D.drawString( "Last test: Image size = " + this.drawAssetSize/1024 + "kb", 370, 200 );

        graphics2D.drawString( "Last test: Image renders = " + (int)((double)this.drawAssetRenderCount
                / ((double)(this.testEndTime - this.testStartTime) / 1000.0)) + "/s", 370, 240 );
        graphics2D.drawString( "Last test: Draw rate = " +
                (int)((((double)this.drawAssetSize * (double)this.drawAssetRenderCount)/1048576.0)
                / ((double)(this.testEndTime - this.testStartTime) / 1000.0)) + "mb/s", 370, 260 );
    }

    /**
     * Silly little method which will provide a coloured outline around the
     * drawn text (by drawing a number of offset copies). Not particularly efficient,
     * nor necessary - still, it looks a bit better than plain text.
     *
     * @param  graphics2D Location to which we are to render
     * @param  text Text string to be drawn
     * @param  x x draw location
     * @param  y x draw location
     * @param  borderColour outline colour
     * @param  innerColour inner colour (doh)
     */
    private void drawOutlineText( Graphics2D graphics2D,
            String text, int x, int y, Color borderColor, Color innerColor ) {
        Color onEntryColor = graphics2D.getColor();

        graphics2D.setColor( borderColor );
        graphics2D.drawString( text, x+1, y+1 );
        graphics2D.drawString( text, x+1, y-1 );
        graphics2D.drawString( text, x-1, y+1 );
        graphics2D.drawString( text, x-1, y-1 );

        graphics2D.setColor( innerColor );
        graphics2D.drawString( text, x, y );

        graphics2D.setColor( onEntryColor );
    }

    /**
     * Perform a batch image draw. Have a peek at the time every
     * 100 draws to ensure we've not extended the benchmark
     * duration. The drawAssetRenderCount will be updated as
     * appropriate.
     *
     * @param  graphics2D Location to which we are to render
     */
    private void renderDrawAsset( Graphics2D graphics2D ) {
        int idx = 0;
        do
        {
            if( idx++ % 100 == 0 )
                if( System.nanoTime() / 1000000 > this.testStartTime + this.TEST_TARGET_DURATION ) {
                finaliseTest();
                this.drawAssetRenderCount += idx;
                return;
                }

            this.drawAsset.draw( graphics2D,
                    randomiser.nextInt( this.screenWidth - drawAsset.width ),
                    randomiser.nextInt( this.screenHeight - drawAsset.height ) );

        } while( idx < this.BATCH_SIZE[drawBatchSizeIdx] );
        this.drawAssetRenderCount += this.BATCH_SIZE[drawBatchSizeIdx];
    }

    /**
     * Perform a batch image effect draw. This method differs from the
     * renderDrawAsset method in that it does not have early termination if
     * we exceed the batch timer - it probably should have as on a slow
     * match with a large batch size we could get involved in this loop
     * for a long time. It won't adversely impact on the benchmark accuracy
     * as the endtime is only recorded once the test completes.
     * <P>
     * Note: This method makes heavy use of the ImageSFX class in order to
     * perform the various image manipulations
     *
     * @param  graphics2D Location to which we are to render
     */
    private void renderImageSFX( Graphics2D graphics2D ) {
        if( System.nanoTime() / 1000000 > this.testStartTime + this.TEST_TARGET_DURATION ) {
            finaliseTest();
            return;
        }

        this.testCounter++;

        BufferedImage assetImage = drawAsset.getImageRealisation();
        int assetWidth = assetImage.getWidth();
        int assetHeight = assetImage.getHeight();

        if( performanceTest == PerformanceTest.ResizedImageDrawRate ) {
            for( int idx = 0; idx < this.BATCH_SIZE[imageSFXBatchSizeIdx]; idx++ ) {
                double scaleWidth = this.randomiser.nextDouble() * 2.0;
                double scaleHeight = this.randomiser.nextDouble() * 2.0;

                int x = this.randomiser.nextInt( 
                        this.SCREEN_WIDTH - (int)((double)assetWidth * scaleWidth) );
                int y = this.randomiser.nextInt( 
                        this.SCREEN_HEIGHT - (int)((double)assetHeight * scaleHeight) );

                imageSFXs.drawResizedImage( graphics2D, assetImage, x, y, scaleWidth, scaleHeight );
            }
        }

        else if( performanceTest == PerformanceTest.FlippedImageDrawRate ) {
            for( int idx = 0; idx < this.BATCH_SIZE[imageSFXBatchSizeIdx]; idx++ ) {
                int x = this.randomiser.nextInt( this.SCREEN_WIDTH - assetWidth );
                int y = this.randomiser.nextInt( this.SCREEN_HEIGHT - assetHeight );

                if( idx %2 == 0 )
                    imageSFXs.drawVerticalFlip( graphics2D, assetImage, x, y);
                else
                    imageSFXs.drawHorizFlip( graphics2D, assetImage, x, y );
            }
        }

        else if( performanceTest == PerformanceTest.FaddedImageDrawRate ) {
            for( int idx = 0; idx < this.BATCH_SIZE[imageSFXBatchSizeIdx]; idx++ ) {
                int x = this.randomiser.nextInt( this.SCREEN_WIDTH - assetWidth );
                int y = this.randomiser.nextInt( this.SCREEN_HEIGHT - assetHeight );

                imageSFXs.drawFadedImage( graphics2D, assetImage,
                        x, y, this.randomiser.nextFloat() );
            }
        }

        else if( performanceTest == PerformanceTest.RotatedImageDrawRate ) {
            AffineTransform origAT = graphics2D.getTransform();

            float imageXSpacing = (float)(this.SCREEN_WIDTH - assetWidth)
            / (float)(this.BATCH_SIZE[imageSFXBatchSizeIdx]+1);
            int imageYSpacing = this.SCREEN_HEIGHT / 3;

            int x = (int)imageXSpacing;
            int y = imageYSpacing - assetHeight/2;

            for( int idx = 0; idx < this.BATCH_SIZE[imageSFXBatchSizeIdx]; idx++ ) {
                x = (idx%2 == 0) ? (int)(imageXSpacing * (float)idx) 
                        : (int)(imageXSpacing * (float)idx + imageXSpacing );
                y = (idx%2 == 0) ? (imageYSpacing - assetHeight/2) : (imageYSpacing*2 - assetHeight/2);

                AffineTransform rot = new AffineTransform();
                rot.rotate( Math.toRadians(this.testCounter), x+assetWidth/2, y+assetHeight/2);
                graphics2D.setTransform(rot);

                graphics2D.drawImage(assetImage, x, y, null);
            }

            graphics2D.setTransform(origAT);
        }

        else if( performanceTest == PerformanceTest.BlurredImageDrawRate ) {
            if( imageSFXBatchSizeIdx > 2 )
                imageSFXBatchSizeIdx = 2;

            for( int idx = 0; idx < this.BATCH_SIZE[imageSFXBatchSizeIdx]; idx++ )
                imageSFXs.drawBlurredImage( graphics2D, assetImage,
                        this.SCREEN_WIDTH/2 - assetWidth/2,
                        this.SCREEN_HEIGHT/2 -assetHeight/2, randomiser.nextInt(5)*2+3 );
        }

        this.drawAssetRenderCount += this.BATCH_SIZE[imageSFXBatchSizeIdx];
    }

    /**
     * Start the application running - creating PerformanceTester with a 
     * false argument will run the tester in JPanel mode
     */
    public static void main(String[] args) {
        PerformanceTester performanceTester 
                = new PerformanceTester(true);
    }
}
