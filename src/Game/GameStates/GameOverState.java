package Game.GameStates;


import Main.Handler;
import Resources.Images;
import UI.ClickListlener;
import UI.UIImageButton;
import UI.UIManager;

import java.awt.*;

import Game.Entities.Dynamic.Player;

/**
 * Created by AlexVR on 7/1/2018.
 */
public class GameOverState extends State {

    private UIManager uiManager;

    public GameOverState(Handler handler) {
        super(handler);
        uiManager = new UIManager(handler);
        handler.getMouseManager().setUimanager(uiManager);

       
        
        uiManager.addObjects(new UIImageButton(handler.getWidth()-210, handler.getHeight()-150, 128+50, 64, Images.DeathRetry, new ClickListlener() {
            @Override
            public void onClick() {
                handler.getMouseManager().setUimanager(null);
                handler.getGame().reStart();
                State.setState(handler.getGame().gameState);
            }
        }));
        	
        uiManager.addObjects(new UIImageButton(60, handler.getHeight()-150, 150, 65, Images.DeathTitle, new ClickListlener() {
            @Override
            public void onClick() {
            	handler.getMouseManager().setUimanager(null);
            	State.setState(handler.getGame().menuState);
            	
            }
        }));
    }

    @Override
    public void tick() {
        handler.getMouseManager().setUimanager(uiManager);
        uiManager.tick();

    }

    @Override
    public void render(Graphics g) {
        g.setColor(Color.darkGray);
        g.fillRect(0,0,handler.getWidth(),handler.getHeight());
        g.drawImage(Images.DeathScreen,0,0,handler.getWidth(),handler.getHeight(),null);
        uiManager.Render(g);
        g.setColor(Color.white);
        g.setFont(new Font("ComicSans",Font.PLAIN,50));
    	//g.drawString("Score: " + handler.getScore()*100, handler.getWidth()-520, handler.getHeight()/2+150);

    }


}