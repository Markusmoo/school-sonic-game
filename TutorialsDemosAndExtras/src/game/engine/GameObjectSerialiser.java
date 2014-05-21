package game.engine;

import java.io.*;
import game.assets.*;
import java.lang.reflect.*;
import game.geometry.*;
import java.awt.Font;
import java.awt.Color;
import game.physics.*;

/**
 * GameObjectSerialiser provides object serialisation methods for game objects.
 * <P>
 * Whilst Java provides built in serialisation for objects it cannot be readily
 * applied to game objects as it will serialise all graphical and sound assets
 * associated with the game object (in fact, as each game object contains a link
 * to the asset manager, the default serialisation process will completely save
 * the contents of the entire asset manager for each serialised game object).
 * <P> 
 * The methods contained within the class provide a means of serialising game
 * objects based upon the notion of building, or reforming, game objects, e.g.
 * each serialised game object records details of the associated assets which
 * are relinked upon object load. This ensure that the asset manager continues
 * to have full control over loaded assets.
 *
 * @author <A HREF="mailto:P.Hanna@qub.ac.uk">Philip Hanna</A>
 * @version $Revision: 1 $ $Date: 2007/08 $
 *
 * @see GameObject
 * @see Body
 */
public final class GameObjectSerialiser {
    
    ///////////////////////////////////////////////////////////////////////////
    // Methods: GameObject/BOdy Serialisation / Loading                      //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Serialise the specified game object (or body) to the specified object 
     * output stream
     *
     * @param  stream ObjectOutputStream to store the game object
     * @param  gameObject GameObject (or Body) to be serialised
     * 
     * @exception IOException if the GameObject cannot be serialised
     */
    public static final void serialiseGameObject(
            ObjectOutputStream stream, GameObject gameObject) throws IOException {
        stream.writeUTF(gameObject.getClass().getName());
        
        // Get the specified game object to save the game object state. It is 
        // intended that each specialised game object will actually call
        // the GameObjectSerialiser.serialiseGameObjectState to save core game
        // object or body variables, but also to extend the save to support
        // any extended variables, etc.
        gameObject.serialiseGameObjectState(stream, gameObject);
    }

