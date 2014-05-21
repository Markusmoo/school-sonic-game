package game.assets;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * ImageAssetTile provides a repeatedly tiled image. A moveable viewport
 * onto the tiled image determines the segment of viewport which will be
 * rendered.
 *
 * @author <A HREF="mailto:P.Hanna@qub.ac.uk">Philip Hanna</A>
 * @version $Revision: 1.1 $ $Date: 2007/08 $
 */
public class ImageAssetTile extends GraphicalAsset {

    ///////////////////////////////////////////////////////////////////////////
    // Class data                                                            //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Image to be tiled
     */
    private BufferedImage image;

    /**
     * Store the view port x and y offset (relative to the start of the
     * base tile image). The viewport width and height is stored within the
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
     * @param  image BufferedImage holding the base tile image
     * @param  viewPortX x offset of the viewport (relative to the start of the tile)
     * @param  viewPortY y offset of the viewport (relative to the start of the tile)
     * @param  viewPortWidth width of the viewport
     * @param  viewPortHeight height of the viewport
     *
     * @exception IllegalArgumentException if an invalid viewport dimension is 
     *            specified
     */
    public ImageAssetTile(String assetName, BufferedImage image, 
            int viewPortX, int viewPortY, int viewPortWidth, int viewPortHeight) {
        super(assetName);

        if (viewPortWidth <= 0 || viewPortHeight <= 0) {
            throw new IllegalArgumentException("ImageAssetTile.constructor: " +
                    "Invalid viewport size.");
        }
        
        this.image = image;

        // Store the viewport offset and dimension (the dimension is stored in
        // the inherited width and height variables, i.e. thereby defining the
        // render width and height of this graphical asset.
        this.viewPortX = viewPortX;
        this.viewPortY = viewPortY;
        this.width = viewPortWidth;
        this.height = viewPortHeight;
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
     * Set the view port dimension to the specified size
     *
     * @param  viewPortWidth integer viewport width
     * @param  viewPortHeight integer viewport height
     */
    public void setViewPortDimension(int viewPortWidth, int viewPortHeight) {
        this.width = viewPortWidth;
        this.height = viewPortHeight;
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
     * within the bounds of the base tile image
     */
    private void validateViewPort() {
        viewPortX = viewPortX % image.getWidth();
        viewPortY = viewPortY % image.getHeight();

        if (viewPortX < 0) {
            viewPortX = image.getWidth() + viewPortX;
        }
        if (viewPortY < 0) {
            viewPortY = image.getHeight() + viewPortY;
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods: Render / Clone                                               //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Generate a BufferedImage realisation of the current viewport contents.
     *
     * @return BufferedImage reference containing the current viewport contents.
     */
    public BufferedImage getImageRealisation() {
        GraphicsConfiguration graphicsConfiguration = 
                GraphicsEnvironment.getLocalGraphicsEnvironment().
                    getDefaultScreenDevice().getDefaultConfiguration();

        BufferedImage realisedImage = graphicsConfiguration.createCompatibleImage(
                width, height, image.getColorModel().getTransparency());

        Graphics2D graphics2D = realisedImage.createGraphics();
        draw(graphics2D, 0, 0);
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
        int tileWidth = image.getWidth();
        int tileHeight = image.getHeight();

        int drawXOffset;
        int drawYOffset;
        int tileXOffset;
        int tileYOffset;
        int drawWidth = 0;
        int drawHeight = 0;

        validateViewPort();

        drawXOffset = 0;
        tileXOffset = viewPortX;
        while (drawXOffset < width) {
            drawYOffset = 0;
            tileYOffset = viewPortY;
            while (drawYOffset < height) {
                drawWidth = tileWidth - tileXOffset;
                if (drawWidth > width - drawXOffset) {
                    drawWidth = width - drawXOffset;
                }
                drawHeight = tileHeight - tileYOffset;
                if (drawHeight > height - drawYOffset) {
                    drawHeight = height - drawYOffset;
                }
                graphics2D.drawImage(image, 
                        drawX + drawXOffset, drawY + drawYOffset, 
                        drawX + drawXOffset + drawWidth, 
                        drawY + drawYOffset + drawHeight, tileXOffset, 
                        tileYOffset, tileXOffset + drawWidth, 
                        tileYOffset + drawHeight, null);

                drawYOffset += drawHeight;
                tileYOffset = (tileYOffset + drawHeight) % tileHeight;
            }

            drawXOffset += drawWidth;
            tileXOffset = (tileXOffset + drawWidth) % tileWidth;
        }
    }

    /**
     * Return a shallow clone of this image asset ribbon.
     *
     * @return  new Asset instance containing a shallow clone of this instance
     */
    public Asset shallowClone() {
        ImageAssetTile clone = new ImageAssetTile(
                assetName, image, viewPortX, viewPortY, width, height);
        return clone;
    }

    /**
     * Return a deep clone of this image asset ribbon.
     *
     * @return  new Asset instance containing a deep clone of this image asset
     */
    public Asset deepClone() {
        ImageAssetTile clone = (ImageAssetTile) shallowClone();

        clone.image = new BufferedImage(
                image.getWidth(), image.getHeight(), image.getType());
        Graphics2D graphics2D = clone.image.createGraphics();
        graphics2D.drawImage(image, null, 0, 0);
        graphics2D.dispose();

        return clone;
    }
}
