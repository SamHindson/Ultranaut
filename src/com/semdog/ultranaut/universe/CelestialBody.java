package com.semdog.ultranaut.universe;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import com.semdog.ultranaut.mathematics.Focusable;

/**
 * This abstract class is the superclass
 * of any natural satellites of Stars / Black holes.
 * 
 * @author Sam
 */

public abstract class CelestialBody implements Environment, Focusable {

	protected Star parent;

	protected float x, y, px, py, dx, dy, tx, ty;
	protected float orbitAngularVelocity, orbitSpeed, rotationalSpeed;
	protected float mass;
	protected float playerEntranceTime, playerEntranceAngle;
	
	protected boolean hasPlayer;

	protected float orbitCircumference;
	protected float orbitRadius;
	protected float influenceSphereRadius;
	protected float averageRadius;

	protected float orbitalAngle, trueOrbitalAngle, axialAngle;

	protected Color primaryColor;
	protected String bodyID, givenID;

	protected Array<Mineral> mineralContent;
	protected HashMap<String, Float> mineralPercents;
	
	protected Body body;

	public CelestialBody(float angle, float distance, Star parent) {
		this.parent = parent;

		x = parent.getX() + distance * MathUtils.cos(angle);
		y = parent.getY() + distance * MathUtils.sin(angle);

		this.orbitRadius = distance;
		this.orbitalAngle = angle;

		px = parent.getX();
		py = parent.getY();
		
		//	Works out a realistic orbit speed using sqrt(mu/r)
		orbitSpeed = (float) Math.sqrt((Universe.GRAVCONSTANT * parent.getMass()) / distance);
		orbitCircumference = MathUtils.PI2 * orbitRadius;
		orbitAngularVelocity = orbitSpeed / distance;
		
		mineralContent = new Array<Mineral>();
		mineralPercents = new HashMap<String, Float>();

		generateContents();
	}

	/**
	 * This method generates the mineral contents for the Celestial Body, and
	 * thereafter gives the body its color.
	 * 
	 * - Uranium 			(for nuclear reactions) 
	 * - Plutonium 			(for nuclear reactions) 
	 * - Water 				(usually in the form of ice) 
	 * - Iron 				(for general construction) 
	 * - Aluminium 			(ditto) 
	 * - Carbon 			(useful for lots of things) 
	 * - Hydrocarbons 		(for the Consciousness Rerouting System and subsequent
	 *						organic synthesis procedures)
	 * - Gold				(electronics)
	 * - Sillicon			(electronics)
	 * 
	 * TODO Add more of these compound things! They are fun to code :)
	 */
	private void generateContents() {
		Color color = Color.BLACK;
		
		int mineralCount = 4;
		int sampleSize = 100;
		
		Array<Integer> mineralIds = new Array<Integer>();
		
		int[] slicePositions = new int[mineralCount + 1];
		slicePositions[0] = 0;
		slicePositions[mineralCount] = 100;
		
		for(int g = 1; g < slicePositions.length - 1; g++) {
			slicePositions[g] = new Random().nextInt(sampleSize);
		}
		
		Arrays.sort(slicePositions);
		
		for(int e = 0; e < mineralCount; e++) {
			int mineralId = 0;
			
			do {
				mineralId = MathUtils.random(Mineral.ELEMENTS.length - 1);
			} while(mineralIds.contains(mineralId, true));
			
			mineralIds.add(mineralId);
			
			mineralContent.add(new Mineral(Mineral.ELEMENTS[mineralId], slicePositions[e + 1] - slicePositions[e]));
			mineralPercents.put(mineralContent.get(e).getType().toString(), mineralContent.get(e).getPercent() * 1.f);
			
			Gdx.app.log("CelestialBody", mineralContent.get(e).getType() + " - " + mineralContent.get(e).getPercent() + "%");
			color.add(Mineral.ELEMENTS[mineralId].getColor().mul((float)(mineralContent.get(e).getPercent() / 100.0f)));
		}
		
		mineralContent.sort(new Comparator<Mineral>() {
			public int compare(Mineral o1, Mineral o2) {
				return (o1.getPercent() > o2.getPercent()) ? 1 : -1;
			}
		});
		
		primaryColor = new Color(color);
	}

