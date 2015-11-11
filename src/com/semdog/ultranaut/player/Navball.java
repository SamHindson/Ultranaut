package com.semdog.ultranaut.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * A ToolboxContent extender which displays various information
 * about the player's whereabouts and movement. More specifically,
 * it shows in which direction the player is travelling and how
 * quickly they're doing it.
 * 
 * @author Sam
 *
 */

public class Navball extends ToolboxContent {
	
	private float verticalSpeed;
	private Vector2 velocity;
	private float altitude;
	
	private float velocityAngle, playerAngle;
	
	private Texture navball, navman;

	public Navball(Toolbox parent) {
		super(parent);
		
		navball = new Texture(Gdx.files.internal("assets/graphics/gizmos/navball.png"));		
		navman = new Texture(Gdx.files.internal("assets/graphics/gizmos/navman.png"));
	}

	@Override
	public void update(float dt) {
		
	}
	
	

	@Override
	public void updateContent(Object o) {
		Player player = (Player) o;
		verticalSpeed = new Vector2(
				player.getVelocity().x * MathUtils.cos(player.getAngleAroundPlanet()), 
				player.getVelocity().y * MathUtils.sin(player.getAngleAroundPlanet())).len();
		
		velocity = player.getVelocity();
		
		altitude = player.getAltitude();
		
		velocityAngle = MathUtils.atan2(player.getVelocity().y, player.getVelocity().x) * MathUtils.radiansToDegrees - 90;
		playerAngle = player.getAngle() * MathUtils.radiansToDegrees;
	}

	@Override
	public void draw(SpriteBatch hudBatch, BitmapFont font) {
		float textHeight = font.getCapHeight();
		font.draw(hudBatch, "Vertical Speed: " + Toolbox.unitFormat.format(verticalSpeed) + "m/s", parent.getPanelOX() + 5, parent.getPanelOY() + 5 + textHeight * 1);
		font.draw(hudBatch, "Velocity: " + velocity.len(), parent.getPanelOX() + 5, parent.getPanelOY() + 5 + textHeight * 2);
		font.draw(hudBatch, "Altitude: " + Toolbox.unitFormat.format(altitude) + "m", parent.getPanelOX() + 5, parent.getPanelOY() + 5 + textHeight * 3);
		
		float nx = parent.getPanelOX() + 50;
		float ny = parent.getPanelOY() + 80;
		
		hudBatch.setColor(Color.BLACK);
		hudBatch.draw(navball, nx, ny, 75, 75, 150, 150, 1, 1, velocityAngle, 0, 0, 200, 200, false, false);
		hudBatch.draw(navman, nx + 40, ny + 40, 40, 40, 80, 80, 1, 1, playerAngle, 0, 0, 200, 200, false, false);
	}

}
