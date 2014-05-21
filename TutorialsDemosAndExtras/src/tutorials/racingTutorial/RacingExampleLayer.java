/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tutorials.racingTutorial;
import game.engine.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
/**
 *
 * @author Michael
 */
public class RacingExampleLayer extends GameLayer {
    private static final int Z_DEPTH_TRACK = 0;
    private static final int Z_DEPTH_CARS = 1;
    
    private BufferedImage trackHitRegions = null;
    
    private double carLastValidX;
    private double carLastValidY;
    private double carLastValidRotation;
    
    public RacingExampleLayer(GameEngine gameEngine){
        super("RacingLayer", gameEngine);
        
        createTrack();
        createCar();
    }
    public void createTrack(){
        GameObject track = new GameObject(this);
        track.setRealisationAndGeometry("Track");
        track.setDrawOrder(Z_DEPTH_TRACK);
        addGameObject(track);
        
        track.x = track.width/2;
        track.y = track.height/2;
        
        trackHitRegions = assetManager.retrieveGraphicalAsset("TrackHitRegions").getImageRealisation();
    }
    public void createCar(){
        RacingCar racingCar = new RacingCar(this);
        racingCar.setName("RacingCar");
        
        racingCar.setPosition(150,350);
        racingCar.setDrawOrder(Z_DEPTH_CARS);
        
        addGameObject(racingCar);
    }
    public void update(){
        RacingCar racingCar = (RacingCar)this.getGameObject("RacingCar");
        racingCar.update();
        considerCarLocation(racingCar);
       
    }
    private void considerCarLocation(RacingCar racingCar){
        Point carTopMiddle = new Point(
                (int)(racingCar.x),
                (int)(racingCar.y-racingCar.width/2));
        AffineTransform rotationalTransform = new AffineTransform();
        rotationalTransform.rotate(racingCar.rotation, racingCar.x,racingCar.y);
        
        Point carTopMiddleRotated = new Point();
        rotationalTransform.transform(carTopMiddle, carTopMiddleRotated);
        
        int pixelTopMiddleRotated = trackHitRegions.getRGB(carTopMiddleRotated.x, carTopMiddleRotated.y);
        
        if( (pixelTopMiddleRotated & 0x00ff0000)>0)
            carOutsideBounds(racingCar);
        else if((pixelTopMiddleRotated & 0x000000ff)>0)
            carOnRough(racingCar);
        
        carLastValidX = racingCar.x;
        carLastValidY = racingCar.y;
        carLastValidRotation = racingCar.rotation;
            
    }
    private void carOutsideBounds(RacingCar racingCar){
        racingCar.x = carLastValidX;
        racingCar.y = carLastValidY;
        racingCar.rotation=carLastValidRotation;
    }
    private void carOnRough(RacingCar racingCar){
        racingCar.velocityx *= 0.95;
        racingCar.velocityy *= 0.95;
        racingCar.angularVelocity *= 0.98;
        
    }
    public void draw(Graphics2D graphics2D){
        Color originalColour = graphics2D.getColor();
        graphics2D.setColor(Color.black);
        graphics2D.fillRect(0,0,gameEngine.screenWidth, gameEngine.screenHeight);
        graphics2D.setColor(originalColour);
        
        super.draw(graphics2D);
    }
}
