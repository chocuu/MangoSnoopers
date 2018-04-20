package edu.cornell.gdiac.mangosnoops.hudentity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.mangosnoops.GameCanvas;
import edu.cornell.gdiac.mangosnoops.Image;

public class VroomStick extends Image {

    /**
     * engaged indicates that the stick has been fully pulled, which
     * triggers the increase of speed for a time that is specified in Road.
     *
     * "speed" is currently implemented like this: make the road "conveyor belt"
     * move more quickly. We could move some of this logic to Car I think.
     */
    private boolean engaged;

    private Rectangle hitbox;

    private double length;
    private float ang = 0;
    private float ENGAGE_ANGLE = -35;

    public VroomStick(float x, float y, float relSca, float cb, Texture tex ) {
        super(x,y,relSca,cb,tex);
        engaged = false;

        hitbox = new Rectangle(position.x*SCREEN_DIMENSIONS.x + 0.736f*texture.getWidth()*SCREEN_DIMENSIONS.y*relativeScale,
                                position.y*SCREEN_DIMENSIONS.y + 0.63f*texture.getHeight()*SCREEN_DIMENSIONS.y*relativeScale,
                                0.28f*texture.getWidth()*SCREEN_DIMENSIONS.y*relativeScale,
                                0.35f*texture.getHeight()*SCREEN_DIMENSIONS.y*relativeScale);
    }

    @Override
    public void draw(GameCanvas canvas) {
        if(texture == null) {
            return;
        }

        float drawY = position.y * canvas.getHeight() + currentShakeAmount;

        canvas.draw(texture, Color.WHITE, 0, 0, position.x*canvas.getWidth(), drawY,
                        ang, relativeScale*canvas.getHeight(), relativeScale*canvas.getHeight());
    }

    public void update(Vector2 in, float dy) {

        if(in != null) {
            in.y = SCREEN_DIMENSIONS.y-in.y;
        }
        if (in != null && hitbox.contains(in)) {
            ang -= dy;
            hitbox.setPosition(hitbox.getX(),(hitbox.getY()- dy));

            if (ang < ENGAGE_ANGLE) {
                ang = ENGAGE_ANGLE;
                engaged = true;
            }
        } else {
            ang += 0.2f * -ang;
            hitbox.setPosition(hitbox.getX(),hitbox.getY() + 0.02f*hitbox.getY());
            engaged = false;
        }

        if(ang >= 0){
            ang = 0;
        }

        if(hitbox.getY()+hitbox.getHeight() > position.y*SCREEN_DIMENSIONS.y + texture.getHeight()*SCREEN_DIMENSIONS.y*relativeScale){
            hitbox.setPosition(position.x*SCREEN_DIMENSIONS.x + 0.736f*texture.getWidth()*SCREEN_DIMENSIONS.y*relativeScale,
                                 position.y*SCREEN_DIMENSIONS.y + 0.652f*texture.getHeight()*SCREEN_DIMENSIONS.y*relativeScale);
        }
    }

    public boolean isEngaged() { return engaged; }

}
