package tutorials.LoadingScreen;

import game.engine.*;
import java.awt.*;

public class LoadingScreenGameLayer extends GameLayer {
	
	public LoadingScreenGameLayer(GameEngine gameEngine){
		super("LoaderBackgroundDone", gameEngine);
		constructGameLayer();
		
	}
	
	private void constructGameLayer(){
		GameObject background = new GameObject(this);
		background.setRealisationAndGeometry("LoaderBackgroundDone");
		background.setPosition(gameEngine.screenWidth/2, gameEngine.screenHeight/2);
		addGameObject(background);
		
	}
	
	public void draw(Graphics2D graphics2D){
		Color originalColour = graphics2D.getColor();graphics2D.setColor(new Color(200,200,0,255));
		graphics2D.fillRect(0, 0, gameEngine.screenWidth,  gameEngine.screenHeight);
		graphics2D.setColor(originalColour);
		
		super.draw(graphics2D);
	}

}
