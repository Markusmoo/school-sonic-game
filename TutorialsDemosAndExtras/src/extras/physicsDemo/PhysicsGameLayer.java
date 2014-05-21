package extras.physicsDemo;

import game.engine.*;
import game.geometry.*;
import game.physics.*;

import java.awt.*;
import java.awt.event.*;

public class PhysicsGameLayer extends CollisionSpace {
    
    ///////////////////////////////////////////////////////////////////////////
    // Class Data                                                            //
    ///////////////////////////////////////////////////////////////////////////
        
    /**
     * Define the different types of physics demo included
     */
    public enum PhysicsDemoType { 
        EggTimer, PennyFall, GlassPyramid, BrickStack,
        BreakableWall, Chains, PoolTable, Truck,
        Suspension, Teeters, Bouncers, AtRest }    
    private PhysicsDemoType physicsDemoType = PhysicsDemoType.EggTimer;

    /**
     * Store the centre of the screen as we'll be using this a lot as
     * a refernece point when setting up the demos
     */    
    private double centerX, centerY;

    /**
     * Variables used to track the drawing of the cannon ball trajectory line
     */
    private boolean drawTrajectoryLine = false;
    private double trajectoryStartX, trajectoryStartY;
        
    /**
     * Restitution and mass of the created cannon ball
     */
    private double cannonBallRestituion;
    private double cannonBallMass;    

    
    ///////////////////////////////////////////////////////////////////////////
    // Constructors                                                          //
    ///////////////////////////////////////////////////////////////////////////
        
    public PhysicsGameLayer( GameEngine gameEngine ) {
        super( "PhysicsGameLayer", gameEngine, 
                gameEngine.screenWidth, gameEngine.screenHeight  );
        
        gameEngine.setMouseCursor(  "MouseCrosshair", 16, 16 );

        centerX = gameEngine.screenWidth/2.0; 
        centerY = gameEngine.screenHeight/2.0;
        
        setupDemo();
    }
        
    public void setupDemo() {        
        // Remove the previous demo
        removeAllBodiesAndJoints();

        // Setup default physics simulation values for the number of
        // impulse iterations and at rest calculations
        IMPULSE_ITERATIONS = 20;        
        STATIONARY_VELOCITY_THRESHOLD = 2.0;
        STATIONARY_MOVEMENT_THRESHOLD = 2.0;
        STATIONARY_ROTATION_THRESHOLD = 0.05;
        STATIONARY_TICK_THRESHOLD = 100;
        
        // Reset the cannon ball properties to their default values
        cannonBallRestituion = 0.0;
        cannonBallMass = 2000.0;
        
        // Create the demo
        switch (physicsDemoType) { 
            case EggTimer: setupEggTimer(); break;
            case PennyFall: setupPennyFall(); break;
            case GlassPyramid: setupGlassPyramid(); break;
            case BrickStack: setupBrickStack(); break;
            case BreakableWall: setupBreakableWall(); break;
            case Chains: setupChains(); break;
            case PoolTable: setupPoolTable(); break;
            case Truck: setupTruck(); break;
            case Suspension: setupSuspension(); break;
            case Teeters: setupTeeters(); break;
            case Bouncers: setupBouncers(); break;
            case AtRest: setupAtRest(); break;
        }
    }
        
    private void setupEggTimer() {
        setGravity(0.0, 100.0);
        
        int NUM_CIRCLES = 100;
        int SPACER_SIZE = 2;
        int CIRCLE_SIZE = 35;
        
        int rowWidth = (int)Math.sqrt( 2.0 * NUM_CIRCLES );
        int offsetY = -rowWidth*(CIRCLE_SIZE+SPACER_SIZE);
        while( rowWidth != 0 ) {
            int offsetX = gameEngine.screenWidth/2 - rowWidth*(CIRCLE_SIZE+SPACER_SIZE)/2;
            for( int colIdx = 0; colIdx < rowWidth; colIdx++ ) {
                createCircle( offsetX-CIRCLE_SIZE/2, offsetY-CIRCLE_SIZE/2 );
                offsetX += CIRCLE_SIZE + SPACER_SIZE;
            }
            offsetY += CIRCLE_SIZE + SPACER_SIZE;
            rowWidth--;
        }
        
        createEdge( centerX - 300.0, centerY-210.0, Math.toRadians( 45.0 ) );
        createEdge( centerX + 300.0, centerY-210.0, Math.toRadians( -45.0 ) );
        
        createEdge( centerX - 300.0, centerY+120.0, Math.toRadians( -45.0 ) );
        createEdge( centerX + 300.0, centerY+120.0, Math.toRadians( 45.0 ) );
        
        createEdge( centerX - 240.0, centerY+300.0, 0.0 );
        createEdge( centerX + 240.0, centerY+300.0, 0.0 );
    }
    