	public abstract void update(float dt);

	public abstract void draw(PolygonSpriteBatch batch);

	/**
	 * The position of the body on the FlightComputer
	 * is its true position, not its current.
	 */
	public Vector2 getFocusPosition() {
		return new Vector2(tx, ty);
	}
	
	public Vector2 getPosition() {
		return new Vector2(x, y);
	}
	
	public float getOrbitSpeed() {
		return orbitSpeed;
	}
	
	public float getOrbitalAngle() {
		return orbitalAngle;
	}
	
	public float getInfluenceSphereRadius() {
		return getOrbitRadius() * (float)Math.pow(mass / parent.getMass(), 1./5.);
	}

	public abstract float getMass();
	
	public void setHasPlayer(boolean hasPlayer) {
		this.hasPlayer = hasPlayer;
	}
	
	public Mineral getMineral(int id) {
		return mineralContent.get(id);
	}

	public Color getPrimaryColor() {
		return primaryColor;
	}

	public float getAverageRadius() {
		return averageRadius;
	}

	/**
	 * When a player enters the body's sphere of influence,
	 * the planet has to stop orbiting its star because Box2D does not work
	 * well with dynamic reference frames.
	 * 
	 * It logs the time the player has entered it and
	 * this is used when the player leaves.
	 */
	public void handlePlayerEntry() {
		Universe.playerEntered(this);
		
		playerEntranceTime = Universe.getAge();
		playerEntranceAngle = orbitalAngle;
		hasPlayer = true;
		
		dx = dy = 0;
	}

	/**
	 * While the player is inside the body's SOI (sphere of influence), the
	 * body ought to have moved a great deal along in its orbit.
	 * 
	 * Thus, when the player leaves, the system has to create the illusion
	 * that the body has been moving this whole time (i.e. if the player
	 * stays on the planet for half a "year", the planet must be on the opposite
	 * sideof the sun). This method works out how far it has moved and teleports the
	 * planet and the player by the distance it has 'travelled' in the 
	 * intervening time.
	 */
	public void handlePlayerExit() {
		Vector2 newPosition = new Vector2();

		float now = Universe.getAge();
		
		//	Works out how long the player has been on it
		float timeDifference = (now - playerEntranceTime);
	
		//	Works out how many radians the body ought to have travelled.
		float radians = orbitAngularVelocity * timeDifference;
		float angleNow = playerEntranceAngle + radians;
		
		//	Works out the position when the player had entered
		Vector2 entryPosition = new Vector2(orbitRadius * MathUtils.cos(playerEntranceAngle), orbitRadius * MathUtils.sin(playerEntranceAngle));

		//	Works out the position now
		float nx = MathUtils.cos(angleNow) * orbitRadius;
		float ny = MathUtils.sin(angleNow) * orbitRadius;
		newPosition.set(nx, ny);
		
		//	Works out the position difference and adds this to the player and planet's position
		Vector2 positionDifference = newPosition.sub(entryPosition);
		Universe.playerExited(this, positionDifference);
		
		body.setTransform(positionDifference.add(body.getPosition()), 0);
	}

	public float getOrbitRadius() {
		return orbitRadius;
	}
	
	//	Returns the true x position of the planet, taking into account the orbit
	public float getTx() {
		return tx;
	}

	//	Returns the true y position of the planet, taking into account the orbit
	public float getTy() {
		return ty;
	}
	
	public Vector2 getTruePosition() {
		return new Vector2(tx, ty);
	}
	
	public float getMineralPercent(String id) {
		return mineralPercents.getOrDefault(id, 0.f);
	}

	protected abstract void reinstantiateBody();
	protected abstract void destroyBody();
}
