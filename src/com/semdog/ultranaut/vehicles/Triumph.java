package com.semdog.ultranaut.vehicles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.semdog.ultranaut.meta.UltranautColors;
import com.semdog.ultranaut.states.TutorialManager;
import com.semdog.ultranaut.universe.Environment;
import com.semdog.ultranaut.universe.Universe;

/**
 * The Triumph Ship is the interplanetary one which runs off Electiricty. It is
 * the one the player may dock with in the tutorial.
 * 
 * @author Sam
 */

public class Triumph extends Ship {

	private float electricity = 100;

	private DockingPort dockingPort;

	private Ship dockedShip;
	private Sprite dockedSprite;

	private ParticleEffect flame;

	public Triumph(Environment env, World sim, Vector2 position) {
		super(env, sim, position);

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(position.x, position.y);

		PolygonShape mainShape = new PolygonShape();
		mainShape.setAsBox(6, 10);

		PolygonShape engineShape = new PolygonShape();
		engineShape.set(new float[] { -6, 10, -4, 12, 4, 12, 6, 10 });

		PolygonShape nozzleShape = new PolygonShape();
		nozzleShape.set(new float[] { -4, 12, -4, 14, 4, 14, 4, 12 });

		PolygonShape leftDockingPortSide = new PolygonShape();
		leftDockingPortSide.set(new float[] { -6, -10, -6, -14, -3.25f, -11, -3.25f, -10 });

		PolygonShape rightDockingPortSide = new PolygonShape();
		rightDockingPortSide.set(new float[] { 6, -10, 6, -14, 3.25f, -11, 3.25f, -10 });

		PolygonShape leftSolarPanel = new PolygonShape();
		leftSolarPanel.set(new float[] { -20, -2, -20, 6, -8, 6, -8, -2 });

		PolygonShape rightSolarPanel = new PolygonShape();
		rightSolarPanel.set(new float[] { 20, -2, 8, -2, 8, 6, 20, 6 });

		PolygonShape dockingPortShape = new PolygonShape();
		dockingPortShape.set(new float[] { -6, -10, 6, -10, 6, -14, -6, -14 });

		dockingPort = new DockingPort(this, 0, -12);

		FixtureDef dockingPortFixture = new FixtureDef();
		dockingPortFixture.isSensor = true;
		dockingPortFixture.shape = dockingPortShape;

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.density = 10.f;
		fixtureDef.shape = mainShape;

		body = sim.createBody(bodyDef);
		body.createFixture(fixtureDef).setUserData(this);
		body.createFixture(engineShape, 12.f);
		body.createFixture(nozzleShape, 6.f);
		body.createFixture(leftDockingPortSide, 5.0f);
		body.createFixture(rightDockingPortSide, 5.0f);
		body.createFixture(leftSolarPanel, 2.0f);
		body.createFixture(rightSolarPanel, 2.0f);

		body.createFixture(dockingPortFixture).setUserData(dockingPort);

		//	This works out the velocity needed to keep it in a circular orbit at an altitude of 1500 and sets its velocity to that.
		float cv = (float) Math.sqrt(env.getMass() * Universe.GRAVCONSTANT / (env.getAverageRadius() + 1500));
		body.setLinearVelocity(cv, 0);
		vx = cv;
		
		Gdx.app.log("Triumph", vx + "!!!");

		sprite = new Sprite(new Texture(Gdx.files.internal("assets/graphics/ships/triumph.png")));

		flame = new ParticleEffect();
		flame.load(Gdx.files.internal("assets/effects/particles/triumphflame.p"), Gdx.files.internal("assets/effects/particles"));

		for (ParticleEmitter emitter : flame.getEmitters()) {
			emitter.setContinuous(false);
		}
	}

	@Override
	public void update(float dt) {
		super.update(dt);
		sprite.setOriginCenter();
		sprite.setSize(40, 28);
		sprite.setPosition(body.getPosition().x - 20, body.getPosition().y - 14);
		sprite.setRotation(body.getAngle() * MathUtils.radiansToDegrees);

		electricity += 0.5f * dt;

		recalculateOrbit();

		float angleInDegrees = angle * MathUtils.radiansToDegrees + 90;
		flame.getEmitters().get(0).getAngle().setHigh(angleInDegrees, angleInDegrees);
		flame.getEmitters().get(1).getAngle().setHigh(angleInDegrees, angleInDegrees);

		flame.setPosition(x + 5 * MathUtils.sin(-angle), y + 5 * MathUtils.cos(-angle));
		flame.update(dt);
	}