    private void setupPennyFall() {
        setGravity(0.0, 200.0);
        
        int NUM_CIRCLES = 500;
        int CIRCLE_SIZE = 35;
        int BLOCK_SPACING = 110;
        int BLOCK_SIZE = 70;
        
        for( int idx = 0; idx < NUM_CIRCLES; idx++ )
            createCircle(
                    gameEngine.randomiser.nextInt(gameEngine.screenWidth-CIRCLE_SIZE ) 
                        + CIRCLE_SIZE/2,
                            -gameEngine.randomiser.nextInt( NUM_CIRCLES * 50 ) );
        
        for( int rowIdx = 0; rowIdx <
                gameEngine.screenHeight/(BLOCK_SPACING+BLOCK_SIZE); rowIdx++ )
            for( int colIdx = 0; colIdx <
                gameEngine.screenWidth/(BLOCK_SPACING+BLOCK_SIZE)+1; colIdx++ )
                createBlock(
                        (colIdx)*(BLOCK_SPACING+BLOCK_SIZE)
                        + (rowIdx % 2 == 0 ? (BLOCK_SPACING+BLOCK_SIZE)/2 : 0),
                        (rowIdx+1)*(BLOCK_SPACING+BLOCK_SIZE),
                        Math.toRadians( gameEngine.randomiser.nextInt( 360 ) ) );
    }
    
    private void setupGlassPyramid() {
        setGravity(0.0, 100.0);
        
        int NUM_SQUARES = 150;
        int SPACER_SIZE = 2;
        int SQUARE_SIZE = 35;
        
        int rowWidth = (int)Math.sqrt( 2.0 * NUM_SQUARES );
        int offsetY = rowWidth*(SQUARE_SIZE+SPACER_SIZE);
        while( rowWidth != 0 ) {
            int offsetX = gameEngine.screenWidth/2 - rowWidth*(SQUARE_SIZE+SPACER_SIZE)/2;
            for( int colIdx = 0; colIdx < rowWidth; colIdx++ ) {
                createGlassSquare( offsetX-SQUARE_SIZE/2, offsetY-SQUARE_SIZE/2, 0.0 );
                offsetX += SQUARE_SIZE + SPACER_SIZE;
            }
            offsetY -= SQUARE_SIZE + SPACER_SIZE;
            rowWidth--;
        }
        
        createEdge( centerX - 250.0, gameEngine.screenHeight-100.0, 0.0 );
        createEdge( centerX + 250.0, gameEngine.screenHeight-100.0, 0.0 );
    }
    
    private void setupBrickStack() {
        setGravity(0.0, 400.0);
        
        IMPULSE_ITERATIONS = 60;
        IMPACT_ABSORPTION = 1.0;
        
        int NUM_BRICKS = 10;
        int SPACER_SIZE = 2;
        int BRICK_OFFSET = 5;
        int BRICK_SIZE = 35;
        int NUM_STACKS = 5;
        int STACK_SPACING = 200;
        
        for( int stackIdx = 0; stackIdx < NUM_STACKS; stackIdx++ )
            for( int idx = 0; idx < NUM_BRICKS; idx++ )
                createBrick( centerX + (STACK_SPACING*(stackIdx-NUM_STACKS/2))
                + gameEngine.randomiser.nextInt(BRICK_OFFSET)-BRICK_OFFSET/2,
                        gameEngine.screenHeight-100-idx*(BRICK_SIZE+SPACER_SIZE), 0 );
        
        createEdge( centerX - 250.0, gameEngine.screenHeight-20.0, 0.0 );
        createEdge( centerX + 250.0, gameEngine.screenHeight-20.0, 0.0 );
    }
    
