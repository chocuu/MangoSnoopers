/*
 * CollisionController.java
 *
 * This controller implements basic collision detection as described in
 * the instructions.  All objects in this game are treated as circles,
 * and a collision happens when circles intersect.
 *
 * This controller is EXTREMELY ineffecient.  To improve its performance,
 * you will need to use collision cells, as described in the instructions.
 * You should not need to modify any method other than the constructor
 * and processCollisions.  However, you will need to add your own methods.
 *
 * This is the only file that you need to modify as the first part of
 * the lab. 
 *
 * Author: Walker M. White
 * Based on original Optimization Lab by Don Holden, 2007
 * LibGDX version, 2/2/2015
 */
package edu.cornell.gdiac.mangosnoops;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import edu.cornell.gdiac.mangosnoops.hudentity.Child;
import edu.cornell.gdiac.mangosnoops.hudentity.Wheel;
import edu.cornell.gdiac.mangosnoops.roadentity.*;

import java.io.File;

/**
 * Controller implementing simple game physics.
 */
public class CollisionController {
	/** TODO: Maybe delete? Window height */
	private static final float WINDOW_HEIGHT = 600;
	/** TODO: Maybe delete? */
	/** A factor to determine the area of the wheel a gnome is allowed to hit */
	private static final float WHEEL_SAFE_AREA = 100.0f;
	/** A factor to determine the gnome is close enough for Ned to shoot. */
	private static final float GNOME_INRANGE = -9.0f;
	/** A factor to determine the gnome and car have collided. */
	private static final float HIT_RANGE = 0.15f;

	private static final float CAR_YRANGE_END = -10f;
	private static final float CAR_YRANGE_START  = -10.5f;

	// These cannot be modified after the controller is constructed.
	// If these change, make a new constructor.
	/** Width of the collision geometry */
	private float width;
	/** Height of the collision geometry */
	private float height;

	/**
	 * Returns width of the game window (necessary to detect out of bounds)
	 *
	 * @return width of the game window
	 */
	public float getWidth() {
		return width;
	}
	
	/**
	 * Returns height of the game window (necessary to detect out of bounds)
	 *
	 * @return height of the game window
	 */
	public float getHeight() {
		return height;
	}

	/**
	 * Creates a CollisionController for the given screen dimensions.
	 *
	 * @param width   Width of the screen 
	 * @param height  Height of the screen 
	 */
	public CollisionController(float width, float height) {
		this.width = width;
		this.height = height;
	}
	
	/**
	 * This is the main (incredibly unoptimized) collision detetection method.
	 *
	 * FIXME: remove camera, canvas params, oh god, what a mess
	 * @param enemies List of live gnomes to check
	 * @param yonda  Player's car
	 */
	public void processCollisions(Array<Enemy> enemies, Car yonda, GameplayController controller) {
		processBounds(yonda);
		for (Enemy e : enemies) {
			handleCollision(yonda, e, controller);
		}
	}

	/**
	 * Check if a GameObject is out of bounds and take action.
	 *
	 * Obviously an object off-screen is out of bounds.
	 *
	 * @param c      Player's car
	 */
	private void processBounds(Car c) {
		//if(c.getX() < 0) c.setX(0);
		//if(c.getX() > width) c.setY(width);
	}

	/**
	 * Process the actions that need to take place when an enemy
	 * collides with the car, namely:
	 *   - Reduce car health
	 *   - Shake the HUD
	 *   - Update childrens' moods
     *   - Destroy enemy
	 *   - Destroy car if health is gone
	 * @param c
	 * @param g
	 */
	private void processCarHitActions(Car c, Enemy e, GameplayController g) {
		c.damage();
		c.shakeCar();
		g.shakeHUD();
		c.getNed().setMood(Child.Mood.SAD);
		c.getNosh().setMood(Child.Mood.SAD);
		if (c.getHealth() == 0) c.setDestroyed(true);
		e.setDestroyed(true);
	}

	/**
	 * Collide a gnome with a car.
	 * FIXME: remove canvas param
	 */
	private void handleCollision(Car c, Enemy e, GameplayController controller) {
//		if(c.nedAwake() && (c.getNed().getCurrentMood() == Child.Mood.HAPPY
//							|| c.getNed().getCurrentMood() == Child.Mood.NEUTRAL)) {
//			if(g.getY() < GNOME_INRANGE) {
//				g.setDestroyed(true);
//			}
//		}
//
//		else {

		boolean isFlamingo = e.getType() == RoadObject.ObjectType.FLAMINGO;
		boolean isFlyingFlamingo = isFlamingo && ((Flamingo) e).isFlyingAway();

		boolean eInYRange = e.getY() > CAR_YRANGE_START && e.getY() < CAR_YRANGE_END;
        boolean eInXRange = Math.abs(e.getX() - c.position.x) < HIT_RANGE;

        if (eInXRange && eInYRange) {
            if (!isFlyingFlamingo) processCarHitActions(c, e, controller);
        }

	}
}