	@Override
	public void draw(SpriteBatch batch) {
		super.draw(batch);

		flame.draw(batch);
		sprite.draw(batch);

		//	If there is a docked ship, draw that ship's sprite where it is meant to be.
		if (dockedShip != null) {
			float eh = dockingPort.getOffsetY();

			float dx = body.getPosition().x - eh * MathUtils.cos(angle - MathUtils.PI / 2);
			float dy = body.getPosition().y - eh * MathUtils.sin(angle - MathUtils.PI / 2);

			dockedSprite.setOriginCenter();

			dockedSprite.setPosition(dx - dockedSprite.getWidth() / 2, dy - dockedSprite.getHeight() / 2);
			dockedSprite.setRotation(angle * MathUtils.radiansToDegrees);

			dockedSprite.draw(batch);
		}
	}

	@Override
	public float getAngle() {
		return super.getAngle() + MathUtils.PI;
	}

	@Override
	public void pull(Vector2 position, float bodyMass, float dt) {
		super.pull(position, bodyMass, dt);
	}

	@Override
	public void updateControl(float dt) {
		recalculateOrbit();

		float angle = body.getAngle() % MathUtils.PI2 + MathUtils.PI;
		float sin = MathUtils.sin(angle) * -1;
		float cos = MathUtils.cos(angle);

		if (electricity < 0.1)
			electricity = 0;

		if (Gdx.input.isKeyPressed(Keys.W) && electricity > 0) {
			applyForce(250000 * sin, 250000 * cos, dt);
			electricity -= dt * 3f;
			for (ParticleEmitter emitter : flame.getEmitters()) {
				emitter.setContinuous(true);
			}
			recalculateOrbit();
		} else {
			for (ParticleEmitter emitter : flame.getEmitters()) {
				emitter.setContinuous(false);
			}
		}

		if (Gdx.input.isKeyPressed(Keys.A)) {
			if (usingInhousePhysics) {
				body.applyTorque(100000f, true);
				body.setAngularDamping(0);
			} else {
				av += 0.01f;
			}
		} else if (Gdx.input.isKeyPressed(Keys.D)) {
			if (usingInhousePhysics) {
				body.applyTorque(-100000f, true);
				body.setAngularDamping(0);
			} else {
				av -= 0.01f;
			}
		} else {
			if (stabilityAssist) {
				if (usingInhousePhysics) {
					body.setAngularDamping(1);
				} else {
					av += -av / 60;
				}
			}
		}

		if (Gdx.input.isKeyJustPressed(Keys.U)) {
			pilot.unboard(body.getPosition(), body.getLinearVelocity(), body.getAngle());
		}
	}

	@Override
	public Color getGizmoColor() {
		return UltranautColors.RED;
	}

	@Override
	public String getGizmoText() {
		return "Triumph Interplanetary Drive";
	}

	public float getElectricityPercent() {
		return electricity;
	}

	@Override
	public void dockedWithShip(Ship docked) {
		dockedShip = docked;
		TutorialManager.showTip(20);

		if (docked instanceof Odyssey) {
			dockedSprite = new Sprite(new Texture(Gdx.files.internal("assets/graphics/ships/odyssey.png")));
			dockedSprite.setSize(14, 12);
			dockedSprite.setOrigin(7, 12);
		}
	}

	@Override
	public void undockedWithShip(Ship undocked) {

	}

	public void releaseDocked() {
		if (dockedShip instanceof Odyssey) {
			float ux = -(dockingPort.getOffsetY() - 10) * MathUtils.sin(angle);
			float uy = (dockingPort.getOffsetY() - 10) * MathUtils.cos(angle);

			Odyssey lander = new Odyssey(environment, Universe.physicsWorld, new Vector2(x + ux, y + uy));
			lander.kick(this);

			pilot.setInShip(lander);
			dockedShip = null;
		}
	}

	@Override
	public void prepareForUpWarp() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void prepareForDownWarp() {
		// TODO Auto-generated method stub
		
	}
}