    private void setupBreakableWall() {
        setGravity(0.0, 400.0);
        
        int BRICK_SIZE = 35;
        double BRICK_MASS = 200.0;
        int WALL_WIDTH = 3;
        int WALL_HEIGHT = gameEngine.screenHeight/BRICK_SIZE+1;
        int WALL_SPACING = (gameEngine.screenWidth - (WALL_WIDTH*BRICK_SIZE*3)) / 5;
        
        Body wall = BodyBuilder.buildConnectedBoxBody(
                WALL_WIDTH, WALL_HEIGHT, 1.0, 100000.0, 0.6, 0.1, "Brick1", this );
        wall.setPosition( centerX-WALL_SPACING, centerY );
        wall.setMass( Body.INFINITE_MASS );
        wall.setBreakingImpulseAssumedBodyMass( WALL_WIDTH * WALL_HEIGHT * BRICK_MASS );
        queueGameObjectToAdd( wall );
        
        wall = BodyBuilder.buildConnectedBoxBody(
                WALL_WIDTH, WALL_HEIGHT, 1.0, 100000.0, 0.2, 0.9, "Brick2", this );
        wall.setPosition( centerX+WALL_SPACING, centerY );
        wall.setMass( Body.INFINITE_MASS );
        wall.setBreakingImpulseAssumedBodyMass( WALL_WIDTH * WALL_HEIGHT * BRICK_MASS );
        queueGameObjectToAdd( wall );
        
        wall = BodyBuilder.buildConnectedBoxBody(
                WALL_WIDTH, WALL_HEIGHT, 1.0, 100000.0, 0.1, 0.1, "Brick1", this );
        wall.setPosition( gameEngine.screenWidth-WALL_WIDTH*BRICK_SIZE, centerY );
        wall.setMass( Body.INFINITE_MASS );
        wall.setBreakingImpulseAssumedBodyMass( WALL_WIDTH * WALL_HEIGHT * BRICK_MASS );
        queueGameObjectToAdd( wall );
    }
    
    private void setupChains() {
        setGravity(0.0, 100.0);
        
        int NUM_CIRCLES_LONG = 20;
        int CIRCLE_WIDTH= 35;
        int CIRCLE_OFFSET = 5;
        
        int NUM_GLASS_SQUARE_LONG = 20;
        int GLASS_SQUARE_WIDTH= 35;
        int GLASS_SQUARE_OFFSET = 3;
        
        Body createdBlock = createBlock( centerX, centerY-100.0, 0.0 );
        createdBlock.setMass( 1000.0 );
        createdBlock.setMass( Body.INFINITE_MASS );
        
        Body lastBody = createdBlock;
        
        for( int idx = 0; idx < NUM_CIRCLES_LONG; idx++ ) {
            Body newBlock = createCircle( centerX - 25.0 
                    - (CIRCLE_WIDTH+CIRCLE_OFFSET)*(idx+1), centerY-100.0 );
            
            createBasicJoint( lastBody, newBlock );
            lastBody = newBlock;
        }
        
        lastBody = createdBlock;
        for( int idx = 0; idx < NUM_GLASS_SQUARE_LONG; idx++ ) {
            Body newBlock = createGlassSquare( 
                    centerX + 25.0 + (GLASS_SQUARE_WIDTH+GLASS_SQUARE_OFFSET)*(idx+1),
                    centerY-100.0, 0.0 );
            
            createBasicJoint( lastBody, newBlock );
            lastBody = newBlock;
        }
        
        createEdge( centerX - 250.0, gameEngine.screenHeight-50.0, 0.0 );
        createEdge( centerX + 250.0, gameEngine.screenHeight-50.0, 0.0 );
    }
    
