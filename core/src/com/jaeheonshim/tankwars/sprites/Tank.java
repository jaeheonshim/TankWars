package com.jaeheonshim.tankwars.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.jaeheonshim.tankwars.TankGame;
import com.jaeheonshim.tankwars.TankInputConfig;
import com.jaeheonshim.tankwars.screens.PlayScreen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class Tank {
    public static float TURN_AMOUNT = 90;
    public static float TANK_SPEED = 200;

    public static float GUN_OFFSET_X = 53;
    public static float GUN_OFFSET_Y = 20;

    public static float BULLET_SPREAD = 5;

    private TankInputConfig inputConfig;

    private Vector2 position;
    private float angle;
    private TankState state;
    private float health;

    private Texture tankTexture;
    private Texture tankDestroyedTexture;
    private Texture tankHPTexture;
    private Animation<TextureRegion> driveAnimation;

    private ArrayList<Bullet> firedBullets;
    private Random random;

    private float stateTime;

    private Polygon bounds;

    private World world;
    private Body body;

    private static List<Tank> instances = new ArrayList();

    public Tank(Vector2 position, World world, TankInputConfig config) {
        this.position = new Vector2(position);
        state = TankState.IDLE;

        tankTexture = new Texture("tank1.png");
        tankDestroyedTexture = new Texture("tank1damaged.png");
        tankHPTexture = new Texture("hp-mini1.png");
        initAnimations();

        firedBullets = new ArrayList<>();

        stateTime = 0f;
        health = 100;

        bounds = new Polygon(new float[] {position.x, position.y, position.x, position.y + tankTexture.getHeight(), position.x + tankTexture.getWidth(), position.y + tankTexture.getHeight(), position.x + tankTexture.getWidth(), position.y});
        bounds.setOrigin(position.x + tankTexture.getWidth() / 2, position.y + tankTexture.getHeight() / 2);
        bounds.setRotation(angle);

        this.world = world;
        definePhysics();

        instances.add(this);

        random = new Random();

        this.inputConfig = config;
    }

    public void definePhysics() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(tankTexture.getWidth() / 2, tankTexture.getHeight() / 2);

        fixtureDef.shape = shape;
        body.createFixture(fixtureDef).setUserData(this);
        shape.dispose();
    }


    public void initAnimations() {
        Texture driveSheet = new Texture("tank1_strip4.png");
        TextureRegion[][] frames = TextureRegion.split(driveSheet, driveSheet.getWidth() / 4, driveSheet.getHeight());
        TextureRegion[] driveFrames = new TextureRegion[4];
        int index = 0;
        for (int i = 0; i < frames.length; i++) {
            for (int j = 0; j < frames[i].length; j++) {
                driveFrames[index++] = frames[i][j];
            }
        }

        driveAnimation = new Animation<>(0.05f, driveFrames);
    }

    public void turn(TurnDirection direction, float amount) {
        state = TankState.DRIVING;
        if(direction == TurnDirection.LEFT) {
            angle += amount;
            angle = angle > 360 ? angle - ((int) angle) / 360 * 360 : angle;
        } else if(direction == TurnDirection.RIGHT) {
            angle -= amount;
            float tempAngle = angle < -360 ? angle + ((int) -angle) / 360 * 360 : angle;
            angle = angle < 0 ? 360 - -tempAngle : angle ;
        }
    }

    public void drive(float speed) {
        state = TankState.DRIVING;
        float xAccel = MathUtils.cosDeg(angle) * speed;
        float yAccel = MathUtils.sinDeg(angle) * speed;

        position.x += xAccel;
        position.y += yAccel;

        if(position.x < 0 || position.x > TankGame.V_WIDTH) {
            position.x -= xAccel;
        }
        if(position.y < 0 || position.y > TankGame.V_HEIGHT) {
            position.y -= yAccel;
        }
    }

    public void fire() {
        float bulletAngle = angle;
        if(random.nextBoolean()) {
            bulletAngle += random.nextInt((int) BULLET_SPREAD);
        } else {
            bulletAngle -= random.nextInt((int) BULLET_SPREAD);
        }

        Vector2 rotationAbsolute = new Vector2(position.x + tankTexture.getWidth() / 2f, position.y + tankTexture.getHeight() / 2f);
        Vector2 rotatedPoint = new Vector2();
        rotatedPoint.x = ((position.x - rotationAbsolute.x) * MathUtils.cosDeg(angle)) - ((position.y - rotationAbsolute.y) * MathUtils.sinDeg(angle)) + rotationAbsolute.x;
        rotatedPoint.y = ((position.x - rotationAbsolute.x) * MathUtils.sinDeg(angle)) + ((position.y - rotationAbsolute.y) * MathUtils.cosDeg(angle)) + rotationAbsolute.y;

        float bulletX = (rotatedPoint.x + (GUN_OFFSET_X * MathUtils.cosDeg(angle) - GUN_OFFSET_Y * MathUtils.sinDeg(angle)));
        float bulletY = (rotatedPoint.y + (GUN_OFFSET_X * MathUtils.sinDeg(angle) + GUN_OFFSET_Y * MathUtils.cosDeg(angle)));


        //Bullet position correction - super sketchy way of doing it
        if(angle <= 90) {
            bulletX -= (6 * angle) / 90;
        } else if(angle > 90 && angle <= 180) {
            float amount = (angle - 90) / 90;
            System.out.println(amount);
            bulletY -= 5 * amount;
            bulletX -= 6;
        } else if(angle > 180 && angle <= 270) {
            float amount = (angle - 180) / 90;
            bulletY -= 5 * (1f - amount);
        }

        new Bullet(new Vector2(bulletX, bulletY), bulletAngle, world, this);
    }

    public void handleInput(float dt) {
        if (Gdx.input.isKeyPressed(inputConfig.getTurnLeft())) {
            turn(Tank.TurnDirection.LEFT, TURN_AMOUNT * dt);
        } else if (Gdx.input.isKeyPressed(inputConfig.getTurnRight())) {
            turn(Tank.TurnDirection.RIGHT, TURN_AMOUNT * dt);
        }
        if (Gdx.input.isKeyPressed(inputConfig.getDriveForward())) {
            drive(TANK_SPEED * dt);
        } else if (Gdx.input.isKeyPressed(inputConfig.getDriveBackward())) {
            drive(-TANK_SPEED * dt);
        }
        if (Gdx.input.isKeyJustPressed(inputConfig.getFire())) {
            fire();
        }
    }

    public void update(float dt) {
        if(state != TankState.DESTROYED) {
            handleInput(dt);
            body.setTransform(position.x + tankTexture.getWidth() / 2, position.y + tankTexture.getHeight() / 2, (float) Math.toRadians(angle));
            stateTime += dt;
            state = TankState.IDLE;

            Iterator<Bullet> bulletIterator = firedBullets.iterator();

            while (bulletIterator.hasNext()) {
                Bullet bullet = bulletIterator.next();
                bullet.update(dt);
                if (bullet.getTimer() > bullet.BULLET_LIFE_TIME) {
                    bullet.dispose();
                    bulletIterator.remove();
                }
            }
        }
    }

    public TextureRegion getTexture() {
        if(state == TankState.DRIVING) {
            return driveAnimation.getKeyFrame(stateTime, true);
        } else if(state == TankState.DESTROYED) {
            return new TextureRegion(tankDestroyedTexture);
        } else {
            return new TextureRegion(tankTexture);
        }
    }

    public Vector2 getPosition() {
        return this.position;
    }

    public float getAngle() {
        return angle;
    }

    public void render(SpriteBatch batch){
        batch.begin();
        batch.draw(getTexture(), position.x, position.y, getTexture().getRegionWidth() / 2, getTexture().getRegionHeight() / 2, getTexture().getRegionWidth(), getTexture().getRegionHeight(), 1, 1, angle);
        batch.draw(tankHPTexture, position.x, position.y + 50, (tankHPTexture.getWidth() * (health / 100)), tankHPTexture.getHeight());
        batch.end();
    }

    public void onBulletCollision() {
        Gdx.app.log("TANK", "BULLET COLLISION");
    }

    public void takeDamage(float damage) {
        health -= damage;
        if(health <= 0) {
            state = TankState.DESTROYED;
        }
    }

    public void dispose() {
        tankTexture.dispose();
    }

    public enum TurnDirection {
        LEFT, RIGHT
    }

    public enum TankState {
        IDLE, FIRING, DRIVING, DESTROYED
    }

    public Polygon getBounds() {
        return bounds;
    }

    public static List<Tank> getInstances() {
        return instances;
    }

    public TankState getState() {
        return state;
    }

    public Body getBody() {
        return body;
    }
}
