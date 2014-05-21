package game.components;

import game.assets.*;
import game.engine.GameLayer;
import game.engine.GameObject;
//import game.geometry.*;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;

/**
 * The class, extending GameObject, provides support for a 'progress bar'
 * form of graphical object. As such, it can be used as a means of tracking
 * health, time, load progression, etc.
 *
 * @author <A HREF="mailto:P.Hanna@qub.ac.uk">Philip Hanna</A>
 * @version $Revision: 1 $ $Date: 2007/08 $
 */
public class Bar extends GameObject {

    ///////////////////////////////////////////////////////////////////////////
    // Class data                                                            //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Graphical asset providing the realisation of the bar. The portion
     * of the bar which is displayed depends upon the currentPoints 
     * and maximumPoints values
     */
    protected GraphicalAsset innerAsset;
    
    /**
     * Graphical asset providing the realisation of the border. The
     * border will always be displayed irrespective of the currentPoints
     * value
     */
    protected GraphicalAsset borderAsset;
    
    /**
     * Maximum number of points that the bar can be subdivided into.
     * This value will determine the size of the 'graphical' chunks
     * that the inner asset is split into as well as providing a
     * game specific maximum value (e.g. the players maximum health
     * may be 150 points, etc.).
     */
    protected int maximumPoints;

    /**
     * Maximum number of points that the currentPoints variable can
     * be either increased or decreased during a single update (in
     * effect this provides a means of controlling how quickly
     * the bar reacts to change).
     */ 
    protected double maximumUpdatePointChange;
        
    /**
     * Target number of points representing the desired value of
     * currentPoints. During each update tick currentPoints will move
     * towards targetPoints (if not already equal) bounded by the 
     * defined maximumUpdatePointChange variable.
     */
    protected int targetPoints;

    /**
     * Current number of points, determining how much of the bar will
     * be displayed in relation to the defined maximumPoints    
     */ 
    protected int currentPoints;

    /**
     * As the maximumUpdatePointChange can hold a fractional offset,
     * an accumulator is maintained to ensure appropriate integer
     * change of currentPoints
     */ 
    private double pointChangeAccumulator = 0.0;

 
    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Construct a new bar object with a default position and draw order.
     * <P>
     * Note: By default the Bar's current points is set equal to the specified
     * maximum points. A bar may also be created without a specified border
     * assey by passing in a null borderAssetName
     *
     * @param  gameLayer GameLayer to which this Bar will belong
     * @param  gameObjectName game object name to be given to this Bar object
     * @param  borderAssetName String asset name of the border asset to be used
     * @param  innerAssetName String asset name of the inner asset to be used
     * @param  maximumPoints integer maximum number of points the bar can be 
     *         divided into
     */
    public Bar(GameLayer gameLayer, String gameObjectName, 
            String borderAssetName, String innerAssetName, int maximumPoints) {
        this(gameLayer, gameObjectName, 
                borderAssetName, innerAssetName, maximumPoints, 0.0, 0.0, 0);
    }

    /**
     * Construct a new bar object at the specification location with a default
     * draw order.
     * <P>
     * Note: By default the Bar's current points is set equal to the specified
     * maximum points. A bar may also be created without a specified border
     * assey by passing in a null borderAssetName
     *
     * @param  gameLayer GameLayer to which this Bar will belong
     * @param  gameObjectName game object name to be given to this Bar object
     * @param  borderAssetName String asset name of the border asset to be used
     * @param  innerAssetName String asset name of the inner asset to be used
     * @param  maximumPoints integer maximum number of points the bar can be 
     *         divided into
     * @param  x double containing the Bar's x layer location 
     * @param  y double containing the Bar's y layer location 
     */    
    public Bar(GameLayer gameLayer, String gameObjectName, 
            String borderAssetName, String innerAssetName, int maximumPoints, 
            double x, double y) {
        this(gameLayer, gameObjectName, borderAssetName, innerAssetName, 
                maximumPoints, x, y, 0);
    }

