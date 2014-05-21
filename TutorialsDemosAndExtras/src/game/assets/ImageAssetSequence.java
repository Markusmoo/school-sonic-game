package game.assets;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * ImageAssetSequence provides a sequence of images that can either
 * be animated across a given interval or selected as needed
 * <P>
 * Note: This Asset differs from most other assets in that it also
 * contains an update method which will monitor the animation and
 * inform all observing objects when the animation has completed.
 * This represents the correct means of updating an asset, i.e. 
 * changes to the asset depend on the update tick.
 * <P>
 * It was necessary to add an update method as the normal method of
 * informing observing objects (i.e. via animation termination
 * occurring as part of the normal render process) may not be triggered
 * for off-screen objects (assuming some form of non-visible cull
 * is performed).
 * <P>
 * It is important to ensure that if events are tiggered on animation
 * completion then the update method will be called if off-screen
 * objects are not drawn.
 *
 * @author <A HREF="mailto:P.Hanna@qub.ac.uk">Philip Hanna</A>
 * @version $Revision: 1.1 $ $Date: 2007/08 $
 */
public class ImageAssetSequence extends GraphicalAsset {

    ///////////////////////////////////////////////////////////////////////////
    // Class data                                                            //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Array of BufferedImages that hold the image sequence
     */
    private BufferedImage[] images;

    /**
     * Variables that control how the images sequence is to be animated.
     * A playCount value of n > 0 entails the sequence will play for a total
     * of n times. A playCount value of n = 0 entails that the sequence will not
     * play but rather the specified homeFrame will be displayed (this can be used
     * to pause animation playback until necessary, e.g. an exploding barrel can
     * default to a barrel image until it is appropriate to play the animation).
     * Alternatively, a value of n = 0 can be used if the individual frames will
     * be selected from the image sequence to be displayed.
     * A playCount value of -1 entails that the animation will continuously loop
     * until instructed to stop. Any other value of playCount is invalid.
     */
    private int playCount;
    private int homeFrame;
    private int currentFrame;
    private long animationPeriodms; // in ms
    private long animinationStartTime;


    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Construct a new image sequence asset consisting of the specified array of
     * images and playback settings.
     * <P>
     * Note: the width and height of the ImageAssetSequence will be set to the
     * dimensions of the specified home frame image. It is recommended that other
     * images within the sequence have the same dimensions as the home frame (the
     * width and height of the asset will not change during animation, although if 
     * a new home frame is specified then the width and height will be changed to
     * the dimensions of the specified homeframe).
     *
     * @param  assetName the name of this asset
     * @param  images BufferedImage array holding an order sequences of image
     *         comprising the images sequence
     * @param  playCount integer value controlling how the sequence will be played.
     *         A value of -1 signifies continuous animation, whilst a value of 0
     *         signifies no animation (the home frame will be displayed) a value
     *         of n > 0 will result in n continuous playbacks before the animation
     *         stops and the home frame is displayed.
     * @param  homeFrame default image index within the sequence to be displayed
     *         if the image sequence is not currently undergoing animation
     * @param  animationPeriodms the period (in ms) of a single animation iteration
     *         over all images stored within the image sequence.
     *
     * @exception IllegalArgumentException if invalid playback settings are specified
     */
    public ImageAssetSequence(String assetName, BufferedImage[] images, 
            int playCount, int homeFrame, long animationPeriodms) {
        super(assetName);

        if (homeFrame < 0 || homeFrame >= images.length || animationPeriodms < 0) {
            throw new IllegalArgumentException("ImageAssetSequence.constructor: " +
                    "Invalid parameter " + " homeFrame = " + homeFrame + 
                    " animationPeriodms = " + animationPeriodms + 
                    " Image sequence length = " + images.length);
        }
        
        this.images = images;
        this.width = images[homeFrame].getWidth();
        this.height = images[homeFrame].getHeight();

        this.playCount = playCount;
        this.homeFrame = homeFrame;
        this.currentFrame = homeFrame;

        this.animationPeriodms = animationPeriodms;
        this.animinationStartTime = -1;
    }

