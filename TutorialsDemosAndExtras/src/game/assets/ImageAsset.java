package game.assets;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * An ImageAsset stores a single image asset
 *
 * @author <A HREF="mailto:P.Hanna@qub.ac.uk">Philip Hanna</A>
 * @version $Revision: 1 $ $Date: 2006/08 $
 */
public class ImageAsset extends GraphicalAsset {

    ///////////////////////////////////////////////////////////////////////////
    // Class data                                                            //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * BufferedImage holding the image asset
     */
    private BufferedImage image;


    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Construct a new image asset object using the specified image
     *
     * @param  assetName the name of this asset
     * @param  image BufferedImage instance holding the image asset
     */
    public ImageAsset(String assetName, BufferedImage image) {
        super(assetName);

        this.image = image;

        width = image.getWidth();
        height = image.getHeight();
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods                                                               //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Return a reference to the BufferedImage image asset
     *
     * @return BufferedImage reference to the stored image asset
     */
    public BufferedImage getImageRealisation() {
        return image;
    }

    /**
     * Render the image asset on the provided graphical object at the
     * specified x and y offset.
     *
     * @param  graphics2D graphical object on which to render this asset
     * @param  drawX x offset at which the asset should be rendered
     * @param  drawY y offset at which the asset should be rendered
     */
    public void draw(Graphics2D graphics2D, int drawX, int drawY) {
        graphics2D.drawImage(image, drawX, drawY, null);
    }

    /**
     * Return a shallow clone of this image asset.
     *
     * @return  new Asset instance containing a shallow clone of this Asset instance
     */
    public Asset shallowClone() {
        ImageAsset clone = new ImageAsset(assetName, image);
        return clone;
    }

    /**
     * Return a deep clone of this image asset.
     *
     * @return  new Asset instance containing a deep clone of this image asset
     */
    public Asset deepClone() {
        ImageAsset clone = (ImageAsset) shallowClone();

        clone.image = new BufferedImage(
                image.getWidth(), image.getHeight(), image.getType());
        Graphics2D graphics2D = clone.image.createGraphics();
        graphics2D.drawImage(image, null, 0, 0);
        graphics2D.dispose();

        return clone;
    }
}
