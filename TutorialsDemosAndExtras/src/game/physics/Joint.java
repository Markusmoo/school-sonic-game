package game.physics;

import game.geometry.*;
import game.engine.GameObject;

/**
 * dJoint provides an abstract superclass of all types of joint,
 * representing a form of connection between two bodies or shapes.
 * <P>
 * Note: The physics engine within this code repository draws upon the
 * work of two individuals. See the CollisionSpace class header for
 * additional information.
 *
 * @see FixedJoint
 * @see HingedJoint
 *
 * @version $Revision: 1.0 $ $Date: 2007/08 $
 */
public abstract class Joint extends GameObject {

    ///////////////////////////////////////////////////////////////////////////
    // Class data                                                            //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * The next unique identifier to be assigned to a created joint and the
     * unique joint identifier assigned to the joint instance.
     */
    private static int UNIQUE_JOINT_NEXT_ID = 0;
    protected int uniqueJointID;

    /**
     * Enumerate type detailing if the joint is between two bodies or
     * between two shapes (of which it is assumed that the shapes will
     * belong to Body instances). 
     */
    public enum Connector { SHAPE, BODY }
    public Connector connector;

    /**
     * The two Body instances connected by the joint (if the joint is
     * between two bodies)
     */
    public Body body1;
    public Body body2;

    /**
     * The two Shape instances connected by the joint (if the joint is
     * between two shapes)
     */
    public Shape shape1;
    public Shape shape2;

    /**
     * Breaking impulse needed to break this joint, alongside a flag
     * specifying if the joint has been broken.
     */
    protected double breakingImpulse = Double.MAX_VALUE;
    protected boolean jointBroken = false;

    
    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Create a new joint that connects the two specified bodies.
     *
     * @param collisionSpace CollisionSpace running the physics simulation
     * @param body1 first Body to be connected by the joint
     * @param body2 second Body to be connected by the joint
     */
    public Joint(CollisionSpace collisionSpace, Body body1, Body body2) {
        super(collisionSpace);

        uniqueJointID = UNIQUE_JOINT_NEXT_ID++;

        this.connector = Connector.BODY;
        this.body1 = body1;
        this.body2 = body2;
    }

    /**
     * Create a new joint that connects the two specified shapes.
     *
     * @param collisionSpace CollisionSpace running the physics simulation
     * @param shape1 first Shape to be connected by the joint
     * @param shape2 second Shape to be connected by the joint
     */
    public Joint(CollisionSpace collisionSpace, Shape shape1, Shape shape2) {
        super(collisionSpace);

        uniqueJointID = UNIQUE_JOINT_NEXT_ID++;

        connector = Connector.SHAPE;
        this.shape1 = shape1; 
        this.shape2 = shape2;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods:                                                              //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Return the breaking impulse associated with this joint
     *
     * @return double breaking impulse for this joint
     */
    public double getBreakingImpulse() {
        return breakingImpulse;
    }

    /**
     * Set the breaking impulse associated with this joint
     *
     * @param breakingImpulse double breaking impulse for this joint
     */
    public void setBreakingImpulse(double breakingImpulse) {
        this.breakingImpulse = breakingImpulse;
    }
    
    /**
     * Perform common joint setup calculations that will be used within
     * all joint impulse iterations.
     *
     * @param invDT inverse time period for the physics simulation step
     */
    protected abstract void preStep(double invDT);

    /**
     * Apply any impulses through the joint to the connected bodies 
     */
    protected abstract void applyImpulse();
}