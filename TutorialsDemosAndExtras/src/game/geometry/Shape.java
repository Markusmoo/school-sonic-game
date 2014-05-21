package game.geometry;

import game.physics.*;

/**
 * Shape provides the superclass of all forms of shape geometry defined for
 * game objects.
 * <P>
 * Note: The physics engine within this code repository draws upon the
 * work of two individuals. See the CollisionSpace class header for
 * additional information.
 * 
 * @version $Revision: 1.0 $ $Date: 2007/08 $
 */
public abstract class Shape {

    ///////////////////////////////////////////////////////////////////////////
    // Class data:                                                           //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Class variables holding the ID of the next shape object and a 
     * instance variable holding the unique ID of the shape instance
     */
    private static int NEXT_UNIQUE_SHAPE_ID = 0;
    public int uniqueShapeID;

    /**
     * Maximum bound dimension for this shape
     */
    public double boundDimension = 0.0;

    /**
     * Surface area of this shape
     */
    public double surfaceArea = 0.0;

    /**
     * Moment of inertia for this shape
     */
    public double momentOfInertiaFactor = 0.0;

    /**
     * x offset of this shape relative to the center of the parent
     * game object
     */
    public double offsetX = 0;

    /**
     * y offset of this shape relative to the center of the parent
     * game object
     */
    public double offsetY = 0;

    /**
     * Body object to which this shape is attached
     */
    public Body body = null;

    
    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////
        
    /**
     * Create a new shape instance
     */
    public Shape() {
        uniqueShapeID = NEXT_UNIQUE_SHAPE_ID++;
    }

    
    ///////////////////////////////////////////////////////////////////////////
    // Methods:                                                              //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Set the shape offsets of the shape to that specified
     *
     * @param  offsetX x offset of this shape
     * @param  offsetY y offset of this shape
     */    
    public void setOffsets(double offsetX, double offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }
}