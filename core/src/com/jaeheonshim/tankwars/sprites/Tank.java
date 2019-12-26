package com.jaeheonshim.tankwars.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
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
    public static float TURN_AMOUNT = 130;
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

    private Sound gunshot;
    private Sound tankHit;
    private Sound explosion;

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

        gunshot = Gdx.audio.newSound(Gdx.files.internal("gunshot.mp3"));
        tankHit = Gdx.audio.newSound(Gdx.files.internal("tank-hit.mp3"));
        explosion = Gdx.audio.newSound(Gdx.files.internal("explosion.mp3"));

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
            body.setAngularVelocity(2);
            System.out.println(body.getAngle());
        } else if(direction == TurnDirection.RIGHT) {
            angle -= amount;
            float tempAngle = angle < -360 ? angle + ((int) -angle) / 360 * 360 : angle;
            angle = angle < 0 ? 360 - -tempAngle : angle;
            body.setAngularVelocity(-2);
        }
    }

    public void drive(float speed) {
        state = TankState.DRIVING;
        float xAccel = MathUtils.cos(body.getAngle()) * speed;
        float yAccel = MathUtils.sin(body.getAngle()) * speed;

        body.setLinearVelocity(xAccel * TankGame.PPM, yAccel * TankGame.PPM);

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

        gunshot.play();
        new Bullet(new Vector2(bulletX, bulletY), bulletAngle, world, this);
    }

    public void handleInput(float dt) {
        if (Gdx.input.isKeyPressed(inputConfig.getTurnLeft())) {
            turn(Tank.TurnDirection.LEFT, TURN_AMOUNT * dt);
        } else if (Gdx.input.isKeyPressed(inputConfig.getTurnRight())) {
            turn(Tank.TurnDirection.RIGHT, TURN_AMOUNT * dt);
        } else {
            body.setAngularVelocity(0);
        }
        if (Gdx.input.isKeyPressed(inputConfig.getDriveForward())) {
            drive(TANK_SPEED * dt);
        } else if (Gdx.input.isKeyPressed(inputConfig.getDriveBackward())) {
            drive(-TANK_SPEED * dt);
        } else {
            body.setLinearVelocity(0, 0);
        }
        if (Gdx.input.isKeyJustPressed(inputConfig.getFire())) {
            fire();
        }
    }

    public void update(float dt) {
        if(Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            state = TankState.IDLE;
            health = 100;
            body.setType(BodyDef.BodyType.DynamicBody);
        }
        if(state != TankState.DESTROYED) {
            handleInput(dt);
            //body.setTransform(position.x + tankTexture.getWidth() / 2, position.y + tankTexture.getHeight() / 2, (float) Math.toRadians(angle));

            // ahhhhhhhhhhhh!! We're all going to die
            position.x = body.getPosition().x - getTexture().getRegionWidth() / 2;
            position.y = body.getPosition().y - getTexture().getRegionHeight() / 2;
            // end ahhhhhhhhh!!

            angle = body.getAngle() * MathUtils.radiansToDegrees;

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
        } else {
            body.setLinearVelocity(0, 0);
            body.setType(BodyDef.BodyType.StaticBody);
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
        batch.draw(getTexture(), body.getPosition().x - getTexture().getRegionWidth() / 2, body.getPosition().y - getTexture().getRegionHeight() / 2, getTexture().getRegionWidth() / 2, getTexture().getRegionHeight() / 2, getTexture().getRegionWidth(), getTexture().getRegionHeight(), 1, 1, MathUtils.radiansToDegrees * body.getAngle());
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
            explosion.play();
        } else {
            tankHit.play();
        }
    }

    public void dispose() {
        tankTexture.dispose();
        tankHit.dispose();
        gunshot.dispose();
    }

    public enum TurnDirection {
        LEFT, RIGHT
    }

    public enum TankState {
        IDLE, FIRING, DRIVING, DESTROYED
    }

    public enum TankTexture {

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
