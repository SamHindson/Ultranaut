package com.semdog.ultranaut;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.semdog.ultranaut.states.AboutState;
import com.semdog.ultranaut.states.MenuState;
import com.semdog.ultranaut.states.ObjectTestState;
import com.semdog.ultranaut.states.PlayState;
import com.semdog.ultranaut.states.SettingsState;

public class UltranautGame extends Game {

	public static int WIDTH = 1280;
	public static int HEIGHT = 720;

	public static final int MENUSTATE = 0, PLAYSTATE = 1, SETTINGSSTATE = 2, ABOUTSTATE = 3;

	private Screen screen;

	public static boolean debug = true;
	public static boolean loading = true;

	public void create() {
		screen = new ObjectTestState(this);
		setScreen(screen);
	}
	
	@Override
	public void render() {
		super.render();
	}

	public void setState(int state) {
		screen.dispose();
		screen = null;
		
		Gdx.app.log("UltranautGame", "Changing my screen!");
		
		loading = true;
		
		switch (state) {
		case MENUSTATE:
			screen = new MenuState(this);
			setScreen(screen);
			break;
		case PLAYSTATE:
			screen = new PlayState(this);
			setScreen(screen);
			break;
		case SETTINGSSTATE:	
			screen = new SettingsState(this);
			setScreen(screen);
			break;
		case ABOUTSTATE:
			screen = new AboutState(this);
			setScreen(screen);
			break;
		default:
			break;
		}
		
		Gdx.app.log("UltranautGame", "Sweet. Got his far");
	}
}