package game.physics;

import game.engine.*;
import game.assets.*;
import game.geometry.*;
import java.util.*;

/**
 * The BodyBuilder class provides a number of utility methods that can be used
 * to build connected bodies. Currently, the class only support rectangular  
 * bodies that comprise of an array of smaller, connected  rectangular shapes.
 * <P>
 * Note: The physics engine within this code repository draws upon the
 * work of two individuals. See the CollisionSpace class header for
 * additional information.
 * <P>
 * @version $Revision: 1.0 $ $Date: 2007/08 $
 */
public final class BodyBuilder {
    
    ///////////////////////////////////////////////////////////////////////////
    // Class data                                                            //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Enumerated type used to denote the face of a connected box
     */
    public enum Face { Top, Bottom, Left, Right }

    
    ///////////////////////////////////////////////////////////////////////////
    // Methods:                                                              //
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Build a fully connected body consisting of boxWidth by boxHeight 
     * connected boxes, with the specified breaking impulse, impulse propagation
     * and impulse brittleness. Each box will be graphical represented by 
     * the specified graphical asset and take on the geometry of ths box.
     *
     * @param boxWidth width of the body in terms of individual boxes
     * @param boxHeight height of the body in terms of individual boxes
     * @param mass mass of the created body
     * @param breakingImpulse for the body
     * @param breakingImpulsePropagation for the body
     * @param breakingImpulseBrittleness for the body
     * @param boxGraphicalAssetName graphical asset and geometry to use as
     *        the base building box
     * @param gameLayer GameLayer to which the box object will belong
     * @return Body instance created by this method
     */
    public static final Body buildConnectedBoxBody( 
            int boxWidth, int boxHeight, double mass, 
            double breakingImpulse, double breakingImpulsePropagation, 
            double breakingImpulseBrittleness,            
            String boxGraphicalAssetName, GameLayer gameLayer ) {
        
        return buildConnectedBoxBody( boxWidth, boxHeight, mass, 
                breakingImpulse, breakingImpulsePropagation, breakingImpulseBrittleness,
                new String[] { boxGraphicalAssetName }, gameLayer );
    }    
        
    /**
     * Build a fully connected body consisting of boxWidth by boxHeight 
     * connected boxes, with the specified breaking impulse, impulse propagation
     * and impulse brittleness. Each box will be randomly consist of one of 
     * graphical representations drawn from the specified asset array.
     * <P>
     * Note: it is assumed that the geometry of each graphical box asset will
     * be the same across all boxes in the array.
     *
     * @param boxWidth width of the body in terms of individual boxes
     * @param boxHeight height of the body in terms of individual boxes
     * @param mass mass of the created body
     * @param breakingImpulse for the body
     * @param breakingImpulsePropagation for the body
     * @param breakingImpulseBrittleness for the body
     * @param boxGraphicalAssetName graphical asset and geometry array to 
     *        select from as the base building boxes
     * @param gameLayer GameLayer to which the box object will belong
     * @return Body instance created by this method
     */    
    public static final Body buildConnectedBoxBody( 
            int boxWidth, int boxHeight, double mass, 
            double breakingImpulse, double breakingImpulsePropagation, 
            double breakingImpulseBrittleness,            
            String boxGraphicalAssetNames[], GameLayer gameLayer ) {

        Body body = buildBoxBody( boxWidth, boxHeight, mass, breakingImpulse,
                breakingImpulsePropagation, breakingImpulseBrittleness, 
                boxGraphicalAssetNames, false, -1, -1, 
                gameLayer );
                      
        return fullyConnectBody( boxWidth, boxHeight, body );
    }
            
    /**
     * Build a fully connected body consisting of boxWidth by boxHeight 
     * connected boxes, with the specified breaking impulse, impulse propagation
     * and impulse brittleness. Each box will be constructed of an ordered
     * sequence of assets drawn from the specified asset array, adopting a
     * rectangular structure. For example, given a large seamless tile which 
     * is separated  into an array of 9 individual images, it would be 
     * possible to construct a larger seamlessly tiled image comprising of 
     * multiple 3x3 collections of individual assets.
     * <P>
     * Note: it is assumed that the geometry of each graphical box asset will
     * be the same across all boxes in the array.
     *
     * @param boxWidth width of the body in terms of individual boxes
     * @param boxHeight height of the body in terms of individual boxes
     * @param mass mass of the created body
     * @param breakingImpulse for the body
     * @param breakingImpulsePropagation for the body
     * @param breakingImpulseBrittleness for the body
     * @param boxGraphicalAssetName graphical asset and geometry array to 
     *        select from as the base building boxes
     * @param assetArrayWidth number of images wide that boxGraphicalAssetName
     *        should be considered as containing
     * @param assetArrayHeight number of images high that boxGraphicalAssetName
     *        should be considered as containing     
     * @param gameLayer GameLayer to which the box object will belong
     * @return Body instance created by this method
     */        
    public static final Body buildConnectedBoxBody( 
            int boxWidth, int boxHeight, double mass, 
            double breakingImpulse, double breakingImpulsePropagation,
            double breakingImpulseBrittleness,            
            String boxGraphicalAssetNames[], 
            int assetArrayWidth, int assetArrayHeight, GameLayer gameLayer ) {

        Body body = buildBoxBody( boxWidth, boxHeight, mass, breakingImpulse,
                breakingImpulsePropagation, breakingImpulseBrittleness, 
                boxGraphicalAssetNames, true, assetArrayWidth, assetArrayHeight, 
                gameLayer );
                      
        return fullyConnectBody( boxWidth, boxHeight, body );
    }    

