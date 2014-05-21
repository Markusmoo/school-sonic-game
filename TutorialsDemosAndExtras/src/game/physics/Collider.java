package game.physics;

/**
 * The collider class provides the collision detection algorithms that are
 * used by the physics engine.
* <P>
 * Note: The physics engine within this code repository draws upon the
 * work of two individuals. See the CollisionSpace class header for
 * additional information.
 * <P>
 * @version $Revision: 1.0 $ $Date: 2007/08 $
 */

import game.geometry.*;

public final class Collider {

    ///////////////////////////////////////////////////////////////////////////
    // Class data                                                            //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Box collision constants used to record the type of collision.
     * The FACE_A and FACE_B constants identify the x and y coordinates
     * of the first and second face respectively, whilst the NO_EDGE,
     * EDGE1, etc. identify which box edge is involved in the collision.
     */
    protected static final int FACE_A_X = 1;
    protected static final int FACE_A_Y = 2;
    protected static final int FACE_B_X = 3;
    protected static final int FACE_B_Y = 4;

    protected static final int NO_EDGE = 0;
    protected static final int EDGE1 = 1;
    protected static final int EDGE2 = 2;
    protected static final int EDGE3 = 3;
    protected static final int EDGE4 = 4;

    /**
     * The following inlined class describes a simple vertex against which
     * the shape will be clipped
     */
    private static final class ClipVertex {
        /** The vertex */
        double vx, vy;

        /** The pair this clipping applied to */
        int inEdge1, outEdge1;
        int inEdge2, outEdge2;
    }

    /**
     * The remove object creation within the physics engine, a number of
     * incident vertex objects are created to be (re)used by the collider
     * methods.
     */
    private static ClipVertex[] incidentEdge 
            = new ClipVertex[]{new ClipVertex(), new ClipVertex()};
    private static ClipVertex[] clipPoints1 
            = new ClipVertex[]{new ClipVertex(), new ClipVertex()};
    private static ClipVertex[] clipPoints2 
            = new ClipVertex[]{new ClipVertex(), new ClipVertex()};

    
    ///////////////////////////////////////////////////////////////////////////
    // Methods:                                                              //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Clip a line segment against the specified line
     *
     * @param vOut the segment to be clipped
     * @param vIn the line to be clipped against
     * @param normal the normal of the line
     * @param offset the offset from segment to line
     * @param clipEdge the edge against which the clipping is to be applied
     * @return integer containing the number of clipped points
     */

    private static final int clipSegmentToLine(
            ClipVertex[] vOut, ClipVertex[] vIn, 
            double normalx, double normaly, double offset, char clipEdge) {
        // Start with no output points
        int numOut = 0;

        // Calculate the distance of end points to the line
        double distance0 = (normalx * vIn[0].vx + normaly * vIn[0].vy) - offset;
        double distance1 = (normalx * vIn[1].vx + normaly * vIn[1].vy) - offset;

        // Consider if the points are behind the plane
        if (distance0 <= 0.0f) {
            vOut[numOut].vx = vIn[0].vx;
            vOut[numOut].vy = vIn[0].vy;
            vOut[numOut].inEdge1 = vIn[0].inEdge1;
            vOut[numOut].outEdge1 = vIn[0].outEdge1;
            vOut[numOut].inEdge2 = vIn[0].inEdge2;
            vOut[numOut].outEdge2 = vIn[0].outEdge2;
            numOut++;
        }

        if (distance1 <= 0.0f) {
            vOut[numOut].vx = vIn[1].vx;
            vOut[numOut].vy = vIn[1].vy;
            vOut[numOut].inEdge1 = vIn[1].inEdge1;
            vOut[numOut].outEdge1 = vIn[1].outEdge1;
            vOut[numOut].inEdge2 = vIn[1].inEdge2;
            vOut[numOut].outEdge2 = vIn[1].outEdge2;
            numOut++;
        }

        // Consider if the points are on different sides of the plane
        if (distance0 * distance1 < 0.0f) {
            // Find intersection point of edge and plane
            double interp = distance0 / (distance0 - distance1);

            vOut[numOut].vx = (vIn[1].vx - vIn[0].vx) * interp;
            vOut[numOut].vy = (vIn[1].vy - vIn[0].vy) * interp;

            vOut[numOut].vx += vIn[0].vx;
            vOut[numOut].vy += vIn[0].vy;

            if (distance0 > 0.0f) {
                vOut[numOut].inEdge1 = clipEdge;
                vOut[numOut].outEdge1 = vIn[0].outEdge1;
                vOut[numOut].inEdge2 = NO_EDGE;
                vOut[numOut].outEdge2 = vIn[0].outEdge2;
            } else {
                vOut[numOut].inEdge1 = vIn[1].inEdge1;
                vOut[numOut].outEdge1 = clipEdge;
                vOut[numOut].inEdge2 = vIn[1].inEdge2;
                vOut[numOut].outEdge2 = NO_EDGE;
            }
            ++numOut;
        }

        return numOut;
    }

