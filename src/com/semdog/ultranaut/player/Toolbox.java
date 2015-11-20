package com.semdog.ultranaut.player;

import java.text.DecimalFormat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.semdog.ultranaut.UltranautGame;

/**
 * This is a class whose object is seen ingame by the little
 * gray boxes in the bottom corners. It is basically hideable UI.
 * 
 * @author Sam
 */

public class Toolbox {
	
	public static DecimalFormat unitFormat = new DecimalFormat("0.##");

	private ToolboxContent content;
	private BitmapFont font;

	private boolean movingIn, movingOut;
	private boolean deployed;
	
	private float deployedPercent;

	private Rectangle deployButton;
	private boolean deployButtonHovered;
	
	private float x, y;
	
	private static Texture texture;
	
	static {
		Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		pixmap.drawPixel(0, 0);
		texture = new Texture(pixmap);
	}

	public Toolbox(Player owner, int position) {
		deployed = false;

		x = 0;
		
		if(position == 0)
			x = 5;
		else {
			x = UltranautGame.WIDTH - 245;
			Gdx.app.log("Toolbox", "Toolbox made on far side. X: " + x);
		}
		
		deployButton = new Rectangle(x, 0, 100, 25);
		
		font = new BitmapFont(Gdx.files.internal("assets/fonts/mohave20_BA.fnt"));
	}

	public void update(float dt) {
		if(movingIn) {
			deployedPercent += (100 - deployedPercent) / 2.f;
			
			y = 250 * (deployedPercent / 100.f);
			deployButton.setPosition(x, y);
			
			if(deployedPercent > 99) {
				movingIn = false;
				deployed = true;
				y = 250;
			}
		} else if(movingOut) {
			deployedPercent += (-5-deployedPercent) / 2.f;
			
			y = 250 * (deployedPercent / 100.f);
			deployButton.setPosition(x, y);
			
			if(deployedPercent < 1) {
				movingOut = false;
				deployed = false;
				y = -5;
			}
		} else {
			deployButtonHovered = deployButton.contains(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
			
			if(deployButtonHovered && Gdx.input.isButtonPressed(Buttons.LEFT)) {
				if(deployed) {
					movingOut = true;
				} else {
					movingIn = true;
				}
			}
		}	
	}
	
	public void draw(SpriteBatch hudBatch) {
		hudBatch.setColor(deployButtonHovered ? new Color(0.1f, 0.1f, 1.0f, 1.f) : new Color(0.3f, 0.3f, 0.3f, 1.f));
		hudBatch.draw(texture, deployButton.x, deployButton.y, deployButton.width, deployButton.height);
		hudBatch.setColor(new Color(0.3f, 0.3f, 0.3f, 0.8f));
		hudBatch.draw(texture, x - 5, deployButton.y - 250, deployButton.width + 150, 250);
		
		content.draw(hudBatch, font);
	}
	
	public float getPanelOX() {
		return x;
	}
	
	public float getPanelOY() {
		return y - 250;
	}

	public void setContent(ToolboxContent content) {
		this.content = content;
	}
}
