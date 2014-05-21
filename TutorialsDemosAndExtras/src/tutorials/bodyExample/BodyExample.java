/*^
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tutorials.bodyExample;
import java.awt.*;
import game.engine.*;
/**
 *
 * @author Michael
 */
public class BodyExample extends GameEngine {
    public boolean buildAssetManager(){
        assetManager.loadAssetsFromFile(getClass().getResource("images/Tutorial2Assets.txt"));
        return true;
    }
    
    protected boolean buildInitialGameLayers(){
        BodyExampleLayer bodyExampleLayer = new BodyExampleLayer(this);
        addGameLayer(bodyExampleLayer);
        
        return true;
    }
    
    public BodyExample(){
        DisplayMode currentDisplayMode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        
        gameStart(currentDisplayMode.getWidth(),
                currentDisplayMode.getHeight(),
                currentDisplayMode.getBitDepth());
        //Hi my name is billy and i like to suck dick
    }
    
    public static void main(String[] args){
        BodyExample instance = new BodyExample();
    }
    
}