    /**
     * Compute the incident edge for the given box-to-box collision (the
     * class clipPoints1, clipPoints2, etc. will be used/updated as part
     * of this method).
     *
     * @param hx box x half width
     * @param hy box y half height
     * @param posx box x position
     * @param posy box y position
     * @param rotc1x box rotation matrix component
     * @param rotc1y box rotation matrix component
     * @param rotc2x box rotation matrix component
     * @param rotc2y box rotation matrix component
     * @param normalx box outwards x normal
     * @param normaly box outwards y normal
     */    
    private static final void computeIncidentEdge(
            double hx, double hy, double posx, double posy, 
            double rotc1x, double rotc1y, double rotc2x, double rotc2y, 
            double normalx, double normaly) {
        
        // The normal is from the reference box. Convert it
        // to the incident boxe's frame and flip sign.
        double nx = (rotc1x * normalx + rotc1y * normaly) * -1.0;
        double ny = (rotc2x * normalx + rotc2y * normaly) * -1.0;

        double nAbsx = nx < 0.0 ? -nx : nx;
        double nAbsy = ny < 0.0 ? -ny : ny;

        if (nAbsx > nAbsy) {
            if (nx > 0.0) {
                incidentEdge[0].vx = hx;
                incidentEdge[0].vy = -hy;
                incidentEdge[0].inEdge2 = EDGE3;
                incidentEdge[0].outEdge2 = EDGE4;

                incidentEdge[1].vx = hx;
                incidentEdge[1].vy = hy;
                incidentEdge[1].inEdge2 = EDGE4;
                incidentEdge[1].outEdge2 = EDGE1;
            } else {
                incidentEdge[0].vx = -hx;
                incidentEdge[0].vy = hy;
                incidentEdge[0].inEdge2 = EDGE1;
                incidentEdge[0].outEdge2 = EDGE2;

                incidentEdge[1].vx = -hx;
                incidentEdge[1].vy = -hy;
                incidentEdge[1].inEdge2 = EDGE2;
                incidentEdge[1].outEdge2 = EDGE3;
            }
        } else {
            if (ny > 0.0) {
                incidentEdge[0].vx = hx;
                incidentEdge[0].vy = hy;
                incidentEdge[0].inEdge2 = EDGE4;
                incidentEdge[0].outEdge2 = EDGE1;

                incidentEdge[1].vx = -hx;
                incidentEdge[1].vy = hy;
                incidentEdge[1].inEdge2 = EDGE1;
                incidentEdge[1].outEdge2 = EDGE2;
            } else {
                incidentEdge[0].vx = -hx;
                incidentEdge[0].vy = -hy;
                incidentEdge[0].inEdge2 = EDGE2;
                incidentEdge[0].outEdge2 = EDGE3;

                incidentEdge[1].vx = hx;
                incidentEdge[1].vy = -hy;
                incidentEdge[1].inEdge2 = EDGE3;
                incidentEdge[1].outEdge2 = EDGE4;
            }
        }

        double newincidentEdgex = (rotc1x * incidentEdge[0].vx 
                + rotc2x * incidentEdge[0].vy) + posx;
        double newincidentEdgey = (rotc1y * incidentEdge[0].vx 
                + rotc2y * incidentEdge[0].vy) + posy;
        incidentEdge[0].vx = newincidentEdgex;
        incidentEdge[0].vy = newincidentEdgey;

        newincidentEdgex = (rotc1x * incidentEdge[1].vx 
                + rotc2x * incidentEdge[1].vy) + posx;
        newincidentEdgey = (rotc1y * incidentEdge[1].vx 
                + rotc2y * incidentEdge[1].vy) + posy;
        incidentEdge[1].vx = newincidentEdgex;
        incidentEdge[1].vy = newincidentEdgey;
    }

