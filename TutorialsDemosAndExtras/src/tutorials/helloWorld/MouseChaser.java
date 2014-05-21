/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tutorials.helloWorld;
import game.engine.*;
/**
 *
 * @author Michael
 */
public class MouseChaser extends GameObject {
    public MouseChaser(GameLayer gameLayer){
        super(gameLayer);
        setRealisationAndGeometry("HelloWorldAsset");
        
    }
    
    public void update(){
        this.x=inputEvent.mouseXCoordinate;
        this.y=inputEvent.mouseYCoordinate;
    }
    
    
                
    }
    
    
    

