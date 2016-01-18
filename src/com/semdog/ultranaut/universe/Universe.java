package com.semdog.ultranaut.universe;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.semdog.ultranaut.UltranautGame;
import com.semdog.ultranaut.mathematics.FlightComputer;
import com.semdog.ultranaut.player.Player;
import com.semdog.ultranaut.vehicles.Odyssey;
import com.semdog.ultranaut.vehicles.Triumph;

/**
 * This class is what it sounds like --- it holds everything visible
 * (and sometimes invisible) ingame such as planets, stars, players and 
 * other entities.
 * 
 * It is responsible for keeping track of them, updating them, drawing them,
 * and removing them if necessary.
 * 
 * It also houses the Box2D physics world, the thing responsible for most ingame
 * physics.
 * 
 * @author Sam
 */

public class Universe {
	//	This is this univere's Gravitational Constant. 
	public static final float GRAVCONSTANT = 1 / 314.f;
	
	private Box2DDebugRenderer renderer;
	public static World physicsWorld;
	public static Array<Mass> masses;

	public static Array<Body> bodyDestroyLine;
	public static Array<Joint> jointDestroyLine;

	public static Array<Entity> entities;

	private static float age = 0;

	private static Player player;
	
	@SuppressWarnings("unused")
	private Odyssey testShip;
	
	@SuppressWarnings("unused")
	private Triumph triumph;

	private Star testStar;

	public static OrthographicCamera camera;

	private SpriteBatch hudBatch;
	private SpriteBatch environmentBatch;
	private PolygonSpriteBatch planetBatch;
	private ShapeRenderer gizmoRenderer;

	private FlightComputer flightComputer;

	private float cameraAngle = 0;

	private boolean fixedCamera = false;
	private boolean flightComputerUp = false;

	/**
	 * This constructor initializes everything in the universe.
	 */
	public Universe() {
		physicsWorld = new World(new Vector2(0, 0), true);

		physicsWorld.setContactListener(new UniverseContactListener());

		bodyDestroyLine = new Array<Body>();
		jointDestroyLine = new Array<Joint>();

		camera = new OrthographicCamera(UltranautGame.WIDTH, UltranautGame.HEIGHT);
		camera.zoom = 0.05f;
		camera.update();

		testStar = new Star(0, 0, this);

		gizmoRenderer = new ShapeRenderer();
		gizmoRenderer.setAutoShapeType(true);
		gizmoRenderer.setProjectionMatrix(camera.combined);

		planetBatch = new PolygonSpriteBatch();
		planetBatch.setProjectionMatrix(camera.combined);

		hudBatch = new SpriteBatch();

		environmentBatch = new SpriteBatch();
		environmentBatch.setProjectionMatrix(camera.combined);

		masses = new Array<Mass>();
		entities = new Array<Entity>();

		player = new Player(testStar.getBody(0), physicsWorld);
		testShip = new Odyssey(testStar.getBody(0), physicsWorld, testStar.getBody(0).getPosition().add(0, testStar.getBody(0).getAverageRadius() + 50));
		
		triumph = new Triumph(testStar.getBody(0), physicsWorld, testStar.getBody(0).getPosition().add(0, testStar.getBody(0).getAverageRadius() + 1500));
		
		flightComputer = new FlightComputer(player);
		
		player.setFlightComputer(flightComputer);
		renderer = new Box2DDebugRenderer();
	}

	/**
	 * This renders the needed entities.
	 */
	public void render() {
		
		//	If the Flight Computer isn't up, then draw everything else.
		if (!flightComputerUp) {
			
			//	The planetBatch is a PolygonSprite batch, which allows for non-rectangular sprites to be drawn.
			planetBatch.begin();
			testStar.draw(planetBatch);
			planetBatch.end();

			//	The environmentBatch is a simple SpriteBatch which draws normal sprites.
			environmentBatch.begin();
			player.draw(environmentBatch);

			for (Entity entity : entities) {
				entity.draw(environmentBatch);
			}
			environmentBatch.end();

			//	The physics world is drawn, so all colliders are visible. Comment this out for an immersive experience.
			renderer.render(physicsWorld, camera.combined);
		}

		//	The hudBatch draws all UI which should be superimposed on whatever is being drawn in the background.
		hudBatch.begin();

		if (flightComputerUp)
			flightComputer.draw(hudBatch);
		else
			//	This draws the player's hud, which includes information like fuel usage.
			player.drawHUD(hudBatch);
		
		//	The player's toolboxes are then drawn no matter what is being drawn underenath them.
		//player.drawToolboxes(hudBatch);

		hudBatch.end();
	}