    /**
     * Consider a collision between the two specified boxes belonging
     * to bodies associated with the specifed arbiter.
     *
     * @param arbiter Arbiter containing the bodies
     * @param boxA first box to be considered within the collision
     * @param boxB second box to be considered within the collision
     */        
    public static final int collide(Arbiter arbiter, Box boxA, Box boxB) {
        // Setup and create the varibles needed within the collider
        double hAx = boxA.width * 0.5, hAy = boxA.height * 0.5;
        double hBx = boxB.width * 0.5, hBy = boxB.height * 0.5;

        double c = Math.cos(arbiter.body1.rotation);
        double s = Math.sin(arbiter.body1.rotation);
        double rotAc1x = c, rotAc1y = s, rotAc2x = -s, rotAc2y = c;

        c = Math.cos(arbiter.body2.rotation);
        s = Math.sin(arbiter.body2.rotation);
        double rotBc1x = c, rotBc1y = s, rotBc2x = -s, rotBc2y = c;

        double posAx = arbiter.body1.x + (rotAc1x * boxA.offsetX + rotAc2x * boxA.offsetY);
        double posAy = arbiter.body1.y + (rotAc1y * boxA.offsetX + rotAc2y * boxA.offsetY);
        double posBx = arbiter.body2.x + (rotBc1x * boxB.offsetX + rotBc2x * boxB.offsetY);
        double posBy = arbiter.body2.y + (rotBc1y * boxB.offsetX + rotBc2y * boxB.offsetY);

        double dpx = posBx - posAx, dpy = posBy - posAy;

        double dAx = rotAc1x * dpx + rotAc1y * dpy, dAy = rotAc2x * dpx + rotAc2y * dpy;
        double dBx = rotBc1x * dpx + rotBc1y * dpy, dBy = rotBc2x * dpx + rotBc2y * dpy;

        double absCc1x = rotAc1x * rotBc1x + rotAc1y * rotBc1y;
        if (absCc1x < 0.0) absCc1x = -absCc1x;
        double absCc1y = rotAc2x * rotBc1x + rotAc2y * rotBc1y;
        if (absCc1y < 0.0) absCc1y = -absCc1y;
        double absCc2x = rotAc1x * rotBc2x + rotAc1y * rotBc2y;
        if (absCc2x < 0.0) absCc2x = -absCc2x;
        double absCc2y = rotAc2x * rotBc2x + rotAc2y * rotBc2y;
        if (absCc2y < 0.0) absCc2y = -absCc2y;

        // Consider the faces of box A and box B, returning if there is no collision
        double faceAx = (dAx < 0.0 ? -dAx : dAx) - hAx - (absCc1x * hBx + absCc2x * hBy);
        double faceAy = (dAy < 0.0 ? -dAy : dAy) - hAy - (absCc1y * hBx + absCc2y * hBy);
        if (faceAx > 0.0 || faceAy > 0.0) return 0;

        double faceBx = (dBx < 0.0 ? -dBx : dBx) - (absCc1x * hAx + absCc1y * hAy) - hBx;
        double faceBy = (dBy < 0.0 ? -dBy : dBy) - (absCc2x * hAx + absCc2y * hAy) - hBy;
        if (faceBx > 0.0 || faceBy > 0.0) return 0;

        // Find the best axis
        int axis;
        double separation, normalx, normaly;

        // Consider the faces of box A
        axis = FACE_A_X;
        separation = faceAx;
        if (dAx > 0) {
            normalx = rotAc1x; normaly = rotAc1y;
        } else {
            normalx = rotAc1x * -1.0; normaly = rotAc1y * -1.0;
        }

        if (faceAy > 1.05 * separation + 0.01 * hAy) {
            axis = FACE_A_Y;
            separation = faceAy;
            if (dAy > 0) {
                normalx = rotAc2x; normaly = rotAc2y;
            } else {
                normalx = rotAc2x * -1.0; normaly = rotAc2y * -1.0;
            }
        }

        // Consider the faces of  box B
        if (faceBx > 1.05 * separation + 0.01 * hBx) {
            axis = FACE_B_X;
            separation = faceBx;
            if (dBx > 0) {
                normalx = rotBc1x; normaly = rotBc1y;
            } else {
                normalx = rotBc1x * -1.0; normaly = rotBc1y * -1.0;
            }
        }

        if (faceBy > 1.05 * separation + 0.01 * hBy) {
            axis = FACE_B_Y;
            separation = faceBy;
            if (dBy > 0) {
                normalx = rotBc2x; normaly = rotBc2y;
            } else {
                normalx = rotBc2x * -1.0; normaly = rotBc2y * -1.0;
            }
        }

        // Setup clipping plane data based on the separating axis
        double frontNormalx, frontNormaly, sideNormalx, sideNormaly;
        double front, negSide, posSide;
        char negEdge, posEdge;

        // Compute the clipping lines and the line segment to be clipped.
        switch (axis) {
            case FACE_A_X:
                {
                    frontNormalx = normalx; frontNormaly = normaly;
                    front = (posAx * frontNormalx + posAy * frontNormaly) + hAx;
                    sideNormalx = rotAc2x; sideNormaly = rotAc2y;

                    double side = posAx * sideNormalx + posAy * sideNormaly;
                    negSide = -side + hAy; posSide = side + hAy;
                    negEdge = EDGE3; posEdge = EDGE1;
                    computeIncidentEdge(hBx, hBy, posBx, posBy, 
                            rotBc1x, rotBc1y, rotBc2x, rotBc2y, 
                            frontNormalx, frontNormaly);
                }
                break;
            case FACE_A_Y:
                {
                    frontNormalx = normalx; frontNormaly = normaly;
                    front = (posAx * frontNormalx + posAy * frontNormaly) + hAy;
                    sideNormalx = rotAc1x; sideNormaly = rotAc1y;

                    double side = posAx * sideNormalx + posAy * sideNormaly;
                    negSide = -side + hAx; posSide = side + hAx;
                    negEdge = EDGE2; posEdge = EDGE4;
                    computeIncidentEdge(hBx, hBy, posBx, posBy, 
                            rotBc1x, rotBc1y, rotBc2x, rotBc2y, 
                            frontNormalx, frontNormaly);
                }
                break;
            case FACE_B_X:
                {
                    frontNormalx = normalx * -1.0; frontNormaly = normaly * -1.0;
                    front = (posBx * frontNormalx + posBy * frontNormaly) + hBx;
                    sideNormalx = rotBc2x; sideNormaly = rotBc2y;

                    double side = posBx * sideNormalx + posBy * sideNormaly;
                    negSide = -side + hBy; posSide = side + hBy;
                    negEdge = EDGE3; posEdge = EDGE1;
                    computeIncidentEdge(hAx, hAy, posAx, posAy, 
                            rotAc1x, rotAc1y, rotAc2x, rotAc2y, 
                            frontNormalx, frontNormaly);
                }
                break;
            case FACE_B_Y:
                {
                    frontNormalx = normalx * -1.0; frontNormaly = normaly * -1.0;
                    front = (posBx * frontNormalx + posBy * frontNormaly) + hBy;
                    sideNormalx = rotBc1x; sideNormaly = rotBc1y;

                    double side = posBx * sideNormalx + posBy * sideNormaly;
                    negSide = -side + hBx; posSide = side + hBx;
                    negEdge = EDGE2; posEdge = EDGE4;
                    computeIncidentEdge(hAx, hAy, posAx, posAy, 
                            rotAc1x, rotAc1y, rotAc2x, rotAc2y, 
                            frontNormalx, frontNormaly);
                }
                break;
            default:
                throw new IllegalStateException("Collider.collider: " +
                        "Unknown face found.");
        }

        // Clip to box side 1
        int np = clipSegmentToLine(clipPoints1, incidentEdge, 
                sideNormalx * -1.0, sideNormaly * -1.0, negSide, negEdge);
        if (np < 2) return 0;

        // Clip to negative box side 1
        np = clipSegmentToLine(clipPoints2, clipPoints1, 
                sideNormalx, sideNormaly, posSide, posEdge);
        if (np < 2) return 0;

        // Now clipPoints2 contains the clipping points.
        // Due to roundoff, it is possible that clipping removes all points.
        int numContacts = 0;
        for (int i = 0; i < 2; ++i) {
            double separation2 = (frontNormalx * clipPoints2[i].vx 
                    + frontNormaly * clipPoints2[i].vy) - front;

            if (separation2 <= 0) {
                Contact contact = arbiter.getNextAvailableContact(numContacts);

                contact.shape1 = boxA; contact.shape2 = boxB;

                contact.separation = separation2;
                contact.normalx = normalx; contact.normaly = normaly;

                // Slide contact point onto reference face (easy to cull)
                double contactPositionx = clipPoints2[i].vx - frontNormalx * separation2;
                double contactPositiony = clipPoints2[i].vy - frontNormaly * separation2;
                contact.x = contactPositionx; contact.y = contactPositiony;

                contact.inEdge1 = clipPoints2[i].inEdge1;
                contact.outEdge1 = clipPoints2[i].outEdge1;
                contact.inEdge2 = clipPoints2[i].inEdge2;
                contact.outEdge2 = clipPoints2[i].outEdge2;

                if (axis == FACE_B_X || axis == FACE_B_Y) {
                    int temp = contact.inEdge1;
                    contact.inEdge1 = contact.inEdge2; contact.inEdge2 = temp;
                    temp = contact.outEdge1;
                    contact.outEdge1 = contact.outEdge2; contact.outEdge2 = temp;
                }

                ++numContacts;
            }
        }

        return numContacts;
    }

