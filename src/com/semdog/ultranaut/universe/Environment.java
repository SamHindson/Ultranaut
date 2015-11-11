package com.semdog.ultranaut.universe;

import com.badlogic.gdx.math.Vector2;

/**
 * An interface which all possible player environments implement.
 * It holds basic information like the environment's mass,
 * position and average radius (in the case of planets and stars)
 * 
 * @author Sam
 */

public interface Environment {
	public String getName();
	public Vector2 getPosition();
	public Environment getParent();
	
	public float getMass();
	public float getAverageRadius();
	public Vector2 getTruePosition();
}
