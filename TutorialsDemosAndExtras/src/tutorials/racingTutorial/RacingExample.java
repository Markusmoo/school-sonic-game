/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tutorials.racingTutorial;
import game.engine.*;
/**
 *
 * @author Michael
 */
public class RacingExample extends GameEngine {
    private static final int SCREEN_WIDTH = 1920;
    private static final int SCREEN_HEIGHT = 1080;
    
    public RacingExample(){
        gameStart( SCREEN_WIDTH, SCREEN_HEIGHT, 32);
    }
    
    public boolean buildAssetManager(){
        assetManager.loadAssetsFromFile( this.getClass().getResource("images/RacingAssets.txt"));
        return true;
    }
            
    protected boolean buildInitialGameLayers(){
        RacingExampleLayer racingLayer = new RacingExampleLayer(this);
        addGameLayer(racingLayer);
        return true;
    }
    public static void main(String[] args){
        RacingExample instance = new RacingExample();
    }
}
