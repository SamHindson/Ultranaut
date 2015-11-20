package com.semdog.ultranaut;

import java.awt.Dimension;
import java.awt.Toolkit;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.semdog.ultranaut.meta.PreferenceManager;

/**
 * This is the main entry point for the Program.
 * 
 * @author Sam
 */

public class Loader {

	static LwjglApplicationConfiguration configuration;
	static LwjglApplication game;

	public static void main(String[] args) {
		PreferenceManager.init();

		configuration = new LwjglApplicationConfiguration();

		if (PreferenceManager.currentPref.isFullscreen()) {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			UltranautGame.WIDTH = screenSize.width;
			UltranautGame.HEIGHT = screenSize.height;
			configuration.fullscreen = true;
		}
		configuration.width = UltranautGame.WIDTH;
		configuration.height = UltranautGame.HEIGHT;

		configuration.title = "Utranaut";
		configuration.resizable = false;
		configuration.samples = 4;
		
		configuration.vSyncEnabled = false;
		configuration.foregroundFPS = 120;

		game = new LwjglApplication(new UltranautGame(args[0]), configuration);
	}
}