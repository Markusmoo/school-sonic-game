package extras.bouncer;

import game.engine.*;
import game.geometry.*;
import game.assets.*;
import game.physics.*;

import java.awt.*;

public class BouncerLayer extends CollisionSpace
{
    private static int LAYER_SIZE = 2000;
    
    public BouncerLayer( GameEngine gameEngine ) 
    {
        super( "BouncerLayer", gameEngine, 
                gameEngine.screenWidth, gameEngine.screenHeight );        
        setGravity( 0.0, 0.0 );

        setupLevel();
    }
    
    public void setupLevel()
    {        
        Body mouseFollower = new Body( this, 500, 200 );
        mouseFollower.setName( "MouseFollower" );
        mouseFollower.setMass( 10.0 );
        mouseFollower.setRealisation( 
                assetManager.retrieveGraphicalAsset( "PurpleCircle1" ) );
        mouseFollower.setGeometry( new Circle( 0.0, 0.0, 50.0 ) );
	addGameObject(mouseFollower); 
        
        for( int idx = 1; idx <= 6; idx++ ) {
            Body ball = new Body( this, 100*idx, 100*idx );
            ball.setMass( 10.0 );
            ball.setRealisation( 
                    assetManager.retrieveGraphicalAsset( 
                        (gameEngine.randomiser.nextDouble() > 0.5 ?
                            "GreenCircle" + (gameEngine.randomiser.nextInt(3)+1) : 
                            "BlueCircle" + (gameEngine.randomiser.nextInt(3)+1)) ) );
            ball.setGeometry( new Circle( 0.0, 0.0, 50.0 ) );
            addGameObject( ball ); 
        }        
    }
     
    @Override
    public void update()
    {                
        updateMousefollower();
        
        super.update();
        
        GameObjectCollection bodies = getGameObjectCollection( "Bodies" );
        for( int idx = 0; idx < bodies.size; idx++ )
            GameObjectUtilities.reboundIfGameLayerExited(bodies.gameObjects[idx]);
    }
        
    private void updateMousefollower() {
        Body mouseFollower = (Body)getGameObject( "MouseFollower" );   

        double velocityFactor = 10.0;
        
        mouseFollower.velocityx = velocityFactor *
                (getLayerPositionFromScreenX( gameEngine.inputEvent.mouseXCoordinate )
                                    - mouseFollower.x);
        
        mouseFollower.velocityy = velocityFactor *
                (getLayerPositionFromScreenY( gameEngine.inputEvent.mouseYCoordinate )
                                    - mouseFollower.y);
    }
        
    @Override
    public void draw( Graphics2D graphics2D ) {        
        Color originalColour = graphics2D.getColor();
        graphics2D.setColor( Color.LIGHT_GRAY );
        graphics2D.fillRect( 0, 0, gameEngine.screenWidth, gameEngine.screenHeight );
        graphics2D.setColor( originalColour );
        
        super.draw( graphics2D );        
    }            
}