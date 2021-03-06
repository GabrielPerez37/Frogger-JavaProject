package Game.World;

import Game.Entities.Dynamic.Player;
import Game.Entities.Static.LillyPad;
import Game.Entities.Static.Log;
import Game.Entities.Static.StaticBase;
import Game.Entities.Static.Tree;
import Game.Entities.Static.Turtle;
import Game.GameStates.State;
import Main.Handler;
import UI.UIManager;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Random;

/**
 * Literally the world. This class is very important to understand.
 * Here we spawn our hazards (StaticBase), and our tiles (BaseArea)
 * 
 * We move the screen, the player, and some hazards. 
 * 				How? Figure it out.
 */
public class WorldManager {

	private ArrayList<BaseArea> AreasAvailables;			// Lake, empty and grass area (NOTE: The empty tile is just the "sand" tile. Ik, weird name.)
	private ArrayList<StaticBase> StaticEntitiesAvailables;	// Has the hazards: LillyPad, Log, Tree, and Turtle.

	private ArrayList<BaseArea> SpawnedAreas;				// Areas currently on world
	private ArrayList<StaticBase> SpawnedHazards;			// Hazards currently on world.

	Long time;
	Boolean reset = true;
	Boolean yLevel = true; 
	Handler handler;		

	private Player player;									// How do we find the frog coordinates? How do we find the Collisions? This bad boy.

	UIManager object = new UIManager(handler);
	UI.UIManager.Vector object2 = object.new Vector();


	private ID[][] grid;									
	private int gridWidth,gridHeight;						// Size of the grid. 
	private int movementSpeed;								// Movement of the tiles going downwards.
	public static int score;
	private int dummyScore;
	
	



	public WorldManager(Handler handler) {
		this.handler = handler;

		AreasAvailables = new ArrayList<>();				// Here we add the Tiles to be utilized.
		StaticEntitiesAvailables = new ArrayList<>();		// Here we add the Hazards to be utilized.

		AreasAvailables.add(new GrassArea(handler, 0));		
		AreasAvailables.add(new WaterArea(handler, 0));
		AreasAvailables.add(new EmptyArea(handler, 0));

		StaticEntitiesAvailables.add(new LillyPad(handler, 0, 0));
		StaticEntitiesAvailables.add(new Log(handler, 0, 0));
		StaticEntitiesAvailables.add(new Tree(handler,0,0));
		StaticEntitiesAvailables.add(new Turtle(handler, 0, 0));

		SpawnedAreas = new ArrayList<>();
		SpawnedHazards = new ArrayList<>();

		player = new Player(handler);       

		gridWidth = handler.getWidth()/64;
		gridHeight = handler.getHeight()/64;
		movementSpeed = 1;
		// movementSpeed = 20; I dare you.

		/* 
		 * 	Spawn Areas in Map (2 extra areas spawned off screen)
		 *  To understand this, go down to randomArea(int yPosition) 
		 */
		int spawn = 0;
		for(int i=0; i<gridHeight+2; i++) {
			if(spawn<8) {
				SpawnedAreas.add(randomArea((-2+i)*64));
				spawn++;
			}
			else{
				SpawnedAreas.add(new EmptyArea(handler, (-2+i)*64));
			}
		}

		player.setX((gridWidth/2)*64);
		player.setY((gridHeight-3)*64);

		// Not used atm.
		grid = new ID[gridWidth][gridHeight];
		for (int x = 0; x < gridWidth; x++) {
			for (int y = 0; y < gridHeight; y++) {
				grid[x][y]=ID.EMPTY;
			}
		}
	}

