package game.engine;

import game.geometry.*;
import game.physics.*;
import java.util.*;

/**
 * GameObjectCollider provides access to key game object collision detection
 * methods alongside a number of intersection utility methods. The intersection
 * methods are based on those contained within the physics classes.
 * <P>
 * Note: the code contained within this class has been written with speed in 
 * mind, as such it is not particular elegant.
 *
 * @author <A HREF="mailto:P.Hanna@qub.ac.uk">Philip Hanna</A>
 * @version $Revision: 1 $ $Date: 2007/08 $
 */
public final class GameObjectCollider {

    ///////////////////////////////////////////////////////////////////////////
    // Methods:                                                              //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Determine if the two specified game objects intersect. 
     *
     * @param gameObjectA first GameObject to compare
     * @param gameObjectb second GameObject to compare
     * 
     * @return boolean true if the game objects intersect, otherwise false
     */
    public static final boolean isIntersection(
            GameObject gameObjectA, GameObject gameObjectB) {
        // Immediately return if either of the specified game objects cannot 
        // intersect other game objects
        if (gameObjectA.canIntersectOtherGraphicalObjects == false 
                || gameObjectB.canIntersectOtherGraphicalObjects == false) {
            return false;
        }

        // Perform an intersection test based on the game objects bounding
        // region, i.e. if there is not possibility of the game objects
        // colliding then immediately return.
        double halfBoundA = gameObjectA.boundingDimension / 2;
        double halfBoundB = gameObjectB.boundingDimension / 2;
        if (gameObjectA.x - halfBoundA > gameObjectB.x + halfBoundB 
                || gameObjectA.x + halfBoundA < gameObjectB.x - halfBoundB 
                || gameObjectA.y - halfBoundA > gameObjectB.y + halfBoundB 
                || gameObjectA.y + halfBoundA < gameObjectB.y - halfBoundB) {
            return false;
        }

        // Compare each shape in the first game object's geometry against each
        // shape in the second game object's geometry, returning as soon as two
        // shapes overlap. Note: this can be an expensive operation if both 
        // game objects contain a complex geometry.
        boolean intersects = false;
        for (int shapeIdxA = 0; !intersects && shapeIdxA 
                < gameObjectA.geometry.length; shapeIdxA++) {
            for (int shapeIdxB = 0; !intersects && shapeIdxB 
                    < gameObjectB.geometry.length; shapeIdxB++) {
                
                // Determine if the shape within each game object is a box 
                // or a circle and call the appropriate intersection method
                if (gameObjectA.geometry[shapeIdxA] instanceof Box) {
                    if (gameObjectB.geometry[shapeIdxB] instanceof Box) {
                        intersects = testRectangleToRectangleIntersection(
                                gameObjectA, (Box) gameObjectA.geometry[shapeIdxA], 
                                gameObjectB, (Box) gameObjectB.geometry[shapeIdxB]);
                    } else {
                        intersects = testRectangleToCircleIntersection(
                                gameObjectA, (Box) gameObjectA.geometry[shapeIdxA], 
                                gameObjectB, (Circle) gameObjectB.geometry[shapeIdxB]);
                    }
                } else {
                    if (gameObjectB.geometry[shapeIdxB] instanceof Box) {
                        intersects = testRectangleToCircleIntersection(
                                gameObjectB, (Box) gameObjectB.geometry[shapeIdxB], 
                                gameObjectA, (Circle) gameObjectA.geometry[shapeIdxA]);
                    } else {
                        intersects = testCircleToCircleIntersection(
                                gameObjectA, (Circle) gameObjectA.geometry[shapeIdxA], 
                                gameObjectB, (Circle) gameObjectB.geometry[shapeIdxB]);
                    }
                }
            }
        }
        return intersects;        
    }
    