    /**
     * Store the specified game object (or body) state to the specified object 
     * output stream
     *
     * @param  stream ObjectOutputStream to store the game object state
     * @param  gameObject GameObject (or Body) to be serialised
     * 
     * @exception IOException if the GameObject cannot be serialised
     */
    public static final void serialiseGameObjectState(
            ObjectOutputStream stream, GameObject gameObject) throws IOException {

        // Store core game object information
        stream.writeUTF(gameObject.gameObjectName);
        stream.writeDouble(gameObject.x);
        stream.writeDouble(gameObject.y);
        stream.writeDouble(gameObject.width);
        stream.writeDouble(gameObject.height);
        stream.writeDouble(gameObject.rotation);
        stream.writeDouble(gameObject.boundingDimension);
        stream.writeDouble(gameObject.velocityx);
        stream.writeDouble(gameObject.velocityy);
        stream.writeDouble(gameObject.angularVelocity);
        stream.writeInt(gameObject.drawOrder);
        stream.writeBoolean(gameObject.canIntersectOtherGraphicalObjects);

        // Store details of the game object realisation
        stream.writeInt(gameObject.graphicalRealisation.length);
        for (int idx = 0; idx < gameObject.graphicalRealisation.length; idx++) {
            try {
                // Invoke the appropraite graphical asset serialisation method
                String serialiseMethodName = "serialise" 
                        + gameObject.graphicalRealisation[idx].getClass().getSimpleName();
                Method serialiseMethod = GameObjectSerialiser.class.getMethod(
                        serialiseMethodName, Class.forName("java.io.ObjectOutputStream"), 
                        Class.forName("game.assets.Asset"));
                serialiseMethod.invoke(null, stream, gameObject.graphicalRealisation[idx]);
            } catch (NoSuchMethodException exception) {
                System.out.println("GameObjectSerialiser.serialiseGameObject : " 
                        + "Asset serialiser not available for: " 
                        + gameObject.graphicalRealisation[idx].getClass().getSimpleName());
            } catch (ClassNotFoundException exception) {
                exception.printStackTrace();
            } catch (IllegalAccessException exception) {
                exception.printStackTrace();
            } catch (InvocationTargetException exception) {
                exception.printStackTrace();
            }
        }

        // Store details of the game object geometry
        stream.writeInt(gameObject.geometry.length);
        for (int idx = 0; idx < gameObject.geometry.length; idx++) {
            try {
                // Invoke the appropraite shape geometry serialisation method
                String serialiseMethodName = "serialise" 
                        + gameObject.geometry[idx].getClass().getSimpleName();
                Method serialiseMethod = GameObjectSerialiser.class.getMethod(
                        serialiseMethodName, Class.forName("java.io.ObjectOutputStream"), 
                        Class.forName("game.geometry.Shape"));
                serialiseMethod.invoke(null, stream, gameObject.geometry[idx]);
            } catch (NoSuchMethodException exception) {
                System.out.println("GameObjectSerialiser.serialiseGameObject : " 
                        + "Shape serialiser not available for: " 
                        + gameObject.geometry[idx].getClass().getSimpleName());
            } catch (ClassNotFoundException exception) {
                exception.printStackTrace();
            } catch (IllegalAccessException exception) {
                exception.printStackTrace();
            } catch (InvocationTargetException exception) {
                exception.printStackTrace();
            }
        }

        // If needed store the game object collection for this game object
        if (gameObject.gameObjectCollection == null) {
            stream.writeUTF("NULL");
        } else {
            stream.writeUTF(gameObject.gameObjectCollection.gameObjectCollectionName);
        }
        
        // If the game object is an instance of the Body class then store core 
        // Body information
        if (gameObject instanceof Body) {

            Body body = (Body) gameObject;

            stream.writeDouble(body.forcex);
            stream.writeDouble(body.forcey);
            stream.writeDouble(body.torque);
            stream.writeDouble(body.friction);
            stream.writeDouble(body.restitution);

            stream.writeDouble(body.getMass());
            stream.writeBoolean(body.gravityEffected);
            stream.writeBoolean(body.permitAtRest);

            stream.writeDouble(body.breakingImpulse);
            stream.writeDouble(body.breakingImpulsePropagation);
            stream.writeDouble(body.breakingImpulseBrittleness);
            stream.writeDouble(body.breakingImpulseAssumedBodyMass);
            stream.writeDouble(body.breakingImpulseImmovableIntegrityFactor);
            stream.writeBoolean(body.breakingImpulseImmovableDestroyMoveable);
            stream.writeBoolean(body.breakingImpulseDestroyBody);

            stream.writeInt(body.shapeConnections.length);
            stream.writeInt(body.MAX_SINGLE_SHAPE_CONNECTIONS);
            for (int idx = 0; idx < body.shapeConnections.length; idx++) {
                for (int idx2 = 0; idx2 < body.MAX_SINGLE_SHAPE_CONNECTIONS; idx2++) {
                    if (body.shapeConnections[idx][idx2] == null) {
                        stream.writeInt(-1);
                    } else {
                        stream.writeInt((body.shapeConnections[idx][idx2]).uniqueShapeID);
                    }
                }
            }
        }
    }

    /**
     * Load a GameObject from the specified input stream into the specified 
     * game layer
     *
     * @param  stream ObjectInputStream to load the game object
     * @param  gameLayer GameLayer to store the game object
     * 
     * @exception IOException if the game object cannot be loaded
     */    
    public static final void loadGameObjectIntoLayer(
            ObjectInputStream stream, GameLayer gameLayer) throws IOException {
        gameLayer.addGameObject(loadGameObject(stream, gameLayer));
    }