    /**
     * Construct a new bar object at the specification location and with the
     * specified draw order.
     * <P>
     * Note: By default the Bar's current points is set equal to the specified
     * maximum points. A bar may also be created without a specified border
     * assey by passing in a null borderAssetName
     *
     * @param  gameLayer GameLayer to which this Bar will belong
     * @param  gameObjectName game object name to be given to this Bar object
     * @param  borderAssetName String asset name of the border asset to be used
     * @param  innerAssetName String asset name of the inner asset to be used
     * @param  maximumPoints integer maximum number of points the bar can be 
     *         divided into
     * @param  x double containing the Bar's x layer location 
     * @param  y double containing the Bar's y layer location 
     * @param  drawOrder integer draw order
     */        
    public Bar(GameLayer gameLayer, String gameObjectName, 
            String borderAssetName, String innerAssetName, int maximumPoints, 
            double x, double y, int drawOrder) {
        super(gameLayer, x, y, drawOrder);

        setName(gameObjectName);

        borderAsset = borderAssetName == null ? 
            null : assetManager.retrieveGraphicalAsset(borderAssetName);
        innerAsset = assetManager.retrieveGraphicalAsset(innerAssetName);

        if (borderAsset == null) {
            setRealisationAndGeometry(innerAsset);
        } else {
            setRealisationAndGeometry(
                    new GraphicalAsset[]{borderAsset, innerAsset});
        }
        
        this.maximumPoints = maximumPoints;
        targetPoints = maximumPoints;
        currentPoints = maximumPoints;
        maximumUpdatePointChange = Integer.MAX_VALUE;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods:                                                              //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Reset the Bar's points to the defined maximum number of points
     */    
    public void resetPoints() {
        currentPoints = maximumPoints;
        targetPoints = maximumPoints;
    }

    /**
     * Set the Bar's current number of points to that specified
     * 
     * @param numPoints integer number of current points
     * 
     * @exception IllegalArgumentException if the specified number of points 
     *            is invalid
     */    
    public void setPoints(int numPoints) {
        if (numPoints < 0 || numPoints > maximumPoints) {
            throw new IllegalArgumentException("Bar.setPoints: " +
                    "Invalid points specified: " + numPoints);
        }
        
        currentPoints = numPoints;
        targetPoints = numPoints;
    }

    /**
     * Add the current number of points into the Bar's current target
     * number of points.
     * <P>
     * Note: Additions are bounded between 0 and the defined maximum
     * number of points.
     * 
     * @param numPoints integer number of points to add
     */    
    public void addPoints(int numPoints) {
        targetPoints += numPoints;

        if (targetPoints < 0) {
            targetPoints = 0;
        }
        if (targetPoints > maximumPoints) {
            targetPoints = maximumPoints;
        }
    }

    /**
     * Subtract the current number of points from the Bar's current target 
     * number of points.
     * <P>
     * Note: Subtrations are bounded between 0 and the defined maximum
     * number of points.
     * 
     * @param numPoints integer number of points to subtract
     */        
    public void subtractPoints(int numPoints) {
        targetPoints -= numPoints;

        if (targetPoints < 0) {
            targetPoints = 0;
        }
        if (targetPoints > maximumPoints) {
            targetPoints = maximumPoints;
        }
    }

    /**
     * Return the current target number of points.
     * 
     * @return integer current target number of points
     */            
    public int getPoints() {
        return targetPoints;
    }

    /**
     * Return the maximum number of points
     * 
     * @return integer maximum number of points
     */            
    public int getMaximumPoints() {
        return maximumPoints;
    }

    /**
     * Set the maximum number of points to that specified
     * 
     * @param maximumPoints integer maximum number of points to use
     */                
    public void setMaximumPoints(int maximumPoints) {
        this.maximumPoints = maximumPoints;
        currentPoints = maximumPoints;
        targetPoints = maximumPoints;
    }

    /**
     * Set the maximum point change per update tick to that specified
     * 
     * @param maximumUpdatePointChange double maximum point change per update
     */                
    public void setMaximumUpdatePointChange(double maximumUpdatePointChange) {
        this.maximumUpdatePointChange = maximumUpdatePointChange;
    }
    
    /**
     * If necessary update the current number of points based upon the 
     * target number of points.
     */
    @Override
    public void update() {
        if (currentPoints == targetPoints) {
            return;
        }
        
        int difference = targetPoints - currentPoints;

        if (Math.abs(difference) < maximumUpdatePointChange) {
            currentPoints = targetPoints;
        } else {
            pointChangeAccumulator += maximumUpdatePointChange;
            if (difference > 0) {
                currentPoints += (int) Math.floor( pointChangeAccumulator );
                pointChangeAccumulator -= Math.floor(pointChangeAccumulator);
            } else {
                currentPoints -= (int) Math.floor( pointChangeAccumulator );
                pointChangeAccumulator -= Math.floor(pointChangeAccumulator);
            }
        }
    }

    /**
     * Set the inner asset offset to that specified.
     * <P>
     * Note: the displayed position of the inner asset can be varied through
     * setting this offset. It is intended that this method be used to provide
     * a means of aligning the inner asset with the border asset (which may
     * be offset through the inclusion of a drop shadow)
     *
     * @param  offsetX double containing the inner draw asset x offset
     * @param  offsetY double containing the inner draw asset y offset
     */            
    public void setInnerAssetOffset(double offsetX, double offsetY) {
        innerAsset.offsetX = offsetX;
        innerAsset.offsetY = offsetY;
    }

    /**
     * Render the bar at the specified x and y draw location
     *
     * @param  graphics2D graphical object on which to render this asset
     * @param  drawX x offset at which the object should be rendered
     * @param  drawY y offset at which the object should be rendered
     */
    @Override
    public void draw(Graphics2D graphics2D, int drawX, int drawY) {
        // Record the original graphics affine transform
        AffineTransform originalAffineTransofrm = graphics2D.getTransform();

        // Create a new affine rotational transform centered on the
        // middle of the object and use this for the graphics object
        AffineTransform rotationAffineTransform = new AffineTransform();
        rotationAffineTransform.rotate(rotation, drawX, drawY);
        graphics2D.transform(rotationAffineTransform);

        // Draw the border asset if necessary
        if (borderAsset != null) {
            borderAsset.draw(graphics2D, 
                    drawX + (int) borderAsset.offsetX - borderAsset.width / 2, 
                    drawY + (int) borderAsset.offsetY - borderAsset.height / 2);
        }
        
        // Draw the inner asset if necessary
        if (currentPoints > 0) {
            int innerX = drawX + (int) innerAsset.offsetX - innerAsset.width / 2;
            int innerY = drawY + (int) innerAsset.offsetY - innerAsset.height / 2;

            int innerWidth = (innerAsset.width * currentPoints) / maximumPoints;
            int innerHeight = innerAsset.height;

            BufferedImage innerImage = innerAsset.getImageRealisation();
            graphics2D.drawImage(innerImage, innerX, innerY, 
                    innerX + innerWidth, innerY + innerHeight, 0, 0, 
                    innerWidth, innerHeight, null);
        }

        // Restore the original graphics affine transform
        graphics2D.setTransform(originalAffineTransofrm);
    }
}
