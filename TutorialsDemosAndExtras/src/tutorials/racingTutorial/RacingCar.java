/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tutorials.racingTutorial;
import game.engine.*;
import java.awt.event.*;
/**
 *
 * @author Michael
 */
public class RacingCar extends GameObject {
    public double carForwardAcceleration = 0.10;
    public double carBackwardAcceleration = 0.08;
    public double carDeceleration = 0.975;
    public double carTurningRate = 0.002;
    
    public double frictionalDriftDeceleration = 0.05;
    public double frictionalMovementDeceleration = 0.98;
    public double frictionalAngularDeceleration = 0.95;
    
    public double maxVelocity = 5.0;
    public double maxAngularVelocity = 0.03;
    
    public RacingCar(GameLayer gameLayer){
        super(gameLayer);
        setRealisationAndGeometry("Car");
    }
    
    @Override
    public void update(){
        if(velocityx != 0.0 || velocityy != 0.0){
        double absVelx = Math.abs(velocityx), absVely = Math.abs(velocityy);
        double drift = Math.abs((absVelx/(absVelx+absVely))*Math.cos(rotation) + 
                (absVely/(absVelx+absVely))*Math.sin(rotation));
        
        double frictionDeceleration = frictionalMovementDeceleration - frictionalDriftDeceleration * drift;
        
        velocityx *= frictionDeceleration;
        velocityy *= frictionDeceleration;
    }
        if(GameInputEventManager.keyPressed[KeyEvent.VK_SPACE]){
            velocityx*= carDeceleration;
            velocityy*= carDeceleration;
        }
        angularVelocity *= frictionalAngularDeceleration;
        
        if(GameInputEventManager.keyPressed[KeyEvent.VK_UP]){
            velocityx += Math.sin(rotation) * carForwardAcceleration;
            velocityy -= Math.cos(rotation) * carForwardAcceleration;
        }else if(GameInputEventManager.keyPressed[KeyEvent.VK_DOWN]){
              velocityx -= Math.sin(rotation) * carBackwardAcceleration;
              velocityy += Math.cos(rotation) * carBackwardAcceleration;
          
        }
        double speedRatioSqrd = (velocityx*velocityx + velocityy*velocityy) / (maxVelocity*maxVelocity);
        if(speedRatioSqrd > 1.0){
            velocityx /= Math.sqrt(speedRatioSqrd);
            velocityy /= Math.sqrt(speedRatioSqrd);
            
        }
        if(inputEvent.keyPressed[KeyEvent.VK_RIGHT]){
            angularVelocity +=carTurningRate * Math.min(1.0, 4.0 * Math.sqrt(velocityx*velocityx+velocityy*velocityy)/maxVelocity);
        }else if(inputEvent.keyPressed[KeyEvent.VK_LEFT]){
            angularVelocity -= carTurningRate * Math.min(1.0, 4.0 * Math.sqrt(velocityx*velocityx+velocityy*velocityy)/maxVelocity);
        }
        if( Math.abs(angularVelocity)>maxAngularVelocity)
            angularVelocity = maxAngularVelocity * Math.signum(angularVelocity);
        
            x += velocityx;
            y += velocityy;
            rotation += angularVelocity;
        
          
    }
    
}
