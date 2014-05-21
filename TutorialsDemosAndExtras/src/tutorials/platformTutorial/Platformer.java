/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tutorials.platformTutorial;
import game.engine.GameEngine;

import java.awt.Color;
import java.awt.Graphics2D;
/**
 *
 * @author Michael
 */
public class Platformer extends GameEngine{
    public static final int SCREEN_WIDTH = 1024;
    public static final int SCREEN_HEIGHT = 768;
    
    public Platformer(){
        gameStart(SCREEN_WIDTH, SCREEN_HEIGHT, 32);
    }
    
    @Override
    public boolean buildAssetManager(){
        assetManager.loadAssetsFromFile(getClass().getResource("images/PlatformerAssets.txt"));
        return true;
    }
    
    protected boolean buildInitialGameLayers(){
        PlatformLayer platformLayer = new PlatformLayer(this);
        addGameLayer(platformLayer);
        return true;
    }
    @Override
    protected void gameRender(Graphics2D graphics2D){
        Color originalColour = graphics2D.getColor();
        graphics2D.setColor(Color.black);
        graphics2D.fillRect(0,0,screenWidth, screenHeight);
        graphics2D.setColor(originalColour);
        
        super.gameRender(graphics2D);
    }
    public static void main(String[] args){
        new Platformer();
    }
}
