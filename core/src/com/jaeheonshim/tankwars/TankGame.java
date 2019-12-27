package com.jaeheonshim.tankwars;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.jaeheonshim.tankwars.screens.CreditScreen;
import com.jaeheonshim.tankwars.screens.PlayScreen;

public class TankGame extends Game {
	public static final int V_WIDTH = 896;
	public static final int V_HEIGHT = 448;
	public static final float PPM = 45;

	public static final float WATER_DAMAGE = 20;

	public Music backgroundMusic;

	SpriteBatch batch;
	
	@Override
	public void create () {
		backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("battle_music.mp3"));
		backgroundMusic.setLooping(true);
		batch = new SpriteBatch();
		setScreen(new CreditScreen(batch, this));
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		super.render();
	}

	public void showPlayScreen() {
		backgroundMusic.setVolume(0.2f);
		backgroundMusic.play();
		setScreen(new PlayScreen(batch));
	}
	
	@Override
	public void dispose () {

	}
}
