package com.semdog.ultranaut.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.semdog.ultranaut.UltranautGame;
import com.semdog.ultranaut.mathematics.FlightComputer;
import com.semdog.ultranaut.mathematics.Focusable;
import com.semdog.ultranaut.mathematics.OrbitalHelper;
import com.semdog.ultranaut.meta.UltranautColors;
import com.semdog.ultranaut.states.TutorialManager;
import com.semdog.ultranaut.universe.CelestialBody;
import com.semdog.ultranaut.universe.Environment;
import com.semdog.ultranaut.universe.Mass;
import com.semdog.ultranaut.universe.Planet;
import com.semdog.ultranaut.universe.Star;
import com.semdog.ultranaut.universe.Universe;
import com.semdog.ultranaut.vehicles.Odyssey;
import com.semdog.ultranaut.vehicles.Ship;
import com.semdog.ultranaut.vehicles.Triumph;

/**
 * This is the player class.
 * Through this, the user controls the player or any ship they are currently controlling,
 * 
 * There are bugs with the movement that are being worked on vigorously (an example is a 
 * bug when the player gets basically glued to the floor).
 * 
 * There is also no player sprite, and this is why the Box2DDebugRenderer is still active;
 * the user has to see where the player's collider is.
 * 
 * @author Sam
 */

public class Player extends Mass {
	private float angle;

	private Fixture mainFixture;

	private BitmapFont largeFont, smallFont;

	private boolean notifying = false;
	private float notifyTime;
	private String notifyText;

	private Toolbox actionToolbox, navballToolbox;
	private ToolboxContent actionToolboxContent, navball;

	private boolean onSurface = false;
	private boolean canBoard = false;
	private Ship potentialBoardShip;
	private Ship ship;

	private Sprite testSprite;
	private Sprite targetDirection;

	private ShapeRenderer gizmoRenderer;

	private boolean controllingShip = false;

	private FlightComputer flightComputer;
	private Focusable target;

	private float undockHoldTime = 2.0f;

	public Player(Environment environment, World physicsWorld) {
		this.environment = environment;

		x = environment.getPosition().x;
		y = environment.getPosition().y + environment.getAverageRadius() + 10;

		reinstantiateBody(x, y, 0);

		testSprite = new Sprite(new Texture(Gdx.files.internal("assets/graphics/player/test.png")));
		testSprite.setSize(1.2f, 2f);
		testSprite.setOriginCenter();

		Gdx.app.log("Player", "Environment is " + environment.getName());

		largeFont = new BitmapFont(Gdx.files.internal("assets/fonts/mohave64_BA.fnt"));
		smallFont = new BitmapFont(Gdx.files.internal("assets/fonts/mohave20_BA.fnt"));

		actionToolbox = new Toolbox(this, 0);
		actionToolboxContent = new ShipToolbox(actionToolbox);
		actionToolbox.setContent(actionToolboxContent);

		navballToolbox = new Toolbox(this, 1);
		navball = new Navball(navballToolbox);
		navballToolbox.setContent(navball);

		gizmoRenderer = new ShapeRenderer();
		gizmoRenderer.setAutoShapeType(true);

		targetDirection = new Sprite(new Texture(Gdx.files.internal("assets/graphics/gizmos/targetdirection.png")));
		targetDirection.setSize(100, 100);
		targetDirection.setOrigin(50, 0);
	}

