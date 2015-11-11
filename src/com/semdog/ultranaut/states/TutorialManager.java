package com.semdog.ultranaut.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.semdog.ultranaut.UltranautGame;
import com.semdog.ultranaut.meta.UltranautColors;
import com.semdog.ultranaut.ui.Button;

/**
 * This class manages the ingame tutorials.
 * 
 * It reads all the tutorial text from a file in the assets folder
 * called 'tutorial.txt', splits it up into sections, puts these
 * sections in an array and displays the sections when needed.
 * 
 * The triggering system is somewhat rudimentary; it triggers a
 * tutorial section when a number is received (that being the number
 * of the desired tutorial piece.) However, if section x is requested to
 * be displayed, the manager will only show the section if the current 
 * section is number (x - 1). 
 * 
 * 
 * @author Sam
 */

public class TutorialManager {
	private static int currentTooltip = -1;
	private static Array<String[]> tips;

	private static boolean hidden = false;
	private static boolean showing;

	private static Button okButton, hideButton;

	private static Texture background;

	private static SpriteBatch batch;
	private static BitmapFont font;

	public static void init() {
		tips = new Array<String[]>();

		FileHandle tutorialTextFile = Gdx.files.internal("assets/tutorial.txt");
		String total = tutorialTextFile.readString();
		
		//	This splits the read file into sections, separated by "@"s
		String[] split = total.split("@");
		
		//	This loop finds the various lines of the section.
		for (int q = 0; q < split.length - 1; q += 2) {
			String[] lines = split[q + 2].split(">");
			tips.add(lines);
		}

		okButton = new Button(UltranautGame.WIDTH / 2 - 130, UltranautGame.HEIGHT * 0.3f, 250, 100, false, false, false, "Ok", 0, UltranautColors.BLUE, () -> {
			dismissNotification();
		});

		hideButton = new Button(UltranautGame.WIDTH / 2 + 130, UltranautGame.HEIGHT * 0.3f, 250, 100, false, false, false, "Hide", 0, UltranautColors.RED, () -> {
			hidden = true;
			dismissNotification();
		});

		Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
		pixmap.setColor(0, 0, 0, 0.6f);
		pixmap.drawPixel(0, 0);

		background = new Texture(pixmap);

		font = new BitmapFont(Gdx.files.internal("assets/fonts/mohave32_BA.fnt"));
		batch = new SpriteBatch();
	}

	/**
	 * This metod is triggered when the "ok" button is pressed.
	 * The showTip() methods here are the tip numbers that
	 * are shown immediately after another tip is displayed 
	 * (i.e. after tip 0 is dismissed, tip 1 is triggered.)
	 */
	private static void dismissNotification() {
		showing = false;
		if(showTip(1))
			return;
		if(showTip(7))
			return;
		if(showTip(9))
			return;
		if(showTip(14))
			return;
		if(showTip(17))
			return;
		if(showTip(21))
			return;
		if(showTip(22))
			return;
		if(showTip(23))
			return;
		if(showTip(24))
			return;
	}

	/**
	 * This method displays a tip with id x if the
	 * current tip has an id of (x - 1)
	 * @param id - the id of the desired tip
	 * @return returns true if the tip is shown
	 */
	public static boolean showTip(int id) {
		if (currentTooltip + 1 == id && !hidden) {
			showing = true;
			currentTooltip++;
			return true;
		}
		return false;
	}

	public static void update(float dt) {
		if (showing) {
			okButton.update(dt);
			hideButton.update(dt);
		}
	}

	public static void render() {
		batch.begin();
		batch.draw(background, 0, 0, UltranautGame.WIDTH, UltranautGame.HEIGHT);
		okButton.draw(batch);
		hideButton.draw(batch);

		float x = UltranautGame.WIDTH * 0.5f;
		float y = UltranautGame.HEIGHT * 0.8f;
		float height = font.getCapHeight();
		
		//	This draws each line of the current tip
		String[] lines = tips.get(currentTooltip);
		for (int e = 0; e < lines.length; e++) {
			GlyphLayout layout = new GlyphLayout(font, lines[e]);
			font.draw(batch, lines[e], x - layout.width / 2, y - height * e - e * 2);
		}
		batch.end();

	}

	public static boolean isShowing() {
		return showing;
	}
}