    /**
     * Determine if the specified shapes in each of the specified game objects
     * intersect.
     *
     * @param gameObjectA first GameObject to compare
     * @param shapeA Shape of the first GameObject to compare
     * @param gameObjectb second GameObject to compare
     * @param shapeB Shape of the second GameObject to compare
     * 
     * @return boolean true if the shapes of the game objects intersect, 
     *         otherwise false
     */
    public static final boolean isIntersection(
            GameObject gameObjectA, Shape shapeA, GameObject gameObjectB, Shape shapeB) {
        // Determine if the two specified shapes are boxes or circles and call the 
        // appropriate intersection method
        if (shapeA instanceof Box) {
            if (shapeB instanceof Box) {
                return testRectangleToRectangleIntersection(
                        gameObjectA, (Box) shapeA, gameObjectB, (Box) shapeB);
            } else {
                return testRectangleToCircleIntersection(
                        gameObjectA, (Box) shapeA, gameObjectB, (Circle) shapeB);
            }
        } else {
            if (shapeB instanceof Box) {
                return testRectangleToCircleIntersection(
                        gameObjectB, (Box) shapeB, gameObjectA, (Circle) shapeA);
            } else {
                return testCircleToCircleIntersection(
                        gameObjectA, (Circle) shapeA, gameObjectB, (Circle) shapeB);
            }
        }
    }

    /**
     * Determine the total region of overlap between the two specified game
     * objects.
     * <P>
     * This method will test all defined geometry elements in both bodies and
     * return an array list array containing pairs of overlapping shapes, e.g.
     * the return, ArrayList<Shape>[], is an array containing two 
     * ArrayList<Shape> collections where the ith Shape of ArrayList<Shape>[0]
     * intersects with the shape at ArrayList<Shape>[1].
     * <P>
     * Note if either specified game object cannot intersect with other 
     * game objects then an empty (but not null) set of array lists will be
     * returned. Likewise if the specified game objects do not overlap then
     * an empty set of array lists will be returned.
     *
     * @param gameObjectA first GameObject to compare
     * @param gameObjectb second GameObject to compare
     * @return ArrayList<Shape>[] of all overlapping shapes
     */
    public static final ArrayList<Shape>[] getIntersection(
            GameObject gameObjectA, GameObject gameObjectB) {
        ArrayList<Shape>[] overlappingShapes 
                = new ArrayList[]{new ArrayList<Shape>(), new ArrayList<Shape>()};

        // Immediately return if either of the specified game objects cannot 
        // intersect other game objects
        if (gameObjectA.canIntersectOtherGraphicalObjects == false ||
                gameObjectB.canIntersectOtherGraphicalObjects == false) {
            return overlappingShapes;
        }
        
        // Perform an intersection test based on the game objects bounding
        // region, i.e. if there is not possibility of the game objects
        // colliding then immediately return.
        double halfBoundA = gameObjectA.boundingDimension / 2;
        double halfBoundB = gameObjectB.boundingDimension / 2;
        if (gameObjectA.x - halfBoundA > gameObjectB.x + halfBoundB 
                || gameObjectA.x + halfBoundA < gameObjectB.x - halfBoundB 
                || gameObjectA.y - halfBoundA > gameObjectB.y + halfBoundB 
                || gameObjectA.y + halfBoundA < gameObjectB.y - halfBoundB) {
            return overlappingShapes;
        }

        // Compare each shape in the first game object's geometry against each
        // shape in the second game object's geometry. If the shapes overlap 
        // then add then to the array lists to be returned.
        for (int shapeIdxA = 0; shapeIdxA < gameObjectA.geometry.length; shapeIdxA++) {
            for (int shapeIdxB = 0; shapeIdxB < gameObjectB.geometry.length; shapeIdxB++) {
                Shape shapeA = gameObjectA.geometry[shapeIdxA];
                Shape shapeB = gameObjectB.geometry[shapeIdxB];

                if (isIntersection(gameObjectA, shapeA, gameObjectB, shapeB)) {
                    overlappingShapes[0].add(shapeA);
                    overlappingShapes[1].add(shapeB);
                }
            }
        }
        
        return overlappingShapes;
    }

