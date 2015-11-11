package com.semdog.ultranaut.universe;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import com.semdog.ultranaut.mathematics.Focusable;
import com.semdog.ultranaut.vehicles.Ship;

/**
 * The Star is currently the only object around which celestial bodies can orbit.
 * 
 * It is simply just a big mass with a certain radius; you cannot fly into them
 * yet.
 * 
 * @author Sam
 */

public class Star implements Environment, Focusable {
	private CelestialBody[] bodies;

	private float x, y;
	private float radius;
	private float mass;
	
	private String id;

	private PolygonSprite sprite;

	private Color color;
	
	//	This array stores references to all objects in its gravitational field which may be focused upon in the FlightComputer.
	private Array<Focusable> focusables;

	public Star(float x, float y, Universe universe) {
		this.x = x;
		this.y = y;

		radius = MathUtils.random(1000, 2500);

		mass = (MathUtils.PI * radius * radius * radius) * 10;

		bodies = new CelestialBody[10];

		color = new Color(1.0f, 1.0f, 0.5f, 1.0f);

		float lastInfluenceSphere = radius * 20;

		for (int g = 0; g < bodies.length; g++) {
			bodies[g] = new Planet(g, MathUtils.random(MathUtils.PI2), lastInfluenceSphere + 20000 * MathUtils.random(0.9f, 2),
					this);
			lastInfluenceSphere = bodies[g].orbitRadius + bodies[g].influenceSphereRadius * 10f;
		}

		float[] v = new float[100];

		// Builds a float array defining the polygon's edges
		for (int t = 0; t < 100; t += 2) {
			float p = (t * 1.0f) / 50.0f;

			float nx = radius + MathUtils.random(-1, 1);
			float ny = radius + MathUtils.random(-1, 1);

			v[t] = nx * MathUtils.cos(p * MathUtils.PI);
			v[t + 1] = ny * MathUtils.sin(p * MathUtils.PI);
		}

		Pixmap mape = new Pixmap((int) (500 * 2), (int) (500 * 2), Format.RGBA8888);

		for (int xx = 0; xx < mape.getWidth(); xx++) {
			for (int yy = 0; yy < mape.getHeight(); yy++) {
				mape.drawPixel(xx, yy, Color.rgba8888(color));
			}
		}

		Texture texture = new Texture(mape);
		PolygonRegion polygonRegion = new PolygonRegion(new TextureRegion(texture), v,
				new EarClippingTriangulator().computeTriangles(v).items);
		sprite = new PolygonSprite(polygonRegion);
		sprite.setOrigin(x, y);
		sprite.setPosition(x, y);
		
		focusables = new Array<Focusable>();
		focusables.addAll(bodies);
				
		//	This generates a random name for the star using letters and numbers
		for(int e = 0; e < MathUtils.random(3, 5); e++) {
			id += (char)MathUtils.random(65, 90);
		}
		
		id += "-";
		id += MathUtils.random(0, 9);
		id += MathUtils.random(0, 9);
		
	}
	
	public void addFocusable(Focusable focusable) {
		focusables.add(focusable);
	}
	
	public void removeFocusable(Focusable focusable) {
		focusables.removeValue(focusable, true);
	}

	public Array<Focusable> getFocusables() {
		return focusables;
	}

	/**
	 * update updates all the Celestial Bodies orbiting the star
	 * and pulls any masses that are within the star's field but 
	 * not on any of the orbiting planets.
	 * 
	 * @param dt
	 */
	public void update(float dt) {
		for (CelestialBody c : bodies) {
			c.update(dt);
		}

		Array<Body> bodies = new Array<Body>();
		Universe.physicsWorld.getBodies(bodies);

		for (Body b : bodies) {
			if (b.getUserData() instanceof Mass) {
				if(b.getUserData() instanceof Ship) {
					if(!(((Ship)b.getUserData()).getEnvironment() instanceof CelestialBody)) {
						((Mass) b.getUserData()).pull(new Vector2(x, y), mass, dt);						
					}
				} else {
					((Mass) b.getUserData()).pull(new Vector2(x, y), mass, dt);						
				}
			}
		}
	}

	//	Draws the sprite and body sprites
	public void draw(PolygonSpriteBatch polygonSpriteBatch) {
		for (CelestialBody c : bodies) {
			c.draw(polygonSpriteBatch);
		}

		sprite.draw(polygonSpriteBatch);
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public Color getColor() {
		return color;
	}

	public CelestialBody getBody(int i) {
		return bodies[i];
	}
	
	public String getID() {
		return id;
	}
	
	public float getMass() {
		return mass;
	}

	public String getName() {
		return getID();
	}

	public Vector2 getFocusPosition() {
		return getPosition();
	}
	
	public Vector2 getPosition() {
		return new Vector2(x, y);
	}

	public Environment getParent() {
		return new DeepSpace();
	}

	public int getCelestialBodyCount() {
		return bodies.length;
	}

	public float getAverageRadius() {
		return radius;
	}

	public Color getGizmoColor() {
		return color;
	}

	public String getGizmoText() {
		return getID();
	}

	public Vector2 getTruePosition() {
		return getFocusPosition();
	}
	
	public Vector2 getVelocity() {
		return Vector2.Zero;
	}
}
