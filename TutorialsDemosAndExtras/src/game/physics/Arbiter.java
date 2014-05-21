package game.physics;

/**
 * The arbiter class provides point of contact correction between two bodies
 * i.e. whenever two bodies overlap, an arbiter is created/reassigned for
 * the two game objects to move them apart. As such this class provides 
 * core collision resolution between bodies.
 * <P>
 * Note: The physics engine within this code repository draws upon the
 * work of two individuals. See the CollisionSpace class header for
 * additional information.
 * <P>
 * @version $Revision: 1.0 $ $Date: 2007/08 $
 */
public final class Arbiter {

    ///////////////////////////////////////////////////////////////////////////
    // Class data                                                            //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Unique identifier that is based on the two bodies associated with 
     * this arbiter.
     */
    protected int arbiterIdentifer;
    
    /**
     * Number of pixels that objects will be allowed to overlap by. Increasing
     * this value can reduce object jitter, but at the expense of greater
     * visual object overlap (obviously!)
     */
    public static double ALLOWED_PENETRATION = 1.0;

    /**
     *  The PENETRATION_BIAS_FACTOR determines the gusto with which the 
     * ALLOWED_PENETRATION is enforced, i.e. a value near 0.0 entails a limited
     * response when bodies overlap, whilst a value near 1.0 entails a strong
     * need to enforce the overlap correction.
     */
    public static double PENETRATION_BIAS_FACTOR = 0.5;

    /**
     * Initial number of contacts that this arbiter can handle. Note:
     * this value is important if bodies with a complex geometry will
     * be used (i.e. with the potential for a large number of contacts).
     * For example a body consisting of a 1 x M array of rectangles will
     * have 2M contacts if fully resting on a long flat surface. It is
     * also desirable to minimise this value to improve performance. A
     * dynamic setup is used, i.e. the array grows as is needed.
     */
    private static final int INITIAL_MAX_CONTACTS = 10;
    private static final int INCREASE_MAX_CONTACTS = 10;

    /**
     * The current number of contacts between the two bodies that this
     * arbiter is handling
     */
    protected int numContacts;

    /**
     * Array of contacts that this arbiter can handle. Note: the array
     * could be longer than the initial creation size and it may also
     * contain contacts that are not currently active.
     */
    protected Contact[] contacts = new Contact[INITIAL_MAX_CONTACTS];

    /**
     * First body associated with this arbiter
     */
    public Body body1;

    /**
     * Second body associated with this arbiter
     */
    public Body body2;

    /**
     * Combined friction between the two bodies
     */
    private double friction;


    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Create a new arbiter - this should only be done by the collision space
     * (i.e. the collision space will create arbiter objects when and if 
     * needed).
     */
    Arbiter() {
        for (int idx = 0; idx < INITIAL_MAX_CONTACTS; idx++) {
            contacts[idx] = new Contact();
        }
    }

