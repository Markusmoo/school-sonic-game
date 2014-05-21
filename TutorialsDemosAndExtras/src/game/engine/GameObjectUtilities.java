package game.engine;

/**
 * GameObjectUtilities is intended to provide a collection of useful
 * methods.
 * <P>
 * The rational behind this class is to avoid cluttering up core class
 * with useful but not essential methods - although, currently, this
 * class does not contain many methods...
 *
 * @author <A HREF="mailto:P.Hanna@qub.ac.uk">Philip Hanna</A>
 * @version $Revision: 1.0 $ $Date: 2007/08 $
 */
public final class GameObjectUtilities {

    ///////////////////////////////////////////////////////////////////////////
    // Methods:                                                              //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Dampen the axial and rotational velocities of the specified game object 
     * by the specified amounts along the x, y and rotational axis.
     *
     * @param  gameObject GameObject to dampen
     * @param  directionalDampening double dampening to be applied to the
     *         x and y velocities
     * @param  angularDampening double dampening to be applied to the
     *         angular velocity
     */
    public static final void dampenVelocities(GameObject gameObject, 
            double directionalDampening, double angularDampening) {
        if (gameObject.velocityx > 0.0) {
            gameObject.velocityx -= directionalDampening;
            if (gameObject.velocityx < 0.0) gameObject.velocityx = 0.0;
        } else {
            gameObject.velocityx += directionalDampening;
            if (gameObject.velocityx > 0.0) gameObject.velocityx = 0.0;
        }

        if (gameObject.velocityy > 0.0) {
            gameObject.velocityy -= directionalDampening;
            if (gameObject.velocityy < 0.0) gameObject.velocityy = 0.0;
        } else {
            gameObject.velocityy += directionalDampening;
            if (gameObject.velocityy > 0.0) gameObject.velocityy = 0.0;
        }

        if (gameObject.angularVelocity > 0.0) {
            gameObject.angularVelocity -= angularDampening;
            if (gameObject.angularVelocity < 0.0) gameObject.angularVelocity = 0.0;
        } else {
            gameObject.angularVelocity += angularDampening;
            if (gameObject.angularVelocity > 0.0) gameObject.angularVelocity = 0.0;
        }
    }

    /**
     * Dampen the axial and rotational velocities of the specified game object 
     * by the specified amounts.
     *
     * @param  gameObject GameObject to dampen
     * @param  negXDampening double dampening applied to negative x velocities
     * @param  posXDampening double dampening applied to positive x velocities
     * @param  negYDampening double dampening applied to negative y velocities
     * @param  posYDampening double dampening applied to positive y velocities
     * @param  negRotDampening double dampening applied to negative rotational velocities
     * @param  posRotDampening double dampening applied to positive rotational velocities
     */
    public static final void dampenVelocities(GameObject gameObject, 
            double negXDampening, double posXDampening, 
            double negYDampening, double posYDampening, 
            double negRotDampening, double posRotDampening) {
        if (gameObject.velocityx > 0.0) {
            gameObject.velocityx -= posXDampening;
            if (gameObject.velocityx < 0.0) gameObject.velocityx = 0.0;
        } else {
            gameObject.velocityx += negXDampening;
            if (gameObject.velocityx > 0.0) gameObject.velocityx = 0.0;
        }

        if (gameObject.velocityy > 0.0) {
            gameObject.velocityy -= posYDampening;
            if (gameObject.velocityy < 0.0) gameObject.velocityy = 0.0;
        } else {
            gameObject.velocityy += negYDampening;
            if (gameObject.velocityy > 0.0) gameObject.velocityy = 0.0;
        }

        if (gameObject.angularVelocity > 0.0) {
            gameObject.angularVelocity -= posRotDampening;
            if (gameObject.angularVelocity < 0.0) gameObject.angularVelocity = 0.0;
        } else {
            gameObject.angularVelocity += negRotDampening;
            if (gameObject.angularVelocity > 0.0) gameObject.angularVelocity = 0.0;
        }
    }

    /**
     * Clamp the axial and rotational velocities of the specified game object 
     * to the specified amounts
     *
     * @param  gameObject GameObject to clamp
     * @param  directionalClamp double clamp to be applied to the
     *         x and y velocities
     * @param  angularClamp double clamp to be applied to the
     *         angular velocity
     */
    public static final void clampVelocities(GameObject gameObject, 
            double directionalClamp, double angularClamp) {
        if (gameObject.velocityx > 0.0) {
            if (gameObject.velocityx > directionalClamp)
                gameObject.velocityx = directionalClamp;
        } else {
            if (gameObject.velocityx < -directionalClamp)
                gameObject.velocityx = -directionalClamp;
        }

        if (gameObject.velocityy > 0.0) {
            if (gameObject.velocityy > directionalClamp)
                gameObject.velocityy = directionalClamp;
        } else {
            if (gameObject.velocityy < -directionalClamp)
                gameObject.velocityy = -directionalClamp;
        }

        if (gameObject.angularVelocity > 0.0) {
            if (gameObject.angularVelocity > angularClamp)
                gameObject.angularVelocity = angularClamp;
        } else {
            if (gameObject.angularVelocity < -angularClamp)
                gameObject.angularVelocity = -angularClamp;
        }
    }

