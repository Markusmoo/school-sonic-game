package game.physics;

import game.geometry.Shape;

/**
 * Contact holds information pertaining to a point of contact between two
 * different bodies. Instances of this class are maintained by an arbiter
 * which manages all contacts between two associated bodies.
 * <P>
 * Note: The physics engine within this code repository draws upon the
 * work of two individuals. See the CollisionSpace class header for
 * additional information.
 * <P>
 * @version $Revision: 1.0 $ $Date: 2007/08 $
 */
public class Contact {

    ///////////////////////////////////////////////////////////////////////////
    // Class data                                                            //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * The two shapes (one from each body) that are in contact
     */
    public Shape shape1;
    public Shape shape2;

    /**
     * The x and y position of the point of contact
     */
    public double x;
    public double y;

    /** 
     * The normal to the point of contact
     */
    public double normalx;
    public double normaly;

    /**
     * Amount that the shapes need to be separated 
     */
    public double separation;

    /**
     * The impulse accumulated in the direction of the normal
     */ 
    public double accumulatedNormalImpulse;

    /** 
     * The impulse accumulated in the direction of the tangent 
     */
    public double accumulatedTangentImpulse;

    /** 
     * The mass applied through the normal at this contact point 
     */
    public double massNormal;

    /** 
     * The mass applied through the tangent at this contact point 
     */
    public double massTangent;

    /** 
     * The correction factor penetration 
     */
    public double bias;

    /** 
     * The restitution at this point of contact 
     */
    public double restitution;

    /** 
     * The bias impulse accumulated 
     */
    public double biasImpulse;

    /** 
     * The pair of edges this contact is between (for box to box collisions)
     */
    public int inEdge1, outEdge1;
    public int inEdge2, outEdge2;

    
    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Create a new point of contact
     */
    public Contact() {
        accumulatedNormalImpulse = 0.0;
        accumulatedTangentImpulse = 0.0;
    }

    
    ///////////////////////////////////////////////////////////////////////////
    // Methods:                                                              //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Set the contact details to that specified in the reference contact
     *
     * @param contact Contact reference that values will be set from
     */
    void set(Contact contact) {
        this.x = contact.x;
        this.y = contact.y;
        this.normalx = contact.normalx;
        this.normaly = contact.normaly;
        separation = contact.separation;
        accumulatedNormalImpulse = contact.accumulatedNormalImpulse;
        accumulatedTangentImpulse = contact.accumulatedTangentImpulse;
        massNormal = contact.massNormal;
        massTangent = contact.massTangent;
        bias = contact.bias;
        restitution = contact.restitution;

        inEdge1 = contact.inEdge1;
        outEdge1 = contact.outEdge1;
        inEdge2 = contact.inEdge2;
        outEdge2 = contact.outEdge2;
    }

    /**
     * Return a unique hashcode for this contact
     * 
     * @return unique hashcode for this contact
     */
    @Override
    public int hashCode() {
        if (this.shape1.uniqueShapeID < this.shape2.uniqueShapeID) {
            return shape1.uniqueShapeID * 65536 + shape2.uniqueShapeID;
        } else {
            return shape2.uniqueShapeID * 65536 + shape1.uniqueShapeID;
        }
    }

    /**
     * Determine if the specified object is equivalent to this contact
     * 
     * @param other Object to compare
     * @return boolean true if the other object is equivalent to this contact,
     *         otherwise false
     */
    @Override
    public boolean equals(Object other) {
        if (other.getClass() == getClass()) {
            return hashCode() == other.hashCode();
        }

        return false;
    }
}
