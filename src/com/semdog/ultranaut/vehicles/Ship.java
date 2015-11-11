package com.semdog.ultranaut.vehicles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.semdog.ultranaut.mathematics.OrbitalHelper;
import com.semdog.ultranaut.player.Player;
import com.semdog.ultranaut.universe.CelestialBody;
import com.semdog.ultranaut.universe.Entity;
import com.semdog.ultranaut.universe.Environment;
import com.semdog.ultranaut.universe.Mass;
import com.semdog.ultranaut.universe.Planet;
import com.semdog.ultranaut.universe.Star;
import com.semdog.ultranaut.universe.Universe;

public abstract class Ship extends Mass implements Entity {
	protected float angle = 0;

	protected boolean active = true;
	protected boolean refuelling = false;
	protected boolean stabilityAssist = true;
	protected boolean warping = false;
	protected boolean onSurface = false;

	protected Player pilot;

	public float angleAroundPlanet;

	protected Sprite sprite;

	public Ship(Environment env, World sim, Vector2 position) {
		x = position.x;
		y = position.y;

		environment = env;

		Universe.masses.add(this);
		Universe.entities.add(this);

		if (env instanceof Star)
			((Star) env).addFocusable(this);
		else if (env instanceof Planet)
			((Star) (((Planet) env).getParent())).addFocusable(this);
	}

	@Override
	public void recalculateOrbit() {
		apoapsis = OrbitalHelper.computeOrbit(environment.getPosition(), new Vector2(x, y), new Vector2(vx, vy), environment.getMass())[5] - environment.getAverageRadius();
		periapsis = OrbitalHelper.computeOrbit(environment.getPosition(), new Vector2(x, y), new Vector2(vx, vy), environment.getMass())[6] - environment.getAverageRadius();
		semimajorAxis = OrbitalHelper.computeOrbit(environment.getPosition(), new Vector2(x, y), new Vector2(vx, vy), environment.getMass())[3];
		semiminorAxis = OrbitalHelper.computeOrbit(environment.getPosition(), new Vector2(x, y), new Vector2(vx, vy), environment.getMass())[4];
		orbitAngle = getTrueAnomaly() + MathUtils.atan2(y - environment.getPosition().y, x - environment.getPosition().x);
		orbitalPeriod = OrbitalHelper.computeOrbit(environment.getPosition(), new Vector2(x, y), new Vector2(vx, vy), environment.getMass())[9];
	}

	public abstract void updateControl(float dt);

	public void update(float dt) {
		x = body.getPosition().x;
		y = body.getPosition().y;

		if (usingInhousePhysics) {
			if (new Vector2(vx, vy).len() >= 119) {
				usingInhousePhysics = false;
				body.setLinearVelocity(0, 0);
			}
		} else if (new Vector2(vx, vy).len() < 119) {
			usingInhousePhysics = true;
			body.setLinearVelocity(vx, vy);
		}

		if (!usingInhousePhysics) {
			x += (vx) * 1 / 60.f;
			y += (vy) * 1 / 60.f;

			angle += av * dt;

			body.setTransform(x, y, angle);
		} else {
			vx = body.getLinearVelocity().x;
			vy = body.getLinearVelocity().y;
			angle = body.getAngle();
		}

		sprite.setOriginCenter();
		sprite.setRotation(body.getAngle() * MathUtils.radiansToDegrees);
	}

	protected void applyForce(float x, float y, float dt) {
		float ax = x / body.getMass();
		float ay = y / body.getMass();

		if (usingInhousePhysics) {
			if (new Vector2(vx + ax * 1 / 60.f, vy + ax * 1 / 60.f).len() >= 119) {
				usingInhousePhysics = false;
				vx += ax * 1 / 60.f;
				vy += ay * 1 / 60.f;
			} else {
				body.applyForceToCenter(x, y, true);
				vx = body.getLinearVelocity().x;
				vy = body.getLinearVelocity().y;
			}
		} else {
			if (new Vector2(vx + ax * 1 / 60.f, vy + ax * 1 / 60.f).len() < 119) {
				usingInhousePhysics = true;
				body.setLinearVelocity(vx, vy);
			} else {
				vx += ax * 1 / 60.f;
				vy += ay * 1 / 60.f;
			}
		}

		recalculateOrbit();
	}

