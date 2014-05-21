package game.physics;

import game.engine.*;
import game.geometry.*;
import java.util.*;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.*;

/*
 * The physics engine within this code repository draws heavily
 * (i.e. it is largely based on) upon the work of two individuals:
 *
 * Original work concerning a 2D box-based physics engine developed
 * by Erin Catto - see http://www.gphysics.com - and the subsequent
 * work of Kevin Glass in producing an extended Java version, Phys2D -
 * see http://www.cokeandcode.com/phys2d/. As such, large sections of
 * this physics engine are based on their intellectual work (of which
 * I am very grateful).
 *
 * This particular implementation is primarily based upon on a redesign
 * of one of the earlier version of Phys2D with the following 
 * extensions/changes:
 *
 * 1. The code was redesigned to minimise the number of created
 *    objects/method calls for reasons of performance (vector/matrix
 *    manipulation is 'inlined' and objects are not disposed but
 *    rather reused). These changes have been at the expense of code
 *    clarity and, to a lesser degree, extensibility (although for
 *    reference it will run around two orders of magnitude faster
 *    than the earlier Phys2D code which appears to have been designed 
 *    with extensibility/elegance in mind - which is no bad thing). I 
 *    particularly recommend the C++ code developed by Catto for gaining 
 *    an understanding of the impulse algorithms involved.
 * 
 * 2. As with other layers within the code repository a sorted listed
 *    of game entities (bodies) is maintained and used to perform
 *    an initial cull of objects to be considered within the
 *    collection detection phase.
 *
 * 3. Resting determination has been introduced to remove objects
 *    from collision tests and impulse application if they are
 *    detected to be at rest. Bodies at rest will be reactivated as
 *    needed to ensure appropriate response. The aggressiveness of the
 *    resting determination can be varied.
 *
 * 4. The geometry of bodies can be described in terms of any
 *    combination of rectangles or circles (i.e. bodies are not
 *    restricted to being a single shape).
 *
 * 5. Both bodies with a geometry consisting of more than one shape
 *    and joints can have associated breaking impulses (i.e. breaking
 *    the body into smaller bodies or breaking the joint). How bodies
 *    fragment can be controlled.
 *
 * The CollisionSpace class provides a central 'layer' to which instances
 * of the Body class can be added and then subject to physical simulation
 * (i.e. CollisionSpace can be viewed as a form of GameLayer to which only
 * Body instances can be added).
 * 
 * @version $Revision: 1 $ $Date: 2008/07 $ *
 */
public class CollisionSpace extends GameLayer {

    ///////////////////////////////////////////////////////////////////////////
    // Class data                                                            //
    ///////////////////////////////////////////////////////////////////////////
    
    /** 
     * The number of impulse iterations that will be fired during each step.
     * This is an important value as it determines the accuracy of the
     * responses. A high value helps to remove object jitter whenever a 
     * lot of objects are touching, but at the expense of extra computation.
     */ 
    protected int IMPULSE_ITERATIONS = 10;

    /** 
     * The base degree of dampening within the simulation. A value close to 
     * 1.0 will result in very rigid bodies, however, this can be at the 
     * expense of slightly jittery behaviour given lots of interacting objects.
     * A lower value will permit more object overlap, reducing jitter, but at
     * the expense of, well, object overlap.
     */
    protected double IMPACT_ABSORPTION = 0.75;

    /** 
     * If required, global limits can be imposed on the maximum travel
     * x and y velocities and the maximum angular velocity.
     */
    protected double MAXIMUM_TRAVEL_VELOCITYX = Double.MAX_VALUE;
    protected double MAXIMUM_TRAVEL_VELOCITYY = Double.MAX_VALUE;
    protected double MAXIMUM_ANGULAR_VELOCITY = Double.MAX_VALUE;

    /** 
     * Parameters controlling the at rest determination. The
     * STATIONARY_TICK_THRESHOLD specifies the number of update
     * ticks that each body must pass to be classified as being
     * at rest (and hence quickly culled from the physics 
     * simulation). The STATIONARY_VELOCITY_THRESHOLD specifies
     * the maximum velocity (in either the x or y direction) -
     * i.e. the body velocity must be below this value in order
     * to pass a at rest test. STATIONARY_MOVEMENT_THRESHOLD
     * specifies the maximum movement (in either the x or y
     * direction) that a body can move before failing an at
     * rest test. Finally, STATIONARY_ROTATION_THRESHOLD specifies
     * the maximum angular velocity (in radians) before an at
     * rest test will be failed.
     * <P>
     * Note: the default values are somewhat conservative and 
     * should safely determines when bodies are a rest. Most
     * aggressive setting may be desirable given high gravity
     * settings and/or lots of interacting bodies.
     */
    protected static double STATIONARY_VELOCITY_THRESHOLD = 1.0;
    protected static double STATIONARY_MOVEMENT_THRESHOLD = 1.0;
    protected static double STATIONARY_ROTATION_THRESHOLD = 0.05;
    protected static int STATIONARY_TICK_THRESHOLD = 100;

    /**
     * Pool of currently active arbiters within the simulation. 
     */
    public HashMap<Integer, Arbiter> arbiters 
            = new LinkedHashMap<Integer, Arbiter>();