    /**
     * Load and return a GameObject from the specified input stream 
     *
     * @param  stream ObjectInputStream to load the game object
     * @param  gameLayer GameLayer reference for loading the game object
     * @return GameObject loaded from the input stream
     * 
     * @exception IOException if the game object cannot be loaded
     */    
    public static final GameObject loadGameObject(
            ObjectInputStream stream, GameLayer gameLayer) throws IOException {

        GameObject gameObject = null;
        String gameObjectClassName = stream.readUTF();

        // Based upon the type of loaded class, create an appropraite type of class 
        try {
            Class gameObjectClass = Class.forName(gameObjectClassName);
            Constructor gameObjectClassConstructor = 
                    gameObjectClass.getConstructor(Class.forName("game.engine.GameLayer"));
            gameObject = (GameObject) gameObjectClassConstructor.newInstance( gameLayer );
        } catch (ClassNotFoundException exception) {
            exception.printStackTrace();
        } catch (NoSuchMethodException exception) {
            exception.printStackTrace();
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
        } catch (InvocationTargetException exception) {
            exception.printStackTrace();
        } catch (InstantiationException exception) {
            System.out.println("GameObjectSerialiser.loadGameObjectIntoLayer: " + 
                    "ERROR - If using game object serialisation there must be " +
                    "a GameLayer only constructor.");
            exception.printStackTrace();
        }

        // Get the created game object to load the game object state. It is 
        // intended that each specialised game object will actually call
        // the GameObjectSerialiser.loadGameObjectState to load core game
        // object or body variables, but also to extend the load to support
        // any extended variables, etc.
        gameObject.loadGameObjectState(stream, gameLayer);

        return gameObject;
    }