    /**
     * Consider a collision between the specified box and circle shapes
     * of bodies associated with the specifed arbiter.
     *
     * @param arbiter Arbiter containing the bodies
     * @param box box to be considered within the collision
     * @param circle to be considered within the collision
     */        
    public static final int collide(Arbiter arbiter, Box box, Circle circle) {
        // Setup and create the varibles needed within the collider        
        double vecx, vecy, locx, locy;
        double vl, vx, vy, locp;
        double contactPointx, contactPointy, projx, projy;
        double otherx, othery;

        double c = Math.cos(arbiter.body1.rotation);
        double s = Math.sin(arbiter.body1.rotation);
        double rotAc1x = c, rotAc1y = s, rotAc2x = -s, rotAc2y = c;

        c = Math.cos(arbiter.body2.rotation);
        s = Math.sin(arbiter.body2.rotation);
        double rotBc1x = c, rotBc1y = s, rotBc2x = -s, rotBc2y = c;

        double x1 = arbiter.body1.x + (rotAc1x * box.offsetX + rotAc2x * box.offsetY);
        double y1 = arbiter.body1.y + (rotAc1y * box.offsetX + rotAc2y * box.offsetY);
        double x2 = arbiter.body2.x + (rotBc1x * circle.offsetX + rotBc2x * circle.offsetY);
        double y2 = arbiter.body2.y + (rotBc1y * circle.offsetX + rotBc2y * circle.offsetY);

        double[] pts = getPoints(box, x1, y1, arbiter.body1.rotation);

        double r2 = circle.radius * circle.radius;

        boolean edgeOverlap = false;
        int numInnerSideTests = 0;
        int closestPoint = -1;
        double closestDistance = Double.MAX_VALUE;

        // Consider each face on the square
        for (int i = 0; i < 4; i++) {
            vecx = pts[(i * 2 + 2) % 8] - pts[i * 2];
            vecy = pts[((i * 2 + 2) % 8) + 1] - pts[i * 2 + 1];

            locx = x2 - pts[i * 2]; locy = y2 - pts[i * 2 + 1];

            vl = Math.sqrt(vecx * vecx + vecy * vecy);
            vx = vecx / vl; vy = vecy / vl;

            locp = vx * locx + vy * locy;
            projx = locp * vx; projy = locp * vy;
            
            if (projx * projx + projy * projy > vecx * vecx + vecy * vecy) {
                contactPointx = pts[(i * 2 + 2) % 8];
                contactPointy = pts[((i * 2 + 2) % 8) + 1];
            } else {
                numInnerSideTests++;

                projx += pts[i * 2]; projy += pts[i * 2 + 1];

                otherx = projx - pts[(i * 2 + 2) % 8];
                othery = projy - pts[((i * 2 + 2) % 8) + 1];

                if (otherx * otherx + othery * othery > vecx * vecx + vecy * vecy) {
                    contactPointx = pts[i * 2]; contactPointy = pts[i * 2 + 1];
                } else {
                    contactPointx = projx; contactPointy = projy;
                }
            }

            contactPointx -= x2; contactPointy -= y2;

            double dis = contactPointx * contactPointx + contactPointy * contactPointy;
            if (dis < closestDistance) {
                closestDistance = dis;
                closestPoint = i;
            }

            if (dis < r2) {
                edgeOverlap = true;
            }
        }

        if (edgeOverlap || numInnerSideTests == 4) {
            Contact contact = arbiter.getNextAvailableContact(0);

            contact.shape1 = box;
            contact.shape2 = circle;

            double dis = Math.sqrt(closestDistance);
            contact.separation = dis - circle.radius;

            vecx = pts[(closestPoint * 2 + 2) % 8] - pts[closestPoint * 2];
            vecy = pts[((closestPoint * 2 + 2) % 8) + 1] - pts[closestPoint * 2 + 1];

            locx = x2 - pts[closestPoint * 2]; locy = y2 - pts[closestPoint * 2 + 1];

            vl = Math.sqrt(vecx * vecx + vecy * vecy);
            vx = vecx / vl; vy = vecy / vl;

            locp = vx * locx + vy * locy;
            projx = locp * vx; projy = locp * vy;

            if (projx * projx + projy * projy > vecx * vecx + vecy * vecy) {
                contactPointx = pts[(closestPoint * 2 + 2) % 8];
                contactPointy = pts[((closestPoint * 2 + 2) % 8) + 1];
            } else {
                projx += pts[closestPoint * 2];
                projy += pts[closestPoint * 2 + 1];

                otherx = projx - pts[(closestPoint * 2 + 2) % 8];
                othery = projy - pts[((closestPoint * 2 + 2) % 8) + 1];

                if (otherx * otherx + othery * othery > vecx * vecx + vecy * vecy) {
                    contactPointx = pts[closestPoint * 2];
                    contactPointy = pts[closestPoint * 2 + 1];
                } else {
                    contactPointx = projx; contactPointy = projy;
                }
            }

            double normalx = x2 - contactPointx, normaly = y2 - contactPointy;
            double normall = Math.sqrt(normalx * normalx + normaly * normaly);
            normalx /= normall; normaly /= normall;

            contact.normalx = normalx; contact.normaly = normaly;
            contact.x = contactPointx; contact.y = contactPointy;

            contact.inEdge1 = 0; contact.outEdge1 = 0;
            contact.inEdge2 = 0; contact.outEdge2 = 0;

            if (numInnerSideTests == 4) {
                contact.normalx = -contact.normalx;
                contact.normaly = -contact.normaly;

                if (edgeOverlap == false) {
                    contact.separation = -contact.separation - 2.0 * circle.radius;
                }
            }

            return 1;
        }

        return 0;
    }

