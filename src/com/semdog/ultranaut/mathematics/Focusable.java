package com.semdog.ultranaut.mathematics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * An interface all targetable entities on the map 
 * ought to implement.
 * 
 * @author Sam
 *
 */

public interface Focusable {
	public Vector2 getFocusPosition();
	public Color getGizmoColor();
	public String getGizmoText();
	public Vector2 getVelocity();
	public Vector2 getPosition();
}
