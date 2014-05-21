package game.assets;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.*;
import java.io.*;
import game.engine.*;

/**
 * AssetManager provides a means of loading, storing and retrieving assets.
 * <P>
 * Functionality within this class can be broadly classified into two distinct
 * groupings:
 * <P>
 * <B>Methods for loading and storing assets:</B> Assets stored within an AssetManager
 * are termed archetype assets.
 * <P>
 * <B>Methods for retrieving stored assets:</B> Three different types of retrieval
 * are offered. The retrieveAsset method returns an Asset instance that contains a
 * shallow clone of the actual asset stored within the archetype asset. The
 * retrieveAssetClone method returns an Asset instance that contains a deep clone of
 * the actual asset stored within the archetype asset. The retrieveAssetArchetype
 * method returns an Asset reference to the archetype asset.
 * <P>
 * It is intended that the retrieveAsset method will be used if a particular
 * object wishes to make use of an asset but does not need to change the asset.
 * For example, a lit torch animation, stored within an ImageAssetSequence asset type
 * may be used by a number of different game object. Hence, whilst each animation
 * might differ in terms of the playspeed, current animation frame, etc. the core
 * graphical asset remains the same.
 * <P>
 * It is intended that the retrieveAssetClone method will be used if a particular
 * objects need to modify an asset in a manner that should not effect other objects.
 * For example, in the game of Space Invaders each base may initially start with the
 * same graphical image, but how each base is eroded will likely differ.
 * <P>
 * The retrieveAssetArchetype is intended to provide a clean means of gaining access
 * to the archetype asset stored within this class. This can be of use to either change
 * the asset or the 'default' asset parameters, e.g. animation speed, that is supplied
 * from either a deep or shallow clone.
 *
 * @author <A HREF="mailto:P.Hanna@qub.ac.uk">Philip Hanna</A>
 * @version $Revision: 1.1 $ $Date: 2007/08 $
 *
 * @see AssetLoader
 */
public class AssetManager {

    ///////////////////////////////////////////////////////////////////////////
    // Class data                                                            //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * A hasp map in which to store the assets.
     */
    protected HashMap<String, Asset> assetStore;

    /**
     * A reference to an AssetLoader instance to help with asset loading
     */
    protected AssetLoader assetLoader;


    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Create a new, empty, AssetManager instance
     */
    public AssetManager() {
        assetStore = new HashMap<String, Asset>();
        assetLoader = new AssetLoader();
    }


    ///////////////////////////////////////////////////////////////////////////
    //  Miscellaneous                                                        //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Registers the specified GameStatisticsRecorded with this asset loader
     * associated with this asset manager.
     * <P>
     * The registered instance will be used to maintain a record of the amount
     * of loaded image data.
     *
     * @param  gameStatisticsRecorder GameStatisticsRecorder instance to be registered
     */
    public final void registerGameStatisticsRecorder(
            GameStatisticsRecorder gameStatisticsRecorder) {
        assetLoader.registerGameStatisticsRecorder(gameStatisticsRecorder);
    }

