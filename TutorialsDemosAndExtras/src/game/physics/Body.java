package game.physics;

import game.geometry.*;
import java.util.*;
import game.engine.*;
import game.assets.*;

/**
 * Body extends GameObject is provide the a basic game object that can be handled
 * by the physics engine, all game objects to be proceeded by the physics engine
 * should be of type Body. Body instances can have a breakable geometry, i.e. should 
 * they receive an impulse that is avoid their breaking impulse then the body then 
 * break into a number of smaller bodies.
 * <P>
 * Note: The physics engine within this code repository draws upon the
 * work of two individuals. See the CollisionSpace class header for
 * additional information.
 * <P>
 * @version $Revision: 1.0 $ $Date: 2007/08 $
 * 
 * @see GameObject
 */
public class Body extends GameObject {

    ///////////////////////////////////////////////////////////////////////////
    // Class data: General                                                   //
    ///////////////////////////////////////////////////////////////////////////
    
    /** 
     * Constant value representing infinite mass. Any body with infinite mass
     * is effectively a fixed, unmoving body, i.e. it will not move following 
     * any collision, it is not effected by any defined gravity, etc.
     */
    public static final double INFINITE_MASS = Double.MAX_VALUE;

    /**
     * The x and y forces that are applied to this body, i.e. these forces will
     * drive velocity change.
     */
    public double forcex, forcey;

    /**
     * The angular force being applied this body - i.e. driving angular velocity 
     */
    public double torque;

    /**
     * The frictional coefficient of this body. A low friction (near 0.0) will
     * entail that the body will easily slide over other bodies. 
     */
    public double friction;

    /** 
     * The restitution of this body, i.e. the ability of the body to absorb
     * impact. A low value, i.e. near 0.0 will entail that most collision
     * energy is absorbed. A higher value entails that most collision energy
     * is maintained (e.g. a pool ball would have a value around 1.0).
     */
    public double restitution = 0.25;

    /**
     * The surface area of this body. 
     * <P>
     * Note: this value is automatically determined whenever the body's
     * geometry is set and should not be directly updated.
     */ 
    protected double surfaceArea;
    
    /**
     * The mass and inverse mass of this body (the inverse mass is maintained
     * to remove the need to repeatedly calculate it during processing.
     */
    protected double mass;
    protected double invMass;
        
    /** 
     * Moment of inertia for this object - i.e. the angular mass controlling
     * how angular rotation is calculated. The inverse angular mass is also
     * maintained to remove the need to repeatedly calculate it during processing. 
     */
    protected double I;
    protected double invI;

    /**
     * Biased x, y and angular velocities of this body. These values are used 
     * and update by any arbiter associated by this object, and then applied 
     * to the body's velocities within the collision space update.
     */
    protected double biasedVelocityx;
    protected double biasedVelocityy;
    protected double biasedAngularVelocity;

    /** 
     * Flag determining if this body is to be subject to any gravitational 
     * force update.
     */
    public boolean gravityEffected = true;

    /**
     * Flag specifying if this body is moveable or immovable.
     * <P>
     * Note: this flag is only intended to provide a quick means of checking
     * if this body is moveable or immovable. It should not be changed
     * directly.
     */
    public boolean isMoveable = true;

    /**
     * Flag determining if this body can benefit from the at rest determination
     * within the physics engine. Bodies that are considered to be at rest 
     * can be quickly discarded within the physics update. The ticksAtRest
     * counter provides a count of the number of update ticks that this body
     * has not 'moved' and is used by the physics engine as a threshold counter
     * on when the body can be considered to be at rest.
     */
    public boolean permitAtRest = true;
    public int ticksAtRest = 0;

    ///////////////////////////////////////////////////////////////////////////
    // Class data: Breakable Body                                            //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Constant value determining the maximum number of inter-shape connections.
     * The default value of 6 is sufficient to permit a fully connected 2-D
     * array of boxes. Increasing this will permit more complex shape arrangements
     * but at the expense of additional storage.
     */
    public static final int MAX_SINGLE_SHAPE_CONNECTIONS = 6;

    /**
     * Two dimension array of Shape references representing pairs of connected
     * shape instances. The shapeConnections array provides a model of current
     * shape linkage. Following a breaking impulse one to potentially all the
     * shape connections will be 'broken' potentially resulting in creation
     * of several body fragments. The breaking Impulse determines the minimum
     * breaking impulse that is needed to break the body. The private
     * beakingImpulseNumResultantBodies is used internally to track how many
     * fragmenets are generated following a breaking impulse.
     */
    public Shape[][] shapeConnections = new Shape[0][0];
    public double breakingImpulse = Double.MAX_VALUE;
    private int beakingImpulseNumResultantBodies;

