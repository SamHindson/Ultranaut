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
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Array;
import com.semdog.ultranaut.meta.UltranautColors;

/**
 * The Planet is the only Celestial Body available in the current
 * version of ULTRANAUT. It is also the only object on which the 
 * player is able to land on, move around on and refuel from.
 * 
 * It orbits its designated star at realistic orbital speeds and
 * has a fairly uniform collider.
 * 
 * @author Sam
 */

public class Planet extends CelestialBody implements Environment {

	private PolygonSprite sprite;
	private float radius;
	private Fixture planetFixture;
	private ChainShape shape;

	public Planet(int id, float angle, float distance, Star parent) {
		super(angle, distance, parent);

		//	Each planet has a radius of between 250 and 1000m
		radius = MathUtils.random(1000, 5000);
		radius = 5000;
		
		//	The mass of the planet is pi*r^3
		mass = MathUtils.PI * radius * radius * radius * 0.1f;
		
		/*	
		 * A sphere of influence is an area in space around which the gravitational
		 * pull of a smaller object overcomes that of a larger, nearby object.
		 * 
		 * The planet's sphere of influence is worked out using the equation
		 * 
		 * r = d * (m/M)^2.5
		 * 
		 * where
		 * r is the influence sphere radius
		 * d is the distance from the larger body
		 * m is the mass of the smaller body
		 * M is the mass of the larger body.
		 */
		influenceSphereRadius = distance * (float)Math.pow(mass / parent.getMass(), 2./5.);

		this.orbitRadius = distance;
		this.orbitalAngle = trueOrbitalAngle = angle;

		float[] planetVertices = new float[200];

		/*	
		 * This value denotes how bumpy the planet's surface ought to be.
		 *	It is currently zero as the means to move around mountains and
		 *	valleys is not yet implemented.
		 */
		float noiseBias = radius / 50;

		// Builds a float array defining the polygon's edges
		for (int t = 0; t < planetVertices.length; t += 2) {
			float p = (t * 1.0f) / (planetVertices.length * 1.0f / 2);

			float m = MathUtils.random(-noiseBias, noiseBias);

			planetVertices[t] = (radius + MathUtils.sin(t) * m) * MathUtils.cos(p * MathUtils.PI);
			planetVertices[t + 1] = (radius + MathUtils.sin(t) * m) * MathUtils.sin(p * MathUtils.PI);
		}

		// Constructs the shape for the RigidBody - the maximum number of
		// vertices for a PolygonShape is ridiculously small, so we'll use
		// a chain shape instead. (This causes the planet to be hollow, but
		// it's fine. We're not mining. This isn't Minecraft)
		shape = new ChainShape();
		shape.createLoop(planetVertices);
		
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.KinematicBody;
		bodyDef.position.set(x, y);

		body = Universe.physicsWorld.createBody(bodyDef);

		body.setLinearDamping(0);
		
		reinstantiateBody();

		// Makes a nice name for the planet.
		bodyID = parent.getID() + "-" + id;

		// Color in between the vertices!
		Pixmap planetPixmap = new Pixmap((int)1, (int)1, Format.RGBA8888);
		createPixmap(planetPixmap);
		
		Texture planetTexture = new Texture(planetPixmap);
		PolygonRegion planetPolygonRegion = new PolygonRegion(new TextureRegion(planetTexture), planetVertices, new EarClippingTriangulator().computeTriangles(planetVertices).items);
		
		sprite = new PolygonSprite(planetPolygonRegion);
		sprite.setOrigin(radius / 2, radius / 2);

		planetPixmap.dispose();
		
		averageRadius = radius;
	}

	//	This method colors in a pixmap for use by a sprite.
	private void createPixmap(Pixmap planetPixmap) {
		for(int x = 0; x < planetPixmap.getWidth(); x++) {
			for(int y = 0; y < planetPixmap.getHeight(); y++) {
				planetPixmap.setColor(primaryColor);
				planetPixmap.fillRectangle(x, y, 1, 1);
			}
		}
	}

