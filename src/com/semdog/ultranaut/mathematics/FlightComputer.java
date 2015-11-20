package com.semdog.ultranaut.mathematics;

import java.text.DecimalFormat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.semdog.ultranaut.UltranautGame;
import com.semdog.ultranaut.meta.UltranautColors;
import com.semdog.ultranaut.player.Player;
import com.semdog.ultranaut.universe.Environment;
import com.semdog.ultranaut.universe.Mass;
import com.semdog.ultranaut.universe.Planet;
import com.semdog.ultranaut.universe.Star;
import com.semdog.ultranaut.vehicles.Ship;

/**
 * This class is what is essentially the ingame map - it shows where objects,
 * ships and players are. It also draws orbits.
 * 
 * @author Sam
 */

public class FlightComputer {

	private static DecimalFormat unitFormat = new DecimalFormat("0.##");

	private OrthographicCamera camera;
	private SpriteBatch computerBatch;
	private ShapeRenderer orbitRenderer, gizmoRenderer;

	private BitmapFont font;

	private float scale = 1000;

	private Star currentSolarSystem;
	private Sprite starSprite;
	private Sprite perigee, apogee;

	private Player player;

	private Focusable cameraFocus;
	private Focusable pointerFocus;
	private Focusable target;

	public FlightComputer(Player player) {
		camera = new OrthographicCamera(UltranautGame.WIDTH / 10.f, UltranautGame.HEIGHT / 10.f);
		camera.position.set(0, 0, 0);
		camera.update();
		computerBatch = new SpriteBatch();

		this.player = player;

		if (player.getEnvironment() instanceof Planet)
			currentSolarSystem = (Star) ((Planet) player.getEnvironment()).getParent();
		else if (player.getEnvironment() instanceof Star)
			currentSolarSystem = (Star) player.getEnvironment();

		Pixmap starPixmap = new Pixmap((int) (currentSolarSystem.getAverageRadius() / 5.f),
				(int) (currentSolarSystem.getAverageRadius() / 5.f), Format.RGBA8888);
		starPixmap.setColor(Color.WHITE);
		starPixmap.fillCircle((int) (currentSolarSystem.getAverageRadius() / 10.f),
				(int) (currentSolarSystem.getAverageRadius() / 10.f),
				(int) (currentSolarSystem.getAverageRadius() / 10.f));
		starPixmap.setColor(currentSolarSystem.getColor());
		starPixmap.fillCircle((int) (currentSolarSystem.getAverageRadius() / 10.f),
				(int) (currentSolarSystem.getAverageRadius() / 10.f),
				(int) (currentSolarSystem.getAverageRadius() / 5.f));

		starSprite = new Sprite(new Texture(starPixmap));
		starSprite.setOriginCenter();
		starSprite.setPosition(0, 0);

		orbitRenderer = new ShapeRenderer();
		orbitRenderer.setAutoShapeType(true);

		gizmoRenderer = new ShapeRenderer();
		gizmoRenderer.setAutoShapeType(true);

		cameraFocus = player;

		perigee = new Sprite(new Texture(Gdx.files.internal("assets/graphics/gizmos/perigee.png")));
		apogee = new Sprite(new Texture(Gdx.files.internal("assets/graphics/gizmos/apogee.png")));
		perigee.setOriginCenter();
		apogee.setOriginCenter();
		perigee.setSize(10, 10);
		apogee.setSize(10, 10);

		font = new BitmapFont(Gdx.files.internal("assets/fonts/mohave32_BA.fnt"));
	}