    private void setupPoolTable() {
        setGravity(0.0, 0.0);
        
        cannonBallRestituion = 1.0;
        cannonBallMass = 100.0;
        
        int NUM_CIRCLES = 15;
        int SPACER_SIZE = 2;
        int CIRCLE_SIZE = 35;
        
        int rowWidth = (int)Math.sqrt( 2.0 * NUM_CIRCLES );
        int offsetX = rowWidth*(CIRCLE_SIZE+SPACER_SIZE);
        while( rowWidth != 0 ) {
            int offsetY = gameEngine.screenHeight/2 - rowWidth*(CIRCLE_SIZE+SPACER_SIZE)/2;
            for( int colIdx = 0; colIdx < rowWidth; colIdx++ ) {
                Body ball = createCircle(
                        gameEngine.screenWidth/2 + offsetX-CIRCLE_SIZE/2, offsetY-CIRCLE_SIZE/2 );
                ball.restitution = 1.0;
                offsetY += CIRCLE_SIZE + SPACER_SIZE;
            }
            offsetX -= CIRCLE_SIZE + SPACER_SIZE;
            rowWidth--;
        }
        
        (createEdge( centerX - 290.0, centerY-330.0, 0.0 )).restitution = 0.9;
        (createEdge( centerX + 290.0, centerY-330.0, 0.0 )).restitution = 0.9;
        
        (createEdge( centerX - 290.0, centerY+330.0, 0.0 )).restitution = 0.9;
        (createEdge( centerX + 290.0, centerY+330.0, 0.0 )).restitution = 0.9;
        
        (createEdge( centerX - 520.0, centerY, Math.toRadians( 90.0 ) )).restitution = 0.9;
        (createEdge( centerX + 520.0, centerY, Math.toRadians( 90.0 ) )).restitution = 0.9;
    }
    
    private void setupTruck() {
        setGravity(0.0, 200.0);
        
        int NUM_TRUCKS = 20;
        
        for( int truckIdx = 0; truckIdx < NUM_TRUCKS; truckIdx++ ) {
            Body truck = new Body( this, (double)gameEngine.randomiser.nextInt(
                    gameEngine.screenWidth ), -truckIdx*75.0, 1 );
            truck.setRealisation( "Truck" );
            truck.setGeometry(
                    new game.geometry.Shape[] {
                        // Truck body
                        new Box( -12.0, -19.0, 125.0, 42.0 ),
                        new Box( -13.0, -51.0, 35.0, 21.0 ),                
                        // Front engine
                        new Circle( 57.0, -22.0, 15.0 ),                
                        // Font tire
                        new Circle( 52.0, 27.0, 34.0 ),
                        new Circle( 65.0, 14.0, 27.0 ),
                        // Back tire
                        new Circle( -55.0, 19.0, 43.0 ),
                        new Circle( -76.0, -5.0, 20.0 ) } );
            truck.setMass( 2000.0 );
            truck.restitution = 0.1;
            queueGameObjectToAdd( truck );
        }
        
        createEdge( centerX - 250.0, gameEngine.screenHeight-50.0, 0.0 );
        createEdge( centerX + 250.0, gameEngine.screenHeight-50.0, 0.0 );       
    }
    
    private void setupSuspension() {
        setGravity(0.0, 100.0);
        
        int SUSPENSION_WIDTH_LEFT = 560;
        int SUSPENSION_WIDTH_RIGHT = 550;
        
        int BRICK_WIDTH= 35;
        
        Body leftBlock = createBlock( 
                (int)(centerX-SUSPENSION_WIDTH_LEFT-100), centerY, 0.0 );
        leftBlock.setMass( Body.INFINITE_MASS );
        
        Body middleBlock = createBlock( centerX, centerY, 0.0 );
        middleBlock.setMass( Body.INFINITE_MASS );
        
        Body rightBlock = createBlock( 
                (int)(centerX+SUSPENSION_WIDTH_RIGHT+100), centerY, 0.0 );
        rightBlock.setMass( Body.INFINITE_MASS );
        
        Body lastBody = leftBlock;
        for( int idx = 0; idx < 17; idx++ ) {
            Body newBrick = createSmallBrick( centerX - 50 -
                    (SUSPENSION_WIDTH_LEFT - (idx*(BRICK_WIDTH+0))),
                    centerY-20, 0.0 );
            createBasicJoint( lastBody, newBrick );
            lastBody = newBrick;
        }
        createBasicJoint( lastBody, middleBlock );
        
        lastBody = rightBlock;
        for( int idx = 0; idx < 15; idx++ ) {
            Body newBrick = createSmallBrick( centerX + 50 +
                    (SUSPENSION_WIDTH_LEFT - (idx*(BRICK_WIDTH+5))),
                    centerY-20, 0.0 );
            createBasicJoint( lastBody, newBrick );
            lastBody = newBrick;
        }
        createBasicJoint( lastBody, middleBlock );
        
        createEdge( centerX - 250.0, gameEngine.screenHeight-50.0, 0.0 );
        createEdge( centerX + 250.0, gameEngine.screenHeight-50.0, 0.0 );
    }
        
