package com.semdog.ultranaut.vehicles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.semdog.ultranaut.meta.UltranautColors;
import com.semdog.ultranaut.player.Player;
import com.semdog.ultranaut.universe.CelestialBody;
import com.semdog.ultranaut.universe.Environment;
import com.semdog.ultranaut.universe.Star;
import com.semdog.ultranaut.universe.Universe;

/**
 * This is the Odyssey Lander class, one of the two ships currently
 * available in ULTRANAUT.
 * 
 * It is the one which uses fuel and monopropellant.
 * 
 * @author Sam
 */

public class Odyssey extends Ship {

	private ParticleEffect leftFlame, rightFlame, leftRCS, rightRCS;
	private float fuelPercent = 100.f, monoPercent = 100.f;
	
	private DockingPort dockingPort;
	
	private float lightIntensity = 0;
	
	public Odyssey(Environment env, World sim, Vector2 position) {
		super(env, sim, position);
		
		reinstantiateBody();

		sprite = new Sprite(new Texture(Gdx.files.internal("assets/graphics/ships/odyssey.png")));
		sprite.setSize(14, 12);

		/*
		 * The following code loads up the particle effects of the engines and RCS thrusters.
		 */
		leftFlame = new ParticleEffect();
		leftFlame.load(Gdx.files.internal("assets/effects/particles/odysseyflame.p"), Gdx.files.internal("assets/effects/particles"));

		rightFlame = new ParticleEffect();
		rightFlame.load(Gdx.files.internal("assets/effects/particles/odysseyflame.p"), Gdx.files.internal("assets/effects/particles"));

		leftRCS = new ParticleEffect();
		leftRCS.load(Gdx.files.internal("assets/effects/particles/rcs.p"), Gdx.files.internal("assets/effects/particles"));

		rightRCS = new ParticleEffect();
		rightRCS.load(Gdx.files.internal("assets/effects/particles/rcs.p"), Gdx.files.internal("assets/effects/particles"));
		sprite.setOriginCenter();
		
		body.setTransform(body.getPosition(), 0);
	}
	
	/**
	 * This method is called when an Odyssey becomes undocked from a Triumph
	 * and thus needs a collider to be recreated.
	 */
	private void reinstantiateBody() {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(x, y);
		bodyDef.fixedRotation = false;

		PolygonShape coreShape = new PolygonShape();
		coreShape.setAsBox(3f, 6f);

		PolygonShape leftQThrusterShape = new PolygonShape();
		leftQThrusterShape.set(new Vector2[] { new Vector2(-3f, -6f), new Vector2(-8f, -6f), new Vector2(-8f, 0f), new Vector2(-3f, 2f) });

		PolygonShape rightQThrusterShape = new PolygonShape();
		rightQThrusterShape.set(new Vector2[] { new Vector2(3f, -6f), new Vector2(8f, -6f), new Vector2(8f, -0f), new Vector2(3f, 2f) });
		
		PolygonShape dockingPortShape = new PolygonShape();
		dockingPortShape.set(new float[] {
				-3, 6,
				-3, 7,
				3, 7,
				3, 6
		});
		
		FixtureDef dockingPortFixture = new FixtureDef();
		dockingPortFixture.isSensor = true;
		dockingPortFixture.shape = dockingPortShape;

		FixtureDef coreFixtureDef = new FixtureDef();
		coreFixtureDef.shape = coreShape;
		coreFixtureDef.friction = 5f;
		coreFixtureDef.density = 10;
		
		dockingPort = new DockingPort(this, 0, -6.5f);

		body = Universe.physicsWorld.createBody(bodyDef);
		body.createFixture(coreFixtureDef).setUserData(this);
		body.createFixture(leftQThrusterShape, 20).setUserData(this);
		body.createFixture(rightQThrusterShape, 20).setUserData(this);

		body.createFixture(dockingPortFixture).setUserData(dockingPort);
		
		body.setUserData(this);

		PolygonShape playerSensorShape = new PolygonShape();
		playerSensorShape.setAsBox(14, 8);

		FixtureDef playerSensorFixture = new FixtureDef();
		playerSensorFixture.density = 0;
		playerSensorFixture.isSensor = true;
		playerSensorFixture.shape = playerSensorShape;

		body.createFixture(playerSensorFixture).setUserData(new PlayerSensor(this));

		PolygonShape leftLegShape = new PolygonShape();
		leftLegShape.set(new Vector2[] { new Vector2(-7f, -6), new Vector2(-8f, -8), new Vector2(-9f, -8), new Vector2(-8, -4), new Vector2(-7f, -4), });

		PolygonShape rightLegShape = new PolygonShape();
		rightLegShape.set(new Vector2[] { new Vector2(7f, -6), new Vector2(8f, -8), new Vector2(9f, -8), new Vector2(8, -4), new Vector2(7f, -4), });

		Fixture ll = body.createFixture(leftLegShape, 4);
		ll.setFriction(5);
		ll.setUserData(this);
		Fixture rl = body.createFixture(rightLegShape, 4);
		rl.setFriction(5);
		rl.setUserData(this);

		body.setTransform(body.getPosition(), MathUtils.PI);
		body.setAngularDamping(0);
		body.setLinearDamping(0);
	}
	
