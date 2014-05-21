package game.physics;

import game.geometry.*;

/**
 * HingedJoint provides a hinged, i.e. moveable, joint between two bodies.
 * to the connected body. Hinged joints may be created that anchor bodies
 * by their mid-points or a specified pair of shapes.
 * <P>
 * Note: The physics engine within this code repository draws upon the
 * work of two individuals. See the CollisionSpace class header for
 * additional information.
 * <P>
 * @version $Revision: 1.0 $ $Date: 2007/08 $
 */
public class HingedJoint extends Joint {

    ///////////////////////////////////////////////////////////////////////////
    // Class data                                                            //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Relaxation value of this joint. The relaxation value determines
     * how much slack is permitted within the joint.
     */
    protected final double BASE_RELAXATION = 1.0;
    private double relaxation;

    /**
     * Bias factor used to correct the position of the joint
     */
    protected double BIAS_CORRECTION_FACTOR = 0.05;

    /**
     * Anchor point for the joint
     */
    protected double anchorx;
    protected double anchory;

    /**
     * Anchor point for the joint from the frame of the first body
     */
    protected double localAnchor1x, localAnchor1y;
    protected double anchor1x, anchor1y;

    /**
     * Anchor point for the joint from the frame of the second body
     */
    protected double localAnchor2x, localAnchor2y;
    protected double anchor2x, anchor2y;

    /** The rotation of the anchor relative to both bodies, alongside
     * a matrix describing the rotation between the bodies 
     */
    private double r1x, r1y;
    private double r2x, r2y;
    private double Mc1x, Mc1y, Mc2x, Mc2y;

    /**
     * Joint bias
     */
    private double biasx, biasy;

    /** 
     * The impulse to be applied throught the joint 
     */
    private double accumulatedImpulsex, accumulatedImpulsey;


    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Create a hinged joint holding two bodies together at the specified 
     * anchor point
     *
     * @param collisionSpace CollisionSpace running the physics simulation
     * @param body1 first Body to be connected by the joint
     * @param body2 second Body to be connected by the joint
     * @param anchorx double x anchor location
     * @param anchory double y anchor location
     */
    public HingedJoint(CollisionSpace collisionSpace, 
            Body body1, Body body2, double anchorx, double anchory) {
        super(collisionSpace, body1, body2);
        
        set(anchorx, anchory);
    }

    /**
     * Create a hinged joint holding two shapes together at the specified 
     * anchor point
     *
     * @param collisionSpace CollisionSpace running the physics simulation
     * @param shape1 first Shape to be connected by the joint
     * @param shape2 second Shape  to be connected by the joint
     * @param anchorx double x anchor location
     * @param anchory double y anchor location
     */
    public HingedJoint(CollisionSpace collisionSpace, 
            Shape shape1, Shape shape2, double anchorx, double anchory) {
        super(collisionSpace, shape1, shape2);

        set(anchorx, anchory);
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
        this.relaxation = relaxation;
    }

    /**
     * Return the relaxation value of this joint. 
     *
     * @return relaxation double relaxation value
     */
    public double getRelaxation() {
        return relaxation;
    }

    /**
     * Return the current x anchor point
     *
     * @return double x anchor point
     */
    public double getAnchorX() {
        return anchorx;
    }

    /**
     * Return the current y anchor point
     *
     * @return double y anchor point
     */
    public double getAnchorY() {
        return anchory;
    }

    /**
     * Set the joint anchor point to that specified
     *
     * @param anchorx double x anchor point
     * @param anchory double y anchor point
     */
    protected void set(double anchorx, double anchory) {
        this.anchorx = anchorx;
        this.anchory = anchory;
        
        Body body1, body2;

        // Determine the local anchor points for each body
        if (this.connector == Connector.BODY) {
            body1 = this.body1; body2 = this.body2;
            anchor1x = body1.x; anchor1y = body1.y;
            anchor2x = body2.x; anchor2y = body2.y;
        } else {
            body1 = this.shape1.body; body2 = this.shape2.body;

            double c = Math.cos( body1.rotation ), s = Math.sin( body1.rotation );
            double rc1x = c, rc1y = s, rc2x = -s, rc2y = c;
            anchor1x = body1.x + rc1x * shape1.offsetX + rc2x * shape1.offsetY;
            anchor1y = body1.y + rc1y * shape1.offsetX + rc2y * shape1.offsetY;

            c = Math.cos( body2.rotation ); s = Math.sin( body2.rotation );
            rc1x = c; rc1y = s; rc2x = -s; rc2y = c;
            anchor2x = body2.x + rc1x * shape2.offsetX + rc2x * shape2.offsetY;
            anchor2y = body2.y + rc1y * shape2.offsetX + rc2y * shape2.offsetY;
        }

        // Calculation rotational transform matrices for each body
        double c = Math.cos(body1.rotation), s = Math.sin(body1.rotation);
        double rot1Tc1x = c, rot1Tc1y = -s, rot1Tc2x = s, rot1Tc2y = c;

        c = Math.cos(body2.rotation); s = Math.sin(body2.rotation);
        double rot2Tc1x = c; double rot2Tc1y = -s; double rot2Tc2x = s; double rot2Tc2y = c;

        // Determine the local anchor points for each body
        double ax = anchorx - anchor1x, ay = anchory - anchor1y;
        localAnchor1x = rot1Tc1x * ax + rot1Tc2x * ay;
        localAnchor1y = rot1Tc1y * ax + rot1Tc2y * ay;

        ax = anchorx - anchor2x; ay = anchory - anchor2y;
        localAnchor2x = rot2Tc1x * ax + rot2Tc2x * ay;
        localAnchor2y = rot2Tc1y * ax + rot2Tc2y * ay;

        // Reset accumulators and joint relaxation
        accumulatedImpulsex = 0.0;
        accumulatedImpulsey = 0.0;
        relaxation = BASE_RELAXATION;
    }