	public void draw(PolygonSpriteBatch planetBatch) {
		sprite.draw(planetBatch);
	}

	public void update(float dt) {
		trueOrbitalAngle += orbitAngularVelocity * dt;
		
		tx = orbitRadius * MathUtils.cos(trueOrbitalAngle);
		ty = orbitRadius * MathUtils.sin(trueOrbitalAngle);
		
		rotationalSpeed = 0;
		axialAngle += rotationalSpeed * dt;

		sprite.setOrigin(0, 0);

		sprite.setRotation(axialAngle);

		orbitalAngle = MathUtils.atan2(y - py, x - px);

		Array<Body> bodies = new Array<Body>();
		Universe.physicsWorld.getBodies(bodies);

		//	Loops through bodies in physics world. If the body is nearby enough, it is pulled.
		for (Body b : bodies) {
			if (b.getFixtureList().get(0).getUserData() instanceof Mass && Vector2.dst(body.getPosition().x, body.getPosition().y, b.getPosition().x, b.getPosition().y) < (influenceSphereRadius)) {
				if(!((Mass)b.getFixtureList().get(0).getUserData()).getEnvironment().equals(this)) {
					((Mass)b.getFixtureList().get(0).getUserData()).setEnvironment(this);
				}
				((Mass) b.getFixtureList().get(0).getUserData()).pull(new Vector2(body.getPosition().x, body.getPosition().y), mass, dt);
			}
		}

		if(!hasPlayer) {
			//	If the player claims to not be on the planet but is nearby enough, tell the player that they're actually on the planet.
			if(Vector2.dst(body.getPosition().x, body.getPosition().y, Universe.getPlayerX(), Universe.getPlayerY()) <= influenceSphereRadius) {
				handlePlayerEntry();
				hasPlayer = true;
				body.setLinearVelocity(Vector2.Zero);
				return;
			}
			
			//	Otherwise, continue orbiting around your star like normal.
			dx = -orbitSpeed * (float) Math.sin(orbitalAngle);
			dy = orbitSpeed * (float) Math.cos(orbitalAngle);
			
			x += dx * dt;
			y += dy * dt;
			
			body.setTransform(x, y, body.getAngle());
			
			sprite.setPosition(body.getPosition().x, body.getPosition().y);
		} else {
			//	If the player is too far away to be considered on the planet anymore, notify the player that they have left.
			if(Vector2.dst(body.getPosition().x, body.getPosition().y, Universe.getPlayerX(), Universe.getPlayerY()) > influenceSphereRadius * 1.0f) {
				handlePlayerExit();
				hasPlayer = false;
				return;
			}
			
			//	Otherwise, stay completely still.
			body.setLinearVelocity(Vector2.Zero);
			sprite.setPosition(body.getPosition().x, body.getPosition().y);
		}
	}

	public String getName() {
		return "Planet " + bodyID;
	}

	@Override
	public float getMass() {
		return mass;
	}

	public float getRadius() {
		return radius;
	}
	
	public Environment getParent() {
		return parent;
	}
	
	public Vector2 getVelocity() {
		return body.getLinearVelocity();
	}
	
	//	A method used when player enter the influence sphere.
	@Override
	protected void reinstantiateBody() {
		FixtureDef planetFixtureDef = new FixtureDef();
		planetFixtureDef.shape = shape;
		planetFixture = body.createFixture(planetFixtureDef);
		planetFixture.setUserData(this);
		planetFixture.setFriction(0.5f);
	}

	@Override
	protected void destroyBody() {
		
	}

	public Color getGizmoColor() {
		return UltranautColors.BLUE;
	}

	public String getGizmoText() {
		if(givenID == null)
			return bodyID;
		else
			return givenID + " - [" + bodyID + "]";
	}

}