	@Override
	public void update(float dt) {
		super.update(dt);

		sprite.setPosition(body.getPosition().x - 8, body.getPosition().y - 6.5f);
		sprite.setSize(16, 13f);

		float degrees = angle * MathUtils.radiansToDegrees - 90;

		leftFlame.getEmitters().first().getAngle().setHigh(degrees - 5, degrees + 5);
		rightFlame.getEmitters().first().getAngle().setHigh(degrees - 5, degrees + 5);
		leftFlame.setPosition(x - 6 * MathUtils.cos(angle), y - 6 * MathUtils.sin(angle));
		rightFlame.setPosition(x + 6 * MathUtils.cos(-angle), y - 6 * MathUtils.sin(-angle));
		leftFlame.update(dt);
		rightFlame.update(dt);

		leftRCS.setPosition(x - 8.5f * MathUtils.cos(angle), y - 8.5f * MathUtils.sin(angle));
		rightRCS.setPosition(x + 8.5f * MathUtils.cos(-angle), y - 8.5f * MathUtils.sin(-angle));
		leftRCS.update(dt);
		rightRCS.update(dt);
	}

	/**
	 * This is called when the player presses R.
	 */
	public void attemptRefuel() {
		if (refuelling) {
			refuelling = false;
			pilot.displayNotification("#RRefuel Cancelled");
		} else {
			if (onSurface && ((CelestialBody) environment).getMineralPercent("HYDROCARBONS") > 0) {
				refuelling = true;
				pilot.displayNotification("#ORefuelling...");
			} else {
				pilot.displayNotification("#RInsufficient Hydrocarbon Contents!");
			}
		}
	}
	
	@Override
	public void draw(SpriteBatch batch) {
		leftFlame.draw(batch);
		rightFlame.draw(batch);
		leftRCS.draw(batch);
		rightRCS.draw(batch);
		super.draw(batch);
	}

