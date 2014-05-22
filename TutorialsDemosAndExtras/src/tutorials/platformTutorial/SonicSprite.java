/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tutorials.platformTutorial;
import game.engine.*;
import game.assets.*;
import game.physics.*;
import game.geometry.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.*;
/**
 *
 * @author Michael
 */
public class SonicSprite extends Body {
    
    public enum SonicState{ RUNNING, WAITING, JUMPING }
    private SonicState sonicState;
    
    public enum SonicFacing{LEFT, RIGHT }
    private SonicFacing sonicFacing;
    
    private double SPRITE_BASE_RUN_VELOCITY = 10.0;
    private double SPRITE_BASE_FLY_VELOCITY = 20.0;
    private double SPRITE_BASE_JUMP_VELOCITY = 250.0;
    
    private double SPRITE_MAX_X_VELOCITY = 400.0;
    private static final double X_VELOCITY_DECELERATION = 20.0;
    
    private int id;
    
    public SonicSprite(GameLayer gameLayer, int playerID){
        super(gameLayer);
        
        id = playerID;
        
        setName("Sonic"+id);

        setMass(500.0);
        restitution = 0.0;
        
        sonicState = SonicState.WAITING;
        sonicFacing = SonicFacing.RIGHT;
        
        GraphicalAsset sonicWaiting = assetManager.retrieveGraphicalAsset("SonicWaiting");
        setRealisationAndGeometry(sonicWaiting);
        setGeometry(new Box(0,0, sonicWaiting.width-20,sonicWaiting.height));
    }

