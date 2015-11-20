package com.semdog.ultranaut.universe;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.semdog.ultranaut.mathematics.Focusable;
import com.semdog.ultranaut.mathematics.OrbitalHelper;

/**
 * This abstract class is what all moveable objects in the game
 * extend. It allows a versatile interface for the interaction
 * between these masses and planets.
 * 
 * @author Sam
 */

public abstract class Mass implements Focusable {
	protected Body body;
	protected float mass;

	protected boolean usingInhousePhysics;
	protected float x, y, vx, vy, av;

	protected float apoapsis, periapsis;
	protected float semimajorAxis, semiminorAxis;
	protected float orbitAngle;
	protected float orbitalPeriod;
	protected float orbitalEccentricity;
	protected Environment environment;

	/**
	 * This method allows masses to be pulled gravitationally towards
	 * planets and stars. The pull is somewhat realistic due to it
	 * being based off Newton's Law of Universal Gravitation:
	 * 
	 * F = G*M*m/r^2
	 * 
	 * where
	 * f is force
	 * G is the Gravitational Constant*
	 * M is the mass of the planet/star
	 * m is the object mass
	 * r is the distance between their centers.
	 * 
	 * *In the case of the physical world of ULTRANAUT, the Gravitaional
	 * Constant is much higher than a realistic one due to the scale of the
	 * universe being so much smaller. This allows planets with small sizes
	 * by our standards (like a 1000m radius) to have "normal" gravitational 
	 * pulls (~10m/s^2)
	 * 
	 * @param position the position towards which the body is pulled
	 * @param bodyMass the mass of the puller
	 * @param dt delta-time
	 */
	public void pull(Vector2 position, float bodyMass, float dt) {
		float distance = Vector2.dst(body.getPosition().x, body.getPosition().y, position.x, position.y);

		float angle = MathUtils.atan2(body.getPosition().y - position.y, body.getPosition().x - position.x);
		float force = (Universe.GRAVCONSTANT * body.getMass() * bodyMass) / (distance * distance);

		float forceX = -force * MathUtils.cos(angle);
		float forceY = -force * MathUtils.sin(angle);

		body.applyForceToCenter(forceX, forceY, true);
	}

	/**
	 * This calculates the most important orbital elements.
	 * These are not calculated every frame as this may consume processing power
	 * and may not be needed to be refreshed.
	 * 
	 * In the game's current state, however, the only three masses (the ODYSSEY,
	 * the TRIUMPH and the player) all need their calculations done all the time.
	 * In subsequent versions with many masses, this method will be utilized properly.
	 */
	public void recalculateOrbit() {
		orbitalEccentricity = OrbitalHelper.computeOrbit(environment.getPosition(), body.getPosition(), body.getLinearVelocity(), environment.getMass())[1];
		apoapsis = OrbitalHelper.computeOrbit(environment.getPosition(), body.getPosition(), body.getLinearVelocity(), environment.getMass())[5] - environment.getAverageRadius();
		periapsis = OrbitalHelper.computeOrbit(environment.getPosition(), body.getPosition(), body.getLinearVelocity(), environment.getMass())[6] - environment.getAverageRadius();
		semimajorAxis = OrbitalHelper.computeOrbit(environment.getPosition(), body.getPosition(), body.getLinearVelocity(), environment.getMass())[3];
		semiminorAxis = OrbitalHelper.computeOrbit(environment.getPosition(), body.getPosition(), body.getLinearVelocity(), environment.getMass())[4];
		orbitAngle = getTrueAnomaly() + MathUtils.atan2(body.getPosition().y - environment.getPosition().y, body.getPosition().x - environment.getPosition().x);
		orbitalPeriod = OrbitalHelper.computeOrbit(environment.getPosition(), body.getPosition(), body.getLinearVelocity(), environment.getMass())[9];
	}

	public float getPeriapsis() {
		return periapsis;
	}

	public float getApoapsis() {
		return apoapsis;
	}

	public float getSemimajorAxis() {
		return semimajorAxis;
	}

	public float getSemiminorAxis() {
		return semiminorAxis;
	}
	
	public float getOrbitalPeriod() {
		return orbitalPeriod;
	}
	
	public float getOrbitalEccentricity() {
		return orbitalEccentricity;
	}

	public float getTrueAnomaly() {
		return OrbitalHelper.computeOrbit(environment.getPosition(), body.getPosition(), body.getLinearVelocity(), environment.getMass())[7];
	}

	public float getOrbitAngle() {
		return getTrueAnomaly() + MathUtils.PI - MathUtils.atan2(body.getPosition().y - environment.getPosition().y, body.getPosition().x - environment.getPosition().x);
	}

	/**
	 * Sometimes bodies do not use the ingame physics engine (due
	 * to velocities exceeding 120m/s in velocity magnitude) and thus 
	 * require an inbuilt system which deals only in movement and not
	 * collision. 
	 */
	public boolean isUsingInhousePhysics() {
		return usingInhousePhysics;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
}