	/**
	 * This handles how the keyboard input affects the object.
	 */
	public void updateControl(float dt) {
		if (Gdx.input.isKeyJustPressed(Keys.R))
			attemptRefuel();

		recalculateOrbit();

		if (!refuelling) {
			float angle = body.getAngle() % MathUtils.PI2;
			float degrees = angle * MathUtils.radiansToDegrees;
			float sin = MathUtils.sin(angle) * -1;
			float cos = MathUtils.cos(angle);

			if (fuelPercent < 0.1)
				fuelPercent = 0;

			//	If the engines are turned on and there is enough fuel, fire onwards.
			if (Gdx.input.isKeyPressed(Keys.W) && fuelPercent > 0) {
				applyForce(50000 * sin, 50000 * cos, dt);
				fuelPercent -= dt * 1.3f;
				recalculateOrbit();
				leftFlame.getEmitters().first().setContinuous(true);
				rightFlame.getEmitters().first().setContinuous(true);
				
				if(lightIntensity < 0.5)
					lightIntensity += 5.5f * dt;
			} else {
				leftFlame.getEmitters().first().setContinuous(false);
				rightFlame.getEmitters().first().setContinuous(false);
				
				if(lightIntensity > 0) 
					lightIntensity -= 5.5f * dt;
			}

			//	Here, A is pressed to rotate the ship counter-clockwise.
			if (Gdx.input.isKeyPressed(Keys.A)) {
				if (usingInhousePhysics) {
					body.applyTorque(100000f, true);
					body.setAngularDamping(0);
				} else {
					av += 0.01f;
				}
			} 
			//	Similarly, when D is pressed the ship will rotate clockwise.
			else if (Gdx.input.isKeyPressed(Keys.D)) {
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

			//	If the RCS system has enough fuel, allow the craft to be controlled by the little jets of gas.
			if (monoPercent > 0.1) {
				if (Gdx.input.isKeyPressed(Keys.UP)) {
					applyForce(10000 * sin, 10000 * cos, dt);
					leftRCS.getEmitters().first().setContinuous(true);
					leftRCS.getEmitters().first().getAngle().setHigh(degrees - 90 - 5, degrees - 90 + 5);
					rightRCS.getEmitters().first().setContinuous(true);
					rightRCS.getEmitters().first().getAngle().setHigh(degrees - 90 - 5, degrees - 90 + 5);

					monoPercent -= dt * 0.1f;
				} else if (Gdx.input.isKeyPressed(Keys.DOWN)) {
					applyForce(-10000 * sin, -10000 * cos, dt);
					leftRCS.getEmitters().first().setContinuous(true);
					leftRCS.getEmitters().first().getAngle().setHigh(degrees + 90 - 5, degrees + 90 + 5);
					rightRCS.getEmitters().first().setContinuous(true);
					rightRCS.getEmitters().first().getAngle().setHigh(degrees + 90 - 5, degrees + 90 + 5);

					monoPercent -= dt * 0.1f;
				} else if (Gdx.input.isKeyPressed(Keys.LEFT)) {
					applyForce(-10000 * MathUtils.cos(angle), -10000 * MathUtils.sin(angle), dt);
					rightRCS.getEmitters().first().setContinuous(true);
					rightRCS.getEmitters().first().getAngle().setHigh(degrees - 5, degrees + 5);

					monoPercent -= dt * 0.05f;
				} else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
					applyForce(10000 * MathUtils.cos(angle), 10000 * MathUtils.sin(angle), dt);
					leftRCS.getEmitters().first().setContinuous(true);
					leftRCS.getEmitters().first().getAngle().setHigh(degrees + 180 - 5, degrees + 180 + 5);

					monoPercent -= dt * 0.05f;
				} else {
					leftRCS.getEmitters().first().setContinuous(false);
					rightRCS.getEmitters().first().setContinuous(false);
				}
			} else {
				monoPercent = 0;
				leftRCS.getEmitters().first().setContinuous(false);
				rightRCS.getEmitters().first().setContinuous(false);
			}

			//	If the player presses U, he/she is unboarded from the ship and given the correct velocity and position for unboarding.
			if (Gdx.input.isKeyJustPressed(Keys.U)) {
				pilot.unboard(body.getPosition(), body.getLinearVelocity(), body.getAngle());
			}
		} else {
			//	This happens when the ship is on the surface of a hydrocarbon-rich planet and is refueling.
			float hydrocarbonContent = ((CelestialBody) environment).getMineralPercent("HYDROCARBONS");
			float intake = 1 / 100.f * hydrocarbonContent / (6.f / 10.f);

			fuelPercent += intake * dt;
			pilot.displayNotification("#ORefuelling... " + (int) fuelPercent + "%");

			if (fuelPercent >= 100) {
				fuelPercent = 100;
				refuelling = false;
				pilot.displayNotification("#BFinished Refuelling!");
			}
		}
	}

	public void setOnSurface(boolean onSurface) {
		this.onSurface = onSurface;
	}

	public boolean isOnSurface() {
		return onSurface;
	}

	public float getFuelPercent() {
		return fuelPercent;
	}

	public float getMonoPercent() {
		return monoPercent;
	}

	@Override
	public Color getGizmoColor() {
		return UltranautColors.GREEN;
	}

	@Override
	public String getGizmoText() {
		return "Odyssey Lander";
	}
	
	@Override
	public void setDriver(Player player) {
		super.setDriver(player);
	}

	/**
	 * This method is called when the docking sequence is complete.
	 */
	@Override
	public void dockedWithShip(Ship docked) {
		pilot.setInShip(docked);
		Universe.masses.removeValue(this, true);
		Universe.entities.removeValue(this, true);
		
		if(environment instanceof Star)
			((Star)environment).removeFocusable(this);
		else
			((Star)environment.getParent()).removeFocusable(this);
		
		Universe.pushDestroyBodyRequests(body);
	}

	@Override
	public void undockedWithShip(Ship undocked) {
		reinstantiateBody();
	}

	/**
	 * This method provides the newly-undocked Odyssey with a kick hard enough
	 * so it moves away from the port so doesn't by mistake dock again.
	 * @param triumph
	 */
	public void kick(Triumph triumph) {
		vx = triumph.getVelocity().x;
		vy = triumph.getVelocity().y;
		
		if(usingInhousePhysics)
			body.setLinearVelocity(triumph.getVelocity());
		
		float angle = MathUtils.atan2(triumph.getPosition().y - y, triumph.getPosition().x - x);
		applyForce(5000 * MathUtils.cos(angle), 5000 * MathUtils.sin(angle), Gdx.graphics.getDeltaTime());
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