	@SuppressWarnings("static-access")
	public void update(){
        SonicState sonicStateOnEntry = sonicState;
        
        if(id==1){
	        if(inputEvent.keyPressed[KeyEvent.VK_UP] && !inputEvent.keyPressed[KeyEvent.VK_DOWN]){
	            velocityy -= SPRITE_BASE_FLY_VELOCITY;
	        }else if(inputEvent.keyPressed[KeyEvent.VK_DOWN] && !inputEvent.keyPressed[KeyEvent.VK_UP]){
	            velocityy += SPRITE_BASE_FLY_VELOCITY;
	        }
        
	        if(inputEvent.keyPressed[ KeyEvent.VK_RIGHT] && !inputEvent.keyPressed[KeyEvent.VK_LEFT]){
	            sonicFacing = SonicFacing.RIGHT;
	            
	            if(sonicState != SonicState.JUMPING){
	                sonicState = SonicState.RUNNING;
	                velocityx += SPRITE_BASE_RUN_VELOCITY;
	            }else
	                velocityx += 0.25 * SPRITE_BASE_RUN_VELOCITY;
	        }else if(inputEvent.keyPressed[KeyEvent.VK_LEFT] && !inputEvent.keyPressed[KeyEvent.VK_RIGHT]){
	            sonicFacing = SonicFacing.LEFT;
	            if(sonicState != SonicState.JUMPING){
	                sonicState = SonicState.RUNNING;
	                velocityx -= SPRITE_BASE_RUN_VELOCITY;
	            }else
	                velocityx -= 0.25 * SPRITE_BASE_RUN_VELOCITY;
	        }else{
	            if(sonicState != SonicState.JUMPING){
	                sonicState = SonicState.WAITING;
	                
	                GameObjectUtilities.dampenVelocities(this, X_VELOCITY_DECELERATION, X_VELOCITY_DECELERATION,0.0,0.0,0.0,0.0);
	            }else {
	                GameObjectUtilities.dampenVelocities(this, X_VELOCITY_DECELERATION*0.2,X_VELOCITY_DECELERATION*0.2,0.0,0.0,0.0,0.0);
	            }
	        }
        }else if(id==2){
        	if(inputEvent.keyPressed[KeyEvent.VK_W] && !inputEvent.keyPressed[KeyEvent.VK_S]){
	            velocityy -= SPRITE_BASE_FLY_VELOCITY;
	        }else if(inputEvent.keyPressed[KeyEvent.VK_S] && !inputEvent.keyPressed[KeyEvent.VK_W]){
	            velocityy += SPRITE_BASE_FLY_VELOCITY;
	        }
        
	        if(inputEvent.keyPressed[ KeyEvent.VK_D] && !inputEvent.keyPressed[KeyEvent.VK_A]){
	            sonicFacing = SonicFacing.RIGHT;
	            
	            if(sonicState != SonicState.JUMPING){
	                sonicState = SonicState.RUNNING;
	                velocityx += SPRITE_BASE_RUN_VELOCITY;
	            }else
	                velocityx += 0.25 * SPRITE_BASE_RUN_VELOCITY;
	        }else if(inputEvent.keyPressed[KeyEvent.VK_A] && !inputEvent.keyPressed[KeyEvent.VK_D]){
	            sonicFacing = SonicFacing.LEFT;
	            if(sonicState != SonicState.JUMPING){
	                sonicState = SonicState.RUNNING;
	                velocityx -= SPRITE_BASE_RUN_VELOCITY;
	            }else
	                velocityx -= 0.25 * SPRITE_BASE_RUN_VELOCITY;
	        }else{
	            if(sonicState != SonicState.JUMPING){
	                sonicState = SonicState.WAITING;
	                
	                GameObjectUtilities.dampenVelocities(this, X_VELOCITY_DECELERATION, X_VELOCITY_DECELERATION,0.0,0.0,0.0,0.0);
	            }else {
	                GameObjectUtilities.dampenVelocities(this, X_VELOCITY_DECELERATION*0.2,X_VELOCITY_DECELERATION*0.2,0.0,0.0,0.0,0.0);
	            }
	        }
        }

        
        boolean touchingPlatform = false;
        GameObjectCollection platforms = gameLayer.getGameObjectCollection("Platforms");
        double sonicBasey = this.y+this.height/2;
        for(int idx = 0; !touchingPlatform && idx < platforms.size; idx++){
            double overlap = 10.0;
            Body platform = (Body)platforms.gameObjects[idx];
            if(sonicBasey - (platform.y-platform.height/2)<10.0 && sonicBasey - (platform.y-platform.height/2)> -10.0 && this.x + this.width/2 - overlap > platform.x - platform.width/2 && this.x -this.width/2 + overlap < platform.x + platform.width/2)
                touchingPlatform = true;
                }
        if(sonicState == SonicState.JUMPING)
            if(touchingPlatform)
                if(this.velocityy < SPRITE_BASE_JUMP_VELOCITY)
                    sonicState = SonicState.WAITING;
        
        if(id == 1){
	        if(inputEvent.keyPressed[KeyEvent.VK_NUMPAD0]){
	            if(touchingPlatform){
	                sonicState = SonicState.JUMPING;
	                velocityy -= SPRITE_BASE_JUMP_VELOCITY;
	            }
	        }
        }else if(id == 2){
	        if(inputEvent.keyPressed[KeyEvent.VK_SPACE]){
	            if(touchingPlatform){
	                sonicState = SonicState.JUMPING;
	                velocityy -= SPRITE_BASE_JUMP_VELOCITY;
	            }
	        }
        }
        if(velocityy>((CollisionSpace)gameLayer).getGravityY() / 10.0)
            sonicState = SonicState.JUMPING;
        
        if(this.sonicState != sonicStateOnEntry){
            switch(sonicState){
                case RUNNING: setRealisation("SonicRunning"); break;
                case WAITING: setRealisation("SonicWaiting"); break;
                case JUMPING: setRealisation("SonicJumping"); break;
                    
            }
        }
        
        GameObjectUtilities.clampVelocities(this, SPRITE_MAX_X_VELOCITY, SPRITE_MAX_X_VELOCITY, Double.MAX_VALUE, Double.MAX_VALUE, 0.0,0.0);
        
        this.rotation = 0.0;
        this.angularVelocity = 0.0;
    }
    
    public int getID(){
    	return id;
    }
    
    @Override
    public void draw(Graphics2D graphics2D, int drawX, int drawY){
        BufferedImage currentRealisation = getRealisation(0).getImageRealisation();
        int width = currentRealisation.getWidth();
        int height = currentRealisation.getHeight();
        
        if(this.sonicFacing == SonicFacing.LEFT){
            graphics2D.drawImage(currentRealisation, drawX+width/2, drawY-height/2, drawX-width/2, drawY+height/2, 0, 0, width, height, null);
        }else{
            graphics2D.drawImage(currentRealisation, drawX-width/2, drawY-height/2, drawX+width/2, drawY+height/2, 0, 0, width, height, null);
        }
    } 
    
    
}