	@Override
	public void pull(Vector2 position, float bodyMass, float dt) {
		float distance = Vector2.dst(body.getPosition().x, body.getPosition().y, position.x, position.y);

		float angle = MathUtils.atan2(body.getPosition().y - position.y, body.getPosition().x - position.x);
		float force = (Universe.GRAVCONSTANT * bodyMass * body.getMass()) / (distance * distance);

		float forceX = -force * MathUtils.cos(angle);
		float forceY = -force * MathUtils.sin(angle);

		float ax = forceX / body.getMass();
		float ay = forceY / body.getMass();

		if (usingInhousePhysics) {
			if (new Vector2(vx + ax * 1 / 60.f, vy + ay * 1 / 60.f).len() >= 119) {
				usingInhousePhysics = false;
				vx += ax * 1 / 60.f;
				vy += ay * 1 / 60.f;
			} else {
				body.applyForceToCenter(forceX, forceY, true);
				vx = body.getLinearVelocity().x;
				vy = body.getLinearVelocity().y;
			}
		} else {
			vx += ax * 1 / 60.f;
			vy += ay * 1 / 60.f;
		}
	}

	public void drawGizmos(ShapeRenderer gizmoRenderer) {
		gizmoRenderer.line(body.getPosition(), new Vector2(body.getPosition().x + vx, body.getPosition().y + vy));
	}

	public Vector2 getVelocity() {
		if (usingInhousePhysics) {
			return body.getLinearVelocity();
		} else {
			return new Vector2(vx, vy);
		}
	}

	public Vector2 getPosition() {
		return new Vector2(x, y);
	}

	public float getAngle() {
		return body.getAngle() % MathUtils.PI2;
	}

	public float getAltitude() {
		return Vector2.dst(x, y, environment.getPosition().x, environment.getPosition().y) - environment.getAverageRadius();
	}

	public void setDriver(Player player) {
		pilot = player;
	}

	public boolean isUsingInhousePhysics() {
		return usingInhousePhysics;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;

		Gdx.app.log("OdysseyLanderTest", "Environment set to " + environment.getName());
	}

	public Environment getEnvironment() {
		return environment;
	}

	public void environmentExited(CelestialBody reference, Environment parent) {
		if (!environment.equals(parent)) {
			Gdx.app.log("OdysseyLanderTest", "Leaving environment: " + reference.getName());

			if (body.getLinearVelocity().len() + reference.getOrbitSpeed() >= 119.9f) {
				usingInhousePhysics = false;
				vx += reference.getOrbitSpeed() * MathUtils.sin(reference.getOrbitalAngle());
				vy += reference.getOrbitSpeed() * MathUtils.cos(reference.getOrbitalAngle());
			} else {
				body.setLinearVelocity(body.getLinearVelocity().x + reference.getOrbitSpeed() * MathUtils.sin(reference.getOrbitalAngle()), body.getLinearVelocity().y + reference.getOrbitSpeed() * MathUtils.cos(reference.getOrbitalAngle()));
			}

			Gdx.app.log("OdysseyLanderTest", "" + reference.getOrbitSpeed());

			setEnvironment(parent);
		}
	}

	public void environmentEntered(Environment reference) {
		if (!environment.equals(reference)) {
			//body.setLinearVelocity(body.getLinearVelocity().x - ((Planet) reference).getVelocity().x, body.getLinearVelocity().y - ((Planet) reference).getVelocity().y);
			setEnvironment(reference);
		}
	}

	public void draw(SpriteBatch batch) {
		sprite.draw(batch);
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getApoapsis() {
		return apoapsis;
	}

	public float getPeriapsis() {
		return periapsis;
	}

	public float getTrueAnomaly() {
		return OrbitalHelper.computeOrbit(environment.getPosition(), body.getPosition(), new Vector2(vx, vy), environment.getMass())[7];
	}

	@Override
	public float getOrbitAngle() {
		if (OrbitalHelper.computeOrbit(environment.getPosition(), body.getPosition(), body.getLinearVelocity(), environment.getMass())[0] > 0) {
			return orbitAngle;
		} else {
			return orbitAngle;
		}
	}

	public void changePosition(Vector2 positionDifference) {
		x += positionDifference.x;
		y += positionDifference.y;

		body.setTransform(body.getPosition().add(positionDifference), body.getAngle());
	}

	public float getAngleAroundEnvironment() {
		if (-MathUtils.atan2(environment.getPosition().y - y, environment.getPosition().x - x) < 0)
			return MathUtils.PI2 - MathUtils.atan2(environment.getPosition().y - y, environment.getPosition().x - x);
		return -MathUtils.atan2(environment.getPosition().y - y, environment.getPosition().x - x);
	}
	
	public void setOnSurface(boolean onSurface) {
		this.onSurface = onSurface;
	}

	public boolean isOnSurface() {
		return onSurface;
	}

	public Vector2 getFocusPosition() {
		Vector2 offset = body.getPosition().sub(environment.getPosition());
		return environment.getTruePosition().add(offset);
	}

	public abstract Color getGizmoColor();
	public abstract String getGizmoText();
	
	public abstract void dockedWithShip(Ship docked);
	public abstract void undockedWithShip(Ship undocked);
}
