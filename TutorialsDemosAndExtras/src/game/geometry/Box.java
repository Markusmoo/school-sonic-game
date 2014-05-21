package game.geometry;

/**
 * Box provides a simple rectangular geometry bound
 * <P>
 * Note: The physics engine within this code repository draws upon the
 * work of two individuals. See the CollisionSpace class header for
 * additional information.
 * <P>
 * Note: If the size of the box is to be changed, it is important
 * that the setDimensions method be called to ensure that the surface factor,
 * moment of inertia and bound dimension are correctly updated.
 * 
 * @version $Revision: 1.0 $ $Date: 2007/08 $
 */
public class Box extends Shape {

    ///////////////////////////////////////////////////////////////////////////
    // Class data:                                                           //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Width of the box
     */
    public double width;

    /**
     * Height of the box
     */
    public double height;


    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Create a box of the specified size and at the specified offset relative
     * to the game object
     *
     * @param  offsetX x offset of this box
     * @param  offsetY y offset of this box
     * @param  width width of this box
     * @param  height height of this box
     * 
     * @exception IllegalArgumentException if the box width or height are invalid
     */
    public Box(double offsetX, double offsetY, double width, double height) {
        if (width <= 0.0 || height <= 0.0) {
            throw new IllegalArgumentException("Box.constructor: " +
                    "Invalid width/height = " + width + " x " + height);
        }
        
        setOffsets(offsetX, offsetY);
        setDimensions(width, height);
    }

    
    ///////////////////////////////////////////////////////////////////////////
    // Methods:                                                              //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Set the dimensions of the box to that specified
     *
     * @param  width width of this box
     * @param  height height of this box
     */
    public void setDimensions(double width, double height) {
        this.width = width;
        this.height = height;

        // I = (1/12)M(a + b)
        this.momentOfInertiaFactor = (width * width + height * height) / 12.0;
        
        // Define the surface factor and bound dimension
        this.boundDimension = Math.sqrt(height * height + width * width);
        this.surfaceArea = width * height;
    }
}