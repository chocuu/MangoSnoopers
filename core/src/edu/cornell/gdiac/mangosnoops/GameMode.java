/*
 * GameMode.java
 *
 * This is the primary class file for running the game.  You should study this file for
 * ideas on how to structure your own root class. This class follows a 
 * model-view-controller pattern fairly strictly.
 *
 * Author: Walker M. White
 * Based on original Optimization Lab by Don Holden, 2007
 * LibGDX version, 2/2/2015
 */
package edu.cornell.gdiac.mangosnoops;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;

import edu.cornell.gdiac.mangosnoops.hudentity.Child;
import edu.cornell.gdiac.mangosnoops.roadentity.Gnome;
import edu.cornell.gdiac.util.*;

/**
 * The primary controller class for the game.
 *
 * While GDXRoot is the root class, it delegates all of the work to the player mode
 * classes. This is the player mode class for running the game. In initializes all 
 * of the other classes in the game and hooks them together.  It also provides the
 * basic game loop (update-draw).
 */
public class GameMode implements Screen {
	/** 
 	 * Track the current state of the game for the update loop.
 	 */
	public enum GameState {
		/** Before the game has started */
		INTRO,
		/** While we are playing the game */
		PLAY,
		/** When the ships is dead (but shells still work) */
		OVER
	}

	// LEVEL FILES TODO idk
	private static String TEST_LEVEL_0_EXCEL = "test_level0.xlsx";

	// GRAPHICS AND SOUND RESOURCES
	/** The file for the background image to scroll */
	private static String BKGD_FILE = "images/background.png";
	/** The font file to use for scores */
	private static String FONT_FILE = "fonts/ComicSans.ttf";
	/** The file for the cloud image */
	private static String CLOUDS_FILE = "images/clouds.png";
	/** The file for the sky image */
	private static String SKY_FILE = "images/sky.png";
	/** The file for the rear view mirror */
	private static final String REARVIEW_MIRROR_FILE = "images/DashHUD/rearview.png";

	/** The file for the angry speech bubble */
	private static final String SPEECH_BUBBLE_FILE = "images/speechbubble.png";

	// Loaded assets
	/** The background image for the game */
	private Texture background;
	/** The font for giving messages to the player */
	private BitmapFont displayFont;
	private static final int FONT_SIZE = 24;
	/** Track all loaded assets (for unloading purposes) */
	private Array<String> assets;

	/** Texture of the clouds */
	private Texture clouds;
	/** Texture of the sky */
	private Texture sky;
	/** Texture of the angry speech bubble */
	private Texture speechBubble;


	/** Counter for the game */
	private int counter;
	/** Tracker for global miles traversed in story mode of game TODO do something w this */
	private float globalMiles;

	/** Factor used to compute where we are in scrolling process */
	private static final float TIME_MODIFIER    = 0.06f;
	/** Offset for the shell counter message on the screen */
	private static final float COUNTER_OFFSET   = 5.0f;
	/** Offset for the game over message on the screen */
	private static final float GAME_OVER_OFFSET = 40.0f;
	/** Origin of health gauge */
	private static final Vector2 HEALTH_GAUGE_ORIGIN = new Vector2(0.0f,0.0f);

	/** Reference to drawing context to display graphics (VIEW CLASS) */
	private GameCanvas canvas;

	/** Reads input from keyboard or game pad (CONTROLLER CLASS) */
	private InputController inputController;
	/** Handle collision and physics (CONTROLLER CLASS) */
	private CollisionController collisionController;
	/** Constructs the game models and handle basic gameplay (CONTROLLER CLASS) */
	private GameplayController gameplayController;
	/** Handles all sound Output **/
	private SoundController soundController;
	/** Variable to track the game state (SIMPLE FIELDS) */
	private GameState gameState;
	/** Variable to track total time played in milliseconds (SIMPLE FIELDS) */
	private float totalTime = 0;
	/** Whether or not this player mode is still active */
	private boolean active;
	/** Listener that will update the player mode when we are done */
	private ScreenListener listener;