	public void update(float dt) {
		recalculateOrbit();
		
		if (controllingShip) {
			x = ship.getX();
			y = ship.getY();
			vx = ship.getVelocity().x;
			vx = ship.getVelocity().y;
		} else {
			x = body.getPosition().x;
			y = body.getPosition().y;
		}

		vx = body.getLinearVelocity().x;
		vy = body.getLinearVelocity().x;

		if (Gdx.input.isKeyJustPressed(Keys.T) && target != null) {
			displayNotification("#RUntargeted " + target.getGizmoText());
			target = null;
			flightComputer.removeTarget();
		}

		if (controllingShip)
			onSurface = ship.isOnSurface();

		if (controllingShip) {
			ship.updateControl(dt);

			if (ship instanceof Triumph) {
				if (Gdx.input.isKeyPressed(Keys.F1)) {
					undockHoldTime -= dt;
					displayNotification("#OHold F1 to Confirm Undock...");

					if (undockHoldTime <= 0) {
						((Triumph) ship).releaseDocked();
					}
				} else {
					if (undockHoldTime < 2)
						displayNotification("#RUndock Cancelled.");
					undockHoldTime = 2;
				}
			}
		} else {
			if (environment instanceof CelestialBody) {
				angle = MathUtils.atan2(y - ((CelestialBody) environment).getPosition().y, x - ((CelestialBody) environment).getPosition().x) - MathUtils.PI / 2;
			} else {
				angle = 0;
			}

			body.setTransform(body.getPosition(), angle);

			if (Gdx.input.isKeyPressed(Keys.D)) {
				body.setLinearDamping(0);
				if (onSurface) {
					body.applyForceToCenter(500 * MathUtils.cos(angle) - 250 * MathUtils.cos(angle - MathUtils.PI * 0.25f), 500 * MathUtils.sin(angle) - 250 * MathUtils.sin(angle - MathUtils.PI * 0.25f), true);

					mainFixture.setFriction(0.0f);
				}
			} else if (Gdx.input.isKeyPressed(Keys.A)) {
				body.setLinearDamping(0);
				if (onSurface) {
					body.applyForceToCenter(-500 * MathUtils.cos(angle) - 250 * MathUtils.cos(angle - MathUtils.PI * 0.25f), -500 * MathUtils.sin(angle) - 250 * MathUtils.sin(angle - MathUtils.PI * 0.25f), true);
					mainFixture.setFriction(0.0f);
				}
			} else {
				mainFixture.setFriction(10.0f);

				if (onSurface)
					body.setLinearDamping(10.0f);
				else
					body.setLinearDamping(0.0f);
			}

			if (Gdx.input.isKeyPressed(Keys.SPACE)) {
				if (onSurface) {
					body.applyForceToCenter(-10000 * MathUtils.sin(angle), 10000 * MathUtils.cos(angle), true);
					TutorialManager.showTip(2);
				}
			}

			if (Gdx.input.isKeyJustPressed(Keys.U)) {
				if (canBoard) {
					setInShip(potentialBoardShip);
					potentialBoardShip = null;
					canBoard = false;
				}
			}

			testSprite.setOriginCenter();
			testSprite.setPosition(x - 1.2f, y - 2.5f);
			testSprite.setSize(2.4f, 5);
			testSprite.setRotation(angle * MathUtils.radiansToDegrees);
		}

		actionToolbox.update(dt);
		actionToolboxContent.updateContent(this);
		actionToolboxContent.update(dt);

		navballToolbox.update(dt);
		navball.updateContent(this);
		navball.update(dt);
		
		if(getApoapsis() > 200)
			TutorialManager.showTip(10);
		
		if(getPeriapsis() > 20)
			TutorialManager.showTip(13);
		
		if(Gdx.input.isKeyJustPressed(Keys.F2))
			TutorialManager.showTip(15);
		
		if(target != null) {
			if(target.getVelocity().sub(getVelocity()).len() < 10 && target.getPosition().sub(getPosition()).len() < 100) {
				TutorialManager.showTip(18);
			}
			
			if(target.getPosition().sub(getPosition()).len() < 30) {
				TutorialManager.showTip(19);
			}
		}
		
		if (notifying)
			notifyTime += dt;
		if (notifyTime > 2.5f)
			notifying = false;
	}

	public void draw(SpriteBatch batch) {
	}

