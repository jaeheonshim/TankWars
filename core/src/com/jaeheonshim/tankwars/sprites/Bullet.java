package com.jaeheonshim.tankwars.sprites;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.jaeheonshim.tankwars.TankGame;
import com.jaeheonshim.tankwars.screens.PlayScreen;

import java.util.ArrayList;
import java.util.List;

public class Bullet {
    public static float BULLET_SPEED = 320;
    public static float BULLET_LIFE_TIME = 2;
    public static float BULLET_DAMAGE = 10;

    private Vector2 position;
    private float angle;

    private Polygon bounds;

    Texture bulletTexture;
    Animation<TextureRegion> collisionAnimation;
    private float collisionAnimationST;

    private float timer;
    private Tank firedTank;
    private boolean collided;

    public boolean dispose = false;

    Body body;
    World world;

    private static List<Bullet> instances = new ArrayList();

    public Bullet(Vector2 position, float angle, World world, Tank firedTank) {
        this.position = position;
        this.angle = angle;
        this.firedTank = firedTank;
        collided = false;

        bulletTexture = new Texture("bullet.png");
        timer = 0;

        bounds = new Polygon(new float[]{position.x, position.y, position.x, position.y + bulletTexture.getHeight(), position.x + bulletTexture.getWidth(), position.y + bulletTexture.getHeight(), position.x + bulletTexture.getWidth(), position.y});
        bounds.setOrigin(position.x + bulletTexture.getWidth() / 2, position.y + bulletTexture.getHeight() / 2);
        bounds.setRotation(angle);
        this.world = world;
        definePhysics();

        instances.add(this);
        initAnimations();
    }

    public void initAnimations() {
        Texture explosionSheet = new Texture("explosion_strip10.png");
        TextureRegion[][] frames = TextureRegion.split(explosionSheet, explosionSheet.getWidth() / 10, explosionSheet.getHeight());
        TextureRegion[] explosionFrames = new TextureRegion[10];
        int index = 0;
        for (int i = 0; i < frames.length; i++) {
            for (int j = 0; j < frames[i].length; j++) {
                explosionFrames[index++] = frames[i][j];
            }
        }

        collisionAnimation = new Animation<>(0.05f, explosionFrames);
    }

    private void definePhysics() {
        BodyDef bodydef = new BodyDef();
        bodydef.position.set(position);
        bodydef.type = BodyDef.BodyType.KinematicBody;
        body = world.createBody(bodydef);

        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(bulletTexture.getWidth() / 2, bulletTexture.getHeight() / 2);
        fixtureDef.shape = shape;

        body.createFixture(fixtureDef).setUserData(this);
        shape.dispose();
    }

    public void update(float dt) {
        if(timer > BULLET_LIFE_TIME) {
            this.dispose();
            dispose = true;
        }
        if (!collided) {
            timer += dt;
            this.position.x += MathUtils.cosDeg(angle) * BULLET_SPEED * dt;
            this.position.y += MathUtils.sinDeg(angle) * BULLET_SPEED * dt;

            body.setTransform(this.position.x + bulletTexture.getWidth() / 2, this.position.y + bulletTexture.getHeight() / 2, angle);
        } else {
            collisionAnimationST += dt;
            if(collisionAnimation.isAnimationFinished(collisionAnimationST)) {
                dispose = true;
                this.dispose();
            }
        }
    }

    public void render(SpriteBatch batch) {
        batch.begin();
        if(!collided) {
            batch.draw(getTexture(), body.getPosition().x, body.getPosition().y, getTexture().getRegionWidth() / 2, getTexture().getRegionHeight() / 2, getTexture().getRegionWidth(), getTexture().getRegionHeight(), 1, 1, angle);
        } else {
            batch.draw(getTexture(), body.getPosition().x - getTexture().getRegionWidth() / 2, body.getPosition().y - getTexture().getRegionHeight() / 2, getTexture().getRegionWidth() / 2, getTexture().getRegionHeight() / 2, getTexture().getRegionWidth(), getTexture().getRegionHeight(), 1, 1, angle);
        }
        batch.end();
    }

    public TextureRegion getTexture() {
        if(!collided) {
            return new TextureRegion(bulletTexture);
        } else {
            return collisionAnimation.getKeyFrame(collisionAnimationST, false);
        }
    }

    public float getAngle() {
        return angle;
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getTimer() {
        return timer;
    }

    public void handleCollisionWithTank() {
        collisionAnimationST = 0f;
        collided = true;
    }

    public Tank getFiredTank() {
        return firedTank;
    }

    public void dispose() {
        bulletTexture.dispose();
    }

    public boolean isCollided() {
        return collided;
    }

    public static List<Bullet> getInstances() {
        return instances;
    }

    public Body getBody() {
        return body;
    }
}
