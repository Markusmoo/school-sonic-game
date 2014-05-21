package game.geometry;

/**
 * Circle provides a simple circular geometry bound
 * <P>
 * Note: The physics engine within this code repository draws upon the
 * work of two individuals. See the CollisionSpace class header for
 * additional information.
 * <P>
 * Note: If the radius of the circle is to be changed, it is important
 * that the setRadius method be called to ensure that the surface factor,
 * moment of inertia and bound dimension are correctly updated.
 *
 * @version $Revision: 1.0 $ $Date: 2007/08 $
 */
public class Circle extends Shape {

    ///////////////////////////////////////////////////////////////////////////
    // Class data:                                                           //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Radius of the circle
     */
    public double radius;


    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Create a circle of the specified size and at the specified offset relative
     * to the game object
     *
     * @param  offsetX x offset of this box
     * @param  offsetY y offset of this box
     * @param  radius radius of this circle
     * 
     * @exception IllegalArgumentException if the box width or height are invalid
     */
    public Circle(double offsetX, double offsetY, double radius) {
        if (radius <= 0.5) {
            throw new IllegalArgumentException("Circle.constructor: " +
                    "Invalid circle radius = " + radius);
        }
        
        setOffsets(offsetX, offsetY);
        setRadius(radius);
    }

    
    ///////////////////////////////////////////////////////////////////////////
    // Methods:                                                              //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Set the radius of the circle to that specified
     *
     * @param  radius radius of this circle
     */
    public void setRadius(double radius) {
        this.radius = radius;

        // I = 0.5R^2
        this.momentOfInertiaFactor = 0.5 * radius * radius;

        // Define the surface factor and bound dimension
        this.boundDimension = 2.0 * radius;
        this.surfaceArea = Math.PI * radius * radius;
    }
}