    /**
     * These is the first of three possible breaking impulse outcome situations; 
     * in this case that the body receiving the breaking impulse is NOT an 
     * immovable body and that following the impulse the body is to potentially 
     * break into fragments. The breakingImpulsePropagation determines what
     * percentage (from 0.0 to 1.0) of the breaking impulse is applied along
     * the shape connection (e.g. if set to 0.5 then the breaking impulse
     * will half for each shape as it travels out from the point of impact.
     * The breakingImpulseBrittleness determines what percentage of the
     * shape connections for a pair of linked shapes will be broken following
     * a breaking impulse, e.g. a value of 0.5 entails that for a pair of
     * connected shapes 50% of the connections will be broken.
     */
    public double breakingImpulsePropagation = 0.00;
    public double breakingImpulseBrittleness = 0.00;
    
    /**
     * These is the second of three possible breaking impulse outcome situations; 
     * in this case, following a breaking impulse the entire body is deleted.
     * If using this outcome there is no need to spend any effort in setting up
     * connections between individual pairs of shapes.
     */    
    public boolean breakingImpulseDestroyBody = false;

    /**
     * These is the last of three possible breaking impulse outcome situations; 
     * in this case that the body receiving the breaking impulse IS an 
     * immovable body and that the immovable body is to be broken into a number
     * of small bodies, some of which may be moveable and some of which may be
     * immoveable. The breakingImpulseImmovableIntegrityFactor value determines
     * the first of two tests which will entail that a resultant fragment is
     * considered to be immovable, i.e. if a fragment contains more than 20%
     * of the total number of shapes as contained within the original body
     * then it is considered to be immoveable (subject to the second condition)
     * The second condition breakingImpulseImmovableMinimumIntegrity controls
     * the minimum number of shapes that an immoveable body must have, i.e.
     * by default 2 shapes. If an immoveable body is broken then the 
     * breakingImpulseAssumedBodyMass mass must be set to inorder to determine
     * the percentage mass of each fragment (Note: the setMass method will
     * automatically set this value to the specified mass, i.e. any changes
     * to the breakingImpulseAssumedBodyMass should only be introduced once
     * the setMass method has been called). Finally, if 
     * breakingImpulseImmovableDestroyMoveable is set, then any moveable 
     * fragments will be automatically destroyed.
     */
    public double breakingImpulseImmovableIntegrityFactor = 0.20;
    public int breakingImpulseImmovableMinimumIntegrity = 2;
    public double breakingImpulseAssumedBodyMass = Double.MAX_VALUE;
    public boolean breakingImpulseImmovableDestroyMoveable = false;
    

    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Create a new body. 
     * <P>
     * Note: By default, each body is assumed to be immoveable
     * 
     * @param gameLayer GameLayer to which this body belongs
     */
    public Body(GameLayer gameLayer) {
        this(gameLayer, 0.0, 0.0, 0);
    }

    /**
     * Create a new body instance with the specified x and
     * y offsets.
     * <P>
     * Note: By default, each body is assumed to be immoveable
     *
     * @param  gameLayer GameLayer instance to which this game object belongs
     * @param  x double x location of this body
     * @param  y double y location of this body
     */    
    public Body(GameLayer gameLayer, double x, double y) {
        this(gameLayer, x, y, 0);
    }