	/**
	 * This method is called 60 times per second (usually) and is responsible
	 * for making all the universe's entities tick and update.
	 * @param dt
	 */
	public void update(float dt) {
		//	Adds time to the Universe's age.
		age += dt;

		player.update(dt);
		
		//	This method tells the physics world to update.
		physicsWorld.step(1 / 120.f, 6, 2);
		
		testStar.update(dt);

		//	Updating entities.
		for (Entity entity : entities) {
			entity.update(dt);
		}

		//	Checks if the flight computer needs to be displayed
		if (Gdx.input.isKeyJustPressed(Keys.X)) {
			flightComputerUp = !flightComputerUp;
		}

		//	If the computer isn't up, this will update the camera.
		if (!flightComputerUp) {
			camera.position.set(player.getPosition(), 0);

			//	Zooms out if Page Up is pressed.
			if (Gdx.input.isKeyPressed(Keys.PAGE_UP)) {
				camera.zoom += camera.zoom < 10 ? 3 * dt : 0;
			}
			
			//	Zooms in if Page Down is pressed.
			if (Gdx.input.isKeyPressed(Keys.PAGE_DOWN)) {
				camera.zoom += camera.zoom > 0.1 ? -3 * dt : 0;
			}

			//	Toggles Fixed Camera mode, which makes the rotation of the camera match that of the player.
			if (Gdx.input.isKeyJustPressed(Keys.C)) {
				fixedCamera = !fixedCamera;
				System.out.println();
			}

			if (fixedCamera) {
				cameraAngle %= MathUtils.PI2;
				float focusAngle = -player.getAngleAroundPlanet() + MathUtils.PI  / 2;
				float da = (focusAngle - cameraAngle) / 100.f;
				cameraAngle += da;
				camera.rotate(-da * MathUtils.radiansToDegrees);
			} else {
				cameraAngle %= MathUtils.PI2;
				float da = -cameraAngle / 10.f;
				cameraAngle += da;
				camera.rotate(-da * MathUtils.radiansToDegrees);
			}

			camera.update();

			//	This sets the offsets for the various batches to match that of the camera's.
			gizmoRenderer.setProjectionMatrix(camera.combined);
			planetBatch.setProjectionMatrix(camera.combined);
			environmentBatch.setProjectionMatrix(camera.combined);
		} else {
			flightComputer.update(dt);
		}
		
		/*
		 * Box2D crashes if joints or bodies are removed during a frame.
		 * Therefore the best solution is to have a list of bodies/joints
		 * waiting to be removed and loop through that once a cycle.
		 * 
		 * If there is something to be removed, the world will remove it 
		 * and break the frame there; removing more than one joint/body
		 * at the end of a frame also causes a crash. 
		 * 
		 * Thus, if a joint has been removed, the body list will not
		 * be looked over and will be reviewed the following frame.
		 */

		boolean removedSomething = false;

		for (Joint joint : jointDestroyLine) {
			jointDestroyLine.removeValue(joint, false);
			physicsWorld.destroyJoint(joint);
			removedSomething = true;
			break;
		}

		if (!removedSomething) {
			for (Body body : bodyDestroyLine) {
				bodyDestroyLine.removeValue(body, false);
				physicsWorld.destroyBody(body);
				removedSomething = true;
				break;
			}
		}
		
		if(removedSomething)
			return;
	}

	/**
	 * Adds a body to a list, storing details of all the bodies
	 * marked for destruction.
	 * 
	 * @param body
	 */
	public static void pushDestroyBodyRequests(Body body) {
		bodyDestroyLine.add(body);
	}
	
	/**
	 * Adds a joint to a list, storing details of all the joints
	 * marked for destruction.
	 * 
	 * @param body
	 */
	public static void pushDestroyJointRequests(Joint joint) {
		jointDestroyLine.add(joint);
	}

	public static void save() {

	}

	public static float getAge() {
		return age;
	}

	public static float getPlayerX() {
		return player.getPosition().x;
	}

	public static float getPlayerY() {
		return player.getPosition().y;
	}

	public static void playerEntered(Environment environment) {
		player.environmentEntered(environment);
	}

	public static void playerExited(Environment environment, Vector2 positionDifference) {
		player.environmentExited(environment, environment.getParent());
		player.changePosition(positionDifference);
	}

}
