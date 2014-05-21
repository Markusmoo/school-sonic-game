/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tutorials.bodyExample;
import game.engine.*;
import game.physics.*;
import java.awt.event.*;
/**
 *
 * @author Michael
 */
public class Spaceship extends Body {
   /* private double shipMovementAcceleration = 10.0;
    private double shipRotationalAcceleration = 0.15;
    private double shipRotationalDampening = 0.1;
   */private double shipMovementAcceleration = 8.0;
    private double shipRotationalAcceleration = 1.15;
    private double shipRotationalDampening = 0.3; 
   
    
    public Spaceship(GameLayer gameLayer){
        super(gameLayer);
        
        x=gameLayer.width / 2;
        y=gameLayer.height /2;
        
        rotation = Math.PI /2.0;
        
        setMass(115.0);
        
        setRealisationAndGeometry("Spaceship");
    }
    
    public void update(){
        
        if(inputEvent.keyPressed[KeyEvent.VK_W]){
            
            velocityx += Math.sin(rotation) * shipMovementAcceleration;
            velocityy -= Math.cos(rotation) * shipMovementAcceleration;
        }else if(inputEvent.keyPressed[KeyEvent.VK_S]){
            velocityx -= Math.sin(rotation) * shipMovementAcceleration;
            velocityy += Math.cos(rotation) * shipMovementAcceleration;
         }
        
        if(inputEvent.keyPressed[KeyEvent.VK_D]){
            angularVelocity += shipRotationalAcceleration;
        }else if(inputEvent.keyPressed[KeyEvent.VK_A]){
            angularVelocity -= shipRotationalAcceleration;
        }
        
        if(Math.abs(angularVelocity) < shipRotationalDampening){
            angularVelocity = 0.0;
        }else {
            angularVelocity -= shipRotationalDampening * Math.signum(angularVelocity);
        }
    }
   
}