    private void setupTeeters() {
        Body ground = createEdge( centerX, 3*centerY, 0.0 );
        
        int edgesWidth = gameEngine.screenWidth / 500;
        int edgesHeight = gameEngine.screenHeight / 250;
        
        for( int heightIdx = 0; heightIdx < edgesHeight; heightIdx++ ) {
            for( int widthIdx = 0; widthIdx < edgesWidth; widthIdx++ ) {
                
                double xOffset = (widthIdx+1)*(gameEngine.screenWidth/(edgesWidth+1))
                + (heightIdx%2==0 ? (gameEngine.screenWidth/((edgesWidth+1)*4)) :
                    -(gameEngine.screenWidth/((edgesWidth+1)*4)) );
                double yOffset = (heightIdx+1)*(gameEngine.screenHeight/(edgesHeight+1));
                
                Body edge = new Body( this, xOffset, yOffset, 1 );
                edge.friction = 1.0;
                edge.setRealisationAndGeometry( "ThinEdge" );
                edge.restitution = 0.0;
                edge.setMass( 200.0 );
                edge.permitAtRest = false;
                queueGameObjectToAdd( edge );
                
                HingedJoint joint = new HingedJoint( this, edge, ground,
                        xOffset, yOffset );
                addJoint( joint );
            }
        }
        
        int NUM_CIRCLES = 500;
        int CIRCLE_SIZE = 35;
        
        for( int idx = 0; idx < NUM_CIRCLES; idx++ )
            createCircle( gameEngine.randomiser.nextInt(
                    gameEngine.screenWidth-CIRCLE_SIZE ) + CIRCLE_SIZE/2,
                        -gameEngine.randomiser.nextInt( NUM_CIRCLES * 20 ) );
    }
        
    private void setupBouncers() {
        setGravity(0.0, 200.0);
        
        int NUM_CIRCLES = 45;
        int CIRCLE_SPACING = 2;
        int CIRCLE_SIZE = 20;
        
        for( int idx = 0; idx < NUM_CIRCLES; idx++ ) {
            Body circle = createSmallCircle(
                    centerX + (idx-NUM_CIRCLES/2)*(CIRCLE_SPACING+CIRCLE_SIZE),
                    400-Math.abs(idx-NUM_CIRCLES/2.0)*10 );
            circle.restitution = 0.8+
                    (1.0 / NUM_CIRCLES)*Math.abs(idx-NUM_CIRCLES/2.0)/4.0;
        }
        
        (createEdge( centerX - 250.0, gameEngine.screenHeight-20.0, 0.0 )).restitution = 1.0;
        (createEdge( centerX + 250.0, gameEngine.screenHeight-20.0, 0.0 )).restitution = 1.0;
    }
    
