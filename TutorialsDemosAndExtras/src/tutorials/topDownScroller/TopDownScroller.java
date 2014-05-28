package tutorials.topDownScroller;
import game.engine.GameEngine;

import java.awt.*;
import java.awt.event.KeyEvent;


public class TopDownScroller extends GameEngine {
	
	private static final int SCREEN_WIDTH =1024;
	private static final int SCREEN_HEIGHT = 768;
	
	private boolean displayHelpOverlay = false ;
	
	public TopDownScroller(){
		this.gameStart(SCREEN_WIDTH, SCREEN_HEIGHT,32);
		
	}
	
	public boolean buildAssetManager(){
		assetManager.loadAssetsFromFile(this.getClass().getResource("imgaes/TopDownScrollerAssets.txt"));
		return true;
	}
	
	//cant find out why this is wrong because im Dyslexic 
	protected boolean buildInitialGameLayers(){
		TopDownScollerLayer spaceLayer = new TopDownScollerLayer(this);
		addGameLayer(spaceLayer);
		
	}
	
	protected void considerInput(){
		super.considerInput();
		
		if(this.inputEvent.keyTyped(KeyEvent.VK_H))
			this.displayHelpOverlay = !this.displayHelpOverlay;
			
	}
	
	protected void gameRender(Graphics2D graphics2D){
		Color originalColour = graphics2D.getColor();
		graphics2D.setColor(Color.black);
		graphics2D.fillRect(0, 0, screenWidth,  screenHeight);
		graphics2D.setColor(originalColour);
		
		graphics2D.setFont(new Font("MONOSPACED",Font.BOLD,12));
		graphics2D.setColor(Color.YELLOW);
		
		graphics2D.drawString("ESC - Quit : H - help", 10, 10);
		
		if(this.displayHelpOverlay == true){
			graphics2D.drawString("Right Arrow - Move right", 10, 30);
			graphics2D.drawString("Left Arrow - Move left", 10,45);
			graphics2D.drawString("Up Arrow - Move forward", 10,60);
			graphics2D.drawString("Down Arrow - Move backwards", 10,75);
			graphics2D.drawString("Space - Fire", 10,90);
			}
		
	}
	public static void main(String[]args){
		TopDownScroller instance = new TopDownScroller();
	}

}