    /**
     * Build a basic unconnected box body 
     *
     * @param boxWidth width of the body in terms of individual boxes
     * @param boxHeight height of the body in terms of individual boxes
     * @param mass mass of the created body
     * @param breakingImpulse for the body
     * @param breakingImpulsePropagation for the body
     * @param breakingImpulseBrittleness for the body
     * @param boxGraphicalAssetName graphical asset and geometry array to 
     *        select from as the base building boxes
     * @param fixedOrder boolean flag determining if the boxes images
     *        selected to build the image should be selected at random from
     *        the asset array or in a fixed order
     * @param assetArrayWidth number of images wide that boxGraphicalAssetName
     *        should be considered as containing
     * @param assetArrayHeight number of images high that boxGraphicalAssetName
     *        should be considered as containing     
     * @param gameLayer GameLayer to which the box object will belong
     * @return Body instance created by this method
     */        
    private static final Body buildBoxBody( 
            int boxWidth, int boxHeight, double mass, 
            double breakingImpulse, double breakingImpulsePropagation, 
            double breakingImpulseBrittleness,            
            String boxGraphicalAssetNames[], boolean fixedOrder, 
            int assetArrayWidth, int assetArrayHeight,
            GameLayer gameLayer ) {

        Random randomiser = new Random();
        
        GraphicalAsset realisation[] = new GraphicalAsset[ boxWidth*boxHeight ];
        Shape geometry[] = new Shape[boxWidth*boxHeight];
                
        for( int heightIdx = 0; heightIdx < boxHeight; heightIdx++ )
            for( int widthIdx = 0; widthIdx < boxWidth; widthIdx++ )
            {
                GraphicalAsset boxImage;
                if( fixedOrder ) {
                    int arrayWidthIdx = widthIdx%assetArrayWidth;
                    int arrayHeightIdx = heightIdx%assetArrayHeight;
                    boxImage = gameLayer.assetManager.retrieveGraphicalAsset( 
                            boxGraphicalAssetNames[
                                arrayHeightIdx*assetArrayWidth+arrayWidthIdx] );
                }
                else
                    boxImage = gameLayer.assetManager.retrieveGraphicalAsset( 
                            boxGraphicalAssetNames[
                                randomiser.nextInt(boxGraphicalAssetNames.length)] );
            
                double offsetX = (widthIdx+0.5)*boxImage.width 
                        - boxWidth*boxImage.width/2;
                double offsetY = (heightIdx+0.5)*boxImage.height 
                        - boxHeight*boxImage.height/2;
                
                realisation[heightIdx*boxWidth+widthIdx] = boxImage;
                realisation[heightIdx*boxWidth+widthIdx].offsetX = offsetX;
                realisation[heightIdx*boxWidth+widthIdx].offsetY = offsetY;
                
                geometry[heightIdx*boxWidth+widthIdx] 
                        = new Box( offsetX, offsetY, boxImage.width, boxImage.height );
            }

        Body body = new Body( gameLayer );
        body.setMass( mass );
        body.setRealisation( realisation );
        body.setGeometry( geometry );
        
        body.setBreakingImpulse( breakingImpulse );
        body.setBreakingImpulseBrittleness( breakingImpulseBrittleness );
        body.setBreakingImpulsePropagation( breakingImpulsePropagation );
 
        return body;
    }
        
    /**
     * Fully connect the geometry within the specified body, interpreted as
     * consisting of an array of connected boxes
     *
     * @param boxWidth width of the body in terms of individual boxes
     * @param boxHeight height of the body in terms of individual boxes
     * @param body Body instance to be connected
     * @return Body instance modified by this method
     */        
    private static Body fullyConnectBody( int boxWidth, int boxHeight, Body body ) {
        for( int heightIdx = 0; heightIdx < boxHeight; heightIdx++ )
            for( int widthIdx = 0; widthIdx < boxWidth; widthIdx++ )
            {
                if( widthIdx < boxWidth -1 )
                    body.connectShape( body.geometry[heightIdx*boxWidth+widthIdx], 
                            body.geometry[heightIdx*boxWidth+widthIdx+1] );
                
                if( heightIdx < boxHeight-1 )
                    body.connectShape( body.geometry[heightIdx*boxWidth+widthIdx], 
                            body.geometry[(heightIdx+1)*boxWidth+widthIdx] );   
            }                        
                        
        return body;        
    }
    