	public void drawHUD(SpriteBatch hudBatch) {
		if (potentialBoardShip != null) {
			largeFont.draw(hudBatch, "Ship Nearby. Press [U] to board.", UltranautGame.WIDTH / 2 - new GlyphLayout(largeFont, "Ship Nearby. Press [U] to board.").width / 2, UltranautGame.HEIGHT * 0.8f);
		}

		if (notifying) {
			String text = notifyText;

			if (notifyText.contains("#B")) {
				largeFont.setColor(UltranautColors.BLUE);
				text = notifyText.split("#B")[1];
			} else if (notifyText.contains("#R")) {
				largeFont.setColor(UltranautColors.RED);
				text = notifyText.split("#R")[1];
			} else if (notifyText.contains("#O")) {
				largeFont.setColor(UltranautColors.ORANGE);
				text = notifyText.split("#O")[1];
			}

			largeFont.draw(hudBatch, text, UltranautGame.WIDTH / 2 - new GlyphLayout(largeFont, text).width / 2, UltranautGame.HEIGHT * 0.9f);
			largeFont.setColor(Color.WHITE);
		}

		if (target != null) {
			float height = smallFont.getCapHeight();
			Vector2 distance = getPosition().sub(target.getPosition());
			Vector2 relativeVelocity = getVelocity().sub(target.getVelocity());
			smallFont.setColor(target.getGizmoColor());
			smallFont.draw(hudBatch, target.getGizmoText(), UltranautGame.WIDTH * 0.2f, UltranautGame.HEIGHT * 0.6f + height * 1);
			smallFont.setColor(UltranautColors.PINK);
			smallFont.draw(hudBatch, "Separation: " + Toolbox.unitFormat.format(distance.len()), UltranautGame.WIDTH * 0.2f, UltranautGame.HEIGHT * 0.6f + height * 0);
			smallFont.draw(hudBatch, "V-Difference: " + Toolbox.unitFormat.format(relativeVelocity.len()), UltranautGame.WIDTH * 0.2f, UltranautGame.HEIGHT * 0.6f + height * -1);

			if (target.getPosition().sub(getPosition()).len() > 300) {
				Vector3 screenPosition = Universe.camera.project(new Vector3(target.getPosition(), 0));
				float tx = screenPosition.x;
				float ty = screenPosition.y;

				smallFont.draw(hudBatch, target.getGizmoText(), tx - new GlyphLayout(smallFont, target.getGizmoText()).width / 2, ty + 30);
			}
		}

		hudBatch.end();
		gizmoRenderer.begin();
		gizmoRenderer.set(ShapeType.Line);
		Gdx.gl20.glLineWidth(3);

		if (target != null) {
			if (target.getPosition().sub(getPosition()).len() > 200) {
				Vector3 screenPosition = Universe.camera.project(new Vector3(target.getPosition(), 0));
				float tx = screenPosition.x;
				float ty = screenPosition.y;
				gizmoRenderer.setColor(UltranautColors.PINK);
				gizmoRenderer.circle(tx, ty, 10);
			}

			gizmoRenderer.circle(UltranautGame.WIDTH * 0.2f + 25, UltranautGame.HEIGHT * 0.5f, 25);
			
			gizmoRenderer.set(ShapeType.Filled);
			gizmoRenderer.setColor(UltranautColors.BLUE);
			gizmoRenderer.identity();
			gizmoRenderer.translate(UltranautGame.WIDTH * 0.2f + 25, UltranautGame.HEIGHT * 0.5f, 0);
			gizmoRenderer.rotate(0, 0, 1, getAngle() * MathUtils.radiansToDegrees);
			gizmoRenderer.triangle(-10, 0, 10, 0, 0, 20);
			gizmoRenderer.identity();
			float angle = MathUtils.atan2(y - target.getPosition().y, x - target.getPosition().x) + MathUtils.PI;
			gizmoRenderer.setColor(UltranautColors.PINK);
			gizmoRenderer.circle(UltranautGame.WIDTH * 0.2f + 25 + 25 * MathUtils.cos(angle), UltranautGame.HEIGHT * 0.5f + 25 * MathUtils.sin(angle), 5);
			Vector2 relativeVelocity = target.getVelocity().sub(getVelocity());
			float velocityAngle = relativeVelocity.angle() * MathUtils.degreesToRadians;
			gizmoRenderer.setColor(UltranautColors.ORANGE);
			gizmoRenderer.circle(UltranautGame.WIDTH * 0.2f + 25 + 35 * MathUtils.cos(velocityAngle + MathUtils.PI), UltranautGame.HEIGHT * 0.5f + 35 * MathUtils.sin(velocityAngle + MathUtils.PI), 5);
			gizmoRenderer.setColor(UltranautColors.YELLOW);
			gizmoRenderer.circle(UltranautGame.WIDTH * 0.2f + 25 + 35 * MathUtils.cos(velocityAngle), UltranautGame.HEIGHT * 0.5f + 35 * MathUtils.sin(velocityAngle), 5);
		}

		gizmoRenderer.set(ShapeType.Filled);

		if (controllingShip) {
			/*if (ship instanceof Odyssey) {
				gizmoRenderer.setColor(UltranautColors.GREEN);
				gizmoRenderer.arc(UltranautGame.WIDTH / 2, 70, 50, 0, ((Odyssey) ship).getFuelPercent() / 100.f * 360);

				gizmoRenderer.setColor(Color.WHITE);
				gizmoRenderer.arc(UltranautGame.WIDTH / 2 - 100, 70, 50, 0, ((Odyssey) ship).getMonoPercent() / 100.f * 360);

				if (((Odyssey) ship).getFuelPercent() < 1)
					gizmoRenderer.setColor(((int) Universe.getAge() * 3) % 2 == 0 ? Color.BLACK : UltranautColors.RED);
				else
					gizmoRenderer.setColor(UltranautColors.BLUE);

				gizmoRenderer.circle(UltranautGame.WIDTH / 2, 70, 40);

				if (((Odyssey) ship).getMonoPercent() < 1)
					gizmoRenderer.setColor(((int) Universe.getAge() * 3) % 2 == 0 ? Color.BLACK : UltranautColors.RED);
				else
					gizmoRenderer.setColor(UltranautColors.ORANGE);

				gizmoRenderer.circle(UltranautGame.WIDTH / 2 - 100, 70, 40);*/

				Gdx.gl20.glLineWidth(1);
				gizmoRenderer.end();
				hudBatch.begin();

				/*if (((Odyssey) ship).getFuelPercent() < 1) {
					smallFont.setColor(((int) Universe.getAge() * 3) % 2 == 0 ? UltranautColors.RED : Color.BLACK);
				} else {
					smallFont.setColor(UltranautColors.GREEN);
				}

				smallFont.draw(hudBatch, "Fuel", UltranautGame.WIDTH / 2 - new GlyphLayout(smallFont, "Fuel").width / 2, 70 + new GlyphLayout(smallFont, "Fuel").height / 2);

				if (((Odyssey) ship).getMonoPercent() < 1) {
					smallFont.setColor(((int) Universe.getAge() * 3) % 2 == 0 ? UltranautColors.RED : Color.BLACK);
				} else {
					smallFont.setColor(Color.WHITE);
				}

				smallFont.draw(hudBatch, "Mono", UltranautGame.WIDTH / 2 - 100 - new GlyphLayout(smallFont, "Mono").width / 2, 70 + new GlyphLayout(smallFont, "Mono").height / 2);
				*/
			} else if (ship instanceof Triumph) {
				gizmoRenderer.setColor(UltranautColors.BLUE);
				gizmoRenderer.arc(UltranautGame.WIDTH / 2, 70, 50, 0, ((Triumph) ship).getElectricityPercent() / 100.f * 360);

				if (((Triumph) ship).getElectricityPercent() < 1)
					gizmoRenderer.setColor(((int) Universe.getAge() * 3) % 2 == 0 ? Color.BLACK : UltranautColors.RED);
				else
					gizmoRenderer.setColor(UltranautColors.NAVY);

				gizmoRenderer.circle(UltranautGame.WIDTH / 2, 70, 40);

				Gdx.gl20.glLineWidth(1);
				gizmoRenderer.end();
				hudBatch.begin();

				if (((Triumph) ship).getElectricityPercent() < 1) {
					smallFont.setColor(((int) Universe.getAge() * 3) % 2 == 0 ? UltranautColors.RED : Color.BLACK);
				} else {
					smallFont.setColor(Color.WHITE);
				}

				smallFont.draw(hudBatch, "Charge", UltranautGame.WIDTH / 2 - new GlyphLayout(smallFont, "Charge").width / 2, 70 + new GlyphLayout(smallFont, "Charge").height / 2);
			}
		 else {
			hudBatch.begin();
			gizmoRenderer.end();
		}
	}
	
