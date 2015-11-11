package com.semdog.ultranaut.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.semdog.ultranaut.UltranautGame;
import com.semdog.ultranaut.meta.UltranautColors;
import com.semdog.ultranaut.ui.Button;
import com.semdog.ultranaut.ui.Title;

/**
 * This is the About State.
 * It shows some information about the game and allows users to report bugs
 * @author Sam
 *
 */

public class AboutState extends ScreenAdapter {
	@SuppressWarnings("unused")
	private UltranautGame game;
	
	private Title title;
	private BitmapFont textFont;
	private Button backButton, bugButton;
	private SpriteBatch batch;
	private GlyphLayout glyphs;
	private String[] lines;
	
	public AboutState(UltranautGame game) {
		this.game = game;
		
		title = new Title(0, UltranautGame.HEIGHT * 0.8f, true, false, "About", false, 3);
		textFont = new BitmapFont(Gdx.files.internal("assets/fonts/mohave32_BA.fnt"));
		
		bugButton = new Button(0, UltranautGame.HEIGHT * 0.2f, 250, 75, true, false, false, "Report a Bug", 0, UltranautColors.RED, () -> {
			reportBug();
		});
		
		backButton = new Button(0, UltranautGame.HEIGHT * 0.2f - 75, 250, 75, true, false, false, "Return", 0, UltranautColors.BLUE, () -> {
			game.setState(UltranautGame.MENUSTATE);
		});
		
		batch = new SpriteBatch();
		
		glyphs = new GlyphLayout();
		
		lines = new String[] {
			"Ultranaut v0.0.1",
			"Copyright Flaming Trouser Studios 2015",
			"Developed by Sam Hindson",
			"",
			"Uses font Mohave under fair noncommercial conditions."
		};
		
		UltranautGame.loading = false;
	}
	
	@Override
	public void render(float delta) {
		super.render(delta);
		update(delta);
		
		batch.begin();
		title.draw(batch);
		
		float fontHeight = textFont.getCapHeight();
		for(int p = 0; p < lines.length; p++) {
			glyphs.setText(textFont, lines[p]);
			textFont.draw(batch, lines[p], UltranautGame.WIDTH / 2 - glyphs.width / 2, UltranautGame.HEIGHT * 0.6f - p * fontHeight);
		}
		
		bugButton.draw(batch);
		backButton.draw(batch);
		
		batch.end();
	}
	
	private void update(float dt) {
		bugButton.update(dt);
		backButton.update(dt);
	}

	private void reportBug() {
		
	}

	public void show() {
		super.show();
	}
	
	@Override
	public void dispose() {
		super.dispose();
		backButton.dispose();
		bugButton.dispose();
		textFont.dispose();
	}
}