    /**
     * Consider a collision between the specified circle and box shapes
     * of bodies associated with the specifed arbiter.
     *
     * @param arbiter Arbiter containing the bodies
     * @param circle to be considered within the collision
     * @param box box to be considered within the collision
     */        
    public static final int collide(Arbiter arbiter, Circle circle, Box box) {
        // Setup and create the varibles needed within the collider        
        double vecx, vecy, locx, locy;
        double vl, vx, vy, locp;
        double contactPointx, contactPointy, projx, projy;
        double otherx, othery;

        double c = Math.cos(arbiter.body2.rotation);
        double s = Math.sin(arbiter.body2.rotation);
        double rotAc1x = c, rotAc1y = s, rotAc2x = -s, rotAc2y = c;

        c = Math.cos(arbiter.body1.rotation);
        s = Math.sin(arbiter.body1.rotation);
        double rotBc1x = c, rotBc1y = s, rotBc2x = -s, rotBc2y = c;

        double x1 = arbiter.body2.x + (rotAc1x * box.offsetX + rotAc2x * box.offsetY);
        double y1 = arbiter.body2.y + (rotAc1y * box.offsetX + rotAc2y * box.offsetY);
        double x2 = arbiter.body1.x + (rotBc1x * circle.offsetX + rotBc2x * circle.offsetY);
        double y2 = arbiter.body1.y + (rotBc1y * circle.offsetX + rotBc2y * circle.offsetY);

        double[] pts = getPoints(box, x1, y1, arbiter.body2.rotation);
        
        double r2 = circle.radius * circle.radius;

        boolean edgeOverlap = false;
        int numInnerSideTests = 0;
        int closestPoint = -1;
        double closestDistance = Double.MAX_VALUE;

        // Consider each face on the square        
        for (int i = 0; i < 4; i++) {
            vecx = pts[(i * 2 + 2) % 8] - pts[i * 2];
            vecy = pts[((i * 2 + 2) % 8) + 1] - pts[i * 2 + 1];

            locx = x2 - pts[i * 2]; locy = y2 - pts[i * 2 + 1];

            vl = Math.sqrt(vecx * vecx + vecy * vecy);
            vx = vecx / vl; vy = vecy / vl;

            locp = vx * locx + vy * locy;
            projx = locp * vx; projy = locp * vy;

            if (projx * projx + projy * projy > vecx * vecx + vecy * vecy) {
                contactPointx = pts[(i * 2 + 2) % 8];
                contactPointy = pts[((i * 2 + 2) % 8) + 1];
            } else {
                numInnerSideTests++;

                projx += pts[i * 2]; projy += pts[i * 2 + 1];

                otherx = projx - pts[(i * 2 + 2) % 8];
                othery = projy - pts[((i * 2 + 2) % 8) + 1];

                if (otherx * otherx + othery * othery > vecx * vecx + vecy * vecy) {
                    contactPointx = pts[i * 2]; contactPointy = pts[i * 2 + 1];
                } else {
                    contactPointx = projx; contactPointy = projy;
                }
            }

            contactPointx -= x2; contactPointy -= y2;
            
            double dis = contactPointx * contactPointx + contactPointy * contactPointy;
            if (dis < closestDistance) {
                closestDistance = dis;
                closestPoint = i;
            }

            if (dis < r2) {
                edgeOverlap = true;
            }
        }

        if (edgeOverlap || numInnerSideTests == 4) {
            Contact contact = arbiter.getNextAvailableContact(0);

            contact.shape1 = circle; contact.shape2 = box;

            double dis = Math.sqrt(closestDistance);
            contact.separation = dis - circle.radius;

//            if( closestPoint < 0 ) return 1;
            
            vecx = pts[(closestPoint * 2 + 2) % 8] - pts[closestPoint * 2];
            vecy = pts[((closestPoint * 2 + 2) % 8) + 1] - pts[closestPoint * 2 + 1];

            locx = x2 - pts[closestPoint * 2]; locy = y2 - pts[closestPoint * 2 + 1];

            vl = Math.sqrt(vecx * vecx + vecy * vecy);
            vx = vecx / vl; vy = vecy / vl;

            locp = vx * locx + vy * locy;
            projx = locp * vx; projy = locp * vy;

            if (projx * projx + projy * projy > vecx * vecx + vecy * vecy) {
                contactPointx = pts[(closestPoint * 2 + 2) % 8];
                contactPointy = pts[((closestPoint * 2 + 2) % 8) + 1];
            } else {
                projx += pts[closestPoint * 2]; projy += pts[closestPoint * 2 + 1];

                otherx = projx - pts[(closestPoint * 2 + 2) % 8];
                othery = projy - pts[((closestPoint * 2 + 2) % 8) + 1];

                if (otherx * otherx + othery * othery > vecx * vecx + vecy * vecy) {
                    contactPointx = pts[closestPoint * 2];
                    contactPointy = pts[closestPoint * 2 + 1];
                } else {
                    contactPointx = projx; contactPointy = projy;
                }
            }

            double normalx = x2 - contactPointx, normaly = y2 - contactPointy;
            double normall = Math.sqrt(normalx * normalx + normaly * normaly);
            normalx /= normall; normaly /= normall;

            contact.normalx = normalx; contact.normaly = normaly;
            contact.x = contactPointx; contact.y = contactPointy;

            contact.inEdge1 = 0; contact.outEdge1 = 0;
            contact.inEdge2 = 0; contact.outEdge2 = 0;

            if (numInnerSideTests == 4) {
                contact.normalx = -contact.normalx;
                contact.normaly = -contact.normaly;

                if (edgeOverlap == false) {
                    contact.separation = -contact.separation - 2.0 * circle.radius;
                }
            }

            double circlex = arbiter.body1.x + (rotBc1x * circle.offsetX + rotBc2x * circle.offsetY);
            double circley = arbiter.body1.y + (rotBc1y * circle.offsetX + rotBc2y * circle.offsetY);
            double ptx = contact.x - circlex, pty = contact.y - circley;

            double ptl = Math.sqrt(ptx * ptx + pty * pty);
            ptx /= ptl; pty /= ptl;
            ptx *= circle.radius; pty *= circle.radius;
            ptx += circlex; pty += circley;

            // Invert contact point and normal
            contact.x = ptx; contact.y = pty;
            contact.normalx = -contact.normalx; contact.normaly = -contact.normaly;

            return 1;
        }

        return 0;
    }