	public void tick() {

		if(player.getY()-1 >= 768) {
			State.setState(handler.getGame().gameOverState);
		}
		
		if(this.handler.getKeyManager().keyJustPressed(this.handler.getKeyManager().num[2])) {
			this.object2.word = this.object2.word + this.handler.getKeyManager().str[1];
		}
		if(this.handler.getKeyManager().keyJustPressed(this.handler.getKeyManager().num[0])) {
			this.object2.word = this.object2.word + this.handler.getKeyManager().str[2];
		}
		if(this.handler.getKeyManager().keyJustPressed(this.handler.getKeyManager().num[1])) {
			this.object2.word = this.object2.word + this.handler.getKeyManager().str[0];
		}
		if(this.handler.getKeyManager().keyJustPressed(this.handler.getKeyManager().num[3])) {
			this.object2.addVectors();
		}
		if(this.handler.getKeyManager().keyJustPressed(this.handler.getKeyManager().num[4]) && this.object2.isUIInstance) {
			this.object2.scalarProduct(handler);
		}

		if(this.reset) {
			time = System.currentTimeMillis();
			this.reset = false;
		}

		if(this.object2.isSorted) {

			if(System.currentTimeMillis() - this.time >= 2000) {		
				this.object2.setOnScreen(true);	
				this.reset = true;
			}

		}
		//Score verifier.
		if(handler.getKeyManager().keyJustPressed(KeyEvent.VK_W)) {
			if(dummyScore < score) {
				dummyScore++;
			}else if(dummyScore== score) {
				dummyScore++;
				score++;
			}else {
				dummyScore = score;
			}
		}
		if(handler.getKeyManager().keyJustPressed(KeyEvent.VK_S)) {
			dummyScore--;
		}
		
		for (BaseArea area : SpawnedAreas) {
			area.tick();
		}
		for (StaticBase hazard : SpawnedHazards) {
			hazard.tick();
		}

		for (int i = 0; i < SpawnedAreas.size(); i++) {
			SpawnedAreas.get(i).setYPosition(SpawnedAreas.get(i).getYPosition() + movementSpeed);

			// Check if Area (thus a hazard as well) passed the screen.
			if (SpawnedAreas.get(i).getYPosition() > handler.getHeight()) {
				// Replace with a new random area and position it on top
				SpawnedAreas.set(i, randomArea(-2 * 64));

			}
			//Make sure players position is synchronized with area's movement
			if (SpawnedAreas.get(i).getYPosition() < player.getY()
					&& player.getY() - SpawnedAreas.get(i).getYPosition() < 3) {
				player.setY(SpawnedAreas.get(i).getYPosition());
			}
		}

		HazardMovement();

		player.tick();
		//make player move the same as the areas
		player.setY(player.getY()+movementSpeed); 

		object2.tick();

	}
	
	public static int getScore() {
    	return score;
    }
	
	private void HazardMovement() {

		for (int i = 0; i < SpawnedHazards.size(); i++) {

			// Moves hazard down
			SpawnedHazards.get(i).setY(SpawnedHazards.get(i).getY() + movementSpeed);
			//Boundaries for Tree
			if(SpawnedHazards.get(i) instanceof Tree && SpawnedHazards.get(i).GetCollision() != null
					&& player.getPlayerCollision().intersects(SpawnedHazards.get(i).GetCollision())) {
				dummyScore--;
				
				
				if(player.facing=="UP") {
					player.setY(player.getY()+64);
				}
				if(player.facing=="DOWN") {
					player.setY(player.getY()-64);
				}
				if(player.facing=="LEFT") {
					player.setX(player.getX()+64);
				}
				if(player.facing=="RIGHT") {
					player.setX(player.getX()-64);
				}
			}
			//Moves Log to the right and re-appear on the left
			if (SpawnedHazards.get(i) instanceof Log) {
				SpawnedHazards.get(i).setX(SpawnedHazards.get(i).getX() + 1);
				if(SpawnedHazards.get(i).getX()>576) {
					SpawnedHazards.get(i).setX(-128);
				}
				if(SpawnedHazards.get(i).GetCollision() != null && player.getPlayerCollision().intersects(SpawnedHazards.get(i).GetCollision()) && player.getX()< 576 && player.getHeight()<576) {
					player.setX(player.getX() + 1);
				}
			}

			// Moves Turtle to the left
			if (SpawnedHazards.get(i) instanceof Turtle) {
				SpawnedHazards.get(i).setX(SpawnedHazards.get(i).getX() - 1);
				
				if(SpawnedHazards.get(i).getX() < 0) {
					SpawnedHazards.get(i).setX(640);
				}
				
				if(SpawnedHazards.get(i).GetCollision() != null && player.getPlayerCollision().intersects(SpawnedHazards.get(i).GetCollision()) && player.getX() < 576 && player.getHeight()<576) {
					player.setX(player.getX() + 1);				
				}
			}
				// Verifies the hazards Rectangles aren't null and
				// If the player Rectangle intersects with the Log or Turtle Rectangle, then
				// move player to the right.
			if (SpawnedHazards.get(i) instanceof Turtle) {
				if (SpawnedHazards.get(i).GetCollision() != null && player.getPlayerCollision().intersects(SpawnedHazards.get(i).GetCollision())) {
						player.setX(player.getX() - 3);
				}
			}
			if(SpawnedHazards.get(i) instanceof Log) {
				if (SpawnedHazards.get(i).GetCollision() != null && player.getPlayerCollision().intersects(SpawnedHazards.get(i).GetCollision())) {
						player.setX(player.getX() + 1);
				}
			}	

			// if hazard has passed the screen height, then remove this hazard.
			if (SpawnedHazards.get(i).getY() > handler.getHeight()) {
				SpawnedHazards.remove(i);
			}
		}
	}