    /**
     * Load a GameObject state from the specified input stream into the 
     * specified game object
     *
     * @param  stream ObjectInputStream to load the game object
     * @param  gameLayer GameLayer reference for loading the game object
     * @param  gameObject GameObject whose state is to be laoded
     * 
     * @exception IOException if the game object cannot be loaded
     */    
    public static final void loadGameObjectState(ObjectInputStream stream, 
            GameLayer gameLayer, GameObject gameObject) throws IOException {

        // Call the setName method to ensure game object ID is updated if needed
        gameObject.setName(stream.readUTF());

        gameObject.x = stream.readDouble();
        gameObject.y = stream.readDouble();
        gameObject.width = stream.readDouble();
        gameObject.height = stream.readDouble();
        gameObject.rotation = stream.readDouble();
        gameObject.boundingDimension = stream.readDouble();
        gameObject.velocityx = stream.readDouble();
        gameObject.velocityy = stream.readDouble();
        gameObject.angularVelocity = stream.readDouble();
        gameObject.drawOrder = stream.readInt();
        gameObject.canIntersectOtherGraphicalObjects = stream.readBoolean();

        // Load the game object's graphical representation
        int graphicalRealisationLength = stream.readInt();
        GraphicalAsset[] graphicalRealisation = new GraphicalAsset[graphicalRealisationLength];
        for (int idx = 0; idx < graphicalRealisationLength; idx++) {
            String loadMethodName = "load" + stream.readUTF();
            try {
                Method serialiseMethod = GameObjectSerialiser.class.getMethod(
                        loadMethodName, Class.forName("java.io.ObjectInputStream"), 
                        Class.forName("game.assets.AssetManager"));
                graphicalRealisation[idx] = (GraphicalAsset) serialiseMethod.invoke( 
                        null, stream, gameLayer.assetManager );
            } catch (NoSuchMethodException exception) {
                System.out.println("GameObjectSerialiser.loadGameObjectIntoLayer : " + 
                        " Asset loaded not available for: " + loadMethodName);
            } catch (ClassNotFoundException exception) {
                exception.printStackTrace();
            } catch (IllegalAccessException exception) {
                exception.printStackTrace();
            } catch (InvocationTargetException exception) {
                exception.printStackTrace();
            }
        }
        gameObject.setRealisation(graphicalRealisation);

        // Load the game object's geometry
        int geometryLength = stream.readInt();
        Shape[] geometry = new Shape[geometryLength];
        for (int idx = 0; idx < geometryLength; idx++) {
            String loadMethodName = "load" + stream.readUTF();
            try {
                Method serialiseMethod = GameObjectSerialiser.class.getMethod(
                        loadMethodName, Class.forName("java.io.ObjectInputStream"));
                geometry[idx] = (Shape) serialiseMethod.invoke( null, stream );
            } catch (NoSuchMethodException exception) {
                System.out.println("GameObjectSerialiser.loadGameObjectIntoLayer : " + 
                        " Shape loaded not available for: " + loadMethodName);
            } catch (ClassNotFoundException exception) {
                exception.printStackTrace();
            } catch (IllegalAccessException exception) {
                exception.printStackTrace();
            } catch (InvocationTargetException exception) {
                exception.printStackTrace();
            }
        }
        gameObject.setGeometry(geometry);

        // If needed, assign the game object to the specified game object collection
        // Also, if this game object is of type body, then register it with the 
        // Bodies game object collection
        String gameObjectCollectionName = stream.readUTF();
        if (gameObjectCollectionName.compareTo("NULL") != 0) {
            if (!(gameObject instanceof Body 
                    && gameObjectCollectionName.compareTo("Bodies") == 0)) {
                if (!gameLayer.hasGameObjectCollection(gameObjectCollectionName)) {
                    gameLayer.addGameObjectCollection(gameObjectCollectionName);
                }
                
                GameObjectCollection gameObjectCollection 
                        = gameLayer.getGameObjectCollection(gameObjectCollectionName);
                gameObjectCollection.add(gameObject);
                gameObject.gameObjectCollection = gameObjectCollection;
            }
        }

        // If this game object is a Body then load in core body information
        if (gameObject instanceof Body) {
            Body body = (Body) gameObject;

            body.forcex = stream.readDouble();
            body.forcey = stream.readDouble();
            body.torque = stream.readDouble();
            body.friction = stream.readDouble();
            body.restitution = stream.readDouble();
            body.setMass(stream.readDouble());

            // Call the set geometry using the current geometry to ensure surface
            // area, etc. are selected
            body.setGeometry(body.geometry);

            body.gravityEffected = stream.readBoolean();
            body.permitAtRest = stream.readBoolean();

            body.breakingImpulse = stream.readDouble();
            body.breakingImpulsePropagation = stream.readDouble();
            body.breakingImpulseBrittleness = stream.readDouble();
            body.breakingImpulseAssumedBodyMass = stream.readDouble();
            body.breakingImpulseImmovableIntegrityFactor = stream.readDouble();
            body.breakingImpulseImmovableDestroyMoveable = stream.readBoolean();
            body.breakingImpulseDestroyBody = stream.readBoolean();

            int geometryConnectionLength = stream.readInt();
            int geometryConnectionDepth = stream.readInt();
            Shape[][] shapeConnections 
                    = new Shape[geometryConnectionLength][geometryConnectionDepth];
            for (int idx = 0; idx < geometryConnectionLength; idx++) {
                for (int idx2 = 0; idx2 < geometryConnectionDepth; idx2++) {
                    int shapeConnectionID = stream.readInt();
                    if (shapeConnectionID == -1) {
                        shapeConnections[idx][idx2] = null;
                    } else {
                        int shapeIdx = 0;
                        int shapeConnector = -1;
                        while (shapeConnector == -1) {
                            if ((body.geometry[shapeIdx]).uniqueShapeID 
                                    == shapeConnectionID) {
                                shapeConnector = shapeIdx;
                            } else {
                                shapeIdx++;
                            }
                        }
                        shapeConnections[idx][idx2] = body.geometry[shapeConnector];
                    }
                }
            }
            body.setShapeConnections(shapeConnections);
        }
    }

    
    ///////////////////////////////////////////////////////////////////////////
    // Methods: Joint Serialisation / Loading                                //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Serialise the specified joint to the specified object output stream
     *
     * @param  stream ObjectOutputStream to store the joint
     * @param  joint Joint to be serialised
     * 
     * @exception IOException if the Joint cannot be serialised
     */
    public static final void serialiseJoint(
            ObjectOutputStream stream, Joint joint) throws IOException {
        // Store the type of Joint object and its connector type
        stream.writeUTF(joint.getClass().getSimpleName());
        stream.writeUTF("" + joint.connector);

        // Save the joint connector information
        if (joint.connector == Joint.Connector.BODY) {
            stream.writeUTF(joint.body1.gameObjectName);
            stream.writeUTF(joint.body2.gameObjectName);
        } else {
            stream.writeUTF(joint.shape1.body.gameObjectName);
            stream.writeInt(joint.shape1.uniqueShapeID);
            stream.writeUTF(joint.shape2.body.gameObjectName);
            stream.writeInt(joint.shape2.uniqueShapeID);
        }

        // If the joint is hinged, then save the anchor point and relaxation
        if (joint instanceof HingedJoint) {
            stream.writeDouble(((HingedJoint) joint).getAnchorX());
            stream.writeDouble(((HingedJoint) joint).getAnchorY());
            stream.writeDouble(((HingedJoint) joint).getRelaxation());
        }

        stream.writeDouble(joint.getBreakingImpulse());
    }