    /**
     * Create a new arbiter - this should only be done by the collision space
     * (i.e. the collision space will create arbiter objects when and if 
     * needed).
     *
     * @param body1 the first body in contact
     * @param body2 the second body in contact
     */
    Arbiter(Body body1, Body body2) {
        // Create a unique identifier for this arbiter (Note: the following 
        // assumes there will not be more than 65536 bodies)
        arbiterIdentifer = 
                body1.uniqueGameObjectID * 65536 + body2.uniqueGameObjectID;
        
        for (int idx = 0; idx < INITIAL_MAX_CONTACTS; idx++)
            contacts[idx] = new Contact();
        
        this.body1 = body1;
        this.body2 = body2;
        
        // Store the average friction between the bodies
        friction = Math.sqrt(body1.friction * body2.friction);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods:                                                              //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Set the bodies associated with this arbiter to those specified.
     * <P>
     * Note: this method is by the collision space to re-use a particular
     * arbiter object for a new pair of bodies.
     *
     * @param body1 the first body in contact
     * @param body2 the second body in contact
     */
    protected final void setBodies(Body body1, Body body2) {
        // Create a unique identifier for this arbiter (Note: the following 
        // assumes there will not be more than 65536 bodies)
        arbiterIdentifer = 
                body1.uniqueGameObjectID * 65536 + body2.uniqueGameObjectID;
        
        numContacts = 0;
        for (int idx = 0; idx < INITIAL_MAX_CONTACTS; idx++) {
            contacts[idx].accumulatedNormalImpulse = 0.0;
            contacts[idx].accumulatedTangentImpulse = 0.0;
        }

        this.body1 = body1;
        this.body2 = body2;

        // Store the average friction between the bodies
        friction = Math.sqrt(body1.friction * body2.friction);
    }

    /**
     * Return the current number of contacts between the two stored bodies
     *
     * @return int the current number of contacts between the stored bodies
     */
    public final int getNumContacts() {
        return numContacts;
    }

    /**
     * Return the specified point of contact
     *
     * @return Contact specified point of contact
     * 
     * @exception NullPointerException if an invalid contact index is specified
     */
    public final Contact getContact(int contactIdx) {
        if (contactIdx < 0 || contactIdx >= numContacts) {
            throw new NullPointerException("Arbiter.getContact: " + 
                    "Invalid contact index specified: " + contactIdx);
        }
        
        return contacts[contactIdx];
    }

    /**
     * Return the specified contact based on the specified contact offset
     *
     * @return Contact specified point of contact
     */
    protected final Contact getNextAvailableContact(int contactOffset) {
        if (numContacts + contactOffset >= contacts.length) {
            Contact[] resizedContacts 
                    = new Contact[contacts.length + INCREASE_MAX_CONTACTS];
            System.arraycopy(contacts, 0, resizedContacts, 0, contacts.length);

            for (int idx = contacts.length; idx < resizedContacts.length; idx++)
                resizedContacts[idx] = new Contact();
            contacts = resizedContacts;
        }

        return contacts[numContacts + contactOffset];
    }

    /**
     * Update this arbiter based on the set of newly identified contacts
     *
     * @param newContacts the new contacts that have been found
     * @param numNewContacts the number of new contacts discovered
     */
    protected void update(Contact[] newContacts, int numNewContacts) {
        // Increase the current array of points of contact if needed
        if (newContacts.length > contacts.length) {
            Contact[] resizedContacts = new Contact[newContacts.length];
            System.arraycopy(contacts, 0, resizedContacts, 0, contacts.length);

            for (int idx = contacts.length; idx < resizedContacts.length; idx++)
                resizedContacts[idx] = new Contact();
            contacts = resizedContacts;
        }

        // Store and update the new points of contact, taking into account
        // an overlap between new and existing points of contact      
        for (int newIdx = 0; newIdx < numNewContacts; newIdx++) {
            Contact newContact = newContacts[newIdx];
            int prevIdx = -1;
            for (int curIdx = 0; curIdx < numContacts; curIdx++) {
                Contact oldContact = contacts[curIdx];
                if (newContact.hashCode() == oldContact.hashCode()) {
                    prevIdx = curIdx;
                    break;
                }
            }

            if (prevIdx > -1) {
                // Store the previous accumulated impulses
                double previousAccumulatedNormalImpulse 
                        = contacts[prevIdx].accumulatedNormalImpulse;
                double previousAccumulatedTangentImpulse 
                        = contacts[prevIdx].accumulatedTangentImpulse;

                // Update and point of contact and transfer across the previous
                // accumulated impulses
                contacts[newIdx].set(newContact);
                contacts[newIdx].accumulatedNormalImpulse 
                        = previousAccumulatedNormalImpulse;
                contacts[newIdx].accumulatedTangentImpulse 
                        = previousAccumulatedTangentImpulse;
            } else {
                contacts[newIdx].set(newContacts[newIdx]);
            }
        }

        numContacts = numNewContacts;
    }
    
    /**
     * Apply the impulse from each point of contact.
     *
     * @param invDT the inverted time
     * @param dt the amount of time to step the simulation by
     * @param damping the percentage of energy to retain through the
     *        collision (1.0 = no lost, 0.0 = total loss)
     */
    protected void preStep(double invDT, double dt, double damping) {
        // Process each point of contact
        for (int i = 0; i < numContacts; ++i) {
            Contact c = contacts[i];

            double cl = Math.sqrt(c.normalx * c.normalx + c.normaly * c.normaly);
            c.normalx /= cl; c.normaly /= cl;

            double r1x = c.x - body1.x, r1y = c.y - body1.y;
            double r2x = c.x - body2.x, r2y = c.y - body2.y;

            // Precompute normal mass, tangent mass, and bias.
            double rn1 = r1x * c.normalx + r1y * c.normaly;
            double rn2 = r2x * c.normalx + r2y * c.normaly;
            double kNormal = body1.invMass + body2.invMass;
            kNormal += body1.invI * (r1x * r1x + r1y * r1y - rn1 * rn1) 
                    + body2.invI * (r2x * r2x + r2y * r2y - rn2 * rn2);
            c.massNormal = damping / kNormal;

            double tangentx = 1.0 * c.normaly, tangenty = -1.0 * c.normalx;
            double rt1 = r1x * tangentx + r1y * tangenty;
            double rt2 = r2x * tangentx + r2y * tangenty;
            double kTangent = body1.invMass + body2.invMass;
            kTangent += body1.invI * (r1x * r1x + r1y * r1y - rt1 * rt1) 
                    + body2.invI * (r2x * r2x + r2y * r2y - rt2 * rt2);
            c.massTangent = damping / kTangent;

            // Calculate the restitution and relative velocity at point of contact
            double relativeVelocityx = body2.velocityx + r2y * body2.angularVelocity 
                    - body1.velocityx - r1y * body1.angularVelocity;
            double relativeVelocityy = body2.velocityy + r2x * -body2.angularVelocity 
                    - body1.velocityy - r1x * -body1.angularVelocity;
            double combinedRestitution = body1.restitution * body2.restitution;
            
            double relVel = c.normalx * relativeVelocityx + c.normaly * relativeVelocityy;
            c.restitution = combinedRestitution * -relVel;
            c.restitution = (c.restitution > 0 ? c.restitution : 0);

            // Calculate the contact bias based on penetration velocity and the 
            // specified penertation biases
            double penVel = -c.separation / dt;
            if (c.restitution >= penVel) {
                c.bias = 0;
            } else {
                c.bias = -PENETRATION_BIAS_FACTOR * invDT 
                        * (c.separation + ALLOWED_PENETRATION < 0.0 
                            ? c.separation + ALLOWED_PENETRATION : 0.0);
            }

            // Apply damping
            c.accumulatedNormalImpulse *= damping;

            // Apply the normal and fricational impulses
            double impulsex = c.normalx * c.accumulatedNormalImpulse 
                    + tangentx * c.accumulatedTangentImpulse;
            double impulsey = c.normaly * c.accumulatedNormalImpulse 
                    + tangenty * c.accumulatedTangentImpulse;
            body1.velocityx += impulsex * -body1.invMass;
            body1.velocityy += impulsey * -body1.invMass;
            body1.angularVelocity += -body1.invI * (r1x * impulsey - r1y * impulsex);
            body2.velocityx += impulsex * body2.invMass;
            body2.velocityy += impulsey * body2.invMass;
            body2.angularVelocity += body2.invI * (r2x * impulsey - r2y * impulsex);

            // Reset the bias
            c.biasImpulse = 0;
        }
    }

    /**
     * Apply the impulse accumulated at the points of contact maintained
     * by this arbiter.
     */
    protected void applyImpulse() {
        // Process each point of contact
        for (int i = 0; i < numContacts; ++i) {
            Contact c = contacts[i];

            double r1x = c.x - body1.x, r1y = c.y - body1.y;
            double r2x = c.x - body2.x, r2y = c.y - body2.y;

            // Determine the relative velocity at the point of contact
            double relativeVelocityx = body2.velocityx + -body2.angularVelocity * r2y 
                    - body1.velocityx - -body1.angularVelocity * r1y;
            double relativeVelocityy = body2.velocityy + body2.angularVelocity * r2x 
                    - body1.velocityy - body1.angularVelocity * r1x;

            // Calculate the normal impulse
            double vn = relativeVelocityx * c.normalx + relativeVelocityy * c.normaly;
            double normalImpulse = c.massNormal * (c.restitution - vn);

            // Clamp the accumulated impulse
            double oldNormalImpulse = c.accumulatedNormalImpulse;
            c.accumulatedNormalImpulse = (oldNormalImpulse + normalImpulse > 0.0 
                    ? oldNormalImpulse + normalImpulse : 0.0);
            normalImpulse = c.accumulatedNormalImpulse - oldNormalImpulse;

            // Apply the contact impulse
            double impulsex = c.normalx * normalImpulse;
            double impulsey = c.normaly * normalImpulse;

            body1.velocityx += impulsex * -body1.invMass;
            body1.velocityy += impulsey * -body1.invMass;
            body1.angularVelocity += -(body1.invI * (r1x * impulsey - r1y * impulsex));
            body2.velocityx += impulsex * body2.invMass;
            body2.velocityy += impulsey * body2.invMass;
            body2.angularVelocity += body2.invI * (r2x * impulsey - r2y * impulsex);

            // Compute the bias impulse
            relativeVelocityx = body2.biasedVelocityx;
            relativeVelocityy = body2.biasedVelocityy;
            relativeVelocityx += -body2.biasedAngularVelocity * r2y;
            relativeVelocityy += body2.biasedAngularVelocity * r2x;
            relativeVelocityx -= body1.biasedVelocityx;
            relativeVelocityy -= body1.biasedVelocityy;
            relativeVelocityx -= -body1.biasedAngularVelocity * r1y;
            relativeVelocityy -= body1.biasedAngularVelocity * r1x;
            double vnb = relativeVelocityx * c.normalx + relativeVelocityy * c.normaly;

            double biasImpulse = c.massNormal * (-vnb + c.bias);
            double oldBiasImpulse = c.biasImpulse;
            c.biasImpulse = (oldBiasImpulse + biasImpulse > 0.0 
                    ? oldBiasImpulse + biasImpulse : 0.0);
            biasImpulse = c.biasImpulse - oldBiasImpulse;

            double Pbx = c.normalx * biasImpulse;
            double Pby = c.normaly * biasImpulse;
            body1.biasedVelocityx += Pbx * -body1.invMass;
            body1.biasedVelocityy += Pby * -body1.invMass;
            body1.biasedAngularVelocity += -(body1.invI * (r1x * Pby - r1y * Pbx));
            body2.biasedVelocityx += Pbx * body2.invMass;
            body2.biasedVelocityy += Pby * body2.invMass;
            body2.biasedAngularVelocity += body2.invI * (r2x * Pby - r2y * Pbx);

            // Compute friction (tangent) impulse
            double maxTangentImpulse = friction * c.accumulatedNormalImpulse;

            // Relative velocity at contact
            relativeVelocityx = body2.velocityx;
            relativeVelocityy = body2.velocityy;
            relativeVelocityx += -body2.angularVelocity * r2y;
            relativeVelocityy += body2.angularVelocity * r2x;
            relativeVelocityx -= body1.velocityx;
            relativeVelocityy -= body1.velocityy;
            relativeVelocityx -= -body1.angularVelocity * r1y;
            relativeVelocityy -= body1.angularVelocity * r1x;

            double tangentx = 1.0 * c.normaly;
            double tangenty = -1.0 * c.normalx;
            double vt = relativeVelocityx * tangentx + relativeVelocityy * tangenty;

            double tangentImpulse = c.massTangent * (-vt);

            // Clamp the friction
            double oldTangentImpulse = c.accumulatedTangentImpulse;
            double accumulatedTangentImpulseMin 
                    = ((oldTangentImpulse + tangentImpulse) < maxTangentImpulse) 
                        ? oldTangentImpulse + tangentImpulse : maxTangentImpulse;
            c.accumulatedTangentImpulse 
                    = (-maxTangentImpulse > accumulatedTangentImpulseMin 
                        ? -maxTangentImpulse : accumulatedTangentImpulseMin);
            tangentImpulse = c.accumulatedTangentImpulse - oldTangentImpulse;

            // Apply the contact impulse
            impulsex = tangentx * tangentImpulse;
            impulsey = tangenty * tangentImpulse;
            body1.velocityx += impulsex * -body1.invMass;
            body1.velocityy += impulsey * -body1.invMass;
            body1.angularVelocity += -body1.invI * (r1x * impulsey - r1y * impulsex);
            body2.velocityx += impulsex * body2.invMass;
            body2.velocityy += impulsey * body2.invMass;
            body2.angularVelocity += body2.invI * (r2x * impulsey - r2y * impulsex);
        }
    }

    /**
     * Return a unique hashcode for this arbiter
     * 
     * @return unique hashcode for this arbiter
     */
    @Override
    public int hashCode() {
        return body1.uniqueGameObjectID * 65536 + body2.uniqueGameObjectID;
    }

    /**
     * Determine if the specified object is equivalent to this aribter
     * 
     * @param other Object to compare
     * @return boolean true if the other object is equivalent to this arbiter,
     *         otherwise false
     */
    @Override
    public boolean equals(Object other) {
        if (other.getClass().equals(getClass())) {
            Arbiter otherArbiter = (Arbiter) other;
            return (otherArbiter.body1.equals(body1) && otherArbiter.body2.equals(body2)) 
                    || (otherArbiter.body1.equals(body2) && otherArbiter.body2.equals(body1));
        }
        
        return false;
    }
}