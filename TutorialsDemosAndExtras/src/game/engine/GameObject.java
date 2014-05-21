package game.engine;

import game.assets.*;
import game.geometry.*;
import java.util.*;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.io.*;
 
/**
 * GameObject provides the superclass of all game objects. Game objects
 * can have a graphical representation and an associated geometry. As
 * graphical objects, game object also have a layer location, dimension
 * and layer draw order. Finally, each game can receive update ticks.
 * <P>
 * The GameObject realisation can either consist of a single GraphicalAsset 
 * or a collection of GraphicalAssets. The width and height of the game
 * object is automatically derived from the defined geometry attached to 
 * the game object.
 * <P>
 * Note: A significant number of class variables are declared to be public
 * in order to provide fast access.
 *
 * @author <A HREF="mailto:P.Hanna@qub.ac.uk">Philip Hanna</A>
 * @version $Revision: 1.1 $ $Date: 2007/08 $
 *
 * @see GameLayer
 * @see GameEngine
 */

public class GameObject extends Observable implements Observer {

    ///////////////////////////////////////////////////////////////////////////
    // Class data: Core game object data                                     //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * This variable is incremented every time a new game object is created.
     * It is used as a means of providing each game object with a unique
     * initial game object name.
     */
    protected static int UNIQUE_GAMEOBJECT_NEXT_ID = 0;

    /**
     * Game object unique numerical identifier
     */
    public int uniqueGameObjectID;

    /**
     * String object name
     * <P>
     * Note: The GameObject is initially provided with a 'unique' name which
     * can be changed through the use of setName method if necessary -
     * although care should normally be exercised to ensure that two game
     * objects within the same layer do not have the same name.
     */
    protected String gameObjectName;

    /**
     * Game object collection, if any, that this game object belongs to.
     */
    protected GameObjectCollection gameObjectCollection = null;

    /**
     * GameLayer to which this game object belongs. Importantly this reference
     * provides a game object with access to its game layer (for example to
     * add/remove game objects, including itself).
     */
    protected GameLayer gameLayer;

    /**
     * Direct link to the GameEngine instance (permitting more direct
     * access to game engine functionality if needed)
     */
    protected GameEngine gameEngine;

    /**
     * Direct link to the AssetManager instance (permitting more direct
     * access to asset manager functionality if needed)
     */
    protected AssetManager assetManager;

    /**
     * Direct link to the GameInputEventManager instance (permitting more
     * direct access to game input information if needed)
     */
    protected GameInputEventManager inputEvent;


    ///////////////////////////////////////////////////////////////////////////
    // Class data: Dimensions and rotation                                   //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * x-location of this object (normally relative to the parent game layer)
     */
    public double x;

    /**
     * y-location of this object (normally relative to the parent game layer)
     */
    public double y;

    /**
     * Width of this object, normally determined from the current geometry
     */
    public double width;

    /**
     * Height  of this object, normally determined from the current geometry
     */
    public double height;

    /**
     * Rotation (in radians) of the graphical realisation of this object. The
     * rotation is measured from 0 (straight-up) and is centered on the middle
     * of the game object.
     */
    public double rotation;

    
    ///////////////////////////////////////////////////////////////////////////
    // Class data: Graphical realisation                                     //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Graphical realisations of the game object
     */
    public GraphicalAsset[] graphicalRealisation;

    /**
     * z-order of this object (normally relative to the parent game layer)
     */
    protected int drawOrder;

    
    ///////////////////////////////////////////////////////////////////////////
    // Class data: Geometry and bounding region                              //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Flag used to determine if this object can intersect other game objects
     */
    public boolean canIntersectOtherGraphicalObjects;

    /**
     * Arraylist of rectangular bounds representing the intersectable regions 
     * of the graphical game object
     * <P>
     * Note: This geometry is defined as public to permit rapid access, however,
     * the geometry should not be changed directly, instead one of the 
     * setGeometry or setGeometryAndRealisation methods should be used as they
     * will correctly update the object's width, height and bounding region to
     * reflect the new geometry. 
     */
    public Shape[] geometry;

    /**
     * The bounding dimension provides a square bounding region center on the 
     * object representing the range of object movement arising from rotation,
     * i.e. the boundingDimension provides a quick means of testing if this
     * game object may potentially overlap with the game object. 
     * <P>
     * The boundingDimension is calculated from the current object's geometry
     * and takes into account variation arising from the rotation of the object.
     */
    public double boundingDimension;