    /**
     * Load a Joint from the specified input stream into the specified 
     * collision space
     *
     * @param  stream ObjectInputStream to load the joint
     * @param  collisionSpace CollisionSpace to store the joint
     * 
     * @exception IOException if the Joint cannot be loaded
     */
    public static final void loadJointIntoLayer(
            ObjectInputStream stream, CollisionSpace collisionSpace) throws IOException {
        collisionSpace.addJoint(loadJoint(stream, collisionSpace));
    }

    /**
     * Load a Joint and return a joint from the specified input stream 
     *
     * @param  stream ObjectInputStream to load the joint
     * @param  collisionSpace CollisionSpace reference space for the joint
     * @return Joint loaded from the input stream
     * 
     * @exception IOException if the Joint cannot be loaded
     */
    public static final Joint loadJoint(
            ObjectInputStream stream, CollisionSpace collisionSpace) throws IOException {

        Joint newJoint;

        String jointType = stream.readUTF();
        String connectorType = stream.readUTF();

        // Load in the joint details depending of the type of joint connector, 
        // i.e. between two bodies or two shapes 
        if (connectorType.compareTo("BODY") == 0) {
            GameObject gameObject1 = collisionSpace.getGameObject(stream.readUTF());
            GameObject gameObject2 = collisionSpace.getGameObject(stream.readUTF());
            if (gameObject1 == null || gameObject2 == null) {
                throw new NullPointerException("GameObjectSerialiser.loadJointIntoLayer" + 
                        " Cannot located game object specified in joint ");
            }
            if (jointType.compareTo("HingedJoint") == 0) {
                newJoint = new HingedJoint(collisionSpace, (Body) gameObject1, 
                        (Body) gameObject2, stream.readDouble(), stream.readDouble());
                ((HingedJoint)newJoint).setRelaxation(stream.readDouble());
            } else {
                newJoint = new FixedJoint(collisionSpace, (Body) gameObject1, (Body) gameObject2);
            }
        } else {
            GameObject gameObject1 = collisionSpace.getGameObject(stream.readUTF());
            int shape1Idx = stream.readInt();
            Shape shape1 = null;
            for (int idx = 0; idx < gameObject1.geometry.length && shape1 == null; idx++) {
                if (gameObject1.geometry[idx].uniqueShapeID == shape1Idx) {
                    shape1 = gameObject1.geometry[idx];
                }
            }
            GameObject gameObject2 = collisionSpace.getGameObject(stream.readUTF());
            int shape2Idx = stream.readInt();
            Shape shape2 = null;
            for (int idx = 0; idx < gameObject2.geometry.length && shape2 == null; idx++) {
                if (gameObject2.geometry[idx].uniqueShapeID == shape2Idx) {
                    shape2 = gameObject2.geometry[idx];
                }
            }
            
            if (jointType.compareTo("HingedJoint") == 0) {
                newJoint = new HingedJoint(collisionSpace, shape1, shape2, 
                        stream.readDouble(), stream.readDouble());
                ((HingedJoint)newJoint).setRelaxation(stream.readDouble());
            } else {
                newJoint = new FixedJoint(collisionSpace, shape1, shape2);
            }
        }

        newJoint.setBreakingImpulse(stream.readDouble());

        return newJoint;
    }

    
    ///////////////////////////////////////////////////////////////////////////
    // Methods: Geometry Serialisation / Loading                             //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Serialise the specified box to the specified object output stream
     *
     * @param  stream ObjectOutputStream to store the joint
     * @param  shape Box shape to be serialised
     * 
     * @exception IOException if the Box cannot be serialised
     */
    public static final void serialiseBox(
            ObjectOutputStream stream, Shape shape) throws IOException {
        Box box = (Box) shape;

        stream.writeUTF(box.getClass().getSimpleName());

        stream.writeDouble(box.offsetX);
        stream.writeDouble(box.offsetY);
        stream.writeDouble(box.width);
        stream.writeDouble(box.height);

        stream.writeInt(box.uniqueShapeID);
    }