    /**
     * List of available (i.e. currently unused) arbiters within the
     * simulation. The arbiter list will be increased in size as 
     * needed, with 'removed' arbiters added to the pool to the list
     * to support the next collision (i.e. arbiter objects are reused).
     */
    private LinkedList<Arbiter> availableArbiters = new LinkedList<Arbiter>();
    private ArrayList<Arbiter> arbitersToRemove = new ArrayList<Arbiter>();

    /**
     * The direction and force of gravity in terms of layer points
     * (typically pixels) per second increase.
     */
    protected double gravityx = 0.0;
    protected double gravityy = 9.8;

    /**
     * By default the step method will step the physics simulation by 
     * an amount corresponding to when the step method was last called -
     * based upon the difference between the current time and the
     * lastUpdateTime (if required lastUpdateTime can be modified to 
     * reflect game pauses). It is possible that the GameEngine will
     * call the update method on this class (and hence step) two or
     * more times in quick succession (i.e. if the UPS drops below the
     * target specified, e.g. the GC or some other process fires). This
     * can cause jittery behaviour in some aspects of the simulation 
     * (especially those that involve taking the inverse of time elapse), 
     * hence a minimum step time was introduced to ensure a more smooth 
     * simulation.
     */
    protected static final double MINIMUM_STEP_PERIOD = 1.0 / 100.0;
    protected long lastUpdateTime = -1;


    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Create a new and empty collision space 
     *
     * @param  gameLayerName name of the collision space
     * @param gameEngine GameEngine to which this layer belongs
     */
    public CollisionSpace(String gameLayerName, GameEngine gameEngine) {
        super(gameLayerName, gameEngine);

        // Create two game object collections to hold bodies and joints
        addGameObjectCollection("Bodies");
        addGameObjectCollection("Joints");

        // Initially set the last update time to that of construction
        lastUpdateTime = System.nanoTime();
    }

    /**
     * Create a new and empty collision space 
     *
     * @param  gameLayerName name of the collision space
     * @param  gameEngine GameEngine to which this game layer belongs
     * @param  width double initial layer width
     * @param  height double initial layer height
     */
    public CollisionSpace( String gameLayerName, 
            GameEngine gameEngine, double width, double height) {
        super(gameLayerName, gameEngine, width, height);

        // Create two game object collections to hold bodies and joints
        addGameObjectCollection("Bodies");
        addGameObjectCollection("Joints");

        // Initially set the last update time to that of construction
        lastUpdateTime = System.nanoTime();
    }

    /**
     * Create a new and empty collision space 
     *
     * @param  gameLayerName name of the collision space
     * @param  gameEngine GameEngine to which this game layer belongs
     * @param  width double initial layer width
     * @param  height double initial layer height
     * @param  viewPortLayerX x layer viewport offset
     * @param  viewPortLayerY y layer viewport offset
     */
    public CollisionSpace(String gameLayerName, 
            GameEngine gameEngine, double width, double height, 
            double viewPortLayerX, double viewPortLayerY) {
        super(gameLayerName, gameEngine, width, height, 
                viewPortLayerX, viewPortLayerY);

        // Create two game object collections to hold bodies and joints
        addGameObjectCollection("Bodies");
        addGameObjectCollection("Joints");

        // Initially set the last update time to that of construction
        lastUpdateTime = System.nanoTime();
    }