	/** 
	 * Preloads the assets for this game.
	 * 
	 * The asset manager for LibGDX is asynchronous.  That means that you
	 * tell it what to load and then wait while it loads them.  This is 
	 * the first step: telling it what to load.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public void preLoadContent(AssetManager manager) {
		// Load the background.
		manager.load(BKGD_FILE,Texture.class);
		assets.add(BKGD_FILE);

		// Load the clouds
		manager.load(CLOUDS_FILE, Texture.class);
		// Load sky
		manager.load(SKY_FILE, Texture.class);
		// Load speech bubble
		manager.load(SPEECH_BUBBLE_FILE, Texture.class);
		assets.add(SPEECH_BUBBLE_FILE);

		// Load the font
		FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
		size2Params.fontFileName = FONT_FILE;
		size2Params.fontParameters.size = FONT_SIZE;
		manager.load(FONT_FILE, BitmapFont.class, size2Params);
		assets.add(FONT_FILE);
		
		// Preload gameplay content
		gameplayController.preLoadContent(manager,assets);
	}

	/** 
	 * Loads the assets for this game.
	 * 
	 * The asset manager for LibGDX is asynchronous.  That means that you
	 * tell it what to load and then wait while it loads them.  This is 
	 * the second step: extracting assets from the manager after it has
	 * finished loading them.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public void loadContent(AssetManager manager) {
		// Allocate the font
		if (manager.isLoaded(FONT_FILE)) {
			displayFont = manager.get(FONT_FILE,BitmapFont.class);
		} else {
			displayFont = null;
		}

		// Allocate assets
		if (manager.isLoaded(BKGD_FILE)) {
			background = manager.get(BKGD_FILE, Texture.class);
			background.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}

		if (manager.isLoaded(CLOUDS_FILE)) {
			clouds = manager.get(CLOUDS_FILE, Texture.class);
		}

		if (manager.isLoaded(SKY_FILE)) {
			sky = manager.get(CLOUDS_FILE, Texture.class);
		}

		if (manager.isLoaded(SPEECH_BUBBLE_FILE)) {
			speechBubble = manager.get(SPEECH_BUBBLE_FILE, Texture.class);
		}

		// Load gameplay content
		gameplayController.loadContent(manager);
	}

	/** 
	 * Unloads the assets for this game.
	 * 
	 * This method erases the static variables.  It also deletes the associated textures 
	 * from the asset manager.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public void unloadContent(AssetManager manager) {
    	for(String s : assets) {
    		if (manager.isLoaded(s)) {
    			manager.unload(s);
    		}
    	}
	}


	/**
	 * Creates a new game with the given drawing context.
	 *
	 * This constructor initializes the models and controllers for the game.  The
	 * view has already been initialized by the root class.
	 */
	public GameMode(GameCanvas canvas) {
	    // TODO DO SOMETHING ELSE W THE EXCEPTIONS
	    try {
            this.canvas = canvas;
            active = false;
            // Null out all pointers, 0 out all ints, etc.
            gameState = GameState.INTRO;
            assets = new Array<String>();

            // Create the controllers.
            inputController = new InputController();
            gameplayController = new GameplayController(new LevelObject(TEST_LEVEL_0_EXCEL));
            collisionController = new CollisionController(canvas.getWidth(), canvas.getHeight());
            soundController = new SoundController();
        } catch (Exception e) {
	        System.out.println(e.getMessage());
        }
	}
	
	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose() {
		inputController = null;
		gameplayController = null;
		collisionController = null;
		canvas = null;
	}
	
	
	/**
	 * Update the game state.
	 *
	 * We prefer to separate update and draw from one another as separate methods, instead
	 * of using the single render() method that LibGDX does.  We will talk about why we
	 * prefer this in lecture.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	private void update(float delta) {
		//Ensure all images are properly drawn, scaled, and updated
		Image.updateScreenDimensions(canvas);
		// Process the game input
		inputController.readInput();
		// Test whether to reset the game.
		try {
			switch (gameState) {
				case INTRO:
					gameState = GameState.PLAY;
					gameplayController.start(canvas.getWidth() / 2.0f, 0);
					break;
				case OVER:

					if (inputController.didReset()) {
						gameplayController.reset();
						soundController.reset();
						canvas.resetCam();
						gameState = GameState.PLAY;
						gameplayController.start(canvas.getWidth() / 2.0f, 0);
					}
					else {
						play(delta);
					}
					break;
				case PLAY:
					if (inputController.didReset()) {
						System.out.println("RESETTING");
						gameplayController.reset();
						soundController.reset();
						canvas.resetCam();
						gameState = GameState.PLAY;
						gameplayController.start(canvas.getWidth() / 2.0f, 0);
					}
					else {
						play(delta);
					}
					break;
				default:
					break;
			}
		} catch (Exception e) {
			// TODO get rid of this block probably
		}

	}

	/**
	 * This method processes a single step in the game loop.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	protected void play(float delta) {

		// Check if game is over
		// TODO: also check if end of level
		if (gameplayController.getCar().isDestroyed()) {
            gameState = GameState.OVER;
        }

		// Update Based on input
        gameplayController.handleEvents(delta, gameplayController.getCar().getNed(), gameplayController.getCar().getNosh());
		gameplayController.resolveActions(inputController, delta);

		// Update child states TODO: idk
		gameplayController.resolveChildren(counter, gameplayController.getCar().getNed(),
											gameplayController.getCar().getNosh(), gameplayController.getRadio());

		// Check for collisions
		totalTime += (delta*1000); // Seconds to milliseconds
		float offset =  canvas.getWidth() - (totalTime * TIME_MODIFIER) % canvas.getWidth();
		collisionController.processCollisions(gameplayController.getGnomez(),gameplayController.getCar(), gameplayController);

		// Play resulting sound
		soundController.play(gameplayController.getRadio());

		// Clean up destroyed objects
		gameplayController.garbageCollect();

		// Set Camera
		canvas.setCameraXY(gameplayController.getCar().getPosition());

		// Update the counter
		if(counter == Integer.MAX_VALUE){
			counter = 0;
		}
		counter += 1;
	}
	
	/**
	 * Draw the status of this player mode.
	 *
	 * We prefer to separate update and draw from one another as separate methods, instead
	 * of using the single render() method that LibGDX does.  We will talk about why we
	 * prefer this in lecture.
	 */
	private void draw(float delta) {

		canvas.clearScreen();


        gameplayController.getRoad().draw(canvas);

		//Gnomez
		for (Gnome g : gameplayController.getGnomez()) {
			g.draw(canvas);
		}

		canvas.drawWorld();


		// ** Draw HUD stuff **
		canvas.beginHUDDrawing();

		// Clouds
		canvas.draw(clouds, Color.WHITE, 0, 0, 0.25f*canvas.getHeight(), 0.715f*canvas.getHeight(), 0,
				(float)canvas.getHeight()/(float)clouds.getWidth(), (float)canvas.getHeight()/(float)clouds.getHeight());

		// Draw speech bubbles, if necessary

		if (!gameplayController.getRoad().reachedEndOfLevel()) {
			gameplayController.getCar().getNed().drawSpeechBubble(canvas, speechBubble);
			gameplayController.getCar().getNosh().drawSpeechBubble(canvas, speechBubble);
		}

		///**  Draw Dash and Interactive HUD Elements **///
		gameplayController.getCar().drawDash(canvas);

		// Vroom Stick
		gameplayController.getVroomStick().draw(canvas);

		// Wheel
		gameplayController.getWheel().draw(canvas);

		// Radio
		gameplayController.getRadio().draw(canvas, displayFont);


		// Health gauge and pointer
		Color healthGaugeColor = Color.WHITE;
        if (gameplayController.getCar().getIsDamaged()) {
        	healthGaugeColor = Color.RED;
		}
        gameplayController.getHealthGauge().draw(canvas, healthGaugeColor);

		// FIXME: this is a mess
		gameplayController.getRearviewBackground().draw(canvas);
		gameplayController.getRearviewEnemy().draw(canvas);
		gameplayController.getRearviewSeats().draw(canvas);
		gameplayController.getRearviewCover().draw(canvas);
		// Draw Ned and Nosh
        /*
		gameplayController.getCar().getNosh().draw(canvas, rearviewBackground);
		gameplayController.getCar().getNed().draw(canvas, rearviewBackground);
		*/

        /*
		// Draw rearview background
		canvas.draw(rearviewBackground,Color.WHITE,rearviewBackground.getWidth()*0.5f,rearviewBackground.getHeight()*0.5f,
					0.844f*canvas.getWidth(),0.871f*canvas.getHeight(),0,
					canvas.getHeight()/(rearviewBackground.getHeight()*3.5f),canvas.getHeight()/(rearviewBackground.getHeight()*3.5f));


		// Draw rearview seats
		canvas.draw(rearviewSeats,Color.WHITE,rearviewBackground.getWidth()*0.5f,rearviewBackground.getHeight()*0.5f,
				0.844f*canvas.getWidth(),0.871f*canvas.getHeight(),0,
				canvas.getHeight()/(rearviewBackground.getHeight()*3.5f),canvas.getHeight()/(rearviewBackground.getHeight()*3.5f));

		// Draw rearview cover
		canvas.draw(rearviewCover,Color.WHITE,rearviewBackground.getWidth()*0.5f,rearviewBackground.getHeight()*0.5f,
				0.844f*canvas.getWidth(),0.871f*canvas.getHeight(),0,
				canvas.getHeight()/(rearviewBackground.getHeight()*3.5f),canvas.getHeight()/(rearviewBackground.getHeight()*3.5f));
				*/

		//Draw inventory
		gameplayController.getInventory().draw(canvas);

		if (gameplayController.getRoad().reachedEndOfLevel()) {
            gameState = GameState.OVER;
		}

		// Draw messages
		switch (gameState) {
			case INTRO:
				break;
			case OVER:
				if (!gameplayController.getCar().isDestroyed()) {
					canvas.drawTextCentered("YOU WON", displayFont, GAME_OVER_OFFSET);
					canvas.drawTextCentered("Press R to restart", displayFont, GAME_OVER_OFFSET - 40);
				} else {
					canvas.drawTextCentered("GNOME OVER", displayFont, GAME_OVER_OFFSET);
					canvas.drawTextCentered("Press R to restart", displayFont, GAME_OVER_OFFSET - 40);
				}
				break;
			case PLAY:
				break;
			default:
				break;
		}

		// Flush information to the graphic buffer.
		canvas.endHUDDrawing();

//		if (gameplayController.getCar().getIsDamaged()) {
//		    canvas.drawDamageIndicator(gameplayController.getCar().getDamageDisplayAlpha());
//        }
	}
	
