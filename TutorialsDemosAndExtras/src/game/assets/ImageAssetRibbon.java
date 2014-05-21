package game.assets;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * An ImageAssetRibbon can hold a number of images arranged in either
 * a horizontal or vertical strip. A moveable viewport onto the image
 * strip determines the segment of veiwport which will be rendered.
 * The viewport will wrap around the image strip as necessary.
 *
 * @author <A HREF="mailto:P.Hanna@qub.ac.uk">Philip Hanna</A>
 * @version $Revision: 1.1 $ $Date: 2007/08 $
 */
public class ImageAssetRibbon extends GraphicalAsset {

    ///////////////////////////////////////////////////////////////////////////
    // Class data                                                            //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Define the orientation of the image strip
     */
    public enum RibbonScrollDirection { VERTICAL, HORIZONTAL }

    /**
     * Define the image strip and keep a record of image start
     * offsets within the strip (to facilitate the mapping of
     * the viewport onto the image strip).
     */
    private BufferedImage[] images;
    private int numImages;
    private int[] imageOffsets;

    /**
     * Define the orientation of the image strip
     */
    private RibbonScrollDirection ribbonScrollDirection;

    /**
     * Store the view port x and y offset (relative to the start of the
     * image strip). The viewport width and height is stored within the
     * width and height variables inherited from GraphicalAsset.
     */
    private int viewPortX;
    private int viewPortY;


    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Construct a new image ribbon asset consisting of the specified
     * image array, with the specified orientation and viewport
     * location/dimension.
     *
     * @param  assetName the name of this asset
     * @param  images BufferedImage array holding an order sequences of image
     *         comprising an image stip.
     * @param  ribbonScrollDirection Vertical or Horizontal orientation of the 
     *         image strip
     * @param  viewPortX x offset of the viewport (relative to the start of 
     *         the image strip)
     * @param  viewPortY y offset of the viewport (relative to the start of 
     *         the image strip)
     * @param  viewPortWidth width of the viewport
     * @param  viewPortHeight height of the viewport
     *
     * @exception IllegalArgumentException if an invalid viewport dimension 
     *            is specified
     */
    public ImageAssetRibbon(String assetName, BufferedImage[] images, 
            RibbonScrollDirection ribbonScrollDirection, 
            int viewPortX, int viewPortY, int viewPortWidth, int viewPortHeight) {
        super(assetName);

        if (viewPortWidth <= 0 || viewPortHeight <= 0) {
            throw new IllegalArgumentException("ImageAssetRibbon.constructor: " +
                    "Invalid viewport size.");
        }
        
        // Store the image sequence and ribbon orientation
        this.images = images;
        this.numImages = images.length;
        this.ribbonScrollDirection = ribbonScrollDirection;

        // Store the viewport offset and dimension (the dimension is stored in
        // the inherited width and height variables, i.e. thereby defining the
        // render width and height of this graphical asset.
        this.viewPortX = viewPortX;
        this.viewPortY = viewPortY;
        this.width = viewPortWidth;
        this.height = viewPortHeight;

        // Compute and store the image start offsets for images within the ribbon
        // This information will be used when mapping the viewport onto the image
        // strip.
        imageOffsets = new int[this.numImages + 1];
        int totalOffset = 0;
        for (int idx = 0; idx < this.numImages; idx++) {
            imageOffsets[idx] = totalOffset;
            totalOffset += (ribbonScrollDirection == RibbonScrollDirection.VERTICAL) ? images[idx].getHeight() : images[idx].getWidth();
        }
        
        imageOffsets[numImages] = totalOffset;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods: Viewport control                                             //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Return the viewport x offset
     *
     * @return int viewport x offset
     */    
    public int getViewPortX() {
        return viewPortX;
    }

    /**
     * Return the viewport y offset
     *
     * @return int viewport y offset
     */    
    public int getViewPortY() {
        return viewPortY;
    }

    /**
     * Set the view port to the specified x and y offsets
     *
     * @param  viewPortX x viewport offset
     * @param  viewPortY y viewport offset
     */
    public void setViewPort(int viewPortX, int viewPortY) {
        this.viewPortX = viewPortX;
        this.viewPortY = viewPortY;
    }

    /**
     * Move the viewport the specified distance along the x and y axis
     *
     * @param  viewPortMoveX distance to move along the x axis
     * @param  viewPortMoveY distance to move along the y axis
     */
    public void moveViewPort(int viewPortMoveX, int viewPortMoveY) {
        viewPortX += viewPortMoveX;
        viewPortY += viewPortMoveY;
    }

    /**
     * Move the viewport up by the specified distance
     * <P>
     * Note: The up direction is relative to the physical screen
     * as opposed to the y-axis.
     *
     * @param  magnitude number of pixels by which the viewport is to be moved
     */
    public void moveViewPortUp(int magnitude) {
        viewPortY -= magnitude;
    }

    /**
     * Move the viewport down by the specified distance
     * <P>
     * Note: The down direction is relative to the physical screen
     * as opposed to the y-axis.
     *
     * @param  magnitude number of pixels by which the viewport is to be moved
     */
    public void moveViewPortDown(int magnitude) {
        viewPortY += magnitude;
    }

    /**
     * Move the viewport left by the specified distance
     *
     * @param  magnitude number of pixels by which the viewport is to be moved
     */
    public void moveViewPortLeft(int magnitude) {
        viewPortX -= magnitude;
    }

    /**
     * Move the viewport right by the specified distance
     *
     * @param  magnitude number of pixels by which the viewport is to be moved
     */
    public void moveViewPortRight(int magnitude) {
        viewPortX += magnitude;
    }

    /**
     * Validate the viewport to ensure that the relevant viewport offset is
     * within the bounds of the image strip.
     *
     * Note: The validation is only done in the direction of the viewport.
     */
    private void validateViewPort() {
        if (ribbonScrollDirection == RibbonScrollDirection.HORIZONTAL) {
            viewPortX = viewPortX % imageOffsets[numImages];
            if( viewPortX < 0 )
                viewPortX = imageOffsets[numImages] + viewPortX;
        } else {
            viewPortY = viewPortY % imageOffsets[numImages];
            if( viewPortY < 0 )
                viewPortY = imageOffsets[numImages] + viewPortY;
        }        
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods: Render                                                       //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Generate a BufferedImage realisation of the current viewport contents.
     *
     * @return BufferedImage reference containing the current viewport contents.
     */
    public BufferedImage getImageRealisation() {
        // Create a new BufferedImage onto which the viewport can be drawn
        GraphicsConfiguration graphicsConfiguration =
                GraphicsEnvironment.getLocalGraphicsEnvironment().
                    getDefaultScreenDevice().getDefaultConfiguration();

        BufferedImage realisedImage = graphicsConfiguration.createCompatibleImage(
                width, height, images[0].getColorModel().getTransparency());

        // Render the current viewport to the BufferedImage instance
        Graphics2D graphics2D = realisedImage.createGraphics();
        this.draw(graphics2D, 0, 0);
        graphics2D.dispose();

        return realisedImage;
    }

    /**
     * Render the current viewport contents on the provided graphical object
     * at the specified x and y offset.
     *
     * @param  graphics2D graphical object on which to render this asset
     * @param  drawX x offset at which the asset should be rendered
     * @param  drawY y offset at which the asset should be rendered
     */
    public void draw(Graphics2D graphics2D, int drawX, int drawY) {
        if (ribbonScrollDirection == RibbonScrollDirection.VERTICAL) {
            drawVertical(graphics2D, drawX, drawY);
        } else {
            drawHorizontal(graphics2D, drawX, drawY);
        }
    }

    /**
     * Render the current viewport contents for a horizontal image strip
     * on the provided graphical object at the specified x and y offset.
     *
     * @param  graphics2D graphical object on which to render this asset
     * @param  drawX x offset at which the asset should be rendered
     * @param  drawY y offset at which the asset should be rendered
     */
    public void drawHorizontal(Graphics2D graphics2D, int drawX, int drawY) {
        // Validate the viewport to ensure it is within the bounds of the strip
        validateViewPort();

        int drawWidth;
        int drawOffset = 0;
        int viewPortOffset = viewPortX;
        int currentImageIdx = 0;

        // Determine the image on which the viewport starts
        while (imageOffsets[currentImageIdx + 1] < viewPortX) {
            currentImageIdx++;
        }
        
        // Repeately draw images until the entire viewport has been rendered
        while (drawOffset < width) {
            // Determine how much of the current image should be drawn 
            drawWidth = imageOffsets[currentImageIdx + 1] - viewPortOffset;
            if (drawOffset + drawWidth > width)
                drawWidth = width - drawOffset;

            // Draw the current image at the correct offset 
            graphics2D.drawImage(images[currentImageIdx], drawX + drawOffset, 
                    drawY, drawX + drawOffset + drawWidth, drawY + height, 
                    viewPortOffset - imageOffsets[currentImageIdx], viewPortY, 
                    viewPortOffset - imageOffsets[currentImageIdx] + drawWidth, 
                    viewPortY + height, null);

            // Wrap around the first image if we run off the end of the image strip
            drawOffset += drawWidth;
            viewPortOffset = (viewPortOffset + drawWidth) % imageOffsets[numImages];
            currentImageIdx = (currentImageIdx + 1) % numImages;
        }
    }

    /**
     * Render the current viewport contents for a vertical image strip
     * on the provided graphical object at the specified x and y offset.
     *
     * @param  graphics2D graphical object on which to render this asset
     * @param  drawX x offset at which the asset should be rendered
     * @param  drawY y offset at which the asset should be rendered
     */
    public void drawVertical(Graphics2D graphics2D, int drawX, int drawY) {
        // Validate the viewport to ensure it is within the bounds of the strip
        validateViewPort();

        int drawHeight;
        int drawOffset = 0;
        int viewPortOffset = viewPortY;
        int currentImageIdx = 0;

        // Determine the image on which the viewport starts
        while (imageOffsets[currentImageIdx + 1] < viewPortY) {
            currentImageIdx++;
        }
        
        // Repeately draw images until the entire viewport has been rendered
        while (drawOffset < height) {
            drawHeight = imageOffsets[currentImageIdx + 1] - viewPortOffset;
            if (drawOffset + drawHeight > height)
                drawHeight = height - drawOffset;

            // Draw the current image at the correct offset within the graphics object
            graphics2D.drawImage(images[currentImageIdx], drawX, drawY + drawOffset, 
                    drawX + width, drawY + drawOffset + drawHeight, viewPortX, 
                    viewPortOffset - imageOffsets[currentImageIdx], viewPortX + width, 
                    viewPortOffset - imageOffsets[currentImageIdx] + drawHeight, null);

            // Wrap around the first image if we run off the end of the image strip
            drawOffset += drawHeight;
            viewPortOffset = (viewPortOffset + drawHeight) % imageOffsets[numImages];
            currentImageIdx = (currentImageIdx + 1) % numImages;
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods: Clone                                                        //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Return a shallow clone of this image asset ribbon.
     *
     * @return  new Asset instance containing a shallow clone of this instance
     */
    public Asset shallowClone() {
        ImageAssetRibbon clone = new ImageAssetRibbon(assetName, images, 
                ribbonScrollDirection, viewPortX, viewPortY, width, height);
        return clone;
    }

    /**
     * Return a deep clone of this image asset ribbon.
     *
     * @return  new Asset instance containing a deep clone of this image asset
     */
    public Asset deepClone() {
        ImageAssetRibbon clone = (ImageAssetRibbon) shallowClone();

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