    /**
     * Consider a collision between the two specified circles
     * of bodies associated with the specifed arbiter.
     *
     * @param arbiter Arbiter containing the bodies
     * @param circleA first circle to be considered within the collision
     * @param circleB second circle to be considered within the collision
     */        
    public static final int collide(Arbiter arbiter, Circle circleA, Circle circleB) {
        double c = Math.cos(arbiter.body1.rotation);
        double s = Math.sin(arbiter.body1.rotation);
        double rotAc1x = c, rotAc1y = s, rotAc2x = -s, rotAc2y = c;

        c = Math.cos(arbiter.body2.rotation);
        s = Math.sin(arbiter.body2.rotation);
        double rotBc1x = c, rotBc1y = s, rotBc2x = -s, rotBc2y = c;

        double posAx = arbiter.body1.x + (rotAc1x * circleA.offsetX + rotAc2x * circleA.offsetY);
        double posAy = arbiter.body1.y + (rotAc1y * circleA.offsetX + rotAc2y * circleA.offsetY);
        double posBx = arbiter.body2.x + (rotBc1x * circleB.offsetX + rotBc2x * circleB.offsetY);
        double posBy = arbiter.body2.y + (rotBc1y * circleB.offsetX + rotBc2y * circleB.offsetY);

        // Test if the circles overlap
        if ((circleA.radius + circleB.radius) * (circleA.radius + circleB.radius) 
                < (posAx - posBx) * (posAx - posBx) + (posAy - posBy) * (posAy - posBy)) {
            return 0;
        }
        
        // Determine collision normall
        double normalx = posBx - posAx, normaly = posBy - posAy;
        double normall = Math.sqrt(normalx * normalx + normaly * normaly);
        double sep = (circleA.radius + circleB.radius) - normall;
        normalx /= normall; normaly /= normall;

        double ptx = normalx * circleA.radius + posAx;
        double pty = normaly * circleA.radius + posAy;

        // Store contact details
        Contact contact = arbiter.getNextAvailableContact(0);

        contact.shape1 = circleA; contact.shape2 = circleB;

        contact.separation = -sep;
        contact.x = ptx; contact.y = pty;
        contact.normalx = normalx; contact.normaly = normaly;

        contact.inEdge1 = 0; contact.outEdge1 = 0;
        contact.inEdge2 = 0; contact.outEdge2 = 0;

        return 1;
    }