    ///////////////////////////////////////////////////////////////////////////
    // Class data: Movement                                                  //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Velocity of the game object along the x axis
     */
    public double velocityx;
    
    /**
     * Velocity of the game object along the y axis
     */
    public double velocityy;

    /**
     * Angular velocity of the game object
     */
    public double angularVelocity;


    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Create a new GameObject instance.
     * <P>
     * By default, each GameObject is assumed to have a FIXED
     * z-layer value, a rotation of 0, and is capable of intersecting
     * other graphical objects.
     *
     * @param  gameLayer GameLayer instance to which this game object belongs
     */
    public GameObject(GameLayer gameLayer) {
        this(gameLayer, 0.0, 0.0, 0);
    }

    /**
     * Create a new GameObjectinstance with the specified x and
     * y offsets.
     * <P>
     * By default, each GameObject is assumed to have a FIXED
     * z-layer value, a rotation of 0, and is capable of intersecting
     * other graphical objects.
     *
     * @param  gameLayer GameLayer instance to which this game object belongs
     * @param  x double x location of this GameObject
     * @param  y double y location of this GameObject
     */
    public GameObject(GameLayer gameLayer, double x, double y) {
        // Call the class single-argument constructor
        this(gameLayer, x, y, 0);
    }

    /**
     * Create a new GameObject instance with the specified x,
     * y offset and specified draw order.
     * <P>
     * By default, each GameObject is assumed to have a FIXED
     * z-layer value, a rotation of 0, and is capable of intersecting
     * other graphical objects.
     *
     * @param  gameLayer GameLayer instance to which this game object belongs
     * @param  x double x location of this GameObject
     * @param  y double y location of this GameObject
     * @param  drawOrder integer draw order of this GameObject
     */
    public GameObject(GameLayer gameLayer, double x, double y, int drawOrder) {
        // Provide the game object with a 'unique' name
        uniqueGameObjectID = UNIQUE_GAMEOBJECT_NEXT_ID++;
        gameObjectName = "GameObject" + uniqueGameObjectID;

        // Store game layer, engine, asset manager and input event links
        if (gameLayer != null) {
            this.gameLayer = gameLayer;
            gameEngine = gameLayer.gameEngine;
            assetManager = gameEngine.assetManager;
            inputEvent = gameEngine.inputEvent;
        }

        // Set the location and draw order to that specified
        this.x = x;
        this.y = y;
        this.drawOrder = drawOrder;

        // Initialise other variables to default starting values
        rotation = 0.0;
        width = 0.0;
        height = 0.0;
        velocityx = 0.0;
        velocityy = 0.0;
        angularVelocity = 0.0;

        // Define the game object as being able to intersect other game 
        // objects and to have a null geometry and null realisation
        canIntersectOtherGraphicalObjects = true;
        geometry = new Shape[0];
        graphicalRealisation = new GraphicalAsset[0];
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods: Get/set methods                                              //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Set the game layer to which this game object belongs.
     * <P>
     * This does not mean that the Asset is actually added to the layer.
     * The GameLayer.addGameObject method will accomplish this.
     *
     * @param  gameLayer GameLayer reference to which this game object belongs
     */
    public final void setGameLayer(GameLayer gameLayer) {
        this.gameLayer = gameLayer;
    }

    /**
     * Get the game layer to which this game object belongs.
     *
     * @return GameLayer reference to which this game object belongs
     */
    public final GameLayer getGameLayer() {
        return gameLayer;
    }

    /**
     * Set the name of this game object to that specified
     * <P>
     * Note: Caution should be exercised to ensure that two game objects within 
     * the same layer do not have the same name.
     *
     * @param  gameObjectName String name to be assigned to this game object
     */
    public final void setName(String gameObjectName) {
        this.gameObjectName = gameObjectName;
    }

    /**
     * Get the name of this game object
     *
     * @return String name of this game object
     */
    public final String getName() {
        return gameObjectName;
    }

    /**
     * Set the x, y location to those specified
     *
     * @param  x x location of this GameObject
     * @param  y y location of this GameObject
     */
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Return the draw order of this game object
     *
     * @return integer draw order of this game object
     */
    public final int getDrawOrder() {
        return drawOrder;
    }

    /**
     * Set the draw order to that specified
     *
     * @param  drawOrder integer draw order to use
     */
    public final void setDrawOrder(int drawOrder) {
        this.drawOrder = drawOrder;
        gameLayer.sortGameObjectsOnDrawOrder();
    }

    /**
     * Return the game object collection that game object belongs to (a null
     * value will be returned if the game object does not belong to a 
     * game object collection).
     *
     * @return GameObjectCollection game object collection this game object
     *         belongs to, or null if no game object
     */
    public final GameObjectCollection getGameObjectCollection() {
        return gameObjectCollection;
    }

    /**
     * Determine if this game object can intersect other game objects
     *
     * @param  canIntersectOtherGraphicalObjects boolean flag determining if
     *         this game object can intersect other game objects
     */
    public final void setCanIntersectOtherGraphicalObjects(
            boolean canIntersectOtherGraphicalObjects) {
        this.canIntersectOtherGraphicalObjects = canIntersectOtherGraphicalObjects;
    }

    
    ///////////////////////////////////////////////////////////////////////////
    // Methods: Update methods                                               //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Game object update - by default does not do anything (i.e. any game object
     * specific actions should be specified within an extending superclass).
     */
    public void update() { }