    /**
     * This method takes as input two bodies with the specified geometry
     * and connects the bodies together at the specified shape (assumed to 
     * be a box) by creating and adding a joint between the specified box
     * faces.
     *
     * @param collisionSpace CollisionSpace to which the joint will be added
     * @param body1 first body to be connected
     * @param body1GeometryWidth geometry width of the first body
     * @param body1GeometryHeight geometry height of the first body
     * @param body1Face face of the first body to be connected
     * @param body2 second body to be connected
     * @param body2GeometryWidth geometry width of the second body
     * @param body2GeometryHeight geometry height of the second  body
     * @param body2Face face of the second body to be connected
     * @param breakingImpulse for the created joint
     * @param numConnections number of connecting joints
     */        
    public static void connectRectangularBodies( CollisionSpace collisionSpace,
            Body body1, int body1GeometryWidth, int body1GeometryHeight, Face body1Face,
            Body body2, int body2GeometryWidth, int body2GeometryHeight, Face body2Face,
            double breakingImpulse, int numConnections )
    {
        if( numConnections == 1 ) {
            int body1GeometryIdx = 0;
            switch( body1Face )
            {
                case Top: 
                    body1GeometryIdx = body1GeometryWidth/2; break;
                case Bottom: 
                    body1GeometryIdx = body1GeometryWidth * (body1GeometryHeight-1) 
                        + body1GeometryWidth/2; break;
                case Left: 
                    body1GeometryIdx = body1GeometryWidth 
                        * (body1GeometryHeight/2); break;
                case Right:
                    body1GeometryIdx = body1GeometryWidth * (body1GeometryHeight/2)
                        + body1GeometryWidth-1; break;                
            }
            
            int body2GeometryIdx = 0;
            switch( body2Face )
            {
                case Top: 
                    body2GeometryIdx = body2GeometryWidth/2; break;
                case Bottom: 
                    body2GeometryIdx = body2GeometryWidth * (body2GeometryHeight-1) 
                        + body2GeometryWidth/2; break;
                case Left: 
                    body2GeometryIdx = body2GeometryWidth 
                        * (body2GeometryHeight/2); break;
                case Right:
                    body2GeometryIdx = body2GeometryWidth * (body2GeometryHeight/2)
                        + body2GeometryWidth-1; break;                
            }
            
            Joint joint = new FixedJoint( collisionSpace,
                    body1.geometry[body1GeometryIdx], 
                    body2.geometry[body2GeometryIdx] );
            joint.setBreakingImpulse( breakingImpulse );
            
            collisionSpace.addJoint( joint );
        }
        else
        {
            double body1Spacing;
            if( body1Face == Face.Top || body1Face == Face.Bottom )
                body1Spacing = (double)(body1GeometryWidth-1) / (numConnections-1);
            else
                body1Spacing = (double)(body1GeometryHeight-1) / (numConnections-1);

            double body2Spacing;
            if( body2Face == Face.Top || body2Face == Face.Bottom )
                body2Spacing = (double)(body2GeometryWidth-1) / (numConnections-1);
            else
                body2Spacing = (double)(body2GeometryHeight-1) / (numConnections-1);            
            
            for( int jointIdx = 0; jointIdx < numConnections; jointIdx++ )
            {
                int body1GeometryIdx = 0;
                switch( body1Face )
                {
                    case Top: 
                        body1GeometryIdx = (int)(jointIdx*body1Spacing); break;
                    case Bottom: 
                        body1GeometryIdx = body1GeometryWidth 
                                * (body1GeometryHeight-1) 
                                + (int)(jointIdx*body1Spacing); break;
                    case Left: 
                        body1GeometryIdx = body1GeometryWidth 
                                * (int)(jointIdx*body1Spacing); break;
                    case Right:
                        body1GeometryIdx = body1GeometryWidth 
                                * (int)(jointIdx*body1Spacing)
                                + body1GeometryWidth-1; break;                
                }

                int body2GeometryIdx = 0;
                switch( body2Face )
                {
                    case Top: 
                        body2GeometryIdx = (int)(jointIdx*body2Spacing); break;
                    case Bottom: 
                        body2GeometryIdx = body2GeometryWidth 
                                * (body2GeometryHeight-1) 
                                + (int)(jointIdx*body2Spacing); break;
                    case Left: 
                        body2GeometryIdx = body2GeometryWidth 
                                * (int)(jointIdx*body2Spacing); break;
                    case Right:
                        body2GeometryIdx = body2GeometryWidth 
                                * (int)(jointIdx*body2Spacing)
                                + body2GeometryWidth-1; break;                
                }
                
                Joint joint = new FixedJoint( collisionSpace,
                        body1.geometry[body1GeometryIdx], 
                        body2.geometry[body2GeometryIdx] );
                joint.setBreakingImpulse( breakingImpulse );
            
                collisionSpace.addJoint( joint );
            }
        }
    }
}