	public void render(Graphics g){

		for(BaseArea area : SpawnedAreas) {
			area.render(g);
		}

		for (StaticBase hazards : SpawnedHazards) {
			hazards.render(g);

		}
		//Draws Score
		
		g.setColor(Color.black);
		g.setFont(new Font ("Impact",Font.BOLD,25));
		String score1= Integer.toString(score);

		g.setColor(Color.BLACK);
		g.fillRect(5, 5, 120, 35);
		g.setColor(Color.WHITE);
		g.setFont(new Font("ComicSans", Font.PLAIN, 20));
		g.drawString("Score: " + score1, 10, 28);
		g.drawRect(5, 5, 120, 35);
		//g.drawRect(30, 8, 100, 35);

		
		player.render(g);      
		this.object2.render(g);      

	}
	


	/*
	 * Given a yPosition, this method will return a random Area out of the Available ones.)
	 * It is also in charge of spawning hazards at a specific condition.
	 */
	private BaseArea randomArea(int yPosition) {
		Random rand = new Random();

		// From the AreasAvailable, get me any random one.
		BaseArea randomArea = AreasAvailables.get(rand.nextInt(AreasAvailables.size())); 

		if(randomArea instanceof GrassArea) {
			randomArea = new GrassArea(handler, yPosition);
			SpawnTree(yPosition);
		}
		else if(randomArea instanceof WaterArea) {
			randomArea = new WaterArea(handler, yPosition);
			SpawnHazard(yPosition);
		}
		else {
			randomArea = new EmptyArea(handler, yPosition);
		}
		return randomArea;
	}

	private void SpawnTree(int yPosition) {
		Random rand = new Random();
		int randInt;
		int choice = rand.nextInt(4);
		if (choice <=2) {
			randInt = 64 * rand.nextInt(4);
			SpawnedHazards.add(new Tree(handler, randInt, yPosition));
		}
	}

	/*
	 * Given a yPositionm this method will add a new hazard to the SpawnedHazards ArrayList
	 */
	private void SpawnHazard(int yPosition) {
		Random rand = new Random();
		int randInt;
		int choice = rand.nextInt(7);
		int lillySpawn = rand.nextInt(5) + 1;
		int election = rand.nextInt(1);

		// Chooses between Log or LillyPad
		if (choice >=5){
			randInt = 64 * rand.nextInt(9);
			SpawnedHazards.add(new LillyPad(handler, randInt, yPosition));
			yLevel = false;
		}

		else {
			if(yLevel) {
				if(election == 1) {
					randInt = 64 * rand.nextInt(4);
					SpawnedHazards.add(new Log(handler, randInt, yPosition));
					yLevel = false;
				}
				else {
					randInt = 64 * rand.nextInt(3);
					SpawnedHazards.add(new Turtle(handler, randInt, yPosition));
					yLevel = false;
				}	
			}
			else if (yLevel) {
				for(int i = 0; i < lillySpawn; i++) {
					randInt = 64 * rand.nextInt(3);
					SpawnedHazards.add(new LillyPad(handler, randInt, yPosition));
				}
				yLevel = true;
			}
			else {
				for(int j=0; j< rand.nextInt(5); j++) {
					randInt = 128 * j;
					SpawnedHazards.add(new Log(handler, randInt, yPosition));
					
				}
				
			}
		}

	}
	
	private void triggerGameOver() {
		for (int i = 0; i < SpawnedHazards.size(); i++)
		if(SpawnedAreas.get(i) instanceof WaterArea) {
			if(player.getY() == SpawnedAreas.get(i).getYPosition()){
				State.setState(handler.getGame().gameOverState); 
			}
		}
		
		
	}

}
