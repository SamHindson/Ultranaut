package com.semdog.ultranaut.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.semdog.ultranaut.UltranautGame;
import com.semdog.ultranaut.ui.Button;
import com.semdog.ultranaut.ui.Title;

/**
 * This is the Menu State.
 * It is supposed to be the main entry point to the game,
 * however there is a bug during Play State initialization.
 * Thus, the menu is only accessible through the Play
 * State's pause menu.
 * 
 * @author Sam
 */

public class MenuState extends ScreenAdapter {
	@SuppressWarnings("unused")
	private UltranautGame game;

	private Title title;
	private Button playButton, settingsButton, aboutButton, exitButton;

	private SpriteBatch batch;

	public MenuState(final UltranautGame game) {
		this.game = game;

		title = new Title(0, UltranautGame.HEIGHT * 0.8f, true, false, "ULTRANAUT", false, 3);

		playButton = new Button(0, 360, 250, 40, true, false, false, "Play", 0, Color.CYAN, () -> {
			game.setState(UltranautGame.PLAYSTATE);
		});

		settingsButton = new Button(0, 320, 250, 40, true, false, false, "Settings", 0, Color.CYAN, () -> {
			game.setState(UltranautGame.SETTINGSSTATE);
		});

		aboutButton = new Button(0, 280, 250, 40, true, false, false, "About", 0, Color.CYAN, () -> {
			game.setState(UltranautGame.ABOUTSTATE);
		});

		exitButton = new Button(0, 240, 250, 40, true, false, false, "Exit", 0, Color.RED, () -> {
			Gdx.app.exit();
		});
	}

	@Override
	public void show() {
		super.show();
		batch = new SpriteBatch();

		UltranautGame.loading = false;
	}

	public void update(float dt) {
		playButton.update(dt);
		settingsButton.update(dt);
		aboutButton.update(dt);
		exitButton.update(dt);
	}

	@Override
	public void render(float delta) {
		super.render(delta);
		update(delta);

		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
		Gdx.gl20.glClearColor(0f, 0.0f, 0.05f, 1.0f);

		batch.begin();
		title.draw(batch);
		playButton.draw(batch);
		settingsButton.draw(batch);
		aboutButton.draw(batch);
		exitButton.draw(batch);
		batch.end();
	}

	@Override
	public void dispose() {
		super.dispose();

		batch.dispose();
		playButton.dispose();
		settingsButton.dispose();
		aboutButton.dispose();
		title.dispose();
		exitButton.dispose();
	}
}
