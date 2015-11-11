package com.semdog.ultranaut.universe;

import com.badlogic.gdx.math.Vector2;

/**
 * A class created for the purpose of being Stars' parents.
 * It does basically nothing else.
 * @author Sam
 */

public class DeepSpace implements Environment {

	public String getName() {
		return "Deep Space";
	}
	
	public Vector2 getPosition() {
		return new Vector2(0, 0);
	}

	public Environment getParent() {
		return null;
	}

	public float getMass() {
		return 0;
	}

	public float getAverageRadius() {
		return 10000;
	}

	public Vector2 getTruePosition() {
		return new Vector2(0, 0);
	}
}