    private void setupAtRest() {
        setGravity(0.0, 200.0);
        
        STATIONARY_VELOCITY_THRESHOLD = 3.0;
        STATIONARY_MOVEMENT_THRESHOLD = 3.0;
        STATIONARY_ROTATION_THRESHOLD = 0.05;
        STATIONARY_TICK_THRESHOLD = 40;
        
        int NUM_GLASS_SQUARES = 7 * gameEngine.screenWidth/100;
        
        for( int idx = 0; idx < NUM_GLASS_SQUARES; idx++ ) {
            Body square = createGlassSquare(
                    gameEngine.randomiser.nextDouble() * gameEngine.screenWidth,
                    gameEngine.randomiser.nextDouble() * gameEngine.screenHeight/2.0,
                    gameEngine.randomiser.nextDouble() * 2.0 * Math.PI );
            square.setRealisation("GlassSquare3");
        }
        
        createEdge( centerX - 250.0, gameEngine.screenHeight-20.0, 0.0 );
        createEdge( centerX + 250.0, gameEngine.screenHeight-20.0, 0.0 );
        
        createEdge( centerX - 550.0, gameEngine.screenHeight-20.0, 0.0 );
        createEdge( centerX + 550.0, gameEngine.screenHeight-20.0, 0.0 );
    }
    
    
    
   
    private Body createSmallCircle( double x, double y ) {
        double CIRCLE_MASS = 40.0;
        
        Body circle = new Body( this, x, y, 1 );
        circle.setRealisation( "SmallCircle"+(gameEngine.randomiser.nextInt(5)+1) );
        circle.setGeometry( new Circle( 0, 0, 10 ) );
        circle.setMass( CIRCLE_MASS );
        circle.restitution = 0.0;
        queueGameObjectToAdd( circle );
        
        return circle;
    }
    
    private Body createCircle( double x, double y ) {
        double CIRCLE_MASS = 100.0;
        
        Body circle = new Body( this, x, y, 1 );
        circle.setRealisation( "Circle"+(gameEngine.randomiser.nextInt(5)+1) );
        circle.setGeometry( new Circle( 0, 0, 17.5 ) );
        circle.setMass( CIRCLE_MASS );
        circle.restitution = 0.0;
        queueGameObjectToAdd( circle );
        
        return circle;
    }
    
    private Body createEdge( double x, double y, double rotation ) {
        Body edge = new Body( this, x, y, 1 );
        edge.friction = 1.0;
        edge.rotation = rotation;
        edge.setRealisationAndGeometry( "Edge"+(gameEngine.randomiser.nextInt(4)+1) );
        edge.restitution = 0.0;
        queueGameObjectToAdd( edge );
        
        return edge;
    }
    
    private Body createBlock( double x, double y, double rotation ) {
        Body block = new Body( this, x, y, 1 );
        block.rotation = rotation;
        block.setRealisationAndGeometry( "Block"+(gameEngine.randomiser.nextInt(3)+1) );
        block.restitution = 0.0;
        queueGameObjectToAdd( block );
        
        return block;
    }
    
    private Body createGlassSquare( double x, double y, double rotation ) {
        double SQUARE_MASS = 100.0;
        
        Body square = new Body( this, x, y, 1 );
        square.restitution = 0.0;
        square.friction = 1.0;
        square.rotation = rotation;
        square.setRealisationAndGeometry( "GlassSquare"+(gameEngine.randomiser.nextInt(6)+1) );
        square.setMass( SQUARE_MASS );
        queueGameObjectToAdd( square );
        
        return square;
    }
    
    private Body createBrick( double x, double y, double rotation ) {
        double BRICK_MASS = 100.0;
        
        Body brick = new Body( this, x, y, 1 );
        brick.restitution = 0.0;
        brick.friction = 1.0;
        brick.rotation = rotation;
        brick.setRealisationAndGeometry( "Brick"+(gameEngine.randomiser.nextInt(7)+1) );
        brick.setMass( BRICK_MASS );
        queueGameObjectToAdd( brick );
        
        return brick;
    }
    
    private Body createWideBrick( double x, double y, double rotation ) {
        double BRICK_MASS = 100.0;
        
        Body brick = new Body( this, x, y, 1 );
        brick.restitution = 0.0;
        brick.friction = 1.0;
        brick.rotation = rotation;
        brick.setRealisationAndGeometry( "Brick"+(gameEngine.randomiser.nextInt(3)+5) );
        brick.setMass( BRICK_MASS );
        queueGameObjectToAdd( brick );
        
        return brick;
    }
    
    private Body createSmallBrick( double x, double y, double rotation ) {
        double BRICK_MASS = 100.0;
        
        Body brick = new Body( this, x, y, 1 );
        brick.restitution = 0.0;
        brick.friction = 1.0;
        brick.rotation = rotation;
        brick.setRealisationAndGeometry( "Brick"+(gameEngine.randomiser.nextInt(4)+1) );
        brick.setMass( BRICK_MASS );
        queueGameObjectToAdd( brick );
        
        return brick;
    }
    
