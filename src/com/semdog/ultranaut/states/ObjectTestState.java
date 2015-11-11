package com.semdog.ultranaut.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.semdog.ultranaut.UltranautGame;

/**
 * This is the Object Test State.
 * Unaccessible by the user, it proves helpful when testing newly
 * coded entities in a controlled environment.
 * 
 * It is currently in a bare bones state as previous tests
 * were cleaned out before handing in.
 * 
 * It contains simple things such as a physics simulation
 * world, a shape renderer and a camera.
 * 
 * @author Sam
 */

public class ObjectTestState extends ScreenAdapter {
	@SuppressWarnings("unused")
	private UltranautGame game;

	private World physicsWorld;
	private OrthographicCamera camera;
	private Box2DDebugRenderer renderer;
	private ShapeRenderer shapeRenderer;
	public ObjectTestState(UltranautGame game) {
		this.game = game;

		physicsWorld = new World(new Vector2(0, 0), true);
		camera = new OrthographicCamera(12.8f * 5, 7.2f * 5);
		camera.position.set(0, 0, 0);
		camera.zoom = 4;
		renderer = new Box2DDebugRenderer();

		shapeRenderer = new ShapeRenderer();
		
		shapeRenderer = new ShapeRenderer();
	}

	@Override
	public void show() {
		super.show();
		
		UltranautGame.loading = false;
	}
	
	@Override
	public void render(float delta) {
		super.render(delta);

		update(delta);

		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
		Gdx.gl20.glClearColor(0.2f, 0.0f, 0.0f, 1.0f);

		renderer.render(physicsWorld, camera.combined);
		
		shapeRenderer.begin(ShapeType.Filled);
		shapeRenderer.end();
	}

	public void update(float dt) {
		camera.update();

		physicsWorld.step(1 / 60.f, 6, 2);
		shapeRenderer.setProjectionMatrix(camera.combined);
	}

}
