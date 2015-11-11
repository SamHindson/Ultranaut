package com.semdog.ultranaut.player;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class ToolboxContent {
	protected Toolbox parent;
	
	public ToolboxContent(Toolbox parent) {
		this.parent = parent;
	}
	
	public abstract void update(float dt);
	public abstract void updateContent(Object o);
	public abstract void draw(SpriteBatch hudBatch, BitmapFont font);
}