    private Body createCannonball(double mass, double force, boolean angled ) {
        Body circle = new Body(this, 0, 
                gameEngine.randomiser.nextInt(gameEngine.screenHeight / 2), 1);
        circle.setRealisation("Circle" + (gameEngine.randomiser.nextInt(5) + 1));
        circle.setGeometry(new Circle(0, 0, 17.5));
        circle.setMass( mass );
        circle.restitution = 0.5;
        circle.setForce( force * circle.getMass(),
                (angled ? gameEngine.randomiser.nextDouble() * circle.getMass() * force : 0 ) );
        queueGameObjectToAdd(circle);
        
        return circle;
    }
    
    private HingedJoint createBasicJoint( Body body1, Body body2 ) {
        HingedJoint joint = new HingedJoint( this, body1, body2,
                (body1.x+body2.x)/2.0, (body1.y+body2.y)/2.0 );
        joint.setRelaxation( 0.25 );
        addJoint( joint );
        
        return joint;
    }
    
    private FixedJoint createFixedJoint( Body body1, Body body2 ) {
        FixedJoint joint = new FixedJoint( this, body1, body2 );
        addJoint( joint );
        
        return joint;
    }
    
    
    
    @Override
    public void update() {
        super.update();
        
        if( inputEvent.keyTyped( KeyEvent.VK_SPACE ) ) {
            createCannonball( gameEngine.randomiser.nextInt( 1000 ), 100000.0, true );
        } else if( inputEvent.keyTyped( KeyEvent.VK_R ) ) {
            setupDemo();
        } else if( inputEvent.keyTyped( KeyEvent.VK_N ) ) {
            switch (physicsDemoType) {
                case EggTimer: physicsDemoType = PhysicsDemoType.PennyFall; break;
                case PennyFall: physicsDemoType = PhysicsDemoType.GlassPyramid; break;
                case GlassPyramid: physicsDemoType = PhysicsDemoType.BrickStack; break;
                case BrickStack: physicsDemoType = PhysicsDemoType.BreakableWall; break;
                case BreakableWall: physicsDemoType = PhysicsDemoType.Chains; break;
                case Chains: physicsDemoType = PhysicsDemoType.PoolTable; break;
                case PoolTable: physicsDemoType = PhysicsDemoType.Truck; break;
                case Truck: physicsDemoType = PhysicsDemoType.Suspension; break;
                case Suspension: physicsDemoType = PhysicsDemoType.Teeters; break;
                case Teeters: physicsDemoType = PhysicsDemoType.Bouncers; break;
                case Bouncers: physicsDemoType = PhysicsDemoType.AtRest; break;
                case AtRest: physicsDemoType = PhysicsDemoType.EggTimer; break;
            }
            setupDemo();
        } else if( inputEvent.keyTyped( KeyEvent.VK_P ) ) {
            switch (physicsDemoType) {
                case EggTimer: physicsDemoType = PhysicsDemoType.AtRest; break;
                case PennyFall: physicsDemoType = PhysicsDemoType.EggTimer; break;
                case GlassPyramid: physicsDemoType = PhysicsDemoType.PennyFall; break;
                case BrickStack: physicsDemoType = PhysicsDemoType.GlassPyramid; break;
                case BreakableWall: physicsDemoType = PhysicsDemoType.BrickStack; break;
                case Chains: physicsDemoType = PhysicsDemoType.BreakableWall; break;
                case PoolTable: physicsDemoType = PhysicsDemoType.Chains; break;
                case Truck: physicsDemoType = PhysicsDemoType.PoolTable; break;
                case Suspension: physicsDemoType = PhysicsDemoType.Truck; break;
                case Teeters: physicsDemoType = PhysicsDemoType.Suspension; break;
                case Bouncers: physicsDemoType = PhysicsDemoType.Teeters; break;
                case AtRest: physicsDemoType = PhysicsDemoType.Bouncers; break;
            }
            setupDemo();
        }
        
        switch( physicsDemoType ) {
            
            // Apply slight dampening to the balls in the pool table demo
            case PoolTable:
                GameObjectCollection bodies = getGameObjectCollection( "Bodies" );
                for( int idx = 0; idx < bodies.size; idx++ ) {
                    GameObjectUtilities.scaleVelocities(
                            bodies.gameObjects[idx], 0.995, 0.995 );
                    if( Math.abs( bodies.gameObjects[idx].velocityx ) < 1 )
                        bodies.gameObjects[idx].velocityx = 0.0;
                    if( Math.abs( bodies.gameObjects[idx].velocityy ) < 1 )
                        bodies.gameObjects[idx].velocityy = 0.0;
                    if( Math.abs( bodies.gameObjects[idx].angularVelocity ) < 0.01 )
                        bodies.gameObjects[idx].angularVelocity = 0.0;
                }
                break;
                
            // Color blocks based on their at rest status
            case AtRest:
                bodies = getGameObjectCollection( "Bodies" );
                for( int idx = 0; idx < bodies.size; idx++ ) {
                    Body body = (Body)bodies.gameObjects[idx];
                    if( body.graphicalRealisation[0].getAssetName().startsWith( "GlassSquare" ) ) {
                        if( body.isMoveable && body.ticksAtRest > STATIONARY_TICK_THRESHOLD ) {
                            if( !body.graphicalRealisation[0].getAssetName().equals( "GlassSquare4" ) )
                                bodies.gameObjects[idx].setRealisation("GlassSquare4");
                        } else {
                            if( !body.graphicalRealisation[0].getAssetName().equals( "GlassSquare3" ) )
                                bodies.gameObjects[idx].setRealisation("GlassSquare3");                               bodies.gameObjects[idx].setRealisation("GlassSquare3");
                        }
                    }
                }
                break;
        }
                
        // Update the cannon ball creation/trajectory if needed
        if( !drawTrajectoryLine && inputEvent.mouseButton1Pressed ) {
            drawTrajectoryLine = true;
            trajectoryStartX = inputEvent.mouseXCoordinate;
            trajectoryStartY = inputEvent.mouseYCoordinate;
        } else if( drawTrajectoryLine && !inputEvent.mouseButton1Pressed ) {
            double cannonBallForceFactor = cannonBallMass * 100.0;
            
            drawTrajectoryLine = false;
            double trajectoryVx = inputEvent.mouseXCoordinate - trajectoryStartX;
            double trajectoryVy = inputEvent.mouseYCoordinate - trajectoryStartY;

            Body cannonBall = createCannonball( cannonBallMass, 1.0, false );
            cannonBall.x = trajectoryStartX;
            cannonBall.y = trajectoryStartY;
            cannonBall.forcex = cannonBallForceFactor * trajectoryVx;

            if( cannonBall.forcex > 2.0E8 ) cannonBall.forcex = 2.0E8;
            cannonBall.forcey = cannonBallForceFactor * trajectoryVy;
            if( cannonBall.forcey > 2.0E8 ) cannonBall.forcey = 2.0E8;
        }
    }

    
    @Override
    public void draw( Graphics2D graphics2D ) {
        
        Color originalColour = graphics2D.getColor();
        graphics2D.setColor( Color.BLUE );
        graphics2D.fillRect( 0, 0, gameEngine.screenWidth, gameEngine.screenHeight );
        graphics2D.setColor( originalColour );
        
        super.draw( graphics2D );
        
        graphics2D.setFont( new Font( "MONOSPACED", Font.BOLD, 12 ) );
        graphics2D.setColor(Color.white);
        graphics2D.drawString( "Demo: " + physicsDemoType, 200, 10 );
        graphics2D.drawString( "Keys: N - next | R - reset | P - previous", 200, 25 );
        graphics2D.drawString( "Click and drag mouse to fire cannon ball", 200, 40 );
        
        if( drawTrajectoryLine ) {
            graphics2D.setColor(
                    new Color( 0.75f +
                    (float)(gameEngine.updateCounter%100 - 50) / 200.0f, 0.0f, 0.0f ) );
            graphics2D.drawLine( (int)trajectoryStartX, (int)trajectoryStartY,
                    inputEvent.mouseXCoordinate, inputEvent.mouseYCoordinate );
        }
        
        graphics2D.setColor( originalColour );
    }
}