	public void update(float dt) {
		camera.position.set(cameraFocus.getFocusPosition().x / scale, cameraFocus.getFocusPosition().y / scale, 0);

		if (Gdx.input.isKeyPressed(Keys.PAGE_UP) && camera.zoom < 300 - 2 * dt) {
			camera.zoom += 2 * dt;
		}

		if (Gdx.input.isKeyPressed(Keys.PAGE_DOWN) && camera.zoom > 2 * dt) {
			camera.zoom += -2 * dt;
		}

		camera.update();
		computerBatch.setProjectionMatrix(camera.combined);
		orbitRenderer.setProjectionMatrix(camera.combined);

		if (cameraFocus instanceof Player) {
			com.semdog.ultranaut.universe.Environment environment = ((Player) cameraFocus).getEnvironment();
			float oa = ((Player) cameraFocus).getOrbitAngle();
			float periapsis = ((Player) cameraFocus).getPeriapsis();
			float apoapsis = ((Player) cameraFocus).getApoapsis();
			perigee.setPosition(
					(environment.getTruePosition().x + MathUtils.cos(oa) * (periapsis + environment.getAverageRadius()))
							/ scale - 5,
					(environment.getTruePosition().y + MathUtils.sin(oa) * (periapsis + environment.getAverageRadius()))
							/ scale - 5);
			apogee.setPosition(
					(environment.getTruePosition().x
							+ MathUtils.cos(oa + MathUtils.PI) * (apoapsis + environment.getAverageRadius())) / scale
							- 5,
					(environment.getTruePosition().y
							+ MathUtils.sin(oa + MathUtils.PI) * (apoapsis + environment.getAverageRadius())) / scale
							- 5);
		}

		// This draws all the Focusables in the Solar System
		for (Focusable focusable : currentSolarSystem.getFocusables()) {
			Vector3 screenLocation = camera.project(
					new Vector3(focusable.getFocusPosition().x / scale, focusable.getFocusPosition().y / scale, 0));
			float sx = screenLocation.x;
			float sy = screenLocation.y;

			// If the mouse pointer is less than 10 pixels away from the
			// focusable gizmo,
			// then it is said to be hovered.
			if (Vector2.dst(sx, sy, Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()) < 10) {
				pointerFocus = focusable;

				// Set target if mouse is down and it's hovered
				if (Gdx.input.isButtonPressed(Buttons.LEFT))
					if (!focusable.equals(target)) {
						target = focusable;
						player.setTarget(target);
					}
				break;
			} else {
				pointerFocus = null;
			}
		}
	}