    /**
     * Clamp the velocities of the specified game object based on the specified
     * amounts.
     *
     * @param  gameObject GameObject to dampen
     * @param  negXClamp double clamp applied to negative x velocities
     * @param  posXClamp double clamp applied to positive x velocities
     * @param  negYClamp double clamp applied to negative y velocities
     * @param  posYClamp double clamp applied to positive y velocities
     * @param  negRotClamp double clamp applied to negative rotational velocities
     * @param  posRotClamp double clamp applied to positive rotational velocities
     */
    public static final void clampVelocities(GameObject gameObject, 
            double negXClamp, double posXClamp, 
            double negYClamp, double posYClamp, 
            double negRotClamp, double posRotClamp) {
        if (gameObject.velocityx > 0.0) {
            if (gameObject.velocityx > posXClamp) gameObject.velocityx = posXClamp;
        } else {
            if (gameObject.velocityx < -negXClamp) gameObject.velocityx = -negXClamp;
        }

        if (gameObject.velocityy > 0.0) {
            if (gameObject.velocityy > posYClamp) gameObject.velocityy = posYClamp;
        } else {
            if (gameObject.velocityy < -negYClamp) gameObject.velocityy = -negYClamp;
        }

        if (gameObject.angularVelocity > 0.0) {
            if (gameObject.angularVelocity > posRotClamp) 
                    gameObject.angularVelocity = posRotClamp;
        } else {
            if (gameObject.angularVelocity < -negRotClamp) 
                    gameObject.angularVelocity = -negRotClamp;
        }
    }

    /**
     * Scale the velocities of the specified game object by the specified amounts.
     *
     * @param  gameObject GameObject to scale
     * @param  directionalScale double scale to be applied to the
     *         x and y velocities
     * @param  angularScale double scale to be applied to the
     *         angular velocity
     */
    public static final void scaleVelocities(GameObject gameObject,
            double directionalScale, double angularScale ) {
        gameObject.velocityx *= directionalScale;
        gameObject.velocityy *= directionalScale;
        gameObject.angularVelocity *= angularScale;        
    }    
    
    /**
     * Delete the game object if it completely leaves the confines of the 
     * game layer 
     *
     * @param  gameObject GameObject to consider
     * @return boolean true if the game object was deleted, otherwise false
     */
    public static final boolean deleteIfGameLayerExited(GameObject gameObject) {
        GameLayer gameLayer = gameObject.gameLayer;

        double rotatedHalfWidth = gameObject.getRotatedWidth() / 2;
        double rotatedHalfHeight = gameObject.getRotatedHeight() / 2;

        if (gameObject.x + rotatedHalfWidth < 0 
                || gameObject.x - rotatedHalfWidth > gameLayer.width 
                || gameObject.y + rotatedHalfHeight < 0 
                || gameObject.y - rotatedHalfHeight > gameLayer.height) {
            gameLayer.queueGameObjectToRemove(gameObject);
            return true;
        }

        return false;
    }

    /**
     * Rebound the game object if any part of it leaves the confines of the
     * game layer 
     *
     * @param  gameObject GameObject to consider
     * @return boolean true if the game object was deleted, otherwise false
     */
    public static final boolean reboundIfGameLayerExited(GameObject gameObject) {
        GameLayer gameLayer = gameObject.gameLayer;

        boolean rebound = false;

        double rotatedHalfWidth = gameObject.getRotatedWidth() / 2;
        double rotatedHalfHeight = gameObject.getRotatedHeight() / 2;

        if (gameObject.x - rotatedHalfWidth < 0) {
            gameObject.x = rotatedHalfWidth;
            gameObject.velocityx = Math.abs(gameObject.velocityx);
            rebound = true;
        } else if (gameObject.x + rotatedHalfWidth > gameLayer.width) {
            gameObject.x = gameLayer.width - rotatedHalfWidth;
            gameObject.velocityx = -Math.abs(gameObject.velocityx);
            rebound = true;
        }

        if (gameObject.y - rotatedHalfHeight < 0) {
            gameObject.y = rotatedHalfHeight;
            gameObject.velocityy = Math.abs(gameObject.velocityy);
            rebound = true;
        } else if (gameObject.y + rotatedHalfHeight > gameLayer.height) {
            gameObject.y = gameLayer.height - rotatedHalfHeight;
            gameObject.velocityy = -Math.abs(gameObject.velocityy);
            rebound = true;
        }

        return rebound;
    }
}