    /**
     * Perform common joint setup calculations that will be used within
     * all joint impulse iterations.
     *
     * @param invDT inverse time period for the physics simulation step
     */
    protected void preStep(double invDT) {
        Body body1 = this.connector == Connector.BODY ? this.body1 : this.shape1.body;
        Body body2 = this.connector == Connector.BODY ? this.body2 : this.shape2.body;

        // Determine rotational matrices for each body
        double c = Math.cos(body1.rotation), s = Math.sin(body1.rotation);
        double rot1c1x = c, rot1c1y = s, rot1c2x = -s, rot1c2y = c;

        c = Math.cos(body2.rotation); s = Math.sin(body2.rotation);
        double rot2c1x = c, rot2c1y = s, rot2c2x = -s, rot2c2y = c;

        // Determine anchor points for each body for this preStep/set of updates
        if (this.connector == Connector.BODY) {
            anchor1x = body1.x; anchor1y = body1.y; anchor2x = body2.x; anchor2y = body2.y;
        } else {
            anchor1x = body1.x + rot1c1x * shape1.offsetX + rot1c2x * shape1.offsetY;
            anchor1y = body1.y + rot1c1y * shape1.offsetX + rot1c2y * shape1.offsetY;
            anchor2x = body2.x + rot2c1x * shape2.offsetX + rot2c2x * shape2.offsetY;
            anchor2y = body2.y + rot2c1y * shape2.offsetX + rot2c2y * shape2.offsetY;
        }

        // Pre-compute anchors, mass matrix, and bias.
        r1x = rot1c1x * localAnchor1x + rot1c2x * localAnchor1y;
        r1y = rot1c1y * localAnchor1x + rot1c2y * localAnchor1y;
        r2x = rot2c1x * localAnchor2x + rot2c2x * localAnchor2y;
        r2y = rot2c1y * localAnchor2x + rot2c2y * localAnchor2y;

        double Kc1x = body1.invMass + body2.invMass + body1.invI * r1y * r1y + body2.invI * r2y * r2y;
        double Kc1y = -body1.invI * r1x * r1y + -body2.invI * r2x * r2y;
        double Kc2x = -body1.invI * r1x * r1y + -body2.invI * r2x * r2y;
        double Kc2y = body1.invMass + body2.invMass + body1.invI * r1x * r1x + body2.invI * r2x * r2x;

        double Kdet = 1.0 / (Kc1x * Kc2y - Kc2x * Kc1y);
        Mc1x = Kdet * Kc2y; Mc1y = -Kdet * Kc1y; Mc2x = -Kdet * Kc2x; Mc2y = Kdet * Kc1x;

        biasx = (anchor2x + r2x) - (anchor1x + r1x); biasy = (anchor2y + r2y) - (anchor1y + r1y);
        biasx *= (-BIAS_CORRECTION_FACTOR * invDT); biasy *= (-BIAS_CORRECTION_FACTOR * invDT);

        // Apply accumulated impulse.
        accumulatedImpulsex *= relaxation; accumulatedImpulsey *= relaxation;

        body1.velocityx += (accumulatedImpulsex * -body1.invMass);
        body1.velocityy += (accumulatedImpulsey * -body1.invMass);
        body1.angularVelocity += -(body1.invI * 
                (r1x * accumulatedImpulsey - r1y * accumulatedImpulsex));

        body2.velocityx += (accumulatedImpulsex * body2.invMass);
        body2.velocityy += (accumulatedImpulsey * body2.invMass);
        body2.angularVelocity += body2.invI * 
                (r2x * accumulatedImpulsey - r2y * accumulatedImpulsex);
    }

    /**
     * Apply any impulses through the joint to the connected bodies 
     */
    protected void applyImpulse() {
        Body body1 = this.connector == Connector.BODY ? this.body1 : this.shape1.body;
        Body body2 = this.connector == Connector.BODY ? this.body2 : this.shape2.body;

        double dvx = body2.velocityx + (-body2.angularVelocity * r2y);
        double dvy = body2.velocityy + (body2.angularVelocity * r2x);
        dvx -= (body1.velocityx + (-body1.angularVelocity * r1y));
        dvy -= (body1.velocityy + (body1.angularVelocity * r1x));
        dvx *= -1.0; dvy *= -1.0; dvx += biasx; dvy += biasy;
        
        if ((dvx * dvx + dvy * dvy) == 0) {
            return;
        }
        
        // Calculate and apply impulse along the joint
        double impulsex = Mc1x * dvx + Mc2x * dvy, impulsey = Mc1y * dvx + Mc2y * dvy;

        double delta1x = impulsex * -body1.invMass, delta1y = impulsey * -body1.invMass;
        body1.velocityx += delta1x; body1.velocityy += delta1y;
        body1.angularVelocity += (-body1.invI * (r1x * impulsey - r1y * impulsex));

        double delta2x = impulsex * body2.invMass, delta2y = impulsey * body2.invMass;
        body2.velocityx += delta2x; body2.velocityy += delta2y;
        body2.angularVelocity += (body2.invI * (r2x * impulsey - r2y * impulsex));

        accumulatedImpulsex += impulsex; accumulatedImpulsey += impulsey;

        // Check to determine if the applied impulse should break the joint
        if (impulsex * impulsex + impulsey * impulsey > breakingImpulse * breakingImpulse) {
            jointBroken = true;
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
            return ((HingedJoint) other).uniqueJointID == uniqueJointID;
        }

        return false;
    }
}
