package com.semdog.ultranaut.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.semdog.ultranaut.UltranautGame;

/**
 * This UI element is used to have Text display
 * at a fixed position without enclosing classes 
 * having to work out where to position text or
 * have a BitmapFont.
 * 
 * @author Sam
 */

public class Title {
	private float ox, oy, x, y;
	private String text;
	private boolean cx, cy;
	
	private GlyphLayout glyphs;
	private BitmapFont font;
	
	public Title(float x, float y, boolean cx, boolean cy, String text, boolean italic, int size) {
		this.cx = cx;
		this.cy = cy;
		
		ox = x;
		oy = y;

		//	Loads a font based on the text size
		switch (size) {
		case 0:
			this.font = new BitmapFont(Gdx.files.internal("assets/fonts/mohave20_BA.fnt"));			
			break;
		case 1:
			this.font = new BitmapFont(Gdx.files.internal("assets/fonts/mohave32_BA.fnt"));			
			break;
		case 2:
			this.font = new BitmapFont(Gdx.files.internal("assets/fonts/mohave64_BA.fnt"));						
			break;
		case 3:
			this.font = new BitmapFont(Gdx.files.internal("assets/fonts/mohave90_BA.fnt"));						
			break;
		default:			
			this.font = new BitmapFont(Gdx.files.internal("assets/fonts/mohave32_BA.fnt"));
			break;
		}

		glyphs = new GlyphLayout();
		setText(text);
	}
	
	public void setText(String text) {
		this.text = text;
		glyphs.setText(font, text);

		//	Works out where to put the text if it is meant to be centered
		if(cx) {
			this.x = UltranautGame.WIDTH / 2 - glyphs.width / 2;
			this.y = oy + glyphs.height / 2;
		} else if(cy) {
			this.x = ox + glyphs.width / 2;
			this.y = UltranautGame.HEIGHT / 2 - glyphs.height / 2;
		} else {			
			this.x = ox + glyphs.width / 2;
			this.y = oy + glyphs.height / 2;
		}
	}
	
	public void draw(SpriteBatch batch) {
		font.draw(batch, text, x, y);
	}
	
	public void dispose() {
		font.dispose();
	}
}
