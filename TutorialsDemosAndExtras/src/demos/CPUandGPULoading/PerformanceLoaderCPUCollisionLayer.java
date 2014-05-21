package demos.CPUandGPULoading;

import game.engine.*;
import game.physics.*;
import game.geometry.*;

import java.awt.event.KeyEvent;
import java.util.*;

public class PerformanceLoaderCPUCollisionLayer extends CollisionSpace {
    
    // Maximum initial ball velocity
    private static double MAX_BALL_VELOCITY = 100.0;
    
    // Flag controlling if we will check for collisions, or not
    // If we don't then the update cost becomes much lower (certainly less
    // than the render cost).
    public boolean enableCollisionChecking = true;
        
    public PerformanceLoaderCPUCollisionLayer( GameEngine gameEngine  ) {
        super( "CollisionTestLayer", gameEngine );
        
        width = gameEngine.screenWidth;
        height = gameEngine.screenHeight;
    
        STATIONARY_TICK_THRESHOLD = 1000000;
            
        addGameObjectCollection( "Balls" );

        // By default CollisionSpace defined a downward gravity of 9.8 units/second
        // For this test, gravity is turned off
        setGravity(0.0, 0.0);
    }
    
    private void createBalls( int numExtraBalls ) {
        for( int ballIdx = 0; ballIdx < numExtraBalls; ballIdx++ ) {
            // Create a ball as a new Body instance with a random location
            Body ball = new Body( this,
                    (new Random()).nextInt( (int)width - 30 ),
                    (new Random()).nextInt( (int)height - 30 ) );
            
            // Retreive, at random, a graphical represenation for the ball
            ball.setRealisation( "Ball" + ((new Random().nextInt(4)+1) ) );
            ball.setGeometry( new Circle( 0, 0, 15 ) );
            
            // Randomise the ball's velocity
            ball.velocityx = ((new Random()).nextDouble() - 0.5) * MAX_BALL_VELOCITY * 2.0;
            ball.velocityy = ((new Random()).nextDouble() - 0.5) * MAX_BALL_VELOCITY * 2.0;
            
            // Make some balls heavier than others
            ball.setMass( gameEngine.randomiser.nextInt(10) + 10 );
            
            // Add the ball to the ball game object set
            queueGameObjectToAdd( ball, "Balls" );
        }
    }
    
    @Override
    public void update() {
        // Check user input and create more balls or turn on/off collision detection
        // as requested
        if( this.gameEngine.inputEvent.keyTyped( KeyEvent.VK_1 ) )
            this.createBalls( 1 );
        else if( this.gameEngine.inputEvent.keyTyped( KeyEvent.VK_2 ) )
            this.createBalls( 10 );
        else if( this.gameEngine.inputEvent.keyTyped( KeyEvent.VK_3 ) )
            this.createBalls( 100 );
        else if( this.gameEngine.inputEvent.keyTyped( KeyEvent.VK_4 ) ) {
            enableCollisionChecking = !enableCollisionChecking;

            // Update each ball so that it cannot intersect other objects, 
            // i.e. it cannot be involved in collisions. It is also necessary
            // to ask the CollisionLayer to remove any arbiters (current points
            // of contact) for the body.
            GameObjectCollection balls = getGameObjectCollection( "Balls" );
            for( int idx = 0; idx < balls.size; idx++ ) {
                balls.gameObjects[idx].canIntersectOtherGraphicalObjects = enableCollisionChecking;
                removeArbitersForBody((Body)balls.gameObjects[idx]);
            }
        }
        
        // Rebound the ball if it leaves the game layer
        GameObjectCollection balls = getGameObjectCollection( "Balls" );
        for( int idx = 0; idx < balls.size; idx++ )
            GameObjectUtilities.reboundIfGameLayerExited( balls.gameObjects[idx] );
        
        // Call the update method of the CollisionLayer which will update bodies
        // based upon their velocities, etc. and interaction with other bodies.
        super.update();
    }
}