    /**
     * Game object observation trigger - by default does not do anything (i.e. 
     * any game object specific actions should be specified within an extending 
     * superclass).
     */
    public void update(Observable observableObject, Object message) { }

    /**
     * Update the game object's position and rotation based upon the current
     * velocities and the specified step value
     *  
     * @param step double step magnitude
     */
    public final void updatePositionAndRotation(double step) {
        x += velocityx * step;
        y += velocityy * step;
        rotation += angularVelocity * step;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods: Realisation and geometry methods                             //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Return the graphical realisation used by this object
     *
     * @return GraphicalAsset realisation of this object
     *
     * @exception IllegalStateException if this object has no current realisation
     */
    public final GraphicalAsset[] getRealisation() {
        if (graphicalRealisation == null) {
            throw new IllegalStateException("GameObject.getRealisation: " + 
                    "Object does not have a current graphica realisation.");
        }
        
        return graphicalRealisation;
    }

    /**
     * Return the specified graphical realisation used by this object
     *
     * @param  realisationIdx index of the graphical realisation to retrieve
     * @return GraphicalAsset specified realisation of this object
     *
     * @exception IllegalStateException if this object have no current realisation
     * @exception IllegalArgumentException if an invalid index is provided
     */
    public final GraphicalAsset getRealisation(int realisationIdx) {
        if (graphicalRealisation == null) {
            throw new IllegalStateException("GameObjectGrpahical.getRealisation: " + 
                    "Object does not have a current graphica realisation.");
        }
        
        if (realisationIdx < 0 || realisationIdx > graphicalRealisation.length) {
            throw new IllegalArgumentException("GameObject.getRealisation: " + 
                    "Invalid realisation index of " + realisationIdx);
        }
        
        return graphicalRealisation[realisationIdx];
    }

    /**
     * Set the graphical realisation to that specified
     * <P>
     * Note: The specified Asset will be automatically added to this object's
     * store of Assets (if not already added).
     * <P>
     * Note: The width and height of the object will be automatically
     * updated to that of the new asset realisation.
     *
     * @param  asset Asset to be used as the graphical realisation
     *
     * @exception NullPointerException if not asset has been specified
     * @exception IllegalArgumentException if a GraphicalAsset has not been 
     *            specified
     */
    public final void setRealisation(GraphicalAsset graphicalAsset) {
        if (graphicalAsset == null) {
            throw new NullPointerException("GameObject.setRealisation: " +
                    "NULL asset specified.");
        }
        
        graphicalRealisation = new GraphicalAsset[]{graphicalAsset};
    }

    /**
     * Retreive and set the graphical realisation to that specified
     * <P>
     * Note: This method assumes that the specified asset can be
     * found the asset store for this particular object.
     * <P>
     * Note: The width and height of the object will be automatically
     * updated to that of the new asset realisation.
     *
     * @param  assetName name of the asset to be retrieved and used
     *
     * @exception NullPointerException if null asset name specified
     * @exception IllegalArgumentException if a corresponding asset 
     *            cannot be retrieved
     */
    public final void setRealisation(String assetName) {
        if (assetName == null) {
            throw new NullPointerException("GameObject.setRealisation: " +
                    "NULL asset name specified.");
        }
        
        GraphicalAsset graphicalAsset 
                = assetManager.retrieveGraphicalAsset(assetName);
        if (graphicalAsset == null) {
            throw new IllegalArgumentException("GameObject.setRealisation: " +
                    "NULL asset retrieved for: " + assetName);
        }
        
        setRealisation(graphicalAsset);
    }

    /**
     * Retreive and set the graphical realisation to the specified collection
     * of graphical assets.
     * <P>
     * Note: This method assumes that the specified assets can be
     * found in the asset store for this particular object.
     * <P>
     * Note: The width and height of the object will be automatically
     * updated to that of the bounds of the new assets.
     *
     * @param  assetNames name array of the asset to be retrieved and used
     *
     * @exception NullPointerException if null parameter specified
     * @exception IllegalArgumentException if a corresponding asset cannot be 
     *            retrieved or an invalid offset arrays provided
     */
    public final void setRealisation(GraphicalAsset[] graphicalAssets) {
        if (graphicalAssets == null) {
            throw new NullPointerException("GameObject.setRealisation: " +
                    "NULL parameter specified.");
        }
        
        graphicalRealisation = graphicalAssets;
    }

    /**
     * Retreive and set the graphical realisation to the specified collection
     * of graphical assets.
     * <P>
     * Note: This method assumes that the specified assets can be
     * found in the asset store for this particular object.
     * <P>
     * Note: The width and height of the object will be automatically
     * updated to that of the bounds of the new assets.
     *
     * @param  assetNames name array of the asset to be retrieved and used
     * @param  assetXOffsets integer x offsets of the assets to be used
     * @param  assetYOffsets integer y offsets of the assets to be used
     *
     * @exception NullPointerException if null parameter specified
     * @exception IllegalArgumentException if a corresponding asset cannot be 
     *            retrieved or an invalid offset arrays provided
     */
    public final void setRealisation(
            String[] assetNames, int[] assetXOffsets, int[] assetYOffsets) {
        if (assetNames == null) {
            throw new NullPointerException("GameObject.setRealisation: " +
                    "NULL parameter specified.");
        }
        
        // Attempt to retreive the specified assets
        GraphicalAsset[] graphicalAssets = new GraphicalAsset[assetNames.length];
        for (int idx = 0; idx < graphicalAssets.length; idx++) {
            graphicalAssets[idx] 
                    = assetManager.retrieveGraphicalAsset(assetNames[idx]);
            if (graphicalAssets[idx] == null) {
                throw new IllegalArgumentException("GameObject.setRealisation:" + 
                        "NULL asset retrieved for: " + assetNames[idx]);
            }
            graphicalAssets[idx].offsetX = assetXOffsets[idx];
            graphicalAssets[idx].offsetY = assetYOffsets[idx];
        }

        setRealisation(graphicalAssets);
    }

    /**
     * Set the geometry to the specified shape
     *
     * @param shape Shape reference defining the geometry
     */
    public void setGeometry(Shape shape) {
        setGeometry(new Shape[]{shape});
    }

    /**
     * Set the geometry to the specified array of shapes
     *
     * @param  geometry array of Shape references
     */
    public void setGeometry(Shape[] geometry) {
        this.geometry = geometry;

        boundingDimension = 0.0;
        double leftBound = 0.0, rightBound = 0.0;
        double topBound = 0.0, bottomBound = 0.0;

        for (int idx = 0; idx < geometry.length; idx++) {
            double boundSize = geometry[idx].boundDimension 
                    + 2.0 * Math.sqrt(geometry[idx].offsetX * geometry[idx].offsetX 
                    + geometry[idx].offsetY * geometry[idx].offsetY);
            if (boundSize > boundingDimension) {
                boundingDimension = boundSize;
            }
            
            if (geometry[idx] instanceof Box) {
                Box box = (Box) geometry[idx];
                if (box.offsetX - box.width / 2 < leftBound) {
                    leftBound = box.offsetX - box.width / 2;
                }
                if (box.offsetX + box.width / 2 > rightBound) {
                    rightBound = box.offsetX + box.width / 2;
                }
                if (box.offsetY - box.height / 2 < bottomBound) {
                    bottomBound = box.offsetY - box.height / 2;
                }
                if (box.offsetY + box.height / 2 > topBound) {
                    topBound = box.offsetY + box.height / 2;
                }
            } else {
                Circle circle = (Circle) geometry[idx];
                if (circle.offsetX - circle.radius < leftBound) {
                    leftBound = circle.offsetX - circle.radius;
                }
                if (circle.offsetX + circle.radius > rightBound) {
                    rightBound = circle.offsetX + circle.radius;
                }
                if (circle.offsetY - circle.radius < bottomBound) {
                    bottomBound = circle.offsetY - circle.radius;
                }
                if (circle.offsetY + circle.radius > topBound) {
                    topBound = circle.offsetY + circle.radius;
                }
            }
        }

        width = rightBound - leftBound;
        height = topBound - bottomBound;
    }

    /**
     * Set the realisation to that specified and based upon the graphical
     * realisation derive a corresponding geometry (i.e. the geometry
     * will reflect the offset and size of the realisation)
     *
     * @param graphicalAsset GraphicalAsset to use as the realisation
     */    
    public final void setRealisationAndGeometry(GraphicalAsset graphicalAsset) {
        setRealisation(graphicalAsset);
        setGeometry(new Box(0, 0, graphicalAsset.width, graphicalAsset.height));
    }

    /**
     * Set the realisation to that specified and based upon the graphical
     * realisation derive a corresponding geometry (i.e. the geometry
     * will reflect the offset and size of the realisation)
     *
     * @param assetName String name of the asset to use as the realisation
     */        
    public final void setRealisationAndGeometry(String assetName) {
        GraphicalAsset graphicalAsset = assetManager.retrieveGraphicalAsset(assetName);
        if (graphicalAsset == null) {
            throw new IllegalArgumentException("GameObject.setRealisationAndGeometry: " +
                    "NULL asset retrieved for: " + assetName);
        }
        
        setRealisationAndGeometry(graphicalAsset);
    }

    /**
     * Set the realisation to that specified and based upon the graphical
     * realisation derive a corresponding geometry (i.e. the geometry
     * will reflect the offset and size of the realisation)
     *
     * @param graphicalAssets Graphical asset array to use as the realisation
     */        
    public final void setRealisationAndGeometry(GraphicalAsset[] graphicalAssets) {
        setRealisation(graphicalAssets);

        Shape[] geometry = new Shape[graphicalAssets.length];
        for (int shapeIdx = 0; shapeIdx < geometry.length; shapeIdx++) {
            geometry[shapeIdx] = new Box(
                    graphicalAssets[shapeIdx].offsetX, graphicalAssets[shapeIdx].offsetY, 
                    graphicalAssets[shapeIdx].width, graphicalAssets[shapeIdx].height);
        }
        
        setGeometry(geometry);
    }

    /**
     * Set the realisation to that specified and based upon the graphical
     * realisation derive a corresponding geometry (i.e. the geometry
     * will reflect the offset and size of the realisation)
     *
     * @param assetNames String array of asset names to use
     * @param assetXOffsets integer array of asset x offsets
     * @param assetYOffsets integer array of asset y offsets
     */        
    public final void setRealisationAndGeometry(
            String[] assetNames, int[] assetXOffsets, int[] assetYOffsets) {
        // Attempt to retreive the specified assets
        GraphicalAsset[] graphicalAssets = new GraphicalAsset[assetNames.length];
        for (int idx = 0; idx < graphicalAssets.length; idx++) {
            graphicalAssets[idx] 
                    = assetManager.retrieveGraphicalAsset(assetNames[idx]);
            
            if (graphicalAssets[idx] == null) {
                throw new IllegalArgumentException(
                        "GameObject.setRealisationAndGeometry:" + 
                        "NULL asset retrieved for: " + assetNames[idx]);
            }
            
            graphicalAssets[idx].offsetX = assetXOffsets[idx];
            graphicalAssets[idx].offsetY = assetYOffsets[idx];
        }

        setRealisationAndGeometry(graphicalAssets);
    }

    /**
     * Clear the geometry associated with this game objet
     */        
    public final void clearGeometry() {
        geometry = new Shape[0];
        boundingDimension = 0.0;
        width = 0.0;
        height = 0.0;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods: Draw methods                                                 //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Draw the graphical game object to the specified graphics object
     * <P>
     * Note: The object is drawn at the current x and y offsets
     *
     * @param  graphics2D Graphics2D object to use to render the object
     */
    public void draw(Graphics2D graphics2D) {
        draw(graphics2D, (int) x, (int) y);
    }

    /**
     * Draw the graphical game object to the specified graphics object
     * at the specified x and y offset
     *
     * @param  graphics2D Graphics2D object to use to render the object
     * @param  drawX integer x draw offset
     * @param  drawY integer y draw offset
     */
    public void draw(Graphics2D graphics2D, int drawX, int drawY) {
        if (graphicalRealisation.length == 0) {
            return;
        }
        
        // Record the original graphics affine transform
        AffineTransform originalAffineTransofrm = graphics2D.getTransform();

        // Create a new affine rotational transform centered on the
        // middle of the object and use this for the graphics object
        AffineTransform rotationAffineTransform = new AffineTransform();
        rotationAffineTransform.rotate(rotation, drawX, drawY);
        graphics2D.transform(rotationAffineTransform);

        for (int idx = 0; idx < graphicalRealisation.length; idx++) {
            graphicalRealisation[idx].draw(graphics2D, 
                    drawX + (int) graphicalRealisation[idx].offsetX 
                        - graphicalRealisation[idx].width / 2, 
                    drawY + (int) graphicalRealisation[idx].offsetY 
                        - graphicalRealisation[idx].height / 2);
        }
        
        // Restore the original graphics affine transform
        graphics2D.setTransform(originalAffineTransofrm);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods: Bounding region and intersection methods                     //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Determine and return the smallest Rectangle that encloses the
     * graphical game object
     *
     * @return Rectangle smallest rectangular bound enclosing this object
     */
    public final Rectangle getBoundingRectangle() {
        double rotmc1x = Math.cos(rotation);
        double rotmc1y = Math.sin(rotation);
        double rotmc2x = -rotmc1y;
        double rotmc2y = rotmc1x;

        double hx = width * 0.5;
        double hy = height * 0.5;

        double leftBound = 0;
        double rightBound = 0;
        double topBound = 0;
        double bottomBound = 0;

        for (int xSign = -1; xSign <= 1; xSign += 2) {
            for (int ySign = -1; ySign <= 1; ySign += 2) {
                double xPos = rotmc1x * hx * xSign + rotmc2x * hy * ySign;
                double yPos = rotmc1y * hx * xSign + rotmc2y * hy * ySign;

                if (xPos < leftBound) {
                    leftBound = xPos;
                } else if (xPos > rightBound) {
                    rightBound = xPos;
                }
                if (yPos < bottomBound) {
                    bottomBound = yPos;
                } else if (yPos > topBound) {
                    topBound = yPos;
                }
            }
        }
        return new Rectangle((int) (x+leftBound), (int) (y+bottomBound), 
                (int) (rightBound-leftBound), (int) (topBound-bottomBound));
    }

    /**
     * Determine and return the current width of this game object based upon
     * the current rotation
     *
     * @return double current width of this game object based on the current
     *         game object width and rotation
     */
    public final double getRotatedWidth() {
        return width * Math.abs(Math.cos(rotation)) 
                + height * Math.abs(Math.sin(rotation));
    }

    /**
     * Determine and return the current height of this game object based upon
     * the current rotation
     *
     * @return double current height of this game object based on the current
     *         game object height and rotation
     */
    public final double getRotatedHeight() {
        return height * Math.abs(Math.cos(rotation)) + width * Math.abs(Math.sin(rotation));
    }

    
    ///////////////////////////////////////////////////////////////////////////
    // Methods: Game object serialisation                                    //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Serialise the game object state to the specified output stream. Note,
     * it is intended that this function be overloaded within extending classes
     * if serialisation is required.
     *
     * @param stream ObjectOutputStream that the object should be stored to
     * @param gameObject gameObject reference to be serialised
     * 
     * @exception IOException if the game object cannot be written
     */
    public void serialiseGameObjectState(
            ObjectOutputStream stream, GameObject gameObject) throws IOException {
        GameObjectSerialiser.serialiseGameObjectState(stream, gameObject);
    }

    /**
     * Reconstitute the game object state from the serialised state. Note,
     * it is intended that this function be overloaded within extending classes
     * if object loading is required.
     *
     * @param stream ObjectInputStream that the object should be loaded from
     * @param gameLayer Game layer reference to be which the loaded game object
     *        should be added
     * 
     * @exception IOException if the game object cannot be loaded
     */
    public void loadGameObjectState(
            ObjectInputStream stream, GameLayer gameLayer) throws IOException {
        GameObjectSerialiser.loadGameObjectState(stream, gameLayer, this);
    }
}