package com.semdog.ultranaut.universe;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * An interface which is implemented by all 'things' in the universe
 * that need to be drawn and updated.
 * 
 * @author Sam
 */

public interface Entity {
	public abstract float getX();
	public abstract float getY();
	
	public abstract void draw(SpriteBatch spriteBatch);
	public abstract void update(float dt);
	public abstract void prepareForUpWarp();
	public abstract void prepareForDownWarp();
}