	public void drawToolboxes(SpriteBatch hudBatch) {
		actionToolbox.draw(hudBatch);
		navballToolbox.draw(hudBatch);
	}

	public Vector2 getPosition() {
		if (controllingShip)
			return ship.getPosition();
		return body.getPosition();
	}

	public float getAngle() {
		if (controllingShip)
			return ship.getAngle();
		return angle;
	}

	public void setOnSurface(boolean onSurface) {
		this.onSurface = onSurface;
	}

	public void setInShip(Ship testShip) {
		ship = testShip;
		ship.setDriver(this);

		if (target != null)
			if (target.equals(ship)) {
				target = null;
				flightComputer.removeTarget();
			}

		if (!controllingShip) {
			controllingShip = true;
			Universe.masses.removeValue(this, true);
			Universe.pushDestroyBodyRequests(body);
		}
	}

	public void environmentEntered(Environment reference) {
		Gdx.app.log("Player", "Copy that. Entering environment.");

		if (!environment.equals((Environment) (reference))) {
			if (controllingShip) {
				ship.environmentEntered(reference);
			} else {
				body.setLinearVelocity(vx - ((Planet) reference).getVelocity().x, vy - ((Planet) reference).getVelocity().y);
			}
			setEnvironment((Environment) reference);
		}
	}