    /**
     * Separate the two specified game objects.
     * 
     * Note if either specified game object cannot intersect with other 
     * game objects then an empty (but not null) set of array lists will be
     * returned. Likewise if the specified game objects do not overlap then
     * an empty set of array lists will be returned.
     *
     * @param gameObjectA first GameObject to compare
     * @param gameObjectb second GameObject to compare
     * @return ArrayList<Shape>[] of all overlapping shapes
     */
    public static final boolean separate(
            GameObject gameObjectA, GameObject gameObjectB, double updatePeriodScale) {

        // Store the input x, y and rotation values of both game objects
        double origAx = gameObjectA.x, origAy = gameObjectA.y;         
        double origBx = gameObjectB.x, origBy = gameObjectB.y;
        double origArot = gameObjectA.rotation ,origBrot = gameObjectB.rotation;

        // Define the maximum separation correction for the game objects based upon
        // the game object's current velocities and the specified update period scale,
        // i.e. the update period should reflect the extent to which the defined
        // velocities change the position and rotation during the last update tick.
        double stepAx = gameObjectA.velocityx / updatePeriodScale;
        double stepAy = gameObjectA.velocityy / updatePeriodScale;
        double stepArot = gameObjectA.angularVelocity / updatePeriodScale;

        double stepBx = gameObjectB.velocityx / updatePeriodScale;
        double stepBy = gameObjectB.velocityy / updatePeriodScale;
        double stepBrot = gameObjectB.angularVelocity / updatePeriodScale;

        // The determine maximum step correction will be further sub-divide into
        // smaller corrections as a means of determining the best correction
        // that will only just separate the two game objects.
        double bestSubStep = -1.0;
        double subStep = 0.0;
        double direction = -2.0;

        // Iterate, using binary division of the substep to determine the 
        // best substep correction that only just separates the two game
        // objects.
        int curItr = 1;
        int MAX_ITR = 10;
        while (curItr < MAX_ITR) {
            subStep += direction / Math.pow(2.0, curItr++);

            // 'Correct' the object's position and rotation
            gameObjectA.x = origAx + stepAx * subStep;
            gameObjectA.y = origAy + stepAy * subStep;
            gameObjectA.rotation = origArot + stepArot * subStep;

            gameObjectB.x = origBx + stepBx * subStep;
            gameObjectB.y = origBy + stepBy * subStep;
            gameObjectB.rotation = origBrot + stepBrot * subStep;

            // Test if this 'correction' has been successful and, if so,
            // store it as the best correction so far determined. If
            // the correction was not successful, then invert the 
            // direction of correction.
            if (isIntersection(gameObjectA, gameObjectB)) {
                direction = -1.0;
            } else {
                bestSubStep = subStep;
                direction = 1.0;
            }
        }

        // Correct the game objects based on the best found correction
        gameObjectA.x = origAx + stepAx * bestSubStep;
        gameObjectA.y = origAy + stepAy * bestSubStep;
        gameObjectA.rotation = origArot + stepArot * bestSubStep;

        gameObjectB.x = origBx + stepBx * bestSubStep;
        gameObjectB.y = origBy + stepBy * bestSubStep;
        gameObjectB.rotation = origBrot + stepBrot * bestSubStep;

        // If it was not possible to separate the objects within the 
        // defined updatePeriodScale and the current number of corrective
        // iterations, then return false, otherwise return true
        if (bestSubStep > -1.0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if the two specified bodies are currently touching, i.e. if
     * they share a common arbiter.
     *
     * @param bodyA first Body to compare
     * @param bodyB second Body to compare
     * @return boolean true if the two bodies are touching, otherwise false
     */
    public static final boolean determineIfBodiesTouching(
            Body bodyA, Body bodyB) {
        // Immediately return if either of the specified bodies cannot 
        // intersect other game objects
        if (bodyA.canIntersectOtherGraphicalObjects == false 
                || bodyB.canIntersectOtherGraphicalObjects == false) {
            return false;
        }
        
        // Perform an intersection test based on the bodies bounding regions.
        // Note: the bounding regions are slightly increased within this method
        // to account for the possibility of two bodies being in contact but
        // not actually overlapping
        double delta = 10.0;
        double halfBoundA = bodyA.boundingDimension / 2 + delta;
        double halfBoundB = bodyB.boundingDimension / 2 + delta;
        if (bodyA.x - halfBoundA > bodyB.x + halfBoundB 
                || bodyA.x + halfBoundA < bodyB.x - halfBoundB 
                || bodyA.y - halfBoundA > bodyB.y + halfBoundB 
                || bodyA.y + halfBoundA < bodyB.y - halfBoundB) {
            return false;
        }
        
        // Iterate through all defined arbiters within the collision space 
        // associated with the bodies and break as soon as any arbiter is 
        // found that shares the two bodies.
        boolean contact = false;
        for (Arbiter arbiter : ((CollisionSpace) bodyA.gameLayer).arbiters.values()) {
            if (arbiter.body1.uniqueGameObjectID == bodyA.uniqueGameObjectID 
                        && arbiter.body2.uniqueGameObjectID == bodyB.uniqueGameObjectID 
                    || arbiter.body1.uniqueGameObjectID == bodyB.uniqueGameObjectID 
                        && arbiter.body2.uniqueGameObjectID == bodyA.uniqueGameObjectID) {
                contact = true;
            }
            if (contact) {
                break;
            }
        }
        return contact;
    }

    /**
     * Determine if the two box shapes of the specified game objects intersect
     *
     * @param gameObjectA first GameObject to compare
     * @param boxA Box shape of the first GameObject to compare
     * @param gameObjectb second GameObject to compare
     * @param boxB Box shape of the second GameObject to compare
     * 
     * @return boolean true if the boxes of the game objects intersect, 
     *         otherwise false
     */
    private static final boolean testRectangleToRectangleIntersection(
            GameObject gameObjectA, Box boxA, GameObject gameObjectB, Box boxB) {

        // Precalculate half widths and heights of each of the boxes
        double hAx = boxA.width * 0.5, hAy = boxA.height * 0.5;
        double hBx = boxB.width * 0.5, hBy = boxB.height * 0.5;

        // Precalculate a rotation matrix for the first game object
        double c = Math.cos(gameObjectA.rotation);
        double s = Math.sin(gameObjectA.rotation);
        double rotAc1x = c, rotAc1y = s, rotAc2x = -s, rotAc2y = c;

        // Precalculate a rotation matrix for the second game object
        c = Math.cos(gameObjectB.rotation);
        s = Math.sin(gameObjectB.rotation);
        double rotBc1x = c, rotBc1y = s, rotBc2x = -s, rotBc2y = c;

        // Determine the center points of both boxes based upon the center point
        // of the game object combined with the rotated box offset.
        double posAx = gameObjectA.x + (rotAc1x * boxA.offsetX + rotAc2x * boxA.offsetY);
        double posAy = gameObjectA.y + (rotAc1y * boxA.offsetX + rotAc2y * boxA.offsetY);
        double posBx = gameObjectB.x + (rotBc1x * boxB.offsetX + rotBc2x * boxB.offsetY);
        double posBy = gameObjectB.y + (rotBc1y * boxB.offsetX + rotBc2y * boxB.offsetY);

        // Determine the inverse rotated vector between both box centers
        double dpx = posBx - posAx, dpy = posBy - posAy;
        double dAx = rotAc1x * dpx + rotAc1y * dpy;
        double dAy = rotAc2x * dpx + rotAc2y * dpy;
        double dBx = rotBc1x * dpx + rotBc1y * dpy;
        double dBy = rotBc2x * dpx + rotBc2y * dpy;
        
        double absCc1x = rotAc1x * rotBc1x + rotAc1y * rotBc1y;
        if (absCc1x < 0.0) absCc1x = -absCc1x;
        double absCc1y = rotAc2x * rotBc1x + rotAc2y * rotBc1y;
        if (absCc1y < 0.0) absCc1y = -absCc1y;
        double absCc2x = rotAc1x * rotBc2x + rotAc1y * rotBc2y;
        if (absCc2x < 0.0) absCc2x = -absCc2x;
        double absCc2y = rotAc2x * rotBc2x + rotAc2y * rotBc2y;
        if (absCc2y < 0.0) absCc2y = -absCc2y;
        
        // Consider each of the faces on the first box
        double faceAx = (dAx < 0.0 ? -dAx : dAx) - hAx - (absCc1x * hBx + absCc2x * hBy);
        double faceAy = (dAy < 0.0 ? -dAy : dAy) - hAy - (absCc1y * hBx + absCc2y * hBy);
        if (faceAx > 0.0 || faceAy > 0.0)
            return false;

        // Consider each of the faces on the second box
        double faceBx = (dBx < 0.0 ? -dBx : dBx) - (absCc1x * hAx + absCc1y * hAy) - hBx;
        double faceBy = (dBy < 0.0 ? -dBy : dBy) - (absCc2x * hAx + absCc2y * hAy) - hBy;
        if (faceBx > 0.0 || faceBy > 0.0)
            return false;

        return true;
    }

    /**
     * Determine if the specified box and circle intersect
     *
     * @param gameObjectA first GameObject to compare
     * @param boxA Box shape of the first GameObject to compare
     * @param gameObjectb second GameObject to compare
     * @param circleB Circle shape of the second GameObject to compare
     * 
     * @return boolean true if the box and circle of the game objects intersect, 
     *         otherwise false
     */
    private static final boolean testRectangleToCircleIntersection(
            GameObject gameObjectA, Box boxA, GameObject gameObjectB, Circle circleB) {
        double vecx, vecy;
        double locx, locy;
        double vl, vx, vy;
        double locp, contactPointx, contactPointy;
        double projx, projy, otherx ,othery;

        // Precalculate a rotation matrix for the first game object
        double c = Math.cos(gameObjectA.rotation);
        double s = Math.sin(gameObjectA.rotation);
        double rotAc1x = c, rotAc1y = s, rotAc2x = -s, rotAc2y = c;

        // Determine the center points of the box based upon the center point
        // of the game object combined with the rotated box offset.
        double x1 = gameObjectA.x + (rotAc1x * boxA.offsetX + rotAc2x * boxA.offsetY);
        double y1 = gameObjectA.y + (rotAc1y * boxA.offsetX + rotAc2y * boxA.offsetY);

        // Work out position of each box corner - returned as array of 4 [x,y] points
        double[] boxPts = new double[8];
        double hx = boxA.width * 0.5;
        double hy = boxA.height * 0.5;
        boxPts[0] = (rotAc1x * -hx + rotAc2x * -hy) + x1;
        boxPts[1] = (rotAc1y * -hx + rotAc2y * -hy) + y1;
        boxPts[2] = (rotAc1x * hx + rotAc2x * -hy) + x1;
        boxPts[3] = (rotAc1y * hx + rotAc2y * -hy) + y1;
        boxPts[4] = (rotAc1x * hx + rotAc2x * hy) + x1;
        boxPts[5] = (rotAc1y * hx + rotAc2y * hy) + y1;
        boxPts[6] = (rotAc1x * -hx + rotAc2x * hy) + x1;
        boxPts[7] = (rotAc1y * -hx + rotAc2y * hy) + y1;

        // Precalculate a rotation matrix for the second game object
        c = Math.cos(gameObjectB.rotation);
        s = Math.sin(gameObjectB.rotation);
        double rotBc1x = c, rotBc1y = s, rotBc2x = -s, rotBc2y = c;

        // Work out center of circle and radius^2
        double x2 = gameObjectB.x + (rotBc1x * circleB.offsetX + rotBc2x * circleB.offsetY);
        double y2 = gameObjectB.y + (rotBc1y * circleB.offsetX + rotBc2y * circleB.offsetY);
        double r2 = circleB.radius * circleB.radius;

        // Test each side of the box against the circle for overlap. A count is 
        // kept of the number of tests falling inside the box (if all four tests
        // fall inside the box without any overlap then the circle is completely
        // contained within the box.
        boolean contact = false;
        int numInnerSideTests = 0;

        for (int i = 0; !contact && i < 4; i++) {
            // Extract the current side vector for the box and the circle
            // to box vector
            vecx = boxPts[(i * 2 + 2) % 8] - boxPts[i * 2];
            vecy = boxPts[((i * 2 + 2) % 8) + 1] - boxPts[i * 2 + 1];
            locx = x2 - boxPts[i * 2]; locy = y2 - boxPts[i * 2 + 1];

            // Project the normal onto the box side and determine the 
            // distance from the normal to the centre of the circle
            vl = Math.sqrt(vecx * vecx + vecy * vecy);
            vx = vecx / vl; vy = vecy / vl;
            locp = vx * locx + vy * locy;
            projx = locp * vx;
            projy = locp * vy;

            if (projx * projx + projy * projy > vecx * vecx + vecy * vecy) {
                contactPointx = boxPts[(i * 2 + 2) % 8];
                contactPointy = boxPts[((i * 2 + 2) % 8) + 1];
            } else {
                numInnerSideTests++;

                projx += boxPts[i * 2]; projy += boxPts[i * 2 + 1];
                otherx = projx - boxPts[(i * 2 + 2) % 8];
                othery = projy - boxPts[((i * 2 + 2) % 8) + 1];

                if (otherx * otherx + othery * othery > vecx * vecx + vecy * vecy) {
                    contactPointx = boxPts[i * 2]; contactPointy = boxPts[i * 2 + 1];
                } else {
                    contactPointx = projx; contactPointy = projy;
                }
            }

            // If the distance is less than the radius then we have a hit 
            contactPointx -= x2; contactPointy -= y2;
            double dis = contactPointx * contactPointx + contactPointy * contactPointy;
            if (dis < r2) {
                contact = true;
            }
        }

        // If all four tests are inside the box, then we also have a hit
        if (numInnerSideTests == 4) {
            contact = true;
        }
        
        return contact;
    }

    /**
     * Determine if the two circle shapes of the specified game objects intersect
     *
     * @param gameObjectA first GameObject to compare
     * @param circleA Circle shape of the first GameObject to compare
     * @param gameObjectb second GameObject to compare
     * @param circleB Circle shape of the second GameObject to compare
     * 
     * @return boolean true if the circles of the game objects intersect, 
     *         otherwise false
     */
    private static final boolean testCircleToCircleIntersection(
            GameObject gameObjectA, Circle circleA, GameObject gameObjectB, Circle circleB) {
        // Precalculate a rotation matrix for the both game objects
        double c = Math.cos(gameObjectA.rotation);
        double s = Math.sin(gameObjectA.rotation);
        double rotAc1x = c, rotAc1y = s, rotAc2x = -s, rotAc2y = c;

        c = Math.cos(gameObjectB.rotation);
        s = Math.sin(gameObjectB.rotation);
        double rotBc1x = c, rotBc1y = s, rotBc2x = -s, rotBc2y = c;

        // Determine the center points of both circles based upon the center point
        // of the game object combined with the rotated circle offset.        
        double posAx = gameObjectA.x + (rotAc1x * circleA.offsetX + rotAc2x * circleA.offsetY);
        double posAy = gameObjectA.y + (rotAc1y * circleA.offsetX + rotAc2y * circleA.offsetY);
        double posBx = gameObjectB.x + (rotBc1x * circleB.offsetX + rotBc2x * circleB.offsetY);
        double posBy = gameObjectB.y + (rotBc1y * circleB.offsetX + rotBc2y * circleB.offsetY);

        // Determine if the circles overlap
        if ((circleA.radius + circleB.radius) * (circleA.radius + circleB.radius) 
                < (posAx - posBx) * (posAx - posBx) + (posAy - posBy) * (posAy - posBy)) {
            return false;
        } else {
            return true;
        }
    }
}