    /**
     * Create a new body instance with the specified x,
     * y offset and specified draw order.
     * <P>
     * Note: By default, each body is assumed to be immoveable
     *
     * @param  gameLayer GameLayer instance to which this game object belongs
     * @param  x double x location of this body 
     * @param  y double y location of this body 
     * @param  drawOrder integer draw order of this body
     */
    public Body(GameLayer gameLayer, double x, double y, int drawOrder) {
        // Should be the game layer to which this belong
        super(gameLayer, x, y, drawOrder);

        forcex = 0.0;
        forcey = 0.0;
        torque = 0.0;
        friction = 1.0;

        mass = INFINITE_MASS;
        invMass = 0.0;

        I = INFINITE_MASS;
        invI = 0.0;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods: Get/set methods                                              //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Set the mass of the body is that specified
     * <P>
     * Note: This method will automatically set the breakingImpulseAssumedBodyMass
     * mass to that specified for moveable bodies. 
     * 
     * @param  mass double mass to use
     * 
     * @exception IllegalArgumentException if an invalid mass is specified
     */
    public void setMass(double mass) {
        if (mass <= 0.0) {
            throw new IllegalArgumentException("Body.setMass: " + 
                    "Invalid mass = " + mass);
        }
        this.mass = mass;
        
        if( mass != Body.INFINITE_MASS )
            breakingImpulseAssumedBodyMass = mass;

        updateMassAndInertiaValues();
    }

    /**
     * Return the mass of this body
     * 
     * @return double mass of this body
     */
    public final double getMass() {
        return mass;
    }
    
    /**
     * Set the force applied on the body to that specified
     * 
     * @param  forcex double force along the x axis
     * @param  forcey double force along the y axis
     */
    public void setForce(double forcex, double forcey) {
        this.forcex = forcex;
        this.forcey = forcey;
    }

    /**
     * Specify if this body can benefit from the at rest detection
     * within the physics engine.
     * 
     * @param  permitAtRest boolean flag specifying if tis body can
     *         be considered within the at rest tests.
     */
    public void setEnableAtRestDetermination(boolean permitAtRest) {
        this.permitAtRest = permitAtRest;
    }
    
    /**
     * Indicate whether this body should be effected by
     * gravity
     *
     * @param gravity True if this body should be effected
     * by gravity
     */
    public void setGravityEffected(boolean gravityEffected) {
        this.gravityEffected = gravityEffected;
    }

    /**
     * Set the geometry of the body to that specified.
     * <P>
     * Note: this method will also update the body's surface area,
     * inertia and create an empty set of shape connections.
     *
     * @param geometry array of Shape objects specifying the body's geometry
     */
    @Override
    public void setGeometry(Shape[] geometry) {
        super.setGeometry(geometry);

        // Link the shape geometry to this body and also update the surface area
        surfaceArea = 0.0;
        for (int idx = 0; idx < geometry.length; idx++) {
            geometry[idx].body = this;
            surfaceArea += geometry[idx].surfaceArea;
        }

        // Create a blank set of shape connections
        shapeConnections = new Shape[geometry.length][MAX_SINGLE_SHAPE_CONNECTIONS];
        for (int idx = 0; idx < shapeConnections.length; idx++) {
            for (int idx2 = 0; idx2 < MAX_SINGLE_SHAPE_CONNECTIONS; idx2++) {
                shapeConnections[idx][idx2] = null;
            }
        }
        
        // Update associated mass and inertia values
        updateMassAndInertiaValues();
    }

    /**
     * Update mass and inertia related values based upon the body's defined
     * mass and geometry.
     */
    private void updateMassAndInertiaValues() {
        if (mass < INFINITE_MASS) {
            invMass = 1.0f / mass;

            I = 0.0;
            for (int idx = 0; idx < geometry.length; idx++) {
                I += (mass * geometry[idx].momentOfInertiaFactor);
            }
            invI = 1.0f / I;
            isMoveable = true;
        } else {
            invMass = 0.0f;
            I = INFINITE_MASS;
            invI = 0.0f;
            isMoveable = false;
        }
    }

    /**
     * Return the surface area of this body
     * 
     * @return double surface area of this body
     */
    public final double getSurfaceArea() {
        return surfaceArea;
    }
    
    /**
     * Store the specified list of shape connections for this body.
     * <P>
     * Note: this method accepts an array list of shape arrays. It is 
     * assumed that the array list is sized and ordered to match the
     * currently defined array of shape instances that define the 
     * geometry of this body.
     * 
     * @param shapeConnections ArrayList of Shape arrays matching
     *        the order and number of the define geometry of this body
     *        and specifying linked shapes.
     */
    public void setShapeConnections(ArrayList<Shape[]> shapeConnections) {
        this.shapeConnections = new Shape[shapeConnections.size()][];

        for (int shapeIdx = 0; shapeIdx < shapeConnections.size(); shapeIdx++) {
            this.shapeConnections[shapeIdx] = shapeConnections.get(shapeIdx);
        }
    }

    /**
     * Store the specified two dimension array of shape connections.
     * <P>
     * Note: it is assumed that the shape array matches the size and
     * order of the currently defined array of shape instances that 
     * define the geometry of this body.
     * 
     * @param shapeConnections two dimensions array of Shapes matching
     *        the order and number of the define geometry of this body
     *        and specifying linked shapes.
     */
    public void setShapeConnections(Shape[][] shapeConnections) {
        this.shapeConnections = shapeConnections;
    }

    /**
     * Connect the first specified shape to the second specified shape
     * 
     * @param firstShape first Shape to connect
     * @param secondShape second Shape to connect
     * 
     * @exception IllegalStateException id the two shapes cannot be linked
     */
    public boolean connectShape(Shape firstShape, Shape secondShape) {
        boolean added = false;
        boolean connected = false;

        int firstShapeIdx = 0;
        int secondShapeIdx = 0;

        try {
            while (!added) {
                if (geometry[firstShapeIdx].uniqueShapeID == firstShape.uniqueShapeID) {
                    while (!added) {
                        if (geometry[secondShapeIdx].uniqueShapeID 
                                == secondShape.uniqueShapeID) {
                            int firstInsertIdx = 0;
                            while (this.shapeConnections[firstShapeIdx][firstInsertIdx] != null 
                                    && shapeConnections[firstShapeIdx][firstInsertIdx].uniqueShapeID 
                                        != secondShape.uniqueShapeID) {
                                firstInsertIdx++;
                            }
                            
                            int secondInsertIdx = 0;
                            while (shapeConnections[secondShapeIdx][secondInsertIdx] != null 
                                    && shapeConnections[secondShapeIdx][secondInsertIdx].uniqueShapeID 
                                    != firstShape.uniqueShapeID) {
                                secondInsertIdx++;
                            }
                            
                            if (shapeConnections[firstShapeIdx][firstInsertIdx] == null 
                                    || shapeConnections[secondShapeIdx][secondInsertIdx] == null) {
                                shapeConnections[firstShapeIdx][firstInsertIdx] = secondShape;
                                shapeConnections[secondShapeIdx][secondInsertIdx] = firstShape;
                                connected = true;
                            }

                            added = true;
                        } else {
                            secondShapeIdx++;
                        }
                    }
                } else {
                    firstShapeIdx++;
                }
            }
        } catch (IndexOutOfBoundsException exception) {
            if (firstShapeIdx < geometry.length && secondShapeIdx < geometry.length) {
                throw new IllegalStateException( "Body.connectShape: " +
                        "Cannot link shapes - maximum number of linked shapes limit already reached." );
            } else {
                throw new IllegalStateException( "Body.connectShape: " +
                        "Cannot locate either firstShape or secondShape in body." );
            }
        }

        return connected;
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
        if (other.getClass() == getClass()) {
            return ((Body) other).uniqueGameObjectID == uniqueGameObjectID;
        }

        return false;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods: Get/set methods                                              //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Set the breaking impulse to that specified.
     * 
     * @param breakingImpulse double breaking impulse to use for this shape
     */
    public void setBreakingImpulse(double breakingImpulse) {
        this.breakingImpulse = breakingImpulse;
    }

    /**
     * Set the breaking impulse propagation to that specified, i.e. the 
     * percentage of the breaking impulse that is transferred
     * to other connected shapes given a breaking impulse applied to a
     * particular shape.
     * 
     * @param breakingImpulse double breaking propagation to use for this shape
     */
    public void setBreakingImpulsePropagation(double breakingImpulsePropagation) {
        this.breakingImpulsePropagation = breakingImpulsePropagation;
    }

    /**
     * Set the breaking impulse brittleness to that specified, i.e. the 
     * percentage of shape connections that are broken given a breaking impulse
     * applied to a particular shape.
     * 
     * @param breakingImpulse double breaking brittleness to use for this shape
     */
    public void setBreakingImpulseBrittleness(double breakingImpulseBrittleness) {
        this.breakingImpulseBrittleness = breakingImpulseBrittleness;
    }

    /**
     * Set the breaking impulse assumed body mass to that specified, i.e. following
     * a breaking impulse the assumed total mass for the body that is be used to
     * determine the mass of body fragments.
     * 
     * @param breakingImpulse double breaking impulse assumed body mass
     */
    public void setBreakingImpulseAssumedBodyMass(
            double breakingImpulseAssumedBodyMass) {
        this.breakingImpulseAssumedBodyMass = breakingImpulseAssumedBodyMass;
    }

    /**
     * Set the breaking impulse immoveable integrity factor, i.e. following
     * a breaking impulse applied to an immovable body, the percentage size of each
     * fragment (relative to the size of the immovable body) that are to be 
     * considered immoveable. Fragments falling below this threshold are assumed
     * to be moveable.
     * 
     * @param breakingImpulse double breaking impulse immoveable integrity factor
     */
    public void setBreakingImpulseImmovableIntegrityFactor(
            double breakingImpulseImmovableIntegrityFactor) {
        this.breakingImpulseImmovableIntegrityFactor 
                = breakingImpulseImmovableIntegrityFactor;
    }

    /**
     * Set the breaking impulse immoveable minimum integrity, i.e. following
     * a breaking impulse applied to an immovable body, the minimum size in 
     * shapes that a fragment must have in order to be considerable in terms
     * of passing the breaking impulse immoveable integrity factor test.
     * 
     * @param breakingImpulse double breaking impulse immoveable minimum integrity
     */
    public void setBreakingImpulseImmovableMinimumIntegrity(
            int breakingImpulseImmovableMinimumIntegrity) {
        this.breakingImpulseImmovableMinimumIntegrity 
                = breakingImpulseImmovableMinimumIntegrity;
    }

    /**
     * Apply the specified impulse to the body at the specified shape.
     * 
     * @param collisionSpace CollisionSpace to which this body belongs
     * @param impulse double impulse to be applied to the body
     * @param shape geometry Shape that receives the impulse
     * @return int number of resultant bodies following the collision
     *         A value of -1 means no change to the body
     *         A value of 0 means the body has been entirely destroyed
     *         A value > 0 means that there was n resultant bodies
     *         following the applied impulse
     */
    public int breakBody(CollisionSpace collisionSpace, double impulse, Shape shape) {
        // Test to see if the body should be destroyed
        if (breakingImpulseDestroyBody && impulse >= breakingImpulse) {
            breakBodyDestroyBody();
            gameLayer.queueGameObjectToRemove(this);
            return 0;
        }

        // If the body has a geometry of lenth 1 it cannot be further broken
        if (geometry.length == 1) {
            return -1;
        }

        // If the specified shape cannot be found within the body, then return
        boolean shapeFound = false;
        for (int idx = 0; idx < geometry.length && !shapeFound; idx++) {
            if (geometry[idx].uniqueShapeID == shape.uniqueShapeID) {
                shapeFound = true;
            }
        }
        if (!shapeFound) {
            return -1;
        }

        // Apply the impulse and then consider building any resultant body fragments        
        beakingImpulseNumResultantBodies = -1;
        breakBodyPropogateImpulse(impulse, shape);
        breakBodyBuildResultantShapes(collisionSpace);

        // If no fragments survived the impulse call the body destroyed 
        // notification method, otherwise if fragments were produced, call the 
        // body broken notification method.
        if (beakingImpulseNumResultantBodies == 0) {
            breakBodyDestroyBody();
        } else if (beakingImpulseNumResultantBodies > 0) {
            breakBodyBrokenBody();
        }
        
        return beakingImpulseNumResultantBodies;
    }

    /**
     * This method will be called whenever the body is destroyed and is intended
     * to be overloaded within extending class to provide appropriate action
     */
    protected void breakBodyDestroyBody() {
    }

    /**
     * This method will be called whenever the body is broken and is intended
     * to be overloaded within extending class to provide appropriate action
     */
    protected void breakBodyBrokenBody() {
    }

    /**
     * Apply the specified impulse to the specified shape
     * 
     * @param impulse double impulse to be applied to the body
     * @param shape geomtry Shape that receives the impulse
     */
    private final void breakBodyPropogateImpulse(double impulse, Shape shape) {
        // Break the connections as appropriate
        int shapeListIdx = 0;
        Shape[] shapeList = new Shape[geometry.length];
        shapeList[0] = shape;

        int applyPropagationScalingIdx = 0;
        double propagatedImpulse = impulse;

        // Iterate while the propagated impulse remains above the level needed 
        // to break a shape connection and there remains shapes that have yet to
        // receive the impulse 
        while (propagatedImpulse > breakingImpulse 
                && shapeListIdx < geometry.length && shapeList[shapeListIdx] != null) {
            
            // Find the current shape index in shapeConnections
            int shapeIdx = 0;
            while (geometry[shapeIdx].uniqueShapeID != shapeList[shapeListIdx].uniqueShapeID)
                shapeIdx++;

            // Add any linked shapes to the shapelist
            int linkedShapeIdx = 0;
            while (linkedShapeIdx < shapeConnections[shapeIdx].length 
                    && shapeConnections[shapeIdx][linkedShapeIdx] != null) {
                int insertIdx = 0;
                while (shapeList[insertIdx] != null && shapeList[insertIdx].uniqueShapeID 
                        != shapeConnections[shapeIdx][linkedShapeIdx].uniqueShapeID) {
                    insertIdx++;
                }
                shapeList[insertIdx] = shapeConnections[shapeIdx][linkedShapeIdx];
                linkedShapeIdx++;
            }

            // Process the links for the current shape
            linkedShapeIdx = 0;
            boolean attemptRemoveLink = true;
            while (attemptRemoveLink && shapeConnections[shapeIdx][linkedShapeIdx] != null) {
                // Remove link to this shape from other linked shape
                int removeShapeIdx = 0;
                while (geometry[removeShapeIdx] != shapeConnections[shapeIdx][linkedShapeIdx])
                    removeShapeIdx++;

                boolean linkFound = false;
                int linkedShapeMoveIdx = 0;
                while (linkedShapeMoveIdx < shapeConnections[removeShapeIdx].length 
                        && shapeConnections[removeShapeIdx][linkedShapeMoveIdx] != null) {
                    if (linkFound)
                        shapeConnections[removeShapeIdx][linkedShapeMoveIdx - 1] 
                                = shapeConnections[removeShapeIdx][linkedShapeMoveIdx];

                    if (shapeConnections[removeShapeIdx][linkedShapeMoveIdx].uniqueShapeID 
                            == geometry[shapeIdx].uniqueShapeID)
                        linkFound = true;

                    linkedShapeMoveIdx++;
                }
                shapeConnections[removeShapeIdx][linkedShapeMoveIdx - 1] = null;

                // Remove link to linked shape from this shape
                linkedShapeMoveIdx = 1;
                while (linkedShapeMoveIdx < shapeConnections[shapeIdx].length 
                        && shapeConnections[shapeIdx][linkedShapeMoveIdx] != null) {
                    shapeConnections[shapeIdx][linkedShapeMoveIdx - 1] 
                            = shapeConnections[shapeIdx][linkedShapeMoveIdx];
                    linkedShapeMoveIdx++;
                }
                shapeConnections[shapeIdx][linkedShapeMoveIdx - 1] = null;

                if (gameEngine.randomiser.nextDouble() > breakingImpulseBrittleness) {
                    attemptRemoveLink = false;
                }
            }

            // Applying impulse scaling if needed
            if (shapeListIdx == applyPropagationScalingIdx) {
                propagatedImpulse *= breakingImpulsePropagation;
                while (applyPropagationScalingIdx < shapeList.length 
                        && shapeList[applyPropagationScalingIdx] != null) {
                    applyPropagationScalingIdx++;
                }

                // Reduce the idx by 1 as it is the next turn on which we wish to scale, 
                // however, the scaling is performed at the end of the previous turn
                applyPropagationScalingIdx--;
            }

            shapeListIdx++;
        }
    }

    /**
     * Following the application of breaking impulse, consider the shape 
     * connections to determine the impulse has resulted in the formation
     * of separate body fragments.
     * 
     * @param collisionSpace CollisionSpace associated with the body
     */
    private final void breakBodyBuildResultantShapes(CollisionSpace collisionSpace) {
        // Reset the shape considered array
        boolean[] shapeConsidered = new boolean[geometry.length];
        for (int idx = 0; idx < shapeConsidered.length; idx++) {
            shapeConsidered[idx] = false;
        }
        
        boolean isMoveableOnEntry = isMoveable;
        int bodyGeometrySizeOnEntry = geometry.length;

        // Create arrays for storing body fragment values
        ArrayList<Integer> resultantShapeIdx = new ArrayList<Integer>(geometry.length);
        ArrayList<Shape[]> resultantShapeLinkage = new ArrayList<Shape[]>(geometry.length);

        resultantShapeIdx.add(0);
        resultantShapeLinkage.add(shapeConnections[0]);
        shapeConsidered[0] = true;
        int shapeIdx = 0;
        int shapeOffsetIdx = 1;
        int numShapesConsidered = 1;

        // Process each shape in the current geometry
        while (numShapesConsidered < geometry.length) {
            // Add in all linking shapes for the current shape
            int shapeLinkIdx = 0;
            while (shapeLinkIdx < shapeConnections[shapeIdx].length 
                    && shapeConnections[shapeIdx][shapeLinkIdx] != null) {
                // Check to see if the link has already been added
                Shape linkedShape = shapeConnections[shapeIdx][shapeLinkIdx];

                int linkedShapeIdx = 0;
                boolean shapeFound = false;
                boolean shapeAlreadyAdded = false;
                for (int idx = 0; idx < geometry.length && !shapeFound; idx++) {
                    if (geometry[idx].uniqueShapeID == linkedShape.uniqueShapeID) {
                        shapeFound = true;
                        linkedShapeIdx = idx;

                        if (shapeConsidered[idx] == true) {
                            shapeAlreadyAdded = true;
                        } else {
                            shapeConsidered[idx] = true;
                        }
                    }
                }
                if (!shapeAlreadyAdded) {
                    resultantShapeIdx.add(linkedShapeIdx);
                    resultantShapeLinkage.add(shapeConnections[linkedShapeIdx]);
                    numShapesConsidered++;
                }

                shapeLinkIdx++;
            }

            if (shapeOffsetIdx < resultantShapeIdx.size()) {
                Shape nextShape = geometry[resultantShapeIdx.get(shapeOffsetIdx)];
                shapeOffsetIdx++;

                shapeIdx = 0;
                while (nextShape.uniqueShapeID != geometry[shapeIdx].uniqueShapeID) {
                    shapeIdx++;
                }
            } else {
                shapeIdx = 0;
                while (shapeIdx < geometry.length && shapeConsidered[shapeIdx] == true) {
                    shapeIdx++;
                }                
                if (resultantShapeIdx.size() < geometry.length) {
                    breakBodyBuildNewShape(collisionSpace, 
                            resultantShapeIdx, resultantShapeLinkage, 
                            numShapesConsidered, isMoveableOnEntry, bodyGeometrySizeOnEntry);
                }
                
                resultantShapeIdx.clear();
                resultantShapeLinkage.clear();
                resultantShapeIdx.add(shapeIdx);
                resultantShapeLinkage.add(shapeConnections[shapeIdx]);
                shapeConsidered[shapeIdx] = true;
                shapeOffsetIdx = 1;
                numShapesConsidered++;
            }
            
            if (numShapesConsidered == geometry.length) {
                breakBodyBuildNewShape(collisionSpace, 
                        resultantShapeIdx, resultantShapeLinkage, 
                        numShapesConsidered, isMoveableOnEntry, bodyGeometrySizeOnEntry);
            }
        }
    }

    /**
     * Create a new resultant body based upon the specified geometry fragment
     * and the transfer of body details for the parent body
     * 
     * @param collisionSpace CollisionSpace associated with the body
     * @param newShapeIdx ArrayList of integer Shape offset representing the
     *        new geometry of the resultant body
     * @param resultantShapeLinkage ArrayList of Shape arrays representing the
     *        new geometry shape linkage of the resultant body
     * @param numShapesConsidered int number of shapes so far considered
     * @param isMoveableOnEntry boolean flag specifying if the parent body is
     *        moveable
     * @param bodyGeometrySizeOnEntry specifying the size of the parent body
     *        on entry
     * 
     * @exception IllegalArgumentException if the specified geometry does not
     *            match the current graphical realisation
     */
    private final void breakBodyBuildNewShape(CollisionSpace collisionSpace, 
            ArrayList<Integer> newShapeIdx, ArrayList<Shape[]> resultantShapeLinkage, 
            int numShapesConsidered, boolean isMoveableOnEntry, int bodyGeometrySizeOnEntry) {
        if (geometry.length != graphicalRealisation.length) {
            throw new IllegalArgumentException("Body.breakBodyBuildNewShape: " +
                    "Called with non-matching geometry and graphical realisation legnth");
        }
        
        // Increment the number of resultant bodies
        if (beakingImpulseNumResultantBodies == -1)
            beakingImpulseNumResultantBodies = 0;
        beakingImpulseNumResultantBodies++;

        int newShapeSize = newShapeIdx.size();
        Shape[] newShape = new Shape[newShapeSize];
        GraphicalAsset[] newRealisation = new GraphicalAsset[newShapeSize];

        // Transfer across the graphical realisation for the fragment
        for (int idx = 0; idx < newShapeSize; idx++) {
            int shapeIdx = newShapeIdx.get(idx);
            newShape[idx] = geometry[shapeIdx];
            newRealisation[idx] = graphicalRealisation[shapeIdx];
        }

        // Work out centre point for specified shapes - not that
        // assumes that the body has a uniform mass distribution
        // over its surface area       
        double totalSurfaceArea = 0.0, averageSurfaceArea = 0.0;
        double centerX = 0.0, centerY = 0.0;
        for (int shapeIdx = 0; shapeIdx < newShape.length; shapeIdx++) {
            double shapeSurfaceArea = newShape[shapeIdx].surfaceArea;
            totalSurfaceArea += shapeSurfaceArea;

            centerX += newShape[shapeIdx].offsetX * shapeSurfaceArea;
            centerY += newShape[shapeIdx].offsetY * shapeSurfaceArea;
        }
        averageSurfaceArea = totalSurfaceArea / newShape.length;
        centerX /= (newShape.length * averageSurfaceArea);
        centerY /= (newShape.length * averageSurfaceArea);

        // Translate each shape relative to the calculated center x and y
        for (int shapeIdx = 0; shapeIdx < newShape.length; shapeIdx++) {
            newShape[shapeIdx].offsetX -= centerX;
            newShape[shapeIdx].offsetY -= centerY;

            newRealisation[shapeIdx].offsetX = newShape[shapeIdx].offsetX;
            newRealisation[shapeIdx].offsetY = newShape[shapeIdx].offsetY;
        }

        // Determine if this fragment should be moveable
        boolean unmovable = false;
        if (isMoveableOnEntry == false) {
            if (newShape.length > Math.ceil(bodyGeometrySizeOnEntry 
                        * breakingImpulseImmovableIntegrityFactor) 
                && newShape.length >= breakingImpulseImmovableMinimumIntegrity) {
                unmovable = true;
            }
        }

        // Obtain a rotational matrix for the shape
        double c = Math.cos( rotation ), s = Math.sin( rotation );
        double sRc1x = c, sRc1y = s, sRc2x = -s, sRc2y = c;

        // Create the new body
        if (numShapesConsidered < geometry.length) {
            // Create a new body object and transfer across the relevant parameters
            // from the parent body
            Body newBody = new Body(gameLayer);
            
            newBody.setGeometry(newShape);
            newBody.setShapeConnections(resultantShapeLinkage);
            newBody.setRealisation(newRealisation);

            if (unmovable) {
                newBody.setMass(Body.INFINITE_MASS);
                newBody.setBreakingImpulseAssumedBodyMass(
                        breakingImpulseAssumedBodyMass * totalSurfaceArea / surfaceArea);
            } else {
                newBody.setMass(breakingImpulseAssumedBodyMass * totalSurfaceArea / surfaceArea);
            }
            
            newBody.x = x + (sRc1x * centerX + sRc2x * centerY);
            newBody.y = y + (sRc1y * centerX + sRc2y * centerY);

            newBody.rotation = this.rotation;
            
            newBody.velocityx = this.velocityx;
            newBody.velocityy = this.velocityy;
            newBody.angularVelocity = this.angularVelocity;

            newBody.friction = this.friction;
            newBody.restitution = this.restitution;

            if (newBody.geometry.length == 1 && !breakingImpulseDestroyBody) {
                newBody.breakingImpulse = Double.MAX_VALUE;
            } else {
                newBody.breakingImpulse = this.breakingImpulse;
            }
            
            newBody.breakingImpulse = this.breakingImpulse;
            newBody.breakingImpulseBrittleness = this.breakingImpulseBrittleness;
            newBody.breakingImpulsePropagation = this.breakingImpulsePropagation;
            newBody.breakingImpulseImmovableIntegrityFactor 
                    = this.breakingImpulseImmovableIntegrityFactor;
            newBody.breakingImpulseImmovableMinimumIntegrity 
                    = this.breakingImpulseImmovableMinimumIntegrity;
            newBody.breakingImpulseImmovableDestroyMoveable 
                    = this.breakingImpulseImmovableDestroyMoveable;
            newBody.breakingImpulseDestroyBody = this.breakingImpulseDestroyBody;

            newBody.drawOrder = this.drawOrder;

            if ((!isMoveableOnEntry && breakingImpulseImmovableDestroyMoveable 
                    && newBody.mass < Body.INFINITE_MASS) == false) {
                collisionSpace.queueGameObjectToAdd(newBody);
            } else {
                beakingImpulseNumResultantBodies--;
            }
        } else {
            // Refactor the parent game object so that it becomes a new fragment
            x += (sRc1x * centerX + sRc2x * centerY);
            y += (sRc1y * centerX + sRc2y * centerY);

            if (unmovable) {
                double breakingImpulseAssumedBodyMass = this.breakingImpulseAssumedBodyMass;
                setMass(Body.INFINITE_MASS);
                setBreakingImpulseAssumedBodyMass(
                        breakingImpulseAssumedBodyMass * totalSurfaceArea / surfaceArea);
            } else {
                setMass(this.breakingImpulseAssumedBodyMass * totalSurfaceArea / surfaceArea);
            }
            
            setGeometry(newShape);
            setShapeConnections(resultantShapeLinkage);
            setRealisation(newRealisation);

            if (!isMoveableOnEntry && breakingImpulseImmovableDestroyMoveable 
                    && mass < Body.INFINITE_MASS) {
                collisionSpace.queueGameObjectToRemove(this);
                beakingImpulseNumResultantBodies--;
            }
        }
    }
}