	public void environmentExited(Environment environment, Environment parent) {
		if (!environment.equals(parent)) {
			Gdx.app.log("Ship", "Leaving environment: " + environment.getName());
			Gdx.app.log("Player", "" + vx + ", " + vy);

			if (environment instanceof CelestialBody) {
				CelestialBody previousOrbitee = (CelestialBody) environment;

				if (body.getLinearVelocity().len() + previousOrbitee.getOrbitSpeed() >= 59.9f) {
					usingInhousePhysics = false;
					vx += previousOrbitee.getOrbitSpeed() * MathUtils.sin(previousOrbitee.getOrbitalAngle());
					vy += previousOrbitee.getOrbitSpeed() * MathUtils.cos(previousOrbitee.getOrbitalAngle());
				} else {
					body.setLinearVelocity(body.getLinearVelocity().x + previousOrbitee.getOrbitSpeed() * MathUtils.sin(previousOrbitee.getOrbitalAngle()), body.getLinearVelocity().y + previousOrbitee.getOrbitSpeed() * MathUtils.cos(previousOrbitee.getOrbitalAngle()));
				}
			}

			Gdx.app.log("Player", "" + vx + ", " + vy);
			setEnvironment(parent);
		}
	}

	public void unboard(Vector2 shipPosition, Vector2 shipVelocity, float shipAngle) {
		controllingShip = false;
		reinstantiateBody(shipPosition.x + 10 * MathUtils.cos(shipAngle), shipPosition.y + 10 * MathUtils.sin(shipAngle), shipAngle);
		body.setLinearVelocity(shipVelocity);
	}

	private void reinstantiateBody(float x, float y, float rotation) {
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DynamicBody;
		bodyDef.position.set(x, y);
		bodyDef.fixedRotation = true;

		PolygonShape torsoShape = new PolygonShape();
		torsoShape.setAsBox(0.6f, 1f);

		PolygonShape footShape = new PolygonShape();
		footShape.set(new Vector2[] { new Vector2(-0.6f, -1), new Vector2(0.6f, -1), new Vector2(0, -1.5f), });

		PolygonShape groundSensorShape = new PolygonShape();
		groundSensorShape.set(new Vector2[] { new Vector2(-0.6f, -1), new Vector2(0.6f, -1), new Vector2(0, -2f), });

		FixtureDef torsoFixtureDef = new FixtureDef();
		torsoFixtureDef.shape = torsoShape;
		torsoFixtureDef.friction = 0.5f;
		torsoFixtureDef.density = 12;

		FixtureDef footFixtureDef = new FixtureDef();
		footFixtureDef.shape = footShape;
		footFixtureDef.friction = 0.1f;
		footFixtureDef.density = 12;

		FixtureDef groundSensorDef = new FixtureDef();
		groundSensorDef.shape = groundSensorShape;
		groundSensorDef.isSensor = true;

		body = Universe.physicsWorld.createBody(bodyDef);
		body.createFixture(torsoFixtureDef).setUserData(this);
		body.createFixture(groundSensorDef).setUserData(new Foot(this));
		mainFixture = body.createFixture(footFixtureDef);
		mainFixture.setUserData(this);

		body.setUserData(this);

		body.setLinearDamping(0);

		torsoShape.dispose();
		footShape.dispose();

		Universe.masses.add(this);
		
		if(environment instanceof Star)
			((Star)environment).addFocusable(this);
		else
			((Star)(environment.getParent())).addFocusable(this);
	}

