package com.semdog.ultranaut.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.semdog.ultranaut.UltranautGame;
import com.semdog.ultranaut.meta.UltranautColors;
import com.semdog.ultranaut.ui.Button;
import com.semdog.ultranaut.universe.Universe;

/**
 * This is the Play State. The main course.
 * 
 * It controls the Universe (!), the Pause Menu
 * & its associated buttons and the Tutorial Manager.
 * 
 * @author Sam
 */

public class PlayState extends ScreenAdapter {
	@SuppressWarnings("unused")
	private UltranautGame game;

	private Universe universe;

	private SpriteBatch pauseMenuBatch;
	private Texture pauseBackground;
	private BitmapFont pauseFont;
	private Button resume, mainMenu, exit;

	private boolean paused = false;

	public PlayState(UltranautGame game) {
		this.game = game;

		universe = new Universe();

		TutorialManager.init();

		pauseFont = new BitmapFont(Gdx.files.internal("assets/fonts/mohave64_BA.fnt"));

		resume = new Button(0, UltranautGame.HEIGHT / 2, 250, 32.5f, true, false, false, "Resume", 0, UltranautColors.GREEN, () -> {
			paused = false;
		});

		mainMenu = new Button(0, UltranautGame.HEIGHT / 2 - 32.5f, 250, 32.5f, true, false, false, "Main Menu", 0, UltranautColors.GREEN, () -> {
			game.setState(UltranautGame.MENUSTATE);
		});

		exit = new Button(0, UltranautGame.HEIGHT / 2 - 75, 250, 32.5f, true, false, false, "Exit", 0, UltranautColors.YELLOW, () -> {
			Gdx.app.exit();
		});

		//	This creates a background for the pause menu
		Pixmap p = new Pixmap(1, 1, Format.RGBA8888);
		p.setColor(UltranautColors.NAVY.r, UltranautColors.NAVY.g, UltranautColors.NAVY.b, 0.8f);
		p.drawPixel(0, 0);
		pauseBackground = new Texture(p);
		
		pauseMenuBatch = new SpriteBatch();
	}
	
	@Override
	public void show() {
		super.show();

		UltranautGame.loading = false;
	}

	@Override
	public void render(float delta) {
		update(delta);

		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
		Gdx.gl20.glClearColor(0.0f, 0.0f, 0.1f, 1.0f);

		universe.render();

		
		if (TutorialManager.isShowing())
			TutorialManager.render();

		if (paused) {
			pauseMenuBatch.begin();
			pauseMenuBatch.draw(pauseBackground, 0, UltranautGame.HEIGHT / 2 - 200, UltranautGame.WIDTH, 400);
			pauseFont.draw(pauseMenuBatch, "Stasis", UltranautGame.WIDTH / 2 - new GlyphLayout(pauseFont, "Stasis").width / 2, UltranautGame.HEIGHT / 2 + 150);
			resume.draw(pauseMenuBatch);
			mainMenu.draw(pauseMenuBatch);
			exit.draw(pauseMenuBatch);
			pauseMenuBatch.end();
		}
	}

	public void update(float dt) {
		//	Handles pause menu logic
		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE))
			paused = !paused;

		//	If the pause menu isn't showing, make everything else tick
		if (!paused) {
			if (!TutorialManager.isShowing())
				universe.update(dt);
			else
				TutorialManager.update(dt);
		} else {
			resume.update(dt);
			mainMenu.update(dt);
			exit.update(dt);
		}
	}
}
