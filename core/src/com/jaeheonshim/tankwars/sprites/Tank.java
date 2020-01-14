package com.jaeheonshim.tankwars.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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

    public static float BULLET_SPREAD = 0;
    public static float RELOAD_TIME = 2;

    private TankInputConfig inputConfig;

    private Vector2 position;
    private Vector2 initPosition;
    private float angle;
    private float initAngle;
    private TankState state;
    private float health;
    private float bullets;
    private boolean reloading;
    private float reloadTimer;
    private boolean takingWaterDamage = false;
    private float waterDamageCounter;

    private Texture tankTexture;
    private Texture tankDestroyedTexture;
    private Texture tankHPTexture;
    private Texture driveSheet;
    private Texture explosionSheet;
    private Texture bulletTexture;

    private BitmapFont font;
    private GlyphLayout glyphLayout;

    private Animation<TextureRegion> driveAnimation;
    private Animation<TextureRegion> explosionAnimation;

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

    public Tank(Vector2 position, float angle, World world, TankInputConfig config) {
        this.position = new Vector2(position);
        this.initPosition = new Vector2(position);
        this.initAngle = angle;
        state = TankState.IDLE;

        tankTexture = new Texture("tanks/tank1.png");
        tankDestroyedTexture = new Texture("tanks/tank1damaged.png");
        driveSheet = new Texture("tanks/tank1_strip4.png");
        explosionSheet = new Texture("explosion_strip10.png");
        tankHPTexture = new Texture("hp-mini1.png");
        bulletTexture = new Texture("bullet.png");

        font = new BitmapFont(Gdx.files.internal("font.fnt"));
        glyphLayout = new GlyphLayout();
        initAnimations();

        gunshot = Gdx.audio.newSound(Gdx.files.internal("gunshot.mp3"));
        tankHit = Gdx.audio.newSound(Gdx.files.internal("tank-hit.mp3"));
        explosion = Gdx.audio.newSound(Gdx.files.internal("explosion.mp3"));

        firedBullets = new ArrayList<>();

        stateTime = 0f;
        health = 100;
        waterDamageCounter = 0;
        this.angle = angle;

        bounds = new Polygon(new float[] {position.x, position.y, position.x, position.y + tankTexture.getHeight(), position.x + tankTexture.getWidth(), position.y + tankTexture.getHeight(), position.x + tankTexture.getWidth(), position.y});
        bounds.setOrigin(position.x + tankTexture.getWidth() / 2, position.y + tankTexture.getHeight() / 2);

        this.world = world;
        definePhysics();

        instances.add(this);

        random = new Random();

        this.inputConfig = config;

        body.setTransform(position, angle * MathUtils.degreesToRadians);
        bullets = 10;
        reloadTimer = RELOAD_TIME;
        reloading = false;
    }

    public Tank(Vector2 position, float angle, World world, TankInputConfig config, TankTexture texture) {
        this(position, angle, world, config);
        switch(texture) {
            case BROWN:
                tankTexture = new Texture("tanks/tank1.png");
                tankDestroyedTexture = new Texture("tanks/tank1damaged.png");
                driveSheet = new Texture("tanks/tank1_strip4.png");
                break;
            case GREEN:
                tankTexture = new Texture("tanks/tank2.png");
                tankDestroyedTexture = new Texture("tanks/tank2damaged.png");
                driveSheet = new Texture("tanks/tank2_strip4.png");
                break;
            case RED:
                tankTexture = new Texture("tanks/tank3.png");
                tankDestroyedTexture = new Texture("tanks/tank3damaged.png");
                driveSheet = new Texture("tanks/tank3_strip4.png");
                break;
            case PURPLE:
                tankTexture = new Texture("tanks/tank4.png");
                tankDestroyedTexture = new Texture("tanks/tank4damaged.png");
                driveSheet = new Texture("tanks/tank4_strip4.png");
                break;
        }
    }

    public void definePhysics() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();

        shape.setAsBox(24, 22.5f, new Vector2(0 - 6, 0), 0);

        fixtureDef.shape = shape;
        body.createFixture(fixtureDef).setUserData(this);
        shape.dispose();
    }


    public void initAnimations() {
        TextureRegion[][] frames = TextureRegion.split(driveSheet, driveSheet.getWidth() / 4, driveSheet.getHeight());
        TextureRegion[] driveFrames = new TextureRegion[4];
        int index = 0;
        for (int i = 0; i < frames.length; i++) {
            for (int j = 0; j < frames[i].length; j++) {
                driveFrames[index++] = frames[i][j];
            }
        }

        driveAnimation = new Animation<>(0.05f, driveFrames);

        frames = TextureRegion.split(explosionSheet, explosionSheet.getWidth() / 10, explosionSheet.getHeight());
        TextureRegion[] explosionFrames = new TextureRegion[10];
        index = 0;
        for (int i = 0; i < frames.length; i++) {
            for (int j = 0; j < frames[i].length; j++) {
                explosionFrames[index++] = frames[i][j];
            }
        }

        explosionAnimation = new Animation<>(0.1f, explosionFrames);
    }

    public void turn(TurnDirection direction, float amount) {
        state = TankState.DRIVING;
        if(direction == TurnDirection.LEFT) {
            angle += amount;
            angle = angle > 360 ? angle - ((int) angle) / 360 * 360 : angle;
            body.setAngularVelocity(2);
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
        if(bullets > 0 && !reloading) {
            float bulletAngle = angle;
            if(BULLET_SPREAD > 0) {
                if (random.nextBoolean()) {
                    bulletAngle += random.nextInt((int) BULLET_SPREAD);
                } else {
                    bulletAngle -= random.nextInt((int) BULLET_SPREAD);
                }
            }

            Vector2 rotationAbsolute = new Vector2(body.getPosition().x, body.getPosition().y);

            float bulletX = rotationAbsolute.x + 20 * MathUtils.cosDeg(angle);
            float bulletY = rotationAbsolute.y + 20 * MathUtils.sinDeg(angle);
            gunshot.play();
            new Bullet(new Vector2(bulletX, bulletY), bulletAngle, world, this);
            bullets--;
            if(bullets <= 0) {
                reloading = true;
            }
        }
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
        } else if(Gdx.input.isKeyJustPressed(inputConfig.getReload())) {
            reload();
        }
    }

    public void update(float dt) {
        stateTime += dt;
        if(Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            state = TankState.IDLE;
            health = 100;
            body.setType(BodyDef.BodyType.DynamicBody);
            stateTime = 0;
            stopWaterDamage();
            body.setTransform(initPosition, initAngle * MathUtils.degreesToRadians);
            reloading = false;
            bullets = 10;
        }
        if(state != TankState.DESTROYED) {
            handleInput(dt);
            //body.setTransform(position.x + tankTexture.getWidth() / 2, position.y + tankTexture.getHeight() / 2, (float) Math.toRadians(angle));

            // ahhhhhhhhhhhh!! We're all going to die
            position.x = body.getPosition().x - getTexture().getRegionWidth() / 2;
            position.y = body.getPosition().y - getTexture().getRegionHeight() / 2;
            // end ahhhhhhhhh!!

            angle = body.getAngle() * MathUtils.radiansToDegrees;

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

            if(takingWaterDamage) {
                waterDamageCounter += dt;
                if (waterDamageCounter > 1) {
                    takeDamage(TankGame.WATER_DAMAGE);
                    waterDamageCounter = 0;
                }
            }

            if(reloading) {
                reloadTimer -= dt;
                if(reloadTimer <= 0) {
                    reloadTimer = RELOAD_TIME;
                    reloading = false;
                    bullets = 10;
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
        if(state == TankState.DESTROYED && !explosionAnimation.isAnimationFinished(stateTime)) {
            TextureRegion explosionFrame = explosionAnimation.getKeyFrame(stateTime, false);
            batch.draw(explosionFrame, body.getPosition().x - explosionFrame.getRegionWidth() * 2 / 2, body.getPosition().y - explosionFrame.getRegionHeight() * 2 / 2, explosionFrame.getRegionWidth() * 2, explosionFrame.getRegionHeight() * 2);
        }
        if(state != TankState.DESTROYED) {
            if (reloading) {
                font.getData().setScale(0.4f);
                String reloadingString = String.format("%.2f", reloadTimer) + " RELOADING!!";
                glyphLayout.setText(font, reloadingString);
                font.draw(batch, reloadingString, body.getPosition().x - glyphLayout.width / 2, body.getPosition().y + 50);
            }

            if (bullets > 0) {
                float offset = 0;
                for (int i = 0; i < bullets; i++) {
                    batch.draw(bulletTexture, (body.getPosition().x - tankTexture.getWidth() / 2) + offset, body.getPosition().y + 37);
                    offset += bulletTexture.getWidth() + 1;
                }
            }
        }

        batch.end();
    }

    public void takeDamage(float damage) {
        health -= damage;
        if(health <= 0) {
            state = TankState.DESTROYED;
            stateTime = 0;
            explosion.play();
        } else {
            tankHit.play();
        }
    }

    public void reload() {
        reloading = true;
    }

    public void takeWaterDamage() {
        takingWaterDamage = true;
        waterDamageCounter = 100;
    }

    public void stopWaterDamage() {
        takingWaterDamage = false;
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
        BROWN, GREEN, RED, PURPLE
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
