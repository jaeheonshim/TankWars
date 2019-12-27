package com.jaeheonshim.tankwars.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.jaeheonshim.tankwars.TankGame;

public class CreditScreen implements Screen {
    SpriteBatch batch;
    Viewport viewport;

    TankGame game;

    Texture animationTexture;
    Animation<TextureRegion> creditAnimation;

    float stateTime;
    float scaleFactor = 0.8f;

    public CreditScreen(SpriteBatch batch, TankGame game) {
        this.batch = batch;
        viewport = new FitViewport(500, 300);

        this.game = game;

        animationTexture = new Texture("credits.png");
        TextureRegion[] creditAnimationFrames = new TextureRegion[180];

        int frameHeight = animationTexture.getHeight() / 36;
        int frameWidth = animationTexture.getWidth() / 5;

        int index = 0;
        for (int j = 0; j < 36; j++) {
            for (int i = 0; i < 5; i++) {
                creditAnimationFrames[index] = new TextureRegion(animationTexture, i * frameWidth, j * frameHeight, frameWidth, frameHeight);
                index++;
            }
        }

        creditAnimation = new Animation<>(0.02f, creditAnimationFrames);
        stateTime = 0f;
    }
    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stateTime += delta;
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        TextureRegion animationFrame = creditAnimation.getKeyFrame(stateTime);
        batch.draw(animationFrame, viewport.getWorldWidth() / 2 - (animationFrame.getRegionWidth() * scaleFactor) / 2, 150, animationFrame.getRegionWidth() * scaleFactor, animationFrame.getRegionHeight() * scaleFactor);
        batch.end();

        if(creditAnimation.isAnimationFinished(stateTime)) {
            game.showPlayScreen();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
