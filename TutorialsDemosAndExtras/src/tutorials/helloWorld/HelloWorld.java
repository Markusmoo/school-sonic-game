/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tutorials.helloWorld;

import game.engine.*;
import java.awt.*;
/**
 *
 * @author Michael
 */
public class HelloWorld extends GameEngine{
 
 
 public HelloWorld(){
     gameStart(1920,1080,32);
 }
 
 public boolean buildAssetManager(){
     assetManager.addImageAsset("HelloWorldAsset",
     getClass().getResource("images/Jimmy.jpg"));
     return true;
 }
 
 
 protected boolean buildInitialGameLayers(){
     HelloWorldLayer helloWorldLayer = new HelloWorldLayer(this);
     addGameLayer(helloWorldLayer);
     return true;
 }
 protected void gameRender(Graphics2D graphics2D){
     super.gameRender(graphics2D);
 }
 public static void main(String[] args){
     HelloWorld instance = new HelloWorld();
 }
    
}
