package game.physics;

import game.geometry.*;

/**
 * FixedJoint provides a rigid, i.e. unmoveable, joint between two bodies.
 * Given such a joint, the impulses applied to one body, are also applied
 * to the connected body. Fixed joints may be created that anchor bodies
 * by their mid-points or a specified pair of shapes.
 * <P>
 * Note: The physics engine within this code repository draws upon the
 * work of two individuals. See the CollisionSpace class header for
 * additional information.
 * <P>
 * @version $Revision: 1.0 $ $Date: 2007/08 $
 */
public class FixedJoint extends Joint {

    ///////////////////////////////////////////////////////////////////////////
    // Class data                                                            //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * The FixedJoint is actually comprised two of hinged joints between
     * the bodies arranged so that no hinged movement is possible.
     */
    protected HingedJoint joint1;
    protected HingedJoint joint2;

    
    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Create a new joint that rigidly connects the two specified bodies.
     *
     * @param collisionSpace CollisionSpace running the physics simulation
     * @param body1 first Body to be connected by the joint
     * @param body2 second Body to be connected by the joint
     */
    public FixedJoint(CollisionSpace collisionSpace, Body body1, Body body2) {
        super(collisionSpace, body1, body2);

        joint1 = new HingedJoint(collisionSpace, body1, body2, body1.x, body1.y);
        joint2 = new HingedJoint(collisionSpace, body2, body1, body2.x, body2.y);
    }

    /**
     * Create a new joint that rigidly connects the two specified shapes.
     * <P>
     * Note: Presently this constructor assumes that the bodies holding the
     * shapes will have zero rotation, i.e. the shape offsets do not take any
     * account of the body rotation. This is an unnecessary assumption and
     * should be removed.
     *
     * @param collisionSpace CollisionSpace running the physics simulation
     * @param shape1 first Shape to be connected by the joint
     * @param shape2 second Shape  to be connected by the joint
     */
    public FixedJoint(CollisionSpace collisionSpace, Shape shape1, Shape shape2) {
        super(collisionSpace, shape1, shape2);

        joint1 = new HingedJoint(collisionSpace, shape1, shape2, 
                shape1.body.x + shape1.offsetX, shape1.body.y + shape1.offsetY);
        joint2 = new HingedJoint(collisionSpace, shape2, 
                shape1, shape2.body.x + shape2.offsetX, shape2.body.y + shape2.offsetY);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods:                                                              //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Set the relaxation value of this joint. The relaxation value determines
     * how much slack is permitted within the joint, i.e. a low relaxation value
     * will ensure that the joint remains highly rigid.
     *
     * @param relaxation double relaxation value to assume
     */
    public void setRelaxation(double relaxation) {
        joint1.setRelaxation(relaxation);
        joint2.setRelaxation(relaxation);
    }

    /**
     * Set the breaking impulse needed to break this joint to that specified.
     *
     * @param breakingImpulse double breaking impulse value to adopt
     */    
    @Override    
    public void setBreakingImpulse(double breakingImpulse) {
        joint1.breakingImpulse = breakingImpulse;
        joint2.breakingImpulse = breakingImpulse;
    }
        
    /**
     * Perform common joint setup calculations that will be used within
     * all joint impulse iterations.
     *
     * @param invDT inverse time period for the physics simulation step
     */
    protected void preStep(double invDT) {
        joint1.preStep(invDT);
        joint2.preStep(invDT);
    }

    /**
     * Apply any impulses through the joint to the connected bodies 
     */
    protected void applyImpulse() {
        joint1.applyImpulse();
        joint2.applyImpulse();

        if (joint1.jointBroken || joint2.jointBroken) {
            this.jointBroken = true;
        }
    }

    /**
     * Return a unique hashcode for this joint
     * 
     * @return unique hashcode for this joint
     */
    @Override
    public int hashCode() {
        return uniqueJointID;
    }

    /**
     * Determine if the specified object is equivalent to this joint
     * 
     * @param other Object to compare
     * @return boolean true if the other object is equivalent to this joint,
     *         otherwise false
     */
    @Override
    public boolean equals(Object other) {
        if (other.getClass() == getClass()) {
            return ((FixedJoint) other).uniqueJointID == uniqueJointID;
        }

        return false;
    }
}