    /**
     * Return an array of eight double representing the location of the
     * four corners of the specified box at the given position and with
     * the given rotation.
     *
     * @param box Box to consider
     * @param posX x position of the box
     * @param posY y position of the box
     * @param rotation rotation of the box
     * @return double array of length 8 containing the box corner points
     */        
    public static final double[] getPoints(
            Box box, double posX, double posY, double rotation) {
        double[] points = new double[8];

        double rotmc = Math.cos(rotation);
        double rotms = Math.sin(rotation);
        double rotmc1x = rotmc, rotmc1y = rotms, rotmc2x = -rotms, rotmc2y = rotmc;
        double hx = box.width * 0.5, hy = box.height * 0.5;

        points[0] = (rotmc1x * -hx + rotmc2x * -hy) + posX;
        points[1] = (rotmc1y * -hx + rotmc2y * -hy) + posY;
        points[2] = (rotmc1x * hx + rotmc2x * -hy) + posX;
        points[3] = (rotmc1y * hx + rotmc2y * -hy) + posY;
        points[4] = (rotmc1x * hx + rotmc2x * hy) + posX;
        points[5] = (rotmc1y * hx + rotmc2y * hy) + posY;
        points[6] = (rotmc1x * -hx + rotmc2x * hy) + posX;
        points[7] = (rotmc1y * -hx + rotmc2y * hy) + posY;

        return points;
    }
}