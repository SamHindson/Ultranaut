package com.semdog.ultranaut.player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.semdog.ultranaut.meta.UltranautColors;
import com.semdog.ultranaut.universe.CelestialBody;
import com.semdog.ultranaut.universe.Environment;
import com.semdog.ultranaut.universe.Planet;
import com.semdog.ultranaut.universe.Star;
import com.semdog.ultranaut.vehicles.Odyssey;

/**
 * This is the toolbox that shows various information about the player's 
 * current environment.
 * 
 * @author Sam
 */

public class ShipToolbox extends ToolboxContent {

	private float x, y;
	private String text;

	public ShipToolbox(Toolbox parent) {
		super(parent);
		x = parent.getPanelOX() + 5;
	}

	public void update(float dt) {
		y = parent.getPanelOY();
	}

	public void updateContent(Object o) {
		Player player = (Player) o;
		Environment environment = player.getEnvironment();

		text = "";

		if (player.isControllingShip()) {
			if (player.getShip() instanceof Odyssey) {

				if (environment instanceof Planet) {

					if (player.isOnSurface()) {
						if (((CelestialBody) environment).getMineralPercent("HYDROCARBONS") > 0) {
							text += "disengage refuelling>";
							text += "Press R to engage/>";
							text += "#GCan refuel!>";
							text += ">";
						}
					}

					for (int t = 0; t < 4; t++) {
						text += ((CelestialBody) environment).getMineral(t).getType() + " - " + ((CelestialBody) environment).getMineral(t).getPercent() + "%>";
					}

					text += "#RSURFACE COMPOSITION:>";
					text += "#B" + environment.getName();

				} else if (environment instanceof Star) {

				}
			}
		}
	}

	@Override
	public void draw(SpriteBatch hudBatch, BitmapFont font) {
		String[] lines = text.split(">");
		int lineCount = lines.length;
		float lineHeight = font.getCapHeight();

		float yOffset = (250 - lineCount * lineHeight) / 2;

		for (int i = 0; i < lineCount; i++) {
			String line = lines[i].split(">")[0];

			if (line.contains("#B")) {
				font.setColor(UltranautColors.BLUE);
				line = line.split("#B")[1];
			} else if (line.contains("#G")) {
				font.setColor(UltranautColors.GREEN);
				line = line.split("#G")[1];
			} else if (line.contains("#R")) {
				font.setColor(UltranautColors.RED);
				line = line.split("#R")[1];
			} else {
				font.setColor(Color.WHITE);
			}

			font.draw(hudBatch, line, x, y + lineHeight * (i + 1) + yOffset);
		}
	}

}