	/**
	 * Called when the Screen is resized. 
	 *
	 * This can happen at any point during a non-paused state but will never happen 
	 * before a call to show().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		// IGNORE FOR NOW
	}

	/**
	 * Called when the Screen should render itself.
	 *
	 * We defer to the other methods update() and draw().  However, it is VERY important
	 * that we only quit AFTER a draw.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void render(float delta) {
		if (active) {
			update(delta);
			draw(delta);
			if (inputController.didExit() && listener != null) {
				listener.exitScreen(this, 0);
			}
		}
	}

	/**
	 * Called when the Screen is paused.
	 * 
	 * This is usually when it's not active or visible on screen. An Application is 
	 * also paused before it is destroyed.
	 */
	public void pause() {
		// TODO Auto-generated method stub
	}

	/**
	 * Called when the Screen is resumed from a paused state.
	 *
	 * This is usually when it regains focus.
	 */
	public void resume() {
		// TODO Auto-generated method stub
	}
	
	/**
	 * Called when this screen becomes the current screen for a Game.
	 */
	public void show() {
		// Useless if called in outside animation loop
		active = true;
	}

	/**
	 * Called when this screen is no longer the current screen for a Game.
	 */
	public void hide() {
		// Useless if called in outside animation loop
		active = false;
	}

	/**
	 * Sets the ScreenListener for this mode
	 *
	 * The ScreenListener will respond to requests to quit.
	 */
	public void setScreenListener(ScreenListener listener) {
		this.listener = listener;
	}

}