	public void draw(SpriteBatch hudBatch) {
		hudBatch.end();

		orbitRenderer.begin();

		Gdx.gl20.glLineWidth(3);

		orbitRenderer.set(ShapeType.Line);

		// This method uses shape translation to draw the orbit of each mass. It
		// utilizes
		// the semimajor and semiminor axes of the orbit for the shape and the
		// orbital
		// rotation for placement.
		for (Focusable focusable : currentSolarSystem.getFocusables()) {
			if (focusable instanceof Mass) {
				float semiminor = ((Mass) focusable).getSemiminorAxis();
				float semimajor = ((Mass) focusable).getSemimajorAxis();
				float f = (float) Math.sqrt(semimajor * semimajor - semiminor * semiminor);

				float eccentricity = ((Mass) focusable).getOrbitalEccentricity();

				Environment environment = ((Mass) focusable).getEnvironment();

				orbitRenderer.setColor(focusable.getGizmoColor());

				if (eccentricity < 1) {
					orbitRenderer.identity();
					orbitRenderer.translate(environment.getTruePosition().x / scale,
							environment.getTruePosition().y / scale, 0);
					orbitRenderer.rotate(0, 0, 1, ((Mass) focusable).getOrbitAngle() * MathUtils.radiansToDegrees - 90);
					orbitRenderer.ellipse((-semiminor) / scale, (-semimajor - f) / scale, semiminor * 2 / scale,
							semimajor * 2 / scale);
					orbitRenderer.identity();
				} else {
					
				}
			}
		}

		for (int q = 0; q < currentSolarSystem.getCelestialBodyCount(); q++) {
			orbitRenderer.set(ShapeType.Line);
			orbitRenderer.setColor(UltranautColors.ORANGE);
			orbitRenderer.circle(currentSolarSystem.getX() / scale, currentSolarSystem.getY() / scale,
					currentSolarSystem.getBody(q).getOrbitRadius() / scale);

			orbitRenderer.setColor(UltranautColors.BLUE);
			orbitRenderer.circle(currentSolarSystem.getBody(q).getTx() / scale,
					currentSolarSystem.getBody(q).getTy() / scale,
					currentSolarSystem.getBody(q).getInfluenceSphereRadius() / scale);

			orbitRenderer.set(ShapeType.Filled);
			orbitRenderer.setColor(Color.WHITE);
			orbitRenderer.circle(currentSolarSystem.getBody(q).getTx() / scale,
					currentSolarSystem.getBody(q).getTy() / scale,
					currentSolarSystem.getBody(q).getAverageRadius() / scale);
			orbitRenderer.setColor(Color.BLACK);
			orbitRenderer.circle(currentSolarSystem.getBody(q).getTx() / scale,
					currentSolarSystem.getBody(q).getTy() / scale,
					currentSolarSystem.getBody(q).getAverageRadius() * 0.85f / scale);

			orbitRenderer.setColor(new Color(currentSolarSystem.getBody(q).getPrimaryColor().r,
					currentSolarSystem.getBody(q).getPrimaryColor().g,
					currentSolarSystem.getBody(q).getPrimaryColor().b, camera.zoom / 300.f));
			orbitRenderer.circle(currentSolarSystem.getBody(q).getTx() / scale,
					currentSolarSystem.getBody(q).getTy() / scale,
					currentSolarSystem.getBody(q).getAverageRadius() * 0.8f / scale);
		}

		orbitRenderer.end();

		computerBatch.begin();

		// perigee.draw(computerBatch);
		// apogee.draw(computerBatch);

		computerBatch.end();

		gizmoRenderer.begin(ShapeType.Filled);

		// This translates the Focusable's world coordinates into screen
		// coordinates
		// and draws little circles around them.
		for (Focusable focusable : currentSolarSystem.getFocusables()) {
			Vector2 location = focusable.getFocusPosition();
			Vector3 focusLocation = camera.project(new Vector3(location.x / scale, location.y / scale, 0));
			float gx = focusLocation.x;
			float gy = focusLocation.y;
			if (focusable.equals(pointerFocus))
				gizmoRenderer.setColor(new Color(focusable.getGizmoColor()).add(Color.WHITE));
			else if (focusable.equals(target))
				gizmoRenderer.setColor(UltranautColors.PINK);
			else
				gizmoRenderer.setColor(focusable.getGizmoColor());
			gizmoRenderer.circle(gx, gy, 10);
		}

		gizmoRenderer.end();

		Gdx.gl20.glLineWidth(1);

		hudBatch.begin();

		// Draws useful text
		if (cameraFocus instanceof Player) {
			Player player = (Player) cameraFocus;

			float lineHeight = font.getCapHeight();

			font.setColor(Color.WHITE);

			if (player.isOnSurface())
				font.draw(hudBatch, "On surface of", UltranautGame.WIDTH * 0.7f,
						UltranautGame.HEIGHT * 0.8f - lineHeight * 0);
			else if (player.getApoapsis() < 0)
				font.draw(hudBatch, "Leaving the pull of", UltranautGame.WIDTH * 0.7f,
						UltranautGame.HEIGHT * 0.8f - lineHeight * 0);
			else if (player.getPeriapsis() < 0)
				font.draw(hudBatch, "On suborbital trajectory of", UltranautGame.WIDTH * 0.7f,
						UltranautGame.HEIGHT * 0.8f - lineHeight * 0);
			else
				font.draw(hudBatch, "Orbiting", UltranautGame.WIDTH * 0.7f,
						UltranautGame.HEIGHT * 0.8f - lineHeight * 0);

			font.setColor(UltranautColors.GREEN);
			font.draw(hudBatch, player.getEnvironment().getName(), UltranautGame.WIDTH * 0.7f,
					UltranautGame.HEIGHT * 0.8f - lineHeight * 1);
			font.setColor(Color.WHITE);

			if (player.isOnSurface())
				font.draw(hudBatch, "It's nice and quiet here.", UltranautGame.WIDTH * 0.7f,
						UltranautGame.HEIGHT * 0.8f - lineHeight * 2);
			else if (player.getApoapsis() < 0) {
				font.setColor(Color.WHITE);
				font.draw(hudBatch, "Perigee: " + unitFormat.format(player.getPeriapsis()) + "m",
						UltranautGame.WIDTH * 0.7f, UltranautGame.HEIGHT * 0.8f - lineHeight * 4);
				font.draw(hudBatch, "Altitude: " + unitFormat.format(player.getAltitude()) + "m",
						UltranautGame.WIDTH * 0.7f, UltranautGame.HEIGHT * 0.8f - lineHeight * 5);
			} else if (player.getPeriapsis() < 0) {
				font.setColor(UltranautColors.RED);
				font.draw(hudBatch, "Impact eminent!", UltranautGame.WIDTH * 0.7f,
						UltranautGame.HEIGHT * 0.8f - lineHeight * 2);
				font.setColor(Color.WHITE);
				font.draw(hudBatch, "Apogee: " + unitFormat.format(player.getApoapsis()) + "m",
						UltranautGame.WIDTH * 0.7f, UltranautGame.HEIGHT * 0.8f - lineHeight * 4);
				font.draw(hudBatch, "Altitude: " + unitFormat.format(player.getAltitude()) + "m",
						UltranautGame.WIDTH * 0.7f, UltranautGame.HEIGHT * 0.8f - lineHeight * 5);
			} else {
				font.draw(hudBatch, "Orbital Period: " + unitFormat.format(player.getOrbitalPeriod()) + "s",
						UltranautGame.WIDTH * 0.7f, UltranautGame.HEIGHT * 0.8f - lineHeight * 2);
				font.draw(hudBatch, "Perigee: " + unitFormat.format(player.getPeriapsis()) + "m",
						UltranautGame.WIDTH * 0.7f, UltranautGame.HEIGHT * 0.8f - lineHeight * 4);
				font.draw(hudBatch, "Apogee: " + unitFormat.format(player.getApoapsis()) + "m",
						UltranautGame.WIDTH * 0.7f, UltranautGame.HEIGHT * 0.8f - lineHeight * 5);
				font.draw(hudBatch, "Altitude: " + unitFormat.format(player.getAltitude()) + "m",
						UltranautGame.WIDTH * 0.7f, UltranautGame.HEIGHT * 0.8f - lineHeight * 6);
			}

			if (pointerFocus != null) {
				boolean valid = false;
				if (pointerFocus instanceof Environment) {
					if (!player.getEnvironment().equals((Environment) pointerFocus)) {
						valid = true;
					}
				}

				if (pointerFocus instanceof Ship) {
					if (player.getShip() != null) {
						if (!player.getShip().equals((Ship) pointerFocus)) {
							valid = true;
						}
					}
				}

				if (valid) {
					font.draw(hudBatch, "Possible Target:", 20, UltranautGame.HEIGHT * 0.8f - lineHeight * 0);
					font.setColor(UltranautColors.ORANGE);
					font.draw(hudBatch, pointerFocus.getGizmoText(), 20, UltranautGame.HEIGHT * 0.8f - lineHeight * 1);
					font.setColor(UltranautColors.BLUE);
					font.draw(hudBatch, "Click to Target...", 20, UltranautGame.HEIGHT * 0.8f - lineHeight * 3);
				}
			}

			if (target != null) {
				Vector2 distance = player.getPosition().sub(target.getPosition());
				Vector2 relativeVelocity = player.getVelocity().sub(target.getVelocity());
				font.setColor(Color.WHITE);
				font.draw(hudBatch, "Targeted Entity:", 20, UltranautGame.HEIGHT * 0.5f - lineHeight * -1);
				font.setColor(UltranautColors.PINK);
				font.draw(hudBatch, target.getGizmoText(), 20, UltranautGame.HEIGHT * 0.5f - lineHeight * 0);
				font.setColor(Color.WHITE);
				font.draw(hudBatch, "Separation: " + unitFormat.format(distance.len()), 20,
						UltranautGame.HEIGHT * 0.5f - lineHeight * 1);
				font.setColor(Color.WHITE);
				font.draw(hudBatch, "Relative Velocity: " + unitFormat.format(relativeVelocity.len()), 20,
						UltranautGame.HEIGHT * 0.5f - lineHeight * 2);
			}
		}
	}

	// Allows targets to be removed on the player side and
	// have their changes visible here.
	public void removeTarget() {
		target = null;
	}
}