	public boolean isControllingShip() {
		return ship != null;
	}

	public Ship getShip() {
		return ship;
	}

	public Vector2 getVelocity() {
		if (controllingShip)
			return ship.getVelocity();
		return body.getLinearVelocity();
	}

	public void setNearbyShip(Ship ship, boolean nearby) {
		if (nearby) {
			canBoard = true;
			potentialBoardShip = ship;
		} else {
			canBoard = false;
			potentialBoardShip = null;
		}
	}

	public void setEnvironment(Environment environment) {
		Gdx.app.log("Player", "Environment set to " + environment.getName());
		this.environment = environment;

		if (controllingShip) {
			ship.setEnvironment(environment);
		}
	}

	public Environment getEnvironment() {
		return environment;
	}

	public float getAngleAroundPlanet() {
		if (controllingShip) {
			return ship.getAngleAroundEnvironment();
		}
		return MathUtils.atan2(y - environment.getPosition().y, x - environment.getPosition().x);
	}

	public float getPeriapsis() {
		if (controllingShip)
			return ship.getPeriapsis();
		return periapsis;
	}

	public float getApoapsis() {
		if (controllingShip)
			return ship.getApoapsis();
		return apoapsis;
	}

	@Override
	public float getSemimajorAxis() {
		if (controllingShip)
			return ship.getSemimajorAxis();
		return semimajorAxis;
	}

	@Override
	public float getSemiminorAxis() {
		if (controllingShip)
			return ship.getSemiminorAxis();
		return semiminorAxis;
	}

	public float getAltitude() {
		if (controllingShip)
			return ship.getAltitude();
		return Vector2.dst(x, y, environment.getPosition().x, environment.getPosition().y) - environment.getAverageRadius();
	}

	public void changePosition(Vector2 positionDifference) {
		if (controllingShip) {
			ship.changePosition(positionDifference);
		} else {
			x += positionDifference.x;
			y += positionDifference.y;

			body.setTransform(body.getPosition().add(positionDifference), body.getAngle());
		}
	}

	public float getTrueAnomaly() {
		if (controllingShip)
			return ship.getTrueAnomaly();
		return OrbitalHelper.computeOrbit(environment.getPosition(), body.getPosition(), new Vector2(vx, vy), environment.getMass())[7];
	}

	public float getOrbitAngle() {
		if (controllingShip) {
			return ship.getOrbitAngle();
		}
		return orbitAngle;
	}

	public Color getGizmoColor() {
		return Color.CYAN;
	}

	public String getGizmoText() {
		return "Herobrine";
	}

	public boolean isOnSurface() {
		if (controllingShip)
			return ship.isOnSurface();
		return onSurface;
	}

	public float getOrbitalPeriod() {
		if (controllingShip)
			return ship.getOrbitalPeriod();
		return orbitalPeriod;
	}

	public void displayNotification(String notifyText) {
		this.notifyText = notifyText;
		notifying = true;
		notifyTime = 0;
	}

	public Vector2 getFocusPosition() {
		if (controllingShip) {
			Vector2 offset = ship.getPosition().sub(environment.getPosition());
			return new Vector2(environment.getTruePosition().x + offset.x, environment.getTruePosition().y + offset.y);
		}
		Vector2 offset = body.getPosition().sub(environment.getPosition());
		return new Vector2(environment.getTruePosition().x + offset.x, environment.getTruePosition().y + offset.y);
	}

	public void setTarget(Focusable target) {
		this.target = target;
	}

	public void setFlightComputer(FlightComputer flightComputer) {
		this.flightComputer = flightComputer;
	}
}