    /**
     * Create a new and empty collision space 
     *
     * @param  gameLayerName name of the collision space
     * @param  gameEngine GameEngine to which this game layer belongs
     * @param  width double initial layer width
     * @param  height double initial layer height
     * @param  viewPortLayerX x layer viewport offset
     * @param  viewPortLayerY y layer viewport offset
     * @param  viewPortScreenX x screen viewport offset
     * @param  viewPortScreenY y screen viewport offset
     * @param  viewPortWidth width of the screen viewport 
     * @param  viewPortHeight height of the screen viewport 
     */
    public CollisionSpace(String gameLayerName, GameEngine gameEngine, 
            double width, double height, 
            double viewPortLayerX, double viewPortLayerY, 
            double viewPortScreenX, double viewPortScreenY, 
            double viewPortWidth, double viewPortHeight) {
        super(gameLayerName, gameEngine, width, height, 
                viewPortLayerX, viewPortLayerY, 
                viewPortScreenX, viewPortScreenY, viewPortWidth, viewPortHeight);

        // Create two game object collections to hold bodies and joints
        addGameObjectCollection("Bodies");
        addGameObjectCollection("Joints");

        // Initially set the last update time to that of construction
        lastUpdateTime = System.nanoTime();
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods: Get/Set/Overloaded GameLayer methods                         //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Perform actions upon entry to an active game state
     */    
    @Override
    protected void enterActiveState() {
        super.enterActiveState();
        
        // Set the last update time to when this layer is made active
        // i.e. following a pause, the physics simulation will automatically
        // reset the last update time.
        lastUpdateTime = System.nanoTime();
    }

    /**
     * Add the specified game object, optionally to the specified game object set.
     * <P>
     * Note: if the added game object is of type Body then it is also added
     * into the physics simulation.
     *
     * @param  gameObject game object to be added
     * @param  gameObjectSetname name of the game object set to which the game
     *         object will be added.
     */
    @Override
    public void addGameObject(GameObject gameObject, String gameObjectSetname) {
        super.addGameObject(gameObject, gameObjectSetname);

        // If the specified game object is of type Body, then add it to
        // Bodies game object set unless the specified gameObjectSetname 
        // already specifies Bodies. Once added, the list of game objects
        // is incrementally sorted.
        if (gameObject instanceof Body) {
            GameObjectCollection bodies = getGameObjectCollection("Bodies");
            if (gameObjectSetname.compareTo("Bodies") != 0) {
                bodies.add(gameObject);
            }
            bodies.incrementalSortGameObjects();
        }
    }

    /**
     * Remove the specified game object.
     * <P>
     * Note: If the specified game object is of type Body, then the
     * object is also removed from the physics simulation.
     *
     * @param  gameObject game object to be removed
     */
    @Override
    public void removeGameObject(GameObject gameObject) {
        super.removeGameObject(gameObject);

        if (gameObject instanceof Body) {
            GameObjectCollection bodies = getGameObjectCollection("Bodies");
            bodies.remove(gameObject);

            // Release any arbiters that were dealing with the removed object
            removeArbitersForBody((Body) gameObject);
        }
    }

    /**
     * Release any arbiters that are associated with the specified body.
     *
     * @param  body Body object to be considered for arbiter release
     */
    public void removeArbitersForBody(Body body) {
        arbitersToRemove.clear();

        for (Arbiter arb : arbiters.values()) {
            if (arb.body1.uniqueGameObjectID == body.uniqueGameObjectID 
                    || arb.body2.uniqueGameObjectID == body.uniqueGameObjectID) {
                arbitersToRemove.add(arb);
            }
        }
        
        for (Arbiter arbToRemove : arbitersToRemove) {
            arbiters.remove(arbToRemove.arbiterIdentifer);
            availableArbiters.add(arbToRemove);
        }
    }

    /**
     * Return the current set of active arbiter objects involved in the
     * physics simulation
     *
     * @return Hashmap containing the list of arbiters
     */
    public HashMap<Integer, Arbiter> getArbiters() {
        return arbiters;
    }

    /**
     * Add the specified joint to the physics simulation
     *
     * @param joint Joint object to be added
     */
    public void addJoint(Joint joint) {
        getGameObjectCollection("Joints").add(joint);
    }
        
    /**
     * Retrieve the current collection of joints within the physics
     * simulation
     *
     * @return GameObjectCollection containing the current collection of 
     *         joints
     */
    public GameObjectCollection getJoints() {
        return getGameObjectCollection("Joints");
    }

    /**
     * Remove the specified joint from the physics simulation
     *
     * @param joint Joint to be removed
     */
    public void removeJoint(Joint joint) {
        getGameObjectCollection("Joints").remove(joint);
    }

    /**
     * Remove all bodies and joints from the physics simulation
     */
    public void removeAllBodiesAndJoints() {
        GameObjectCollection bodies = getGameObjectCollection("Bodies");
        while (bodies.size > 0) {
            removeGameObject(bodies.gameObjects[0]);
        }
        
        GameObjectCollection joints = getGameObjectCollection("Joints");
        while (joints.size > 0) {
            removeJoint((Joint) joints.gameObjects[0]);
        }
        
        arbiters.clear();
    }

    /**
     * Remove all game objects from the physics simulation
     */
    @Override
    public void removeAllGameObjects() {
        super.removeAllGameObjects();

        arbiters = new LinkedHashMap<Integer, Arbiter>();
        arbitersToRemove = new ArrayList<Arbiter>();
        availableArbiters = new LinkedList<Arbiter>();
    }

    /**
     * Set the gravity strength applied to moveable bodies
     *
     * @param x double x gravitational strength
     * @param y double y gravitational strength
     */
    public void setGravity(double x, double y) {
        gravityx = x;
        gravityy = y;
    }

    /**
     * Return the x gravitational strength
     *
     * @return double x gravitational strength
     */
    public double getGravityX() {
        return gravityx;
    }

    /**
     * Return the y gravitational strength
     *
     * @return double y gravitational strength
     */
    public double getGravityY() {
        return gravityy;
    }

    /**
     * Call back method invoked whenever a body is broken or destroyed
     * within the physics simulation. 
     * <P>
     * It is intended that this method be overloaded to provide whatever
     * action, if any, if needed following body fragmentation or destruction.
     * 
     * @param body Body that was broken or destroyed
     * @param int number of resultantBodies following the impulse, or 0 if
     *        the body has been destroyed
     */
    protected void bodyBrokenOrDestroyed(Body body, int resultantBodies) { }

    
    ///////////////////////////////////////////////////////////////////////////
    // Methods: Physics simulation update                                    //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Default collision space update behaviour, i.e. the physics simulation
     * is stepped and then the inherited game layer update method is called.
     */
    @Override
    public void update() {
        this.step();
        super.update();
    }

    /**
     * Step the physics simulation based on the amount of time that has
     * elapsed since the last step.
     */
    public void step() {
        long timeNow = System.nanoTime();
        double dt = (timeNow - lastUpdateTime) / 1000000000.0;
        lastUpdateTime = timeNow;

        // Ensure that the step is not less than the minimum permitted
        // step duration
        if (dt < MINIMUM_STEP_PERIOD) {
            dt = MINIMUM_STEP_PERIOD;
        }

        // Step the physics simulation
        step(dt);
    }

    /**
     * Step the simulation by the specified amount of time.
     *
     * @param dt double amount of time to step the simulation
     */
    public void step(double dt) {        
        // Call the resolve method to update the set of arbiters
        // that are used to model collisions between bodies.
        resolve();

        // Determine if we have a high gravitational force. A high
        // gravitational force is specified to by any force that
        // will results in at least a 0.5 pixel movement of game
        // objects within the current update. A high gravitational
        // force can be problematic as a larger correction will be
        // needed to compensate for the gravitation pull (which can
        // result in some jitter).        
        boolean highGravityX = gravityx * dt > 0.5 ? true : false;
        boolean highGravityY = gravityy * dt > 0.5 ? true : false;

        ////////////////////////////////////////////////////////////////////////
        // Update the velocity of each body in the physics simulation         //
        // based upon body forces and any gravitational pull                  //
        ////////////////////////////////////////////////////////////////////////
          
        GameObjectCollection bodies = this.getGameObjectCollection("Bodies");
        for (int idx = 0; idx < bodies.size; idx++) {
            Body body = (Body) bodies.gameObjects[idx];

            // If the body is not moveable then skip any positional update
            if (body.isMoveable == false)                
                continue;
            
            // If the body velocity will exceed the at rest stationary
            // velocity threshold, then reset the ticksAtRest to zero.
            if (Math.abs(body.velocityx) > STATIONARY_VELOCITY_THRESHOLD 
                    || Math.abs(body.velocityy) > STATIONARY_VELOCITY_THRESHOLD)
                body.ticksAtRest = 0;

            // Determine the velocity change from any force applied to the body
            double velocityChangex = body.forcex * body.invMass;
            double velocityChangey = body.forcey * body.invMass;

            // If the body can be affected by gravity and the body is not
            // considered to be at rest, then update the velocities based
            // on the gravitational strength. Note, if a high gravitational
            // pull has been detected then the gravitational pull is slow
            // started (i.e. the full gravitational pull is applied over a couple
            // of updates). This entails that the gravitational pull of a body 
            // resting on a surface is more easily corrected.
            if (body.gravityEffected || body.I == Body.INFINITE_MASS) {
                if (body.ticksAtRest < STATIONARY_TICK_THRESHOLD) {
                    if (highGravityX) {
                        velocityChangex += Math.max(0.5 / dt, 
                                Math.min(body.velocityx / dt, gravityx));
                    } else {
                        velocityChangex += gravityx;
                    }
                    
                    if (highGravityY) {
                        velocityChangey += Math.max(0.5 / dt, 
                                Math.min(Math.abs(body.velocityy / dt), gravityy));
                    } else {
                        velocityChangey += gravityy;
                    }
                }
            }
            
            // Update the body velocities
            body.velocityx += velocityChangex * dt;
            body.velocityy += velocityChangey * dt;
            body.angularVelocity += dt * body.invI * body.torque;
        }

        ////////////////////////////////////////////////////////////////////////
        // Fire all active arbiters to update the velocities of all bodies    //
        // that are currently in contact with other bodies. Additionally      //
        // update the velocities of all bodies with associated joints         //
        ////////////////////////////////////////////////////////////////////////
                
        boolean body1AtRest, body2AtRest;
        double invDT = dt > 0.0 ? 1.0 / dt : 0.0;

        // Setup each arbiter associated with bodies that are not considered
        // to be at rest, i.e. removed from the physics simulation
        for (Arbiter arb : arbiters.values()) {
            body1AtRest = arb.body1.ticksAtRest > STATIONARY_TICK_THRESHOLD;
            body2AtRest = arb.body2.ticksAtRest > STATIONARY_TICK_THRESHOLD;

            if (!(body1AtRest && body2AtRest)) {
                arb.preStep(invDT, dt, IMPACT_ABSORPTION);
            }
        }

        // Setup each joint
        GameObjectCollection joints = this.getGameObjectCollection("Joints");
        for (int jointIdx = 0; jointIdx < joints.size; jointIdx++) {
            ((Joint) joints.gameObjects[jointIdx]).preStep(invDT);
        }
        
        // Apply the specified number of impulse iterations for each arbiter
        // and joint in order to correct any body overlap or joint impulses
        for (int i = 0; i < IMPULSE_ITERATIONS; ++i) {
            for (Arbiter arb : arbiters.values()) {
                body1AtRest = arb.body1.ticksAtRest > STATIONARY_TICK_THRESHOLD;
                body2AtRest = arb.body2.ticksAtRest > STATIONARY_TICK_THRESHOLD;
                if (!(body1AtRest && body2AtRest)) {
                    arb.applyImpulse();
                }
            }

            for (int jointIdx = 0; jointIdx < joints.size; jointIdx++) {
                ((Joint) joints.gameObjects[jointIdx]).applyImpulse();
            }
        }
        
        ////////////////////////////////////////////////////////////////////////
        // Update the position of bodies based upon the set velocities as     //
        // computed from body forces, gravitational pull, joint impulse and   //
        // arbiter correction.                                                //
        ////////////////////////////////////////////////////////////////////////
        
        double originalx, originaly, originalrot;
        for (int idx = 0; idx < bodies.size; idx++) {
            Body body = (Body) bodies.gameObjects[idx];

            if (body.ticksAtRest > STATIONARY_TICK_THRESHOLD)
                continue;
                
            originalx = body.x; originaly = body.y;
            originalrot = body.rotation;

            // Update the body's x position, bounded by the maximum x velocity
            if (Math.abs(body.velocityx + body.biasedVelocityx) 
                    > MAXIMUM_TRAVEL_VELOCITYX) {
                body.x += Math.signum(body.velocityx + body.biasedVelocityx) 
                        * dt * MAXIMUM_TRAVEL_VELOCITYX;
            } else {
                body.x += dt * (body.velocityx + body.biasedVelocityx);
            }

            // Update the body's y position, bounded by the maximum y velocity            
            if (Math.abs(body.velocityy + body.biasedVelocityy) 
                    > MAXIMUM_TRAVEL_VELOCITYY) {
                body.y += Math.signum(body.velocityy + body.biasedVelocityy) 
                        * dt * MAXIMUM_TRAVEL_VELOCITYX;
            } else {
                body.y += dt * (body.velocityy + body.biasedVelocityy);
            }

            // Update the body's rotation, bounded by the maximum angular velocity
            if (Math.abs(body.angularVelocity + body.biasedAngularVelocity) 
                    > MAXIMUM_ANGULAR_VELOCITY) {
                body.rotation += Math.signum(body.angularVelocity 
                        + body.biasedAngularVelocity) * dt * MAXIMUM_ANGULAR_VELOCITY;
            } else {
                body.rotation += dt 
                        * (body.angularVelocity + body.biasedAngularVelocity);
            }

            // If the body has not moved beyond the maximum limit defined for
            // the at rest determination, then increase the number of ticks
            // that the body is considered to be a rest
            if (body.permitAtRest 
                    && Math.abs(body.x - originalx) < STATIONARY_MOVEMENT_THRESHOLD 
                    && Math.abs(body.y - originaly) < STATIONARY_MOVEMENT_THRESHOLD 
                    && Math.abs(body.rotation - originalrot) 
                        < STATIONARY_ROTATION_THRESHOLD) {
                body.ticksAtRest++;
            }

            // Reset the forces that were applied to the body during this update
            body.biasedVelocityx = 0.0; body.biasedVelocityy = 0.0;
            body.biasedAngularVelocity = 0.0;
            body.forcex = 0.0; body.forcey = 0.0; body.torque = 0.0;
        }

        ////////////////////////////////////////////////////////////////////////
        // Consider broken joints and bodies                                  //
        ////////////////////////////////////////////////////////////////////////
        
        // Remove any broken joints (i.e. those that received an impulse greater
        // than the breaking impulse
        int jointIdx = 0;
        while (jointIdx < joints.size) {
            if (((Joint) joints.gameObjects[jointIdx]).jointBroken)
                joints.remove(jointIdx);
            else
                jointIdx++;
        }
        
        // Consider arbiter impulses to locate any impulses greater than the 
        // breaking impulse of the associated body.
        for (Arbiter arb : arbiters.values()) {
            // Only proceed if the body is breakable
            if (arb.body1.breakingImpulse < Double.MAX_VALUE) {
                boolean bodyBroken = false;
                for (int contactIdx = 0; 
                        contactIdx < arb.numContacts && !bodyBroken; contactIdx++) {
                    if (arb.body1.breakingImpulse < arb.contacts[contactIdx].accumulatedNormalImpulse) {
                        // Consider a break of the body with the applied impulse to the 
                        // specified shape. If the body is fragmented or destroyed then
                        // call the bodyBrokenOrDestroyed method.
                        bodyBroken = true;
                        int resultantBodies = arb.body1.breakBody(
                                this, arb.contacts[contactIdx].accumulatedNormalImpulse, 
                                arb.contacts[contactIdx].shape1);
                        if (resultantBodies != -1) {
                            bodyBrokenOrDestroyed(arb.body1, resultantBodies);
                        }
                    }
                }
            }

            // Only proceed if the body is breakable
            if (arb.body2.breakingImpulse < Double.MAX_VALUE) {
                boolean bodyBroken = false;
                for (int contactIdx = 0; contactIdx < arb.numContacts && !bodyBroken; contactIdx++) {
                    if (arb.body2.breakingImpulse < arb.contacts[contactIdx].accumulatedNormalImpulse) {
                        // Consider a break of the body with the applied impulse to the 
                        // specified shape. If the body is fragmented or destroyed then
                        // call the bodyBrokenOrDestroyed method.
                        bodyBroken = true;
                        int resultantBodies = arb.body2.breakBody(
                                this, arb.contacts[contactIdx].accumulatedNormalImpulse, 
                                arb.contacts[contactIdx].shape2);
                        if (resultantBodies != -1) {
                            bodyBrokenOrDestroyed(arb.body1, resultantBodies);
                        }
                    }
                }
            }
        }
    }

    /**
     * Consider all bodies for collision and update the set of arbiters
     * to reflect the current set of points of contact between bodies.
     */
    public void resolve() {
        // Ensure the collection of bodies are correctly sorted
        GameObjectCollection bodies = getGameObjectCollection("Bodies");
        bodies.incrementalSortGameObjects();

        // Create or retreive an empty arbiter
        Arbiter arbiter;
        if (availableArbiters.size() > 0)
            arbiter = availableArbiters.removeFirst();
        else arbiter = new Arbiter();

        // Consider each body for overlap against other bodies
        for (int body1Idx = 0; body1Idx < bodies.size - 1; body1Idx++) {
            Body body1 = (Body) bodies.gameObjects[body1Idx];
            
            // If this object cannot intersect other game object then return
            if (body1.canIntersectOtherGraphicalObjects == false)
                continue;

            // Determine if this body can be considered to be at stationary, i.e.
            // if the velocity is less than the stationary velocity threshold
            // This will be used when checking if this body can be considered
            // to be at rest.
            boolean body1AtRest = Math.abs(body1.velocityx) < STATIONARY_VELOCITY_THRESHOLD 
                    && Math.abs(body1.velocityy) < STATIONARY_VELOCITY_THRESHOLD;
                        
            // Calculate the end point of body1, which can be used as a cutoff point
            // when testing for overlap between other bodies, i.e. any bodies with  
            // a start location beyond the end of body1 cannot overlap.
            double body1Endx = body1.x + body1.boundingDimension / 2;

            // Consider all possibly overlapping bodies with body1 along the x-axis
            int body2Idx = body1Idx + 1;
            while (body2Idx < bodies.size && (bodies.gameObjects[body2Idx].x 
                    - bodies.gameObjects[body2Idx].boundingDimension) <= body1Endx) {
                Body body2 = (Body) bodies.gameObjects[body2Idx++];

                // If this object cannot intersect other game object then go onto
                // the next body to compare
                if (body2.canIntersectOtherGraphicalObjects == false)
                    continue;
                
                // Test for potential overlap along the y-axis and go onto the next
                // body if body2 cannot intersect body1
                if (body1.y - body1.boundingDimension / 2 
                            > body2.y + body2.boundingDimension / 2 
                        || body1.y + body1.boundingDimension / 2 
                            < body2.y - body2.boundingDimension / 2)
                    continue;

                // If both bodies are immoveable then return
                if (body1.isMoveable == false && body2.isMoveable == false)
                    continue;
                    
                // Determine if either body1 or body2 has exceed the specified
                // STATIONARY_VELOCITY_THRESHOLD. If so, then if the bodies are
                // moveable reset the number of ticks at rest, i.e. this check
                // will effectively reactivate any oject that might be involved
                // in a collision.
                if (!body1AtRest || (Math.abs(body2.velocityx) 
                            >= STATIONARY_VELOCITY_THRESHOLD 
                        || Math.abs(body2.velocityy) 
                            >= STATIONARY_VELOCITY_THRESHOLD)) {
                    if (body1.isMoveable == true)
                        body1.ticksAtRest = 0;

                    if (body2.isMoveable == true)
                        body2.ticksAtRest = 0;
                }

                // If both bodies are considered to be at rest then go onto the
                // next body to comapre
                if (body1.ticksAtRest > STATIONARY_TICK_THRESHOLD 
                        && body2.ticksAtRest > STATIONARY_TICK_THRESHOLD)
                    continue;
                

                // Ensure the two bodies are ordered correctly in terms of 
                // providing each arbiter with a consistent identity
                Body bodyi, bodyj;
                if (body1.uniqueGameObjectID < body2.uniqueGameObjectID) {
                    bodyi = body1; bodyj = body2;
                } else {
                    bodyi = body2; bodyj = body1;
                }

                // Add the specified bodies into the next available arbiter
                arbiter.setBodies(bodyi, bodyj);

                // Create rotational matrices for both bodies in order to
                // determine the offset of shape geometry within each body
                double c = Math.cos( bodyi.rotation );
                double s = Math.sin( bodyi.rotation );
                double biRc1x = c, biRc1y = s, biRc2x = -s, biRc2y = c;
                
                c = Math.cos( bodyj.rotation );
                s = Math.sin( bodyj.rotation );
                double bjRc1x = c, bjRc1y = s, bjRc2x = -s, bjRc2y = c;

                double firstShapeX, firstShapeY;
                double secondShapeX, secondShapeY;

                // Consider each item of shape geometry within both 
                // bodies for shape overlap                
                for (int body1ShapeIdx = 0; body1ShapeIdx 
                        < bodyi.geometry.length; body1ShapeIdx++) {
                    for (int body2ShapeIdx = 0; body2ShapeIdx 
                            < bodyj.geometry.length; body2ShapeIdx++) {
                        // Extract the shapes under comparison
                        Shape firstShape = bodyi.geometry[body1ShapeIdx];
                        Shape secondShape = bodyj.geometry[body2ShapeIdx];

                        // Permit a quick bounds check for both shapes to see
                        // if they might overlap. If they cannot overlap, then
                        // go onto the next shape pairing
                        firstShapeX = bodyi.x + 
                                (biRc1x * firstShape.offsetX + biRc2x * firstShape.offsetY);
                        secondShapeX = bodyj.x + 
                                (bjRc1x * secondShape.offsetX + bjRc2x * secondShape.offsetY);
                        if (firstShapeX - firstShape.boundDimension / 2 
                                    > secondShapeX + secondShape.boundDimension / 2 
                                || firstShapeX + firstShape.boundDimension / 2 
                                    < secondShapeX - secondShape.boundDimension / 2) {
                            continue;
                        }
                        
                        firstShapeY = bodyi.y + 
                                (biRc1y * firstShape.offsetX + biRc2y * firstShape.offsetY);
                        secondShapeY = bodyj.y + 
                                (bjRc1y * secondShape.offsetX + bjRc2y * secondShape.offsetY);
                        if (firstShapeY - firstShape.boundDimension / 2 
                                    > secondShapeY + secondShape.boundDimension / 2 
                                || firstShapeY + firstShape.boundDimension / 2 
                                    < secondShapeY - secondShape.boundDimension / 2) {
                            continue;
                        }
                        
                        // Invoke an appropriate collider test based on the types
                        // of shape involved in the collision
                        if (firstShape instanceof Box) {
                            if (secondShape instanceof Box)
                                arbiter.numContacts += Collider.collide(
                                        arbiter, (Box) firstShape, (Box) secondShape);
                            else
                                arbiter.numContacts += Collider.collide(
                                        arbiter, (Box) firstShape, (Circle) secondShape);
                        } else {
                            if (secondShape instanceof Box)
                                arbiter.numContacts += Collider.collide(
                                        arbiter, (Circle) firstShape, (Box) secondShape);
                            else
                                arbiter.numContacts += Collider.collide(
                                        arbiter, (Circle) firstShape, (Circle) secondShape);
                        }
                    }
                }
                                
                // If the number of contacts between the bodies is greater than 
                // zero, i.e. they collide, then test if this arbiter is already
                // defined within the arbiter pool (i.e. it is an known collision)
                // and, if so, update the existing collision based on the
                // detected current overlap. If this is a new arbiter, then add it
                // to the pool of arbiters and retrieve another empty arbiter 
                // to use for the next test.
                if (arbiter.numContacts > 0) {
                    Arbiter arb = arbiters.get(arbiter.arbiterIdentifer);
                    if (arb != null) {
                        arb.update(arbiter.contacts, arbiter.numContacts);
                    } else {
                        arbiters.put(arbiter.arbiterIdentifer, arbiter);

                        if (availableArbiters.size() > 0) {
                            arbiter = availableArbiters.removeFirst();
                        } else {
                            arbiter = new Arbiter();
                        }
                    }
                }
            }
        }
                
        // Now that we have finished test for overlap between all bodies, add
        // the last empty arbiter back into the available arbiters pool (i.e.
        // ensuring it can be reused on subsequent tests).
        availableArbiters.add(arbiter);

        // Parse all arbiters within the arbiter pool to determine which
        // arbiters can be released (i.e. are not needed any more)
        arbitersToRemove.clear();
        for (Arbiter arb : arbiters.values()) {
                        
            // If both bodies in contact are currently at rest then go onto
            // the next arbiter
            if (arb.body1.ticksAtRest > STATIONARY_TICK_THRESHOLD 
                    && arb.body2.ticksAtRest > STATIONARY_TICK_THRESHOLD) {
                continue;
            }            

            // If the two bodies cannot overlap. then remove the arbiter
            if (arb.body1.x - arb.body1.boundingDimension / 2 
                        > arb.body2.x + arb.body2.boundingDimension / 2 
                    || arb.body1.y - arb.body1.boundingDimension / 2 
                        > arb.body2.y + arb.body2.boundingDimension / 2 
                    || arb.body1.x + arb.body1.boundingDimension / 2 
                        < arb.body2.x - arb.body2.boundingDimension / 2 
                    || arb.body1.y + arb.body1.boundingDimension / 2 
                        < arb.body2.y - arb.body2.boundingDimension / 2) {
                arbitersToRemove.add(arb);
                continue;
            }

            // Test of overlap between the bodies involved in the arbiter
            arb.numContacts = 0;
            for (int body1ShapeIdx = 0; body1ShapeIdx 
                        < arb.body1.geometry.length; body1ShapeIdx++) {
                for (int body2ShapeIdx = 0; body2ShapeIdx 
                        < arb.body2.geometry.length; body2ShapeIdx++) {
                    Shape firstShape = arb.body1.geometry[body1ShapeIdx];
                    Shape secondShape = arb.body2.geometry[body2ShapeIdx];

                    if (firstShape instanceof Box) {
                        if (secondShape instanceof Box)
                            arb.numContacts += Collider.collide(
                                    arb, (Box) firstShape, (Box) secondShape);
                        else 
                            arb.numContacts += Collider.collide(
                                    arb, (Box) firstShape, (Circle) secondShape);
                    } else {
                        if (secondShape instanceof Box)
                            arb.numContacts += Collider.collide(
                                    arb, (Circle) firstShape, (Box) secondShape);
                        else
                            arb.numContacts += Collider.collide(
                                    arb, (Circle) firstShape, (Circle) secondShape);
                    }
                }
            }
            
            // If there are no current contacts, then remove the arbiter
            if (arb.numContacts == 0) {
                arbitersToRemove.add(arb);
            }
        }

        // Remove all specified arbiters
        for (Arbiter arbToRemove : arbitersToRemove) {
            arbiters.remove(arbToRemove.arbiterIdentifer);
            availableArbiters.add(arbToRemove);
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods: Draw                                                         //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Draw all graphical game objects contained within the game layer to the
     * specified graphics object.
     */
    @Override
    public void draw(Graphics2D graphics2D) {
        super.draw(graphics2D);

        if (drawGameObjectDebugInformation) {
            drawBodyDebugInformation(graphics2D);
        }
    }

    /**
     * This can be used to draw body information useful in debugging,
     * including if the body is currently at rest (green center circle),
     * joint outlines and arbiter locations (i.e. points of contact).
     *
     * @param  graphics2D Graphics object to draw to
     */
    public void drawBodyDebugInformation(Graphics2D graphics2D) {
        GameObjectCollection bodies = this.getGameObjectCollection("Bodies");

        AffineTransform originalTransform = graphics2D.getTransform();
        java.awt.Shape originalClipRegion = graphics2D.getClip();

        if (drawScaleFactor != 1.0)
            graphics2D.transform( 
                    AffineTransform.getScaleInstance(drawScaleFactor, drawScaleFactor));

        double viewPortHalfWidth = (viewPortWidth / 2) / drawScaleFactor;
        double viewPortHalfHeight = (viewPortHeight / 2) / drawScaleFactor;
        
        if (viewPortWidth < gameEngine.screenWidth 
                || viewPortHeight < gameEngine.screenHeight) {
            originalClipRegion = graphics2D.getClip();
            graphics2D.setClip(
                    (int) (viewPortScreenX/drawScaleFactor-viewPortHalfWidth), 
                    (int) (viewPortScreenY/drawScaleFactor-viewPortHalfHeight), 
                    (int) (2.0*viewPortHalfWidth), (int) (2.0*viewPortHalfHeight));
        }

        for (int bodyIdx = 0; bodyIdx < bodies.size; bodyIdx++) {
            Body body = (Body) bodies.gameObjects[bodyIdx];

            double bodyHalfBound = body.boundingDimension / 2;
            if ((body.x - bodyHalfBound > viewPortLayerX + viewPortHalfWidth 
                    || body.x + bodyHalfBound < viewPortLayerX - viewPortHalfWidth 
                    || body.y - bodyHalfBound > viewPortLayerY + viewPortHalfHeight 
                    || body.y + bodyHalfBound < viewPortLayerY - viewPortHalfHeight) 
                        == false) {
                
                if (body.ticksAtRest > STATIONARY_TICK_THRESHOLD)
                    graphics2D.setColor(Color.GREEN);
                else
                    graphics2D.setColor(Color.RED);
                graphics2D.fillOval(
                        (int) (viewPortScreenX / drawScaleFactor + body.x-4.0 - viewPortLayerX),
                        (int) (viewPortScreenY / drawScaleFactor + body.y-4.0 - viewPortLayerY), 
                        8, 8);
            }
        }

        graphics2D.setColor(Color.GREEN);
        GameObjectCollection joints = this.getGameObjectCollection("Joints");
        for (int jointIdx = 0; jointIdx < joints.size; jointIdx++) {
            Joint joint = (Joint) joints.gameObjects[jointIdx];

            Body body1, body2;
            int x1, y1, x2, y2;

            if (joint.connector == Joint.Connector.BODY) {
                body1 = joint.body1; body2 = joint.body2;
                x1 = (int) body1.x; y1 = (int) body1.y;
                x2 = (int) body2.x; y2 = (int) body2.y;
            } else {
                body1 = joint.shape1.body; body2 = joint.shape2.body;

                double c = Math.cos( body1.rotation );
                double s = Math.sin( body1.rotation );
                double rc1x = c, rc1y = s, rc2x = -s, rc2y = c;
                x1 = (int) (body1.x + rc1x * joint.shape1.offsetX + rc2x * joint.shape1.offsetY);
                y1 = (int) (body1.y + rc1y * joint.shape1.offsetX + rc2y * joint.shape1.offsetY);

                c = Math.cos( body2.rotation );
                s = Math.sin( body2.rotation );
                rc1x = c; rc1y = s; rc2x = -s; rc2y = c;
                x2 = (int) (body2.x + rc1x * joint.shape2.offsetX + rc2x * joint.shape2.offsetY);
                y2 = (int) (body2.y + rc1y * joint.shape2.offsetX + rc2y * joint.shape2.offsetY);
            }

            if (joint instanceof FixedJoint) {
                graphics2D.drawLine(x1, y1, x2, y2);
            } else if (joint instanceof HingedJoint) {
                HingedJoint hingedJoint = (HingedJoint) joint;

                double c = Math.cos( body1.rotation );
                double s = Math.sin( body1.rotation );
                double rc1x = c, rc1y = s, rc2x = -s, rc2y = c;
                int p1x = x1 + (int) (rc1x * hingedJoint.localAnchor1x + rc2x * hingedJoint.localAnchor1y);
                int p1y = y1 + (int) (rc1y * hingedJoint.localAnchor1x + rc2y * hingedJoint.localAnchor1y);

                c = Math.cos( body2.rotation );
                s = Math.sin( body2.rotation );
                rc1x = c; rc1y = s; rc2x = -s; rc2y = c;
                int p2x = x2 + (int) (rc1x * hingedJoint.localAnchor2x + rc2x * hingedJoint.localAnchor2y);
                int p2y = y2 + (int) (rc1y * hingedJoint.localAnchor2x + rc2y * hingedJoint.localAnchor2y);

                graphics2D.drawLine(x1, y1, p1x, p1y);
                graphics2D.drawLine(p1x, p1y, x2, y2);
                graphics2D.drawLine(x2, y2, p2x, p2y);
                graphics2D.drawLine(p2x, p2y, x1, y1);
            }
        }

        for (Arbiter arbiter : arbiters.values()) {
            for (int arbiterIdx = 0; arbiterIdx < arbiter.numContacts; arbiterIdx++) {
                int contactX = (int) arbiter.contacts[arbiterIdx].x;
                int contactY = (int) arbiter.contacts[arbiterIdx].y;

                graphics2D.setColor(Color.RED);
                graphics2D.fillOval(contactX - 4, contactY - 4, 8, 8);
                graphics2D.setColor(Color.DARK_GRAY);
                graphics2D.drawLine(contactX, contactY, 
                        contactX + (int) (arbiter.contacts[arbiterIdx].normalx*10.0), 
                        contactY + (int) (arbiter.contacts[arbiterIdx].normaly*10.0));
            }
        }

        graphics2D.setTransform(originalTransform);
        graphics2D.setClip(originalClipRegion);
    }
}