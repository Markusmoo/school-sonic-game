package tutorials.topDownScroller;
import game.engine.GameEngine;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;


public class TopDownScroller extends GameEngine {
	
	private static final long serialVersionUID = 1L;
	private static final int SCREEN_WIDTH =1024;
	private static final int SCREEN_HEIGHT = 768;
	public static int SCORE = 0;
	public static String HIT_POINTS = "* * *";
	
	private boolean displayHelpOverlay = false ;
	
	public TopDownScroller(){
		this.gameStart(SCREEN_WIDTH, SCREEN_HEIGHT,32);
		
	}
	
	public boolean buildAssetManager(){
		assetManager.loadAssetsFromFile(this.getClass().getResource("images/TopDownScrollerAssets.txt"));
		return true;
	}
	
	protected boolean buildInitialGameLayers(){
		TopDownScrollerLayer spaceLayer = new TopDownScrollerLayer(this);
		addGameLayer(spaceLayer);
		return true;
	}
	
	protected void considerInput(){
		super.considerInput();
		
		if(this.inputEvent.keyTyped(KeyEvent.VK_H))
			this.displayHelpOverlay = !this.displayHelpOverlay;
			
	}
	
	protected void gameRender(Graphics2D g){
		Color originalColour = g.getColor();
		g.setColor(Color.black);
		g.fillRect(0, 0, screenWidth,  screenHeight);
		g.setColor(originalColour);
		
		super.gameRender(g);
		
		g.setFont(new Font("MONOSPACE",Font.BOLD,12));
		g.setColor(Color.YELLOW);
		
		g.drawString("ESC - Quit : H - help", 10, 10);
		
		if(this.displayHelpOverlay == true){
			g.drawString("Right Arrow - Move right", 10, 30);
			g.drawString("Left Arrow - Move left", 10,45);
			g.drawString("Up Arrow - Move forward", 10,60);
			g.drawString("Down Arrow - Move backwards", 10,75);
			g.drawString("Space - Fire", 10,90);
		}
		
		g.setFont(new Font("MONOSPACE",Font.BOLD,34));
		g.setColor(Color.BLACK);
		g.drawString("Score: "+SCORE, (SCREEN_WIDTH/2)-30, 30);
		g.setColor(Color.red);
		g.drawString("Hitpoints: "+HIT_POINTS, (SCREEN_WIDTH/2)-30, SCREEN_HEIGHT-30);
	}
	public static void main(String[] args){
		new TopDownScroller();
	}

}
