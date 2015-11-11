package com.semdog.ultranaut.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.semdog.ultranaut.UltranautGame;
import com.semdog.ultranaut.meta.PreferenceManager;
import com.semdog.ultranaut.meta.UltranautColors;
import com.semdog.ultranaut.ui.Button;
import com.semdog.ultranaut.ui.Title;

/**
 * This is the settings state.
 * 
 * It allows the user to edit the game's Graphical, Audio (not implemented)
 * and Gameplay Settings (not implemented.)
 * 
 * It loads the settings from the PreferenceManager and saves them when
 * exiting.
 * 
 * @author Sam
 */

public class SettingsState extends ScreenAdapter {
	private UltranautGame game;
	
	private BitmapFont littleFont;
	
	private Button fullScreenToggle, antiAliasingToggle;
	private Button backButton;
	
	private boolean fullScreen, antiAliasing;
	
	private SpriteBatch batch;
	
	private Title title;
	
	public SettingsState(UltranautGame game) {
		this.game = game;
		
		littleFont = new BitmapFont(Gdx.files.internal("assets/fonts/mohave32_BA.fnt"));
		
		float fontHeight = littleFont.getCapHeight();

		
		//	This works out where to put the buttons
		float fullScreenX = UltranautGame.WIDTH / 2 - (new GlyphLayout(littleFont, "Fullscreen Mode").width) / 2 - 50;
		float fullScreenY = UltranautGame.HEIGHT / 2 + fontHeight / 2;
		float antiX = UltranautGame.WIDTH / 2 - (new GlyphLayout(littleFont, "Antialiasing").width + 100) / 2 - 50;
		float antiY = UltranautGame.HEIGHT / 2 - fontHeight / 2 - fontHeight * 2;
		
		//	Loads what to put on the Buttons
		String fs = PreferenceManager.currentPref.isFullscreen() ? "On" : "Off";
		String aa = PreferenceManager.currentPref.isAntialiasing() ? "On" : "Off";
		
		//	Generates the Buttons
		fullScreenToggle = new Button(fullScreenX, fullScreenY, 100, fontHeight + 2, true, false, false, fs, 0, UltranautColors.GREEN, () -> {
			fullScreen = !fullScreen;
			fullScreenToggle.setText(fullScreen ? "On" : "Off");
			Gdx.app.log("SettingsState", "Toggled Fullscreen");
		});
		
		antiAliasingToggle = new Button(antiX, antiY, 100, fontHeight + 2, true, false, false, aa, 0, UltranautColors.GREEN, () -> {
			antiAliasing = !antiAliasing;
			antiAliasingToggle.setText(antiAliasing ? "On" : "Off");			
			Gdx.app.log("SettingsState", "Toggled Anti");
		});
		
		backButton = new Button(0, UltranautGame.HEIGHT * 0.2f, 250, 75, true, false, false, "Back", 0, UltranautColors.RED, () -> {
			saveAndExit();
		});
		
		title = new Title(0, UltranautGame.HEIGHT * 0.8f, true, false, "Settings", false, 2);
		
		Gdx.app.log("SettingsState", "Finished loading!");
	}

	@Override
	public void show() {
		super.show();
		batch = new SpriteBatch();

		UltranautGame.loading = false;
	}
	
	@Override
	public void render(float delta) {
		update(delta);
		
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
		Gdx.gl20.glClearColor(0f, 0.0f, 0.05f, 1.0f);
		
		//	Renders the UI with a SpriteBatch
		batch.begin();
		fullScreenToggle.draw(batch);
		antiAliasingToggle.draw(batch);
		backButton.draw(batch);
		
		float fontHeight = littleFont.getCapHeight();
		littleFont.draw(batch, "Fullscreen Mode", UltranautGame.WIDTH / 2 - (new GlyphLayout(littleFont, "Fullscreen Mode").width) / 2, UltranautGame.HEIGHT / 2 + fontHeight * 2);
		littleFont.draw(batch, "Antialiasing", UltranautGame.WIDTH / 2 - (new GlyphLayout(littleFont, "Antialiasing").width) / 2, UltranautGame.HEIGHT / 2 - fontHeight * 1);
		
		title.draw(batch);
		batch.end();
	}

	private void update(float delta) {
		fullScreenToggle.update(delta);
		antiAliasingToggle.update(delta);
		backButton.update(delta);
	}

	private void saveAndExit() {
		//	This sets the current preferences and saves the preference files.
		PreferenceManager.setValues(fullScreen, antiAliasing);
		game.setState(UltranautGame.MENUSTATE);
	}
}