    /**
     * Perform appropriate game shutdown actions for all loaded assets.
     * <P>
     * If the asset is either a sound asset or MIDI asset then a request
     * is made to stop any playback of the clip or MIDI sequence.
     */
    public void performGameShutDownActions() {
        for (Asset asset : assetStore.values()) {
            if (asset instanceof SoundAsset) {
                ((SoundAsset) asset).stop();
            } else if (asset instanceof MidiAsset) {
                ((MidiAsset) asset).close();
            }
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods : Adding Graphical Assets                                     //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Create and store an ImageAsset instance that contains the specified image.
     *
     * @param  assetName the name of this asset
     * @param  imageName URL specified location of the source image
     */
    public void addImageAsset(String assetName, URL imageName) {
        addImageAsset(assetName, imageName, null);
    }

    /**
     * Create and store an ImageAsset instance using the specified image, or a region
     * thereof if an image segment rectangle has been specified.
     *
     * Note: A imageSegmentRectangle value of null will be interpreted as meaning
     * the entire source image should be stored.
     *
     * @param  assetName the name of this asset
     * @param  imageName URL specified location of the source image
     * @param  imageSegmentRectangle Rectangle containing the image segment to be 
     *         extracted
     * @see    ImageAsset
     */
    public void addImageAsset(
            String assetName, URL imageName, Rectangle imageSegmentRectangle) {
        BufferedImage image = assetLoader.loadImage(imageName);

        // Only extract an image segment if imageSegementRectangle has been defined
        if (imageSegmentRectangle != null) {
            image = assetLoader.extractImageSegment(image, 
                    imageSegmentRectangle.x, imageSegmentRectangle.y, 
                    imageSegmentRectangle.width, imageSegmentRectangle.height);
        }
        
        // Creat a new ImageAsset object using the loaded image and add it to the store
        ImageAsset imageAsset = new ImageAsset(assetName, image);
        assetStore.put(assetName, imageAsset);
    }

    /**
     * Create and store an ImageAsset instance from the segmentation of the specified
     * base image. 
     * <P>
     * This particular method can be used for loading in an sequence of regular sized
     * images, e.g. a font sheet, animation, etc.
     * <P>
     * Note: For legacy reason this method can differ in terms of how loaded
     * assets are named. In particular, if the number of assets wide and high 
     * are <B>both</B> higher than 9 then each asset adopts the following name:
     * baseAssetName + numHigh + "-" + numWide, eg. test1-11. If this is not 
     * the case then each asset is named baseAssetName + numHigh + "" + numWide, 
     * e.g test45. 
     * 
     * @param  baseAssetName the name of this asset
     * @param  imageName URL specified location of the source image
     * @param  numAssetsWide integer number of segment images wide
     * @param  numAssetsHigh integer number of segment images high
     * @see    ImageAsset
     */
    public void addImageAssets(
            String baseAssetName, URL imageName, int numAssetsWide, int numAssetsHigh) {        
        BufferedImage baseImage = assetLoader.loadImage(imageName);
        double imageWidth = (double) baseImage.getWidth() / (double) numAssetsWide;
        double imageHeight = (double) baseImage.getHeight() / (double) numAssetsHigh;

        // Extract and store each image
        for (int heightIdx = 0; heightIdx < numAssetsHigh; heightIdx++) {
            for (int widthIdx = 0; widthIdx < numAssetsWide; widthIdx++) {                
                String assetName = 
                    (numAssetsWide>=10 && numAssetsHigh>=10)
                        ? baseAssetName + heightIdx + "-" + widthIdx
                        : baseAssetName + heightIdx + "" + widthIdx;
                ImageAsset imageAsset = new ImageAsset(assetName, 
                        assetLoader.extractImageSegment(baseImage, 
                        (int) (widthIdx*imageWidth), (int) (heightIdx*imageHeight), 
                        (int) imageWidth, (int) imageHeight));
                assetStore.put(assetName, imageAsset);
            }
        }
    }

    /**
     * Create and store an ImageAssetSequence image based upon the sequence of
     * specified images and playback parameters.
     *
     * Note: Whilst not a strict requirement, it is recommended that a specified image
     * sequence contain images of the same dimension (e.g. width and height).
     *
     * @param  assetName the name of this asset
     * @param  playCount number of times this animation should play (-1 = continuously)
     * @param  animationPeriod length of each animation cycle (in ms)
     * @param  homeFrame default animation frame to display prior to animation start
     * @param  imageNames URL array specifying the locations of the source images
     * @see    ImageAssetSequence
     */
    public void addImageAssetSequence(String assetName, 
            int playCount, long animationPeriod, int homeFrame, URL[] imageNames) {
        BufferedImage[] images = new BufferedImage[imageNames.length];
        for (int idx = 0; idx < imageNames.length; idx++) {
            images[idx] = assetLoader.loadImage(imageNames[idx]);
        }

        // Creat a new ImageAssetSequence using the loaded images and add to the store
        ImageAssetSequence imageAssetAnimated = new ImageAssetSequence(
                assetName, images, playCount, homeFrame, animationPeriod);
        assetStore.put(assetName, imageAssetAnimated);
    }

    /**
     * Create and store an ImageAssetSequence image based upon the specified image which
     * will be equally divided (length wise) into the specified number of segments, i.e.
     * the source image is a horizontal image strip.
     *
     * @param  assetName the name of this asset
     * @param  playCount number of times this animation should play (-1 = continuously)
     * @param  animationPeriod length of each animation cycle (in ms)
     * @param  homeFrame default animation frame to display prior to animation start
     * @param  numberOfSegments number of length-wise segments of equal length that the 
     *         source image strip should be divided into
     * @param  imageName URL specifying the location of the source image strip
     * @see    ImageAssetSequence
     */
    public void addImageAssetSequence(
            String assetName, int playCount, long animationPeriod, 
            int homeFrame, int numberOfSegments, URL imageName) {
        // Load the image strip and work out the width and height of each image 
        BufferedImage sourceImage = assetLoader.loadImage(imageName);
        double segmentWidth = (double) sourceImage.getWidth() / (double) numberOfSegments;
        int segmentHeight = sourceImage.getHeight();

        // Extract and store each image
        BufferedImage[] images = new BufferedImage[numberOfSegments];
        for (int idx = 0; idx < numberOfSegments; idx++) {
            images[idx] = assetLoader.extractImageSegment(
                    sourceImage, (int) (idx * segmentWidth), 0, 
                    (int) segmentWidth, segmentHeight);
        }
        
        // Creat a new ImageAssetSequence using the images and add to the store
        ImageAssetSequence imageAssetAnimated = new ImageAssetSequence(
                assetName, images, playCount, homeFrame, animationPeriod);
        assetStore.put(assetName, imageAssetAnimated);
    }


    /**
     * Create and store an ImageAssetSequence image based upon the specified image which
     * will be equally divided (length wise and high wise) into the specified number of 
     * segments, i.e. the source image is an array based image strip.
     *
     * @param  assetName the name of this asset
     * @param  playCount number of times this animation should play (-1 = continuously)
     * @param  animationPeriod length of each animation cycle (in ms)
     * @param  homeFrame default animation frame to display prior to animation start
     * @param  numSegmentsWide number of segments wide to split the image
     * @param  numSegmentsHigh number of segments high to split the image
     * @param  imageName URL specifying the location of the source image array
     * @see    ImageAssetSequence
     */
    public void addImageAssetSequence(String assetName, 
            int playCount, long animationPeriod, int homeFrame, 
            int numSegmentsWide, int numSegmentsHigh, URL imageName) {
        // Load the image strip and work out the width and height of each image 
        BufferedImage sourceImage = assetLoader.loadImage(imageName);
        double segmentWidth = (double) sourceImage.getWidth() / (double) numSegmentsWide;
        double segmentHeight = (double) sourceImage.getHeight() / (double) numSegmentsHigh;

        // Extract and store each image
        BufferedImage[] images = new BufferedImage[numSegmentsWide * numSegmentsHigh];
        for (int heightIdx = 0; heightIdx < numSegmentsHigh; heightIdx++) {
            for (int widthIdx = 0; widthIdx < numSegmentsWide; widthIdx++) {
                images[heightIdx * numSegmentsWide + widthIdx] 
                        = assetLoader.extractImageSegment( sourceImage, 
                            (int) (widthIdx * segmentWidth), 
                            (int) (heightIdx * segmentHeight), 
                            (int) segmentWidth, (int) segmentHeight);
            }
        }
        
        // Creat a new ImageAssetSequence using the extracted images and add to the store
        ImageAssetSequence imageAssetAnimated = new ImageAssetSequence(
                    assetName, images, playCount, homeFrame, animationPeriod);
        assetStore.put(assetName, imageAssetAnimated);
    }

    /**
     * Create and store an ImageAssetSequence image based upon the specified image segments
     * as extracted from the source image.
     *
     * @param  assetName the name of this asset
     * @param  playCount number of times this animation should play (-1 = continuously)
     * @param  animationPeriod length of each animation cycle (in ms)
     * @param  homeFrame default animation frame to display prior to animation start
     * @param  imageName URL specifying the location of the source image
     * @param  imageSegmentRectangles Rectangle array specifying the image segments to be
     *         extracted from the source image
     * @see    ImageAssetSequence
     */
    public void addImageAssetSequence(String assetName, 
            int playCount, long animationPeriod, int homeFrame, 
            URL imageName, Rectangle[] imageSegmentRectangles) {
        BufferedImage sourceImage = assetLoader.loadImage(imageName);

        // Extract and store each image segment
        BufferedImage[] images = new BufferedImage[imageSegmentRectangles.length];
        for (int idx = 0; idx < imageSegmentRectangles.length; idx++) {
            images[idx] = assetLoader.extractImageSegment(sourceImage, 
                    imageSegmentRectangles[idx].x, imageSegmentRectangles[idx].y, 
                    imageSegmentRectangles[idx].width, imageSegmentRectangles[idx].height);
        }
        
        // Creat a new ImageAssetSequence using the extracted images and add to store
        ImageAssetSequence imageAssetAnimated = new ImageAssetSequence(
                assetName, images, playCount, homeFrame, animationPeriod);
        assetStore.put(assetName, imageAssetAnimated);
    }

    /**
     * Create and store the specified sequence of images as an ImageAssetSequence
     * <B>
     * Note: This method assumes that the loaded images sequence will not be used
     * as an animated sequence of images
     *
     * @param  assetName the name of this asset
     * @param  imageNames URL array specifying the location of the source images
     * @see    ImageAssetSequence
     */
    public void addImageAssetSequence(String assetName, URL[] imageNames) {
        BufferedImage[] images = new BufferedImage[imageNames.length];
        for (int idx = 0; idx < imageNames.length; idx++) {
            images[idx] = assetLoader.loadImage(imageNames[idx]);
        }
        
        // Creat a new ImageAssetSequence using the loaded images and add to the store
        ImageAssetSequence imageAssetAnimated = new ImageAssetSequence(assetName, images);
        assetStore.put(assetName, imageAssetAnimated);
    }
    
    /**
     * Create a sequence of images based on segmenting the specified source
     * image into the specified number of equi-sized images.
     * <B>
     * Note: This method assumes that the loaded images sequence will not be used
     * as an animated sequence of images
     *
     * @param  assetName the name of this asset
     * @param  numberOfSegments integer number of segments to extract
     * @param  imageName URL specifying the location of the source image
     * @see    ImageAssetSequence
     */    
    public void addImageAssetSequence(
            String assetName, int numberOfSegments, URL imageName) {
        // Load the image strip and work out the width and height of each image 
        BufferedImage sourceImage = assetLoader.loadImage(imageName);
        double segmentWidth = (double) sourceImage.getWidth() / (double) numberOfSegments;
        int segmentHeight = sourceImage.getHeight();

        // Extract and store each image
        BufferedImage[] images = new BufferedImage[numberOfSegments];
        for (int idx = 0; idx < numberOfSegments; idx++) {
            images[idx] = assetLoader.extractImageSegment(sourceImage, 
                    (int) (idx * segmentWidth), 0, (int) segmentWidth, segmentHeight);
        }
        
        // Creat a new ImageAssetSequence using the extracted images and add to the store
        ImageAssetSequence imageAssetAnimated = new ImageAssetSequence(assetName, images);
        assetStore.put(assetName, imageAssetAnimated);
    }

    /**
     * Create a sequence of images based on segmenting the specified source
     * image into the specified number of equi-sized image segments.
     * <B>
     * Note: This method assumes that the loaded images sequence will not be used
     * as an animated sequence of images
     *
     * @param  assetName the name of this asset
     * @param  numSegmentsWide integer specifying the number of segments wide
     * @param  numSegmentsHigh integer specifying the number of segments high
     * @param  imageName URL specifying the location of the source image
     * @see    ImageAssetSequence
     */    
    public void addImageAssetSequence(
            String assetName, int numSegmentsWide, int numSegmentsHigh, URL imageName) {
        // Load the image strip and work out the width and height of each image 
        BufferedImage sourceImage = assetLoader.loadImage(imageName);
        double segmentWidth = (double) sourceImage.getWidth() / (double) numSegmentsWide;
        double segmentHeight = (double) sourceImage.getHeight() / (double) numSegmentsHigh;

        // Extract and store each image
        BufferedImage[] images = new BufferedImage[numSegmentsWide * numSegmentsHigh];
        for (int heightIdx = 0; heightIdx < numSegmentsHigh; heightIdx++) {
            for (int widthIdx = 0; widthIdx < numSegmentsWide; widthIdx++) {
                images[heightIdx * numSegmentsWide + widthIdx] 
                        = assetLoader.extractImageSegment(sourceImage, 
                            (int) (widthIdx * segmentWidth), 
                            (int) (heightIdx * segmentHeight), 
                            (int) segmentWidth, (int) segmentHeight);
            }
        }
        
        // Create a new ImageAssetSequence using the extracted images and add to the store
        ImageAssetSequence imageAssetAnimated = new ImageAssetSequence(assetName, images);
        assetStore.put(assetName, imageAssetAnimated);
    }

    /**
     * Create a sequence of images based on segmenting the specified source
     * image into the series of rectangular segments
     * <B>
     * Note: This method assumes that the loaded images sequence will not be used
     * as an animated sequence of images
     *
     * @param  assetName the name of this asset
     * @param  imageName URL specifying the location of the source image
     * @param  imageSegmentRectangles Rectangle array holding the image segments
     * @see    ImageAssetSequence
     */    
    public void addImageAssetSequence(
            String assetName, URL imageName, Rectangle[] imageSegmentRectangles) {
        BufferedImage sourceImage = assetLoader.loadImage(imageName);

        // Extract and store each image segment
        BufferedImage[] images = new BufferedImage[imageSegmentRectangles.length];
        for (int idx = 0; idx < imageSegmentRectangles.length; idx++) {
            images[idx] = assetLoader.extractImageSegment(sourceImage, 
                    imageSegmentRectangles[idx].x, imageSegmentRectangles[idx].y, 
                    imageSegmentRectangles[idx].width, imageSegmentRectangles[idx].height);
        }
        
        // Creat a new ImageAssetSequence using the extracted images and add to the store
        ImageAssetSequence imageAssetAnimated = new ImageAssetSequence(assetName, images);
        assetStore.put(assetName, imageAssetAnimated);
    }

    /**
     * Create and store an ImageAssetRibbon consisting of the specified sequence of
     * images and with the specified orientation and viewport dimensions
     *
     * @param  assetName the name of this asset
     * @param  imageNames URL array specifying the location of the source images
     * @param  ribbonScrollDirection specifying the orientation of the ribbon
     * @param  viewPortX specifying the x viewport offset relative to the first image 
     * @param  viewPortY specifying the y viewport offset relative to the first image 
     * @param  viewPortWidth specifying the width of the viewport onto the ribbon
     * @param  viewPortHeight specifying the height of the viewport onto the ribbon
     * @see    ImageAssetRibbon
     */
    public void addImageAssetRibbon(String assetName, 
            int viewPortX, int viewPortY, int viewPortWidth, int viewPortHeight, 
            ImageAssetRibbon.RibbonScrollDirection ribbonScrollDirection, URL[] imageNames) {
        BufferedImage[] images = new BufferedImage[imageNames.length];
        for (int idx = 0; idx < imageNames.length; idx++) {
            images[idx] = assetLoader.loadImage(imageNames[idx]);
        }
        
        // Creat a new ImageAssetRibbon using the loaded images and add to the store
        ImageAssetRibbon imageAssetRibbon = new ImageAssetRibbon(
                assetName, images, ribbonScrollDirection, 
                viewPortX, viewPortY, viewPortWidth, viewPortHeight);
        assetStore.put(assetName, imageAssetRibbon);
    }

    /**
     * Create and store an ImageAssetTile consisting of the specified image tile
     * with the specified viewport dimensions
     *
     * @param  assetName the name of this asset
     * @param  imageName URL specifying the location of the source image tile
     * @param  viewPortX specifying the x viewport offset relative to the 'initial' tile
     * @param  viewPortY specifying the y viewport offset relative to the 'initial' tile
     * @param  viewPortWidth specifying the width of the viewport onto the tiled image
     * @param  viewPortHeight specifying the height of the viewport onto the tiled image
     * @see    ImageAssetTile
     */
    public void addImageAssetTile(
            String assetName, int viewPortX, int viewPortY, 
            int viewPortWidth, int viewPortHeight, URL imageName) {
        BufferedImage image = assetLoader.loadImage(imageName);

        // Creat a new ImageAssetTile using the loaded image tile and add to the store
        ImageAssetTile imageAssetTile = new ImageAssetTile(assetName, 
                image, viewPortX, viewPortY, viewPortWidth, viewPortHeight);
        assetStore.put(assetName, imageAssetTile);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods : Adding Sound Assets                                         //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Create and store a SoundAssetClip containing the specified audio clip
     *
     * @param  assetName the name of this asset
     * @param  clipName URL specifying the location of the source audio clip
     * @param  continuallyPlayClip flag determining if audio clip should be looped 
     * @see    SoundAssetClip
     */
    public void addSoundAssetClip(
            String assetName, boolean continuallyPlayClip, URL clipName) {
        /*
         * In order to ensure that the SoundAssetClip can return a deep clone the 
         * loadAudioClip method of AssetLoader is not used, instead the SoundAssetClip 
         * is permitted to load the clip itself.
         */
        SoundAssetClip soundAssetClip 
                = new SoundAssetClip(assetName, clipName, continuallyPlayClip);
        assetStore.put(assetName, soundAssetClip);
    }

    /**
     * Create and store a SoundAssetAssembly containing the specified audio clip
     *
     * @param  assetName the name of this asset
     * @param  clipName URL specifying the location of the source audio clip
     * @param  clipAssemblySize determining how many copies of the source audio clip 
     *         will be made available for simultaneous playback
     * @see    SoundAssetAssembly
     */
    public void addSoundAssetAssembly(
            String assetName, int clipAssemblySize, URL clipName) {
        /*
         * In order to ensure that the SoundAssetAssembly can return a deep clone the 
         * loadAudioClip method of AssetLoader is not used, instead the 
         * SoundAssetAssembly is permitted to load the clip itself.
         */
        SoundAssetAssembly soundAssetAssembly 
                = new SoundAssetAssembly(assetName, clipName, clipAssemblySize);
        assetStore.put(assetName, soundAssetAssembly);
    }

    /**
     * Create and store a MidiAsset containing the specified MIDI sequence
     *
     * @param  assetName the name of this asset
     * @param  continuallyPlayClip flag determining if audio clip should be looped 
     * @param  midiURL URL specifying the location of the source MIDI sequence
     * @see    MidiAsset
     */
    public void addMidiAsset(String assetName, boolean continuallyPlay, URL midiURL) {
        /*
         * In order to ensure that the MidiAsset can return a deep clone the 
         * loadMidiSequence method of AssetLoader is not used, instead the 
         * MidiAsset is permitted to load the sequence itself.
         */
        MidiAsset midiAsset = new MidiAsset(assetName, midiURL, continuallyPlay);
        assetStore.put(assetName, midiAsset);
    }

    
    ///////////////////////////////////////////////////////////////////////////
    // Methods : Image and Sound File Loader                                 //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Load the image and/or sound assets from the specified text file.
     * <P>
     * Note: The following format is assumed for each of the supported image/
     * sound asset types:
     * <P>
     * ImageAsset name URL
     * <P>
     * ImageAssetSequence name Animated playCount animationPeriod homeFrame URLs numURLs URL...
     * ImageAssetSequence name Animated playCount animationPeriod homeFrame Segments numSegments URL
     * ImageAssetSequence name Animated playCount animationPeriod homeFrame Rectangles URL numRectangles Rectangle...
     * ImageAssetSequence name Animated playCount animationPeriod homeFrame Array numImagesWide numImagesHigh URL
     * <P>
     * ImageAssetSequence name Nonanimated URLs numURLs URL...
     * ImageAssetSequence name Nonanimated Segments numSegments URL
     * ImageAssetSequence name Nonanimated Rectangles URL numRectangles Rectangle...
     * ImageAssetSequence name Nonanimated Array numImagesWide, numImagesHigh URL
     * <P>
     * ImageAssetRibbon name viewPortX viewPortY width height scrollDirection numURLs URL...
     * <P>
     * ImageAssetTile name viewPortX viewPortY width height URL
     * <P>
     * SoundAssetClip name continuallyPLayClip URL
     * SoundAssetAssembly name clipAssemblySize URL
     * <P>
     * MidiAsset name continuallyPlayMIDI URL
     * <P>
     * Examples include:
     * ImageAsset BrickSetC6 images\Textures\BrickSetC6.png
     * ImageAssetTile BuilderBackgroundTile 0 0 1 1 images/BuilderGUI/BuilderBackgroundTile.png
     * ImageAssetSequence GUIFontLarge Nonanimated Array 26 4 images\Common\GUIFontLarge.png
     * ImageAssetSequence Crystal Animated -1 1000 0 Segments 8 images\Decoration\Crystal.png
     * SoundAssetClip  Appear false sounds\Appear.wav
     * SoundAssetAssembly Shot 5 sounds\Shot.wav
     * 
     * @param  assetFileName URL of the file containing asset load information
     * @see    MidiAsset
     */
    public void loadAssetsFromFile(URL assetFileName) {
        int lineNumber = 0;
        String inputLine = null;

        try {
            BufferedReader inputFile = new BufferedReader(
                    new InputStreamReader(assetFileName.openStream()));

            // Process each line at a time
            inputLine = inputFile.readLine();
            while (inputLine != null) {
                lineNumber++;

                // Skip any lines that start with a comment. i.e. //, sequence
                if (inputLine.length() > 1 
                        && inputLine.substring(0, 2).compareTo("//") == 0) {
                    inputLine = inputFile.readLine();
                    continue;
                }

                // Tokenize the line and skip over any lines will no tokens
                StringTokenizer tokenizer = new StringTokenizer(inputLine);
                if (tokenizer.countTokens() == 0) {
                    inputLine = inputFile.readLine();
                    continue;
                }

                // Extract the asset type and asset name
                String assetType = tokenizer.nextToken();
                String assetName = tokenizer.nextToken();

                // Based on the specified asset type, process the remaining tokens
                // and make use of the other AssetManager methods to load the
                // specified asset.
                
                ////////////////////////
                // Image asset loader //
                ////////////////////////
                
                if (assetType.compareTo("ImageAsset") == 0) {
                    URL url = new URL(assetFileName, tokenizer.nextToken());
                    if (tokenizer.hasMoreTokens()) {
                        Rectangle region = new Rectangle(
                                Integer.parseInt(tokenizer.nextToken()), 
                                Integer.parseInt(tokenizer.nextToken()), 
                                Integer.parseInt(tokenizer.nextToken()), 
                                Integer.parseInt(tokenizer.nextToken()));
                        addImageAsset(assetName, url, region);
                    } else {
                        addImageAsset(assetName, url);
                    }
                } else if (assetType.compareTo("ImageAssets") == 0) {
                    URL url = new URL(assetFileName, tokenizer.nextToken());
                    int numAssetsWide = Integer.parseInt(tokenizer.nextToken());
                    int numAssetsHigh = Integer.parseInt(tokenizer.nextToken());
                    addImageAssets(assetName, url, numAssetsWide, numAssetsHigh);
                } 
                
                /////////////////////////////////
                // Image asset sequence loader //
                /////////////////////////////////
                
                else if (assetType.compareTo("ImageAssetSequence") == 0) {
                    int playCount = 0, homeFrame = 0;
                    long animationPeriod = 0;

                    boolean animated = (tokenizer.nextToken().compareTo("Animated") == 0) 
                            ? true : false;
                    if (animated) {
                        playCount = Integer.parseInt(tokenizer.nextToken());
                        animationPeriod = Long.parseLong(tokenizer.nextToken());
                        homeFrame = Integer.parseInt(tokenizer.nextToken());
                    }

                    // Load in the image sequence based on a series of URLs
                    String sequenceType = tokenizer.nextToken();
                    if (sequenceType.compareTo("URLs") == 0) {
                        URL[] urls = new URL[Integer.parseInt(tokenizer.nextToken())];
                        for (int idx = 0; idx < urls.length; idx++)
                            urls[idx] = new URL(assetFileName, tokenizer.nextToken());

                        if (animated) {
                            addImageAssetSequence(assetName, 
                                    playCount, animationPeriod, homeFrame, urls);
                        } else {
                            addImageAssetSequence(assetName, urls);
                        }
                    } 

                    // Load in the image sequence by splitting the image into segments
                    else if (sequenceType.compareTo("Segments") == 0) {
                        int numSegments = Integer.parseInt(tokenizer.nextToken());
                        URL url = new URL(assetFileName, tokenizer.nextToken());

                        if (animated) {
                            addImageAssetSequence(assetName, playCount, 
                                    animationPeriod, homeFrame, numSegments, url);
                        } else {
                            addImageAssetSequence(assetName, numSegments, url);
                        }
                    } 
                    
                    // Load in the image sequence based on an array of image segments 
                    else if (sequenceType.compareTo("Array") == 0) {
                        int numSegmentsWide = Integer.parseInt(tokenizer.nextToken());
                        int numSegmentsHigh = Integer.parseInt(tokenizer.nextToken());
                        URL url = new URL(assetFileName, tokenizer.nextToken());

                        if (animated) {
                            addImageAssetSequence(assetName, playCount, 
                                    animationPeriod, homeFrame, numSegmentsWide, 
                                    numSegmentsHigh, url);
                        } else {
                            addImageAssetSequence(assetName, 
                                    numSegmentsWide, numSegmentsHigh, url);
                        }
                    } 
                    
                    // Load in the image sequence based on the rectangular segments
                    else if (sequenceType.compareTo("Rectangles") == 0) {
                        URL url = new URL(assetFileName, tokenizer.nextToken());
                        Rectangle[] regions 
                                = new Rectangle[Integer.parseInt(tokenizer.nextToken())];
                        for (int idx = 0; idx < regions.length; idx++) {
                            regions[idx] = new Rectangle(
                                    Integer.parseInt(tokenizer.nextToken()), 
                                    Integer.parseInt(tokenizer.nextToken()), 
                                    Integer.parseInt(tokenizer.nextToken()), 
                                    Integer.parseInt(tokenizer.nextToken()));
                        }
                        if (animated) {
                            addImageAssetSequence(assetName, playCount, 
                                    animationPeriod, homeFrame, url, regions);
                        } else {
                            addImageAssetSequence(assetName, url, regions);
                        }
                    }
                } 
                
                ////////////////////////////////////////
                // Image asset ribbon and tile loader //
                //////////////////////////////////////// 
                
                else if (assetType.compareTo("ImageAssetRibbon") == 0 
                        || assetType.compareTo("ImageAssetTile") == 0) {
                    int viewPortX = Integer.parseInt(tokenizer.nextToken());
                    int viewPortY = Integer.parseInt(tokenizer.nextToken());
                    int viewPortWidth = Integer.parseInt(tokenizer.nextToken());
                    int viewPortHeight = Integer.parseInt(tokenizer.nextToken());

                    if (assetType.compareTo("ImageAssetTile") == 0) {
                        addImageAssetTile(assetName, viewPortX, viewPortY, 
                                viewPortWidth, viewPortHeight, 
                                new URL(assetFileName, tokenizer.nextToken()));
                    } else {
                        ImageAssetRibbon.RibbonScrollDirection direction 
                                = tokenizer.nextToken().compareTo("Vertical") == 0 
                                    ? ImageAssetRibbon.RibbonScrollDirection.VERTICAL 
                                    : ImageAssetRibbon.RibbonScrollDirection.HORIZONTAL;
                        URL[] urls = new URL[Integer.parseInt(tokenizer.nextToken())];
                        for (int idx = 0; idx < urls.length; idx++) {
                            urls[idx] = new URL(assetFileName, tokenizer.nextToken());
                        }
                        addImageAssetRibbon(assetName, viewPortX, viewPortY, 
                                viewPortWidth, viewPortHeight, direction, urls);
                    }
                } 
                
                /////////////////////////////////
                // Sound and MIDI asset loader //
                /////////////////////////////////
                
                else if (assetType.compareTo("SoundAssetClip") == 0) {
                    addSoundAssetClip(assetName, 
                            Boolean.parseBoolean(tokenizer.nextToken()), 
                            new URL(assetFileName, tokenizer.nextToken()));
                } else if (assetType.compareTo("SoundAssetAssembly") == 0) {
                    addSoundAssetAssembly(assetName, 
                            Integer.parseInt(tokenizer.nextToken()), 
                            new URL(assetFileName, tokenizer.nextToken()));
                } else if (assetType.compareTo("MidiAsset") == 0) {
                    addMidiAsset(assetName, 
                            Boolean.parseBoolean(tokenizer.nextToken()), 
                            new URL(assetFileName, tokenizer.nextToken()));
                }

                inputLine = inputFile.readLine();
            }
            
            inputFile.close();
            
        } catch( NoSuchElementException elementException ) {
            System.err.println("AssetManager.loadAssetsFromFile" + 
                    "Error parseing line " + lineNumber + " [" + inputLine + "] "
                    + elementException.toString());
            elementException.printStackTrace();          
        } catch (IOException ioException) {
            System.err.println("AssetManager.loadAssetsFromFile" + 
                    "Error loading from file at line " + lineNumber + 
                    " " + ioException.toString());
            ioException.printStackTrace();
        } catch (NullPointerException nullPointer) {
            System.err.println("AssetManager.loadAssetsFromFile" + 
                    "Null pointer exception for [" + assetFileName + "] " + nullPointer.toString());
            nullPointer.printStackTrace();
        }
    }    
    
    
    ///////////////////////////////////////////////////////////////////////////
    // Methods : Asset Retreival                                             //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Attempt to return a shallow clone of the specified archetype asset
     *
     * @param  assetName the name of the asset to be retrieved
     * @return Asset retrieved by the asset manager
     *
     * @exception IllegalArgumentException if the specified asset cannot be found
     */
    public Asset retrieveAsset(String assetName) {
        if (assetStore.containsKey(assetName) == false) {
            throw new IllegalArgumentException( "AssetManager.retrieveAsset: " +
                    "Cannot locate " + assetName + " in asset store.");
        }
        
        Asset asset = assetStore.get(assetName);
        return asset.shallowClone();
    }

    /**
     * Attempt to return a deep clone of the specified archetype asset (i.e.
     * the returned Asset will not reference the archetype asset and can
     * be freely changed without effecting other game object that use the
     * archetype asset.
     *
     * @param  assetName the name of the asset to be retrieved
     *
     * @exception IllegalArgumentException if the specified asset cannot be found
     */
    public Asset retrieveAssetClone(String assetName) {
        if (assetStore.containsKey(assetName) == false) {
            throw new IllegalArgumentException("AssetManager.retrieveAsset: " +
                    "Cannot locate " + assetName + " in asset store.");
        }
        
        Asset asset = assetStore.get(assetName);
        return asset.deepClone();
    }

    /**
     * Attempt to return a reference to the stored archetype asset.
     *
     * Note: It is anticipated this method will only be called to modify
     * the 'default' parameters returned by shallow/deep cloning and/or
     * the asset
     *
     * @param  assetName the name of the asset to be retrieved
     *
     * @exception IllegalArgumentException if the specified asset cannot be found
     */
    public Asset retrieveAssetArchetype(String assetName) {
        if (assetStore.containsKey(assetName) == false) {
            throw new IllegalArgumentException("AssetManager.retrieveAsset: " +
                    "Cannot locate " + assetName + " in asset store.");
        }
        
        return assetStore.get(assetName);
    }

    /**
     * Attempt to return a shallow clone of the specified graphical asset
     *
     * @param  assetName the name of the asset to be retrieved
     * @return GraphicalAsset retrieved by the asset manager
     *
     * @exception IllegalArgumentException if the specified asset cannot be found
     *            or if the specified asset is not of type GraphicalAsset
     */
    public GraphicalAsset retrieveGraphicalAsset(String assetName) {
        if (assetStore.containsKey(assetName) == false) {
            throw new IllegalArgumentException("AssetManager.retrieveGraphicalAsset: " +
                    "Cannot locate " + assetName + " in asset store.");
        }
        
        Asset asset = assetStore.get(assetName);        
        if (asset instanceof GraphicalAsset == false) {
            throw new IllegalArgumentException("AssetManager.retrieveGraphicalAsset: " + 
                    assetName + " is not a GraphicalAsset.");
        }
        
        return (GraphicalAsset) asset.shallowClone();
    }

    /**
     * Attempt to return the graphical asset archetype of the specified graphical asset
     *
     * @param  assetName the name of the archetype asset to be retrieved
     * @return GraphicalAsset retrieved by the asset manager
     *
     * @exception IllegalArgumentException if the specified asset cannot be found
     * @exception IllegalArgumentException if the specified asset cannot be found
     *            or if the specified asset is not of type GraphicalAsset
     */
    public GraphicalAsset retrieveGraphicalAssetArchetype(String assetName) {
        if (assetStore.containsKey(assetName) == false) {
            throw new IllegalArgumentException(
                    "AssetManager.retrieveGraphicalAssetArchetype: " +
                    "Cannot locate " + assetName + " in asset store.");
        }
        
        Asset asset = assetStore.get(assetName);
        if (asset instanceof GraphicalAsset == false) {
            throw new IllegalArgumentException( 
                    "AssetManager.retrieveGraphicalAssetArchetype: " + 
                    assetName + " not a GraphicalAsset.");
        }
        
        return (GraphicalAsset) asset;
    }

    /**
     * Attempt to return a shallow clone of the specified sound asset
     *
     * @param  assetName the name of the asset to be retrieved
     * @return SoundAsset retrieved by the asset manager
     *
     * @exception IllegalArgumentException if the specified asset cannot be found
     *            or if the specified asset is not of type SoundAsset
     */
    public SoundAsset retrieveSoundAsset(String assetName) {
        if (assetStore.containsKey(assetName) == false) {
            throw new IllegalArgumentException("AssetManager.retrieveSoundAsset: " +
                    "Cannot locate " + assetName + " in asset store.");
        }
        
        Asset asset = assetStore.get(assetName);
        if (asset instanceof SoundAsset == false) {
            throw new IllegalArgumentException("AssetManager.retrieveSoundAsset: " + 
                    assetName + " not a SoundAsset.");
        }
        
        return (SoundAsset) asset.shallowClone();
    }

    /**
     * Attempt to return a shallow clone of the specified sound archetype asset
     *
     * @param  assetName the name of the asset to be retrieved
     * @return SoundAsset retrieved by the asset manager
     *
     * @exception IllegalArgumentException if the specified asset cannot be found
     *            or if the specified asset is not of type SoundAsset
     */
    public SoundAsset retrieveSoundAssetArchetype(String assetName) {
        if (assetStore.containsKey(assetName) == false) {
            throw new IllegalArgumentException(
                    "AssetManager.retrieveSoundAssetArchetype: Cannot locate " + 
                    assetName + " in asset store.");
        }
        
        Asset asset = assetStore.get(assetName);
        if (asset instanceof SoundAsset == false) {
            throw new IllegalArgumentException(
                    "AssetManager.retrieveSoundAssetArchetype: " + 
                    assetName + " not a SoundAsset.");
        }
        
        return (SoundAsset) asset;
    }
}
