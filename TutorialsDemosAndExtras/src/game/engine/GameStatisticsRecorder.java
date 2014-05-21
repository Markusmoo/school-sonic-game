package game.engine;

import java.awt.*;
import java.text.DecimalFormat;

/**
 * GameStatisticsRecorder provides basic performance measurement for a
 * game engine instance.
 * <P>
 * Note: Parts of this class are largely based on that available
 * from within Davison's Killer Games Design in Java.
 *
 * @author <A HREF="mailto:P.Hanna@qub.ac.uk">Philip Hanna</A>
 * @version $Revision: 1.1 $ $Date: 2007/08 $
 *
 * @see GameEngine
 */
public final class GameStatisticsRecorder {

    ///////////////////////////////////////////////////////////////////////////
    // Class data:                                                           //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Game engine target update/render period (in ns)
     */
    private long gameUpdatePeriod;

    /**
     * Target game statistics update period (in ns), i.e. statistics
     * will be measured across this interval. A longer interval makes
     * for more accurate statistics, but at the expense of a longer
     * update period.
     */
    private static long STATS_INTERVAL = 200000000L;

    /**
     * Variables used to hold measurement - values are maintained for
     * the number of frames per second (fps), updates per second (ups)
     * and average update and render times (ms) (not include the time needed
     * to show the contents of the rendered buffer). The initial amount of
     * video memory available to the program is also recorded.
     */
    private int fps;
    private int ups;
    private int updateTime;
    private int renderTime;
    private int initialAvailableVideoMemory;

    /**
     * Current statistics interval duration and time at which the
     * last interval completed (all in ns). The total number of
     * intervals over which statistics have been recorded is also
     * maintained.
     */
    private long currentIntervalDuration = 0L;
    private long previousIntervalTime = 0L;

    /**
     * Variables recording the number of renders this interval (with
     * each render having a corresponding update) and the number of
     * additional updates, i.e. missed renders. The total amount of
     * time spent sleeping is also recorded, as is the total initial
     * amount of video memory available.
     */
    private long numRendersThisInterval = 0L;
    private long additionalUpdatesThisInterval = 0L;
    private long singleUpdateTime = 0L;
    private long singleRenderTime = 0L;
    private long totalUpdateTime = 0L;
    private long totalRenderTime = 0L;

    /**
     * Variables that provide linage to the current graphics device
     * and associated game engine.
     */
    private GraphicsDevice currentGraphicsDevice;
    private GameEngine gameEngine;