    /**
     * Serialise the specified box to the specified object output stream
     *
     * @param  stream ObjectInputStream from which to load the shape
     * @return Box loaded Box shape
     * 
     * @exception IOException if the Box cannot be serialised
     * @exception ClassNotFoundException if the specified geometry cannot be found
     */
    public static final Box loadBox(
            ObjectInputStream stream) throws IOException, ClassNotFoundException {
        Box box = new Box(stream.readDouble(), stream.readDouble(), 
                stream.readDouble(), stream.readDouble());
        box.uniqueShapeID = stream.readInt();
        return box;
    }

    /**
     * Serialise the specified circle to the specified object output stream
     *
     * @param  stream ObjectOutputStream to store the joint
     * @param  shape Circle shape to be serialised
     * 
     * @exception IOException if the Circle cannot be serialised
     */
    public static final void serialiseCircle(
            ObjectOutputStream stream, Shape shape) throws IOException {
        Circle circle = (Circle) shape;

        stream.writeUTF(circle.getClass().getSimpleName());

        stream.writeDouble(circle.offsetX);
        stream.writeDouble(circle.offsetY);
        stream.writeDouble(circle.radius);

        stream.writeInt(circle.uniqueShapeID);
    }

    /**
     * Serialise the specified circle to the specified object output stream
     *
     * @param  stream ObjectInputStream from which to load the shape
     * @return Circle loaded Circle shape
     * 
     * @exception IOException if the Circle cannot be serialised
     * @exception ClassNotFoundException if the specified geometry cannot be found
     */
    public static final Circle loadCircle(
            ObjectInputStream stream) throws IOException, ClassNotFoundException {
        Circle circle = new Circle(
                stream.readDouble(), stream.readDouble(), stream.readDouble());
        circle.uniqueShapeID = stream.readInt();
        return circle;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Methods: Geometry Serialisation / Loading                             //
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Serialise the specified image asset to the specified object output stream
     *
     * @param  stream ObjectOutputStream to store the joint
     * @param  asset Asset to be serialised
     * 
     * @exception IOException if the asset cannot be serialised
     */
    public static final void serialiseImageAsset(
            ObjectOutputStream stream, Asset asset) throws IOException {
        ImageAsset imageAsset = (ImageAsset) asset;

        stream.writeUTF(imageAsset.getClass().getSimpleName());
        stream.writeUTF(imageAsset.getAssetName());

        stream.writeInt(imageAsset.width);
        stream.writeInt(imageAsset.height);
        stream.writeDouble(imageAsset.offsetX);
        stream.writeDouble(imageAsset.offsetY);
    }

    /**
     * Load the specified image asset from the specified object input stream
     *
     * @param  stream ObjectInputStream from which to load the asset
     * @param  assetManager AssetManager from which the archetype asset will
     *         be retrieved
     * @return ImageAsset loaded asset
     * 
     * @exception IOException if the asset cannot be loaded
     * @exception ClassNotFoundException if the specified asset type cannot be
     *            found
     */
    public static final ImageAsset loadImageAsset(
            ObjectInputStream stream, AssetManager assetManager) 
                throws IOException, ClassNotFoundException {

        String assetName = stream.readUTF();
        ImageAsset imageAsset = (ImageAsset) assetManager.retrieveAsset( assetName );

        imageAsset.width = stream.readInt();
        imageAsset.height = stream.readInt();
        imageAsset.offsetX = stream.readDouble();
        imageAsset.offsetY = stream.readDouble();

        return imageAsset;
    }

    /**
     * Serialise the specified image ribbon asset to the specified object output 
     * stream
     *
     * @param  stream ObjectOutputStream to store the joint
     * @param  asset Asset to be serialised
     * 
     * @exception IOException if the asset cannot be serialised
     */
    public static final void serialiseImageAssetRibbon(
            ObjectOutputStream stream, Asset asset) throws IOException {
        ImageAssetRibbon imageAssetRibbon = (ImageAssetRibbon) asset;

        stream.writeUTF(imageAssetRibbon.getClass().getSimpleName());
        stream.writeUTF(imageAssetRibbon.getAssetName());

        stream.writeInt(imageAssetRibbon.width);
        stream.writeInt(imageAssetRibbon.height);
        stream.writeDouble(imageAssetRibbon.offsetX);
        stream.writeDouble(imageAssetRibbon.offsetY);
        stream.writeInt(imageAssetRibbon.getViewPortX());
        stream.writeInt(imageAssetRibbon.getViewPortY());
    }

    /**
     * Load the specified image ribbon asset from the specified object input stream
     *
     * @param  stream ObjectInputStream from which to load the asset
     * @param  assetManager AssetManager from which the archetype asset will
     *         be retrieved
     * @return ImageAssetRibbon loaded asset
     * 
     * @exception IOException if the asset cannot be loaded
     * @exception ClassNotFoundException if the specified asset type cannot be
     *            found
     */
    public static final ImageAssetRibbon loadImageAssetRibbon(
            ObjectInputStream stream, AssetManager assetManager) 
                throws IOException, ClassNotFoundException {

        String assetName = stream.readUTF();
        ImageAssetRibbon imageAssetRibbon 
                = (ImageAssetRibbon) assetManager.retrieveAsset( assetName );

        imageAssetRibbon.width = stream.readInt();
        imageAssetRibbon.height = stream.readInt();
        imageAssetRibbon.offsetX = stream.readDouble();
        imageAssetRibbon.offsetY = stream.readDouble();
        imageAssetRibbon.setViewPort(stream.readInt(), stream.readInt());

        return imageAssetRibbon;
    }

    /**
     * Serialise the specified image ribbon sequence to the specified object output 
     * stream
     *
     * @param  stream ObjectOutputStream to store the joint
     * @param  asset Asset to be serialised
     * 
     * @exception IOException if the asset cannot be serialised
     */
    public static final void serialiseImageAssetSequence(
            ObjectOutputStream stream, Asset asset) throws IOException {
        ImageAssetSequence imageAssetSequence = (ImageAssetSequence) asset;

        stream.writeUTF(imageAssetSequence.getClass().getSimpleName());
        stream.writeUTF(imageAssetSequence.getAssetName());

        stream.writeInt(imageAssetSequence.width);
        stream.writeInt(imageAssetSequence.height);
        stream.writeDouble(imageAssetSequence.offsetX);
        stream.writeDouble(imageAssetSequence.offsetY);
        stream.writeInt(imageAssetSequence.getPlayCount());
        stream.writeInt(imageAssetSequence.getHomeFrame());
        stream.writeInt(imageAssetSequence.getCurrentFrame());
        stream.writeLong(imageAssetSequence.getAnimationPeriod());
    }

    /**
     * Load the specified image sequence asset from the specified object input stream
     *
     * @param  stream ObjectInputStream from which to load the asset
     * @param  assetManager AssetManager from which the archetype asset will
     *         be retrieved
     * @return ImageAssetSequence loaded asset
     * 
     * @exception IOException if the asset cannot be loaded
     * @exception ClassNotFoundException if the specified asset type cannot be
     *            found
     */
    public static final ImageAssetSequence loadImageAssetSequence(
            ObjectInputStream stream, AssetManager assetManager) 
                throws IOException, ClassNotFoundException {

        String assetName = stream.readUTF();
        ImageAssetSequence imageAssetSequence 
                = (ImageAssetSequence) assetManager.retrieveAsset( assetName );

        imageAssetSequence.width = stream.readInt();
        imageAssetSequence.height = stream.readInt();
        imageAssetSequence.offsetX = stream.readDouble();
        imageAssetSequence.offsetY = stream.readDouble();
        imageAssetSequence.setPlayCount(stream.readInt());
        imageAssetSequence.setHomeFrame(stream.readInt());
        imageAssetSequence.setCurrentFrame(stream.readInt());
        imageAssetSequence.setAnimationPeriod(stream.readLong());

        return imageAssetSequence;
    }

    /**
     * Serialise the specified image tile asset to the specified object output 
     * stream
     *
     * @param  stream ObjectOutputStream to store the joint
     * @param  asset Asset to be serialised
     * 
     * @exception IOException if the asset cannot be serialised
     */
    public static final void serialiseImageAssetTile(
            ObjectOutputStream stream, Asset asset) throws IOException {
        ImageAssetTile imageAssetTile = (ImageAssetTile) asset;

        stream.writeUTF(imageAssetTile.getClass().getSimpleName());
        stream.writeUTF(imageAssetTile.getAssetName());

        stream.writeInt(imageAssetTile.width);
        stream.writeInt(imageAssetTile.height);
        stream.writeDouble(imageAssetTile.offsetX);
        stream.writeDouble(imageAssetTile.offsetY);
        stream.writeInt(imageAssetTile.getViewPortX());
        stream.writeInt(imageAssetTile.getViewPortY());
    }

    /**
     * Load the specified image tile asset from the specified object input stream
     *
     * @param  stream ObjectInputStream from which to load the asset
     * @param  assetManager AssetManager from which the archetype asset will
     *         be retrieved
     * @return ImageAssetTile loaded asset
     * 
     * @exception IOException if the asset cannot be loaded
     * @exception ClassNotFoundException if the specified asset type cannot be
     *            found
     */
    public static final ImageAssetTile loadImageAssetTile(
            ObjectInputStream stream, AssetManager assetManager) 
                throws IOException, ClassNotFoundException {

        String assetName = stream.readUTF();
        ImageAssetTile imageAssetTile 
                = (ImageAssetTile) assetManager.retrieveAsset( assetName );

        imageAssetTile.width = stream.readInt();
        imageAssetTile.height = stream.readInt();
        imageAssetTile.offsetX = stream.readDouble();
        imageAssetTile.offsetY = stream.readDouble();
        imageAssetTile.setViewPort(stream.readInt(), stream.readInt());

        return imageAssetTile;
    }

    /**
     * Serialise the specified draw message asset to the specified object output 
     * stream
     *
     * @param  stream ObjectOutputStream to store the joint
     * @param  asset Asset to be serialised
     * 
     * @exception IOException if the asset cannot be serialised
     */
    public static final void serialiseDrawnAssetMessage(
            ObjectOutputStream stream, Asset asset) throws IOException {
        DrawnAssetMessage drawnAssetMessage = (DrawnAssetMessage) asset;

        stream.writeUTF(drawnAssetMessage.getClass().getSimpleName());

        stream.writeUTF(drawnAssetMessage.getAssetName());
        stream.writeUTF(drawnAssetMessage.getMessage());
        stream.writeObject(drawnAssetMessage.getFont());
        stream.writeObject(drawnAssetMessage.getColor());

        stream.writeInt(drawnAssetMessage.width);
        stream.writeInt(drawnAssetMessage.height);
        stream.writeDouble(drawnAssetMessage.offsetX);
        stream.writeDouble(drawnAssetMessage.offsetY);
        stream.writeBoolean(drawnAssetMessage.getIsMessageDisplayTimeEnabled());
        stream.writeLong(drawnAssetMessage.getMessageDisplayDuration());
        stream.writeLong(drawnAssetMessage.getMessageDisplayInitialPause());
    }

    /**
     * Load the specified draw message asset from the specified object input stream
     *
     * @param  stream ObjectInputStream from which to load the asset
     * @param  assetManager AssetManager from which the archetype asset will
     *         be retrieved
     * @return DrawnAssetMessage loaded asset
     * 
     * @exception IOException if the asset cannot be loaded
     * @exception ClassNotFoundException if the specified asset type cannot be
     *            found
     */
    public static final DrawnAssetMessage loadDrawnAssetMessage(
            ObjectInputStream stream, AssetManager assetManager) 
                throws IOException, ClassNotFoundException {

        String assetName = stream.readUTF();
        String messageText = stream.readUTF();
        Font messageFont = (Font) stream.readObject();
        Color messageColor = (Color) stream.readObject();

        DrawnAssetMessage drawnAssetMessage 
                = new DrawnAssetMessage(assetName, messageText, messageFont, messageColor);

        drawnAssetMessage.width = stream.readInt();
        drawnAssetMessage.height = stream.readInt();
        drawnAssetMessage.offsetX = stream.readDouble();
        drawnAssetMessage.offsetY = stream.readDouble();

        if (stream.readBoolean() == true) {
            drawnAssetMessage.enableMessageDisplayTime(stream.readLong(), stream.readLong());
        }
        return drawnAssetMessage;
    }
}