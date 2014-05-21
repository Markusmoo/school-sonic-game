package demos.GraphicalAssetDemos;

import game.assets.ImageAssetSequence;
import game.engine.*;

/**
 * This class extends the GameObjectSprite to provide a moving object that
 * randomly changes its animation
 */
public class BuilderSprite extends GameObject {
    
    // Define probability of changing to the specified state
    private static final double WALK_PROBABILITY = 0.01;
    private static final double WAIT_PROBABILITY = 0.03;
    private static final double HIT_PROBABILITY = 0.03;
    
    // Define the different states that the builder can be in
    public enum BuilderState { WALKING, WAITING, HITTING };
    private BuilderState builderState;
    
    // Movement speed of the builder
    private double builderSpeed;
    
    ImageAssetSequence walkingAnimation;
    ImageAssetSequence waitingAnimation;
    ImageAssetSequence hittingAnimation;
        
    public BuilderSprite( GameLayer gameLayer, int x, int y ) {
        super( gameLayer, x, y );
    
        // Set the draw order of this sprite so that it is drawn on top of the road
        setDrawOrder( 1 );
                
        // Give each builder a random (but similar) speed
        builderSpeed = gameEngine.randomiser.nextDouble() + 1.5;
        
        // Retrieve a new walking animation, change its playback speed to
        // take recognition of the builder speed and then add it to this sprite
        walkingAnimation = (ImageAssetSequence)
                assetManager.retrieveAsset( "BuilderWalking" );
        walkingAnimation.setAnimationPeriod(
                (int)(walkingAnimation.getAnimationPeriod() 
                    * builderSpeed ) );
                
        // Retrieve a new waiting animation, change its playback speed to
        // take recognition of the builder speed and then add it to this sprite
        waitingAnimation = (ImageAssetSequence)
                assetManager.retrieveAsset( "BuilderWaiting" );
        waitingAnimation.setAnimationPeriod(
                (int)(waitingAnimation.getAnimationPeriod() * (1.0/builderSpeed) ) );
        
        // Retrieve a new hitting animation, change its playback speed to
        // take recognition of the builder speed and then add it to this sprite
        hittingAnimation = (ImageAssetSequence)
                assetManager.retrieveAsset( "BuilderHitting" );
        hittingAnimation.setAnimationPeriod(
                (int)(hittingAnimation.getAnimationPeriod() * (1.0/builderSpeed) ) );
        
        // Initial the builder is in a waiting state, with a waiting animation
        builderState = BuilderState.WAITING;
        setRealisationAndGeometry( waitingAnimation );
    }
    
    @Override
    public void update() {
        
        graphicalRealisation[0].update();
        
        if( gameEngine.randomiser.nextDouble() < WALK_PROBABILITY ) {
            if( builderState != BuilderState.WALKING ) {
                builderState = BuilderState.WALKING;
                velocityx = builderSpeed;
                setRealisationAndGeometry( walkingAnimation );
            }
        } else if( gameEngine.randomiser.nextDouble() < WAIT_PROBABILITY ) {
            if( builderState != BuilderState.WAITING ) {
                builderState = BuilderState.WAITING;
                velocityx = 0.0;
                setRealisationAndGeometry( waitingAnimation );
            }
        } else if( gameEngine.randomiser.nextDouble() < HIT_PROBABILITY ) {
            if( builderState != BuilderState.HITTING ) {
                builderState = BuilderState.HITTING;
                velocityx= 0.0;
                setRealisationAndGeometry( hittingAnimation );
            }
        }
        
        // Update the position and remove if the layer is exited
        x += velocityx;
        GameObjectUtilities.deleteIfGameLayerExited( this );
    }    
}