    /**
     * Variables that record the total amount of loaded image assets.
     * This value should broadly equate to the amout of video memory
     * used by the game (it can be useful under Vista where the video
     * model will report an unlimited amount of available memory)
     */
    private long totalLoadedImageAssetByteSize = 0;


    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Create a new GameStatisticsRecorder instance.
     *
     * @param  gameEngine GameEngine instance which will be monitored
     * @param  gameUpdatePeriod update period (in ns) of the game engine
     */
    public GameStatisticsRecorder(GameEngine gameEngine, long gameUpdatePeriod) {
        this.gameEngine = gameEngine;
        this.gameUpdatePeriod = gameUpdatePeriod;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods:                                                              //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Record the initial amount of video memory available to the application.
     * This method should only be called once FSEM has been enabled (but before
     * game objects have started to move image data across into video memory).
     */
    public void recordInitialAvailableVideoMemory() {
        currentGraphicsDevice 
                = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        initialAvailableVideoMemory 
                = currentGraphicsDevice.getAvailableAcceleratedMemory();
    }

    /**
     * Prepare to start recording statistics, i.e. set the timestamp of
     * the last statistics interval to the current time
     */
    public void startRecordingStatistics() {
        previousIntervalTime = System.nanoTime();
    }

    /**
     * Record the start of the update interval
     * 
     * @param time long time value
     */
    public void recordStartTime(long time) {
        singleUpdateTime = time;
    }

    /**
     * Record the end of the update interval and the start
     * of the render interval
     * 
     * @param time long time value
     */
    public void recordUpdateTime(long time) {
        singleUpdateTime = time - singleUpdateTime;
        singleRenderTime = time;
    }

    /**
     * Record the end of the render interval
     * 
     * @param time long time value
     */
    public void recordRenderTime(long time) {
        singleRenderTime = time - singleRenderTime;
    }

    /**
     * Record the size of the loaded asset
     * 
     * @param loadedImageAssetSize long number of bytes loaded
     */
    public void recordLoadedAssetSize(long loadedImageAssetSize) {
        totalLoadedImageAssetByteSize += loadedImageAssetSize;
    }

    /**
     * Update the statistics for the last game update cycle and update the
     * overall statistics if the recording interval has expired.
     *
     * @param  additionalUpdatesWithoutRender number of additional updates
     *         this cycle without a corresponding render
     * @param  updateRenderTime length of time to perform the update/render 
     *         for this cycle
     */
    public void storeStats(int additionalUpdatesWithoutRender) {
        // Update game statistics for the completed update/render cycle
        numRendersThisInterval++;
        additionalUpdatesThisInterval += additionalUpdatesWithoutRender;
        currentIntervalDuration += gameUpdatePeriod;
        totalUpdateTime += singleUpdateTime;
        totalRenderTime += singleRenderTime;

        // Update the overall statistics if the statistics interval has expired
        if (currentIntervalDuration >= STATS_INTERVAL) {
            long timeNow = System.nanoTime();
            long realElapsedTime = timeNow - previousIntervalTime;

            // Work out the fps, ups and load for the current interval
            fps = (int) Math.round(
                (((double)numRendersThisInterval / realElapsedTime ) * 1000000000L));
            ups = (int) Math.round(
                (((double)(numRendersThisInterval + additionalUpdatesThisInterval) 
                                        / realElapsedTime) * 1000000000L));
            updateTime = (int) ((double)(totalUpdateTime / 1000000L) / 
                    (double)(numRendersThisInterval + additionalUpdatesThisInterval));
            renderTime = (int) ((double)(totalRenderTime / 1000000L) / 
                    (double)(numRendersThisInterval));

            // Get ready to record statistics within the next interval
            previousIntervalTime = timeNow;
            currentIntervalDuration = 0L;
            additionalUpdatesThisInterval = 0L;
            numRendersThisInterval = 0L;
            totalUpdateTime = 0L;
            totalRenderTime = 0L;
        }
    }

    /**
     * Return the average number of frames per second across the recorded interval
     *
     * @return int containing the average fps
     */
    public int getAverageFPS() {
        return fps;
    }

    /**
     * Return the average number of updates per second across the recorded interval
     *
     * @return int containing the average ups
     */
    public int getAverageUPS() {
        return ups;
    }

    /**
     * Return the average update time across the recorded interval measured
     * in ms.
     *
     * @return int containing the average update time (ms) across the interval
     */
    public int getAverageUpdateTime() {
        return updateTime;
    }

    /**
     * Return the average update load across the recorded interval measured
     *
     * @return double containing the average update load (%) across the interval
     */
    public double getAverageUpdateLoad() {
        return (double) (updateTime*1000000) / (double) gameUpdatePeriod;
    }

    /**
     * Return the average render time across the recorded interval measured
     * in ms.
     *
     * @return int containing the average render time (ms) across the interval
     */
    public int getAverageRenderTime() {
        return renderTime;
    }

    /**
     * Return the average render load across the recorded interval measured
     * (not including the time needed to show the rendered buffer contents
     *
     * @return double containing the average render load (%) across the interval
     */
    public double getAverageRenderLoad() {
        return (double) (renderTime*1000000) / (double) gameUpdatePeriod;
    }

    /**
     * Return the amount of video memory allocated since statistics were
     * started to be recorded
     *
     * @return int amount of allocated video memory (in bytes)
     */
    public int getAllocatedVideoMemory() {
        return initialAvailableVideoMemory 
                - currentGraphicsDevice.getAvailableAcceleratedMemory();
    }

    /**
     * Return the amount of video memory currently available
     *
     * @return int amount of available video memory (in bytes)
     */
    public int getAvailableVideoMemory() {
        return currentGraphicsDevice.getAvailableAcceleratedMemory();
    }

    /**
     * Return the total amount of loaded image data (in bytes)
     *
     * @return long total amount of loaded image data
     */
    public long getTotalLoadedImageAssetByteSize() {
        return totalLoadedImageAssetByteSize;
    }
    
    /**
     * Return the current number of active game objects (i.e. game objects
     * within game layers which are currently active)
     *
     * @return number of current active game objects
     */
    public int getNumActiveGameObjects() {
        int numActiveGameObjects = 0;
        for (GameLayer gameLayer : gameEngine.gameLayers.values()) {
            if (gameLayer.gameLayerActivity == GameLayer.GameLayerActivity.ACTIVE) {
                numActiveGameObjects += gameLayer.gameObjects.size();
            }
        }

        return numActiveGameObjects;
    }

    /**
     * Return the current number of visible game objects (i.e. game objects
     * within game layers which are currently visible)
     *
     * @return number of current active game objects
     */
    public int getNumVisibleGameObjects() {
        int numVisibleGameObjects = 0;

        for (GameLayer gameLayer : gameEngine.gameLayers.values()) {
            if (gameLayer.gameLayerVisibility == GameLayer.GameLayerVisibility.VISIBLE) {
                numVisibleGameObjects += gameLayer.gameObjectsDrawOrderSorted.size();
            }
        }
        return numVisibleGameObjects;
    }

    /**
     * Draw out a summary of the statistics to the specified graphics device
     *
     * @param  graphics2D Graphics objects to which the statistics should be drawn
     * @param  drawX integer x offset at which the statistics should be drawn
     * @param  drawY integer y offset at which the statistics should be drawn
     */
    public void drawStatistics(Graphics2D graphics2D, int drawX, int drawY) {
        Color originalColour = graphics2D.getColor();
        Font originalFont = graphics2D.getFont();
        DecimalFormat decimalFormat = new DecimalFormat("0.##");

        graphics2D.setColor(new Color(0, 0, 0, 55));
        graphics2D.fillRect(drawX, drawY, 150, 55);

        graphics2D.setColor(Color.white);
        graphics2D.setFont(new Font("MONOSPACED", Font.BOLD, 12));

        graphics2D.drawString("FPS=" + fps + " : " + renderTime + "ms (" 
                + decimalFormat.format(100.0 * getAverageRenderLoad()) + "%)", 
                drawX + 5, drawY + 12);
        graphics2D.drawString("UPS=" + ups + " : " + updateTime + "ms (" 
                + decimalFormat.format(100.0 * getAverageUpdateLoad()) + "%)", 
                drawX + 5, drawY + 24);
        graphics2D.drawString("Video mem = " + (getAllocatedVideoMemory() / 1048576) 
                + "/" + (getAvailableVideoMemory() / 1048576) + "MB", 
                drawX + 5, drawY + 36);
        graphics2D.drawString("Active objects = " + getNumActiveGameObjects(), 
                drawX + 5, drawY + 48);

        graphics2D.setColor(originalColour);
        graphics2D.setFont(originalFont);
    }
}