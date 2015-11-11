package com.semdog.ultranaut.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.semdog.ultranaut.UltranautGame;

/**
 * This is the class of the Button, a clickable UI element.
 * It has a position, text, color and click action which is
 * executed when it is clicked.
 * 
 * @author Sam
 */

public class Button {

	private float x, y, width, height;
	private boolean drawBounds;
	private String text;

	private GlyphLayout glyphs;
	private BitmapFont font;

	private Color color;

	private Event clickEvent;

	private boolean hovered, held;

	private static Texture background;

	//	A static method to create the background which is used by all buttons.
	static {
		Pixmap pixmap = new Pixmap(1, 1, Format.RGB888);
		pixmap.setColor(Color.WHITE);
		pixmap.drawPixel(0, 0);

		background = new Texture(pixmap);
	}

	public Button(float x, float y, float width, float height, boolean cx, boolean cy, boolean drawBounds, String text, int size, Color color, Event clickEvent) {
		this.text = text;

		switch (size) {
		case 0:
			this.font = new BitmapFont(Gdx.files.internal("assets/fonts/mohave32_BA.fnt"));
			break;
		case 1:
			this.font = new BitmapFont(Gdx.files.internal("assets/fonts/mohave64_BA.fnt"));
			break;
		default:
			this.font = new BitmapFont(Gdx.files.internal("assets/fonts/mohave32_BA.fnt"));
			break;
		}

		//	A GlyphLayout is created to keep track of the width of the text
		glyphs = new GlyphLayout();
		glyphs.setText(font, text);

		if (cx) {
			this.x = UltranautGame.WIDTH / 2;
			this.y = y;
		} else if (cy) {
			this.x = x;
			this.y = UltranautGame.HEIGHT / 2;
		} else {
			this.x = x;
			this.y = y;
		}

		this.width = width;
		this.height = height;

		this.drawBounds = drawBounds;

		this.color = color;

		this.clickEvent = clickEvent;
	}

	public void draw(SpriteBatch batch) {
		if (drawBounds) {
			batch.draw(background, x - width / 2, y - height / 2, width, height);
			batch.setColor(Color.WHITE);

			if (!hovered)
				batch.setColor(color);
		} else {
			if (hovered)
				font.setColor(color);
			else
				font.setColor(Color.WHITE);
		}

		font.draw(batch, text, x - glyphs.width / 2, y + glyphs.height / 2);
	}
	
	public void setText(String text) {
		this.text = text;
		glyphs.setText(font, text);
	}

	public void update(float dt) {
		hovered = Gdx.input.getX() > x - width / 2 
				&& Gdx.input.getX() < x + width / 2 
				&& UltranautGame.HEIGHT - Gdx.input.getY() > y - height / 2 
				&& UltranautGame.HEIGHT - Gdx.input.getY() < y + height / 2;

	
		if(Gdx.input.isButtonPressed(Buttons.LEFT) && hovered)
			held = true;
		
		if(held && !Gdx.input.isButtonPressed(Buttons.LEFT)) {
			clickEvent.execute();
			held = false;
			hovered = false;
		}
	}

	public void dispose() {
		font.dispose();
	}
}