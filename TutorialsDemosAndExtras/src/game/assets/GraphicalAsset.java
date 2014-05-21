package game.assets;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * GraphicalAsset is the abstract superclass of all graphical asset be
 * they image based or generated (i.e drawn). Any new type of graphical
 * asset should either directly or indirectlly inherit from this
 * class.
 *
 * @author <A HREF="mailto:P.Hanna@qub.ac.uk">Philip Hanna</A>
 * @version $Revision: 1.1 $ $Date: 2007/08 $
 */
public abstract class GraphicalAsset extends Asset {

    ///////////////////////////////////////////////////////////////////////////
    // Class data                                                            //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Pixel width of this graphical asset. It is granted public
     * access in order to ensure faster read access.
     */
    public int width;

    /**
     * Pixel height of this graphical asset. It is granted public
     * access in order to ensure faster read access.
     */
    public int height;


    /**
     * X offset of this graphical asset relative to the game object.
     * It is granted public access in order to ensure faster read access.
     */
    public double offsetX = 0;

    /**
     * Y offset of this graphical asset relative to the game object.
     * It is granted public access in order to ensure faster read access.
     */
    public double offsetY = 0;


    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Construct the graphical asset to use the specified asset name
     *
     * @param  assetName the name of this asset
     */
    public GraphicalAsset(String assetName) {
        super(assetName);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods                                                               //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Return a BufferedImage object that contains the current graphical
     * representation of the GraphicalAsset.
     *
     * Note: All extending classes must provide an implementation of this
     * method.
     *
     * @return BufferedImage representation of the asset
     */
    public abstract BufferedImage getImageRealisation();

    /**
     * Render the GraphicalAsset on the provided graphical object at the
     * specified x and y offset.
     *
     * Note: All extending classes must provide an implementation of this
     * method.
     *
     * @param  graphics2D graphical object on which to render this asset
     * @param  drawX x offset at which the asset should be rendered
     * @param  drawY y offset at which the asset should be rendered
     */
    public abstract void draw(Graphics2D graphics2D, int drawX, int drawY);
}