    /**
     * Construct a new image sequence asset consisting of the specified array of
     * images, assuming no playback animation.
     * <P>
     * Note: the width and height of the ImageAssetSequence will be set to the
     * dimensions of the specified home frame image. It is recommended that other
     * images within the sequence have the same dimensions as the home frame -
     * although if a new home frame is specified then the width and height will 
     * be changed to the dimensions of the specified homeframe).
     *
     * @param  assetName the name of this asset
     * @param  images BufferedImage array holding an order sequences of image
     *         comprising the images sequence
     */
    public ImageAssetSequence(String assetName, BufferedImage[] images) {
        super(assetName);

        this.images = images;
        this.width = images[0].getWidth();
        this.height = images[0].getHeight();

        this.playCount = 0;
        this.homeFrame = 0;
        this.currentFrame = 0;

        this.animationPeriodms = 0;
        this.animinationStartTime = -1;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods: Set/Get Playback/Image Settings                              //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Return the current play count
     *
     * @return integer containing the current play count 
     */
    public int getPlayCount() {
        return playCount;
    }

    /**
     * Return the current home frame
     *
     * @return integer containing the current home frame
     */
    public int getHomeFrame() {
        return homeFrame;
    }

    /**
     * Return the current frame
     *
     * @return integer containing the current frame
     */    
    public int getCurrentFrame() {
        return determineCurrentFrame();
    }

    /**
     * Set the current frame to that specified
     *
     * @param currentFrame integer specifying the frame
     */
    public void setCurrentFrame(int currentFrame) {
        this.currentFrame = currentFrame;
    }

    /**
     * Return the number of images stored within the sequence
     *
     * @return integer containing the number of images in the sequence
     */    
    public int getNumberOfImages() {
        return images.length;
    }

    /**
     * Return the specified image from the stored image sequence
     *
     * @param imageIdx index of the image to be returned from the sequence
     * @return integer containing the number of images in the sequence
     * 
     * @exception IllegalArgumentException if an invalid image idx is specified 
     */    
    public BufferedImage getImage(int imageIdx) {
        if (imageIdx < 0 || imageIdx >= images.length) {
            throw new IllegalArgumentException("ImageAssetSequence.getImage: " +
                    "Invalid image index " + imageIdx);
        }
        
        return images[imageIdx];
    }

    /**
     * Return the current frame within the image sequence
     *
     * @return BufferedImage reference to the current frame within the sequence
     */
    public BufferedImage getImageRealisation() {
        return images[determineCurrentFrame()];
    }
    
    /**
     * Return the current animation period (in ms)
     *
     * @return current animation period (in ms)
     */
    public long getAnimationPeriod() {
        return animationPeriodms;
    }

    /**
     * Set the current animation period to that specified (in ms)
     *
     * @param animationPeriodms new animation period (in ms)
     *
     * @exception IllegalArgumentException if a negative animation period is specified
     */
    public void setAnimationPeriod(long animationPeriodms) {
        if (animationPeriodms < 0) {
            throw new IllegalArgumentException("ImageAssetSequence.setAnimationPeriod: " +
                    "Invalid animation period " + animationPeriodms);
        }
        
        this.animationPeriodms = animationPeriodms;
    }

    /**
     * Set the play count to that specified
     *
     * @param playCount new play count (-1: continuous, 0: no animation, 
     *        n: loop n times)
     */
    public void setPlayCount(int playCount) {
        this.playCount = playCount;
        animinationStartTime = -1;
    }

    /**
     * Set the home frame to that specified.
     * <P>
     * Note: The width and height of the asset will be updated to reflect
     * the dimensions of the specified home frame
     *
     * @param homeFrame new home frame
     */
    public void setHomeFrame(int homeFrame) {
        this.homeFrame = homeFrame;

        width = images[homeFrame].getWidth();
        height = images[homeFrame].getHeight();
    }

    /**
     * Based on the current playback settings and the current time determine which
     * image from the sequence should be displayed. If the sequence is not currently
     * animated, then the home frame will be returned.
     *
     * @param homeFrame new home frame
     */
    private int determineCurrentFrame() {
        boolean animationJustCompleted = false;

        if (animinationStartTime == -1)
            animinationStartTime = System.nanoTime() / 1000000;
        long currentTime = System.nanoTime() / 1000000;

        // Check if the animation has exceeded its specified overall duration
        if (playCount > 0) {
            if (currentTime - animinationStartTime 
                    > (long) playCount * animationPeriodms) {
                animationJustCompleted = true;
                playCount = 0;

                // Notify any observers that the animation has completed
                setChanged();
                notifyObservers("AnimationCompleted");
            }
        }
        
        // If the sequence is not animation then return the homeframe, unless 
        // the animation was stopped as part of this call, in which case return 
        // the last image within the sequence (this ensures that an animation 
        // will always finish by displaying the last image in the sequence).
        if (playCount == 0) {
            if (animationJustCompleted) {
                currentFrame = images.length - 1;
            } else {
                currentFrame = homeFrame;
            }
        } else {
            // Determine the correct image to display within the sequence based 
            // upon the elapsed time from the start of the animation.
            long timeIntoAniminationPeriod 
                    = (currentTime - animinationStartTime) % animationPeriodms;
            currentFrame = (int) ( (timeIntoAniminationPeriod * (long)images.length )
                                / animationPeriodms );
        }

        return currentFrame;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods: Render / Clone / Update                                      //
    ///////////////////////////////////////////////////////////////////////////    

    /**
     * Update the image asset sequence by testing to see if the animation has 
     * completed. If the animation has completed then any observing objects 
     * will be notified.
     */
    @Override
    public void update() {
        // Determine the current frame (which will also notify any observing 
        // objects when the animation completes.
        determineCurrentFrame();
    }
        
    /**
     * Render the image frame on the provided graphical object at the
     * specified x and y offset.
     *
     * @param  graphics2D graphical object on which to render this asset
     * @param  drawX x offset at which the asset should be rendered
     * @param  drawY y offset at which the asset should be rendered
     */
    public void draw(Graphics2D graphics2D, int drawX, int drawY) {
        graphics2D.drawImage(images[currentFrame], drawX, drawY, null);
    }

    /**
     * Return a shallow clone of this image asset sequence
     *
     * @return  new Asset instance containing a shallow clone of this instance
     */
    public Asset shallowClone() {
        ImageAssetSequence clone = new ImageAssetSequence(
                assetName, images, playCount, homeFrame, animationPeriodms);
        return clone;
    }

    /**
     * Return a deep clone of this image asset sequence.
     *
     * @return  new Asset instance containing a deep clone of this image asset
     */
    public Asset deepClone() {
        ImageAssetSequence clone = (ImageAssetSequence) shallowClone();

        for (int idx = 0; idx < clone.images.length; idx++) {
            clone.images[idx] = new BufferedImage(images[idx].getWidth(), 
                    images[idx].getHeight(), images[idx].getType());

            Graphics2D graphics2D = clone.images[idx].createGraphics();
            graphics2D.drawImage(images[idx], null, 0, 0);
            graphics2D.dispose();
        }

        return clone;
    }
}
