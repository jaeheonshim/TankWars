package com.jaeheonshim.tankwars.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.jaeheonshim.tankwars.TankGame;
import com.jaeheonshim.tankwars.TankInputConfig;
import com.jaeheonshim.tankwars.WorldContactListener;
import com.jaeheonshim.tankwars.sprites.Bullet;
import com.jaeheonshim.tankwars.sprites.Tank;

import java.util.ArrayList;
import java.util.Iterator;

import static com.jaeheonshim.tankwars.sprites.Tank.TANK_SPEED;
import static com.jaeheonshim.tankwars.sprites.Tank.TURN_AMOUNT;

public class PlayScreen implements Screen {
    Viewport viewport;
    OrthographicCamera camera;
    TiledMapRenderer mapRenderer;
    TiledMap map;

    SpriteBatch batch;
    ShapeRenderer renderer;

    Tank tank;
    Tank tank1;

    private World world;
    private Box2DDebugRenderer b2dr;

    public PlayScreen(SpriteBatch batch) {
        this.batch = batch;
        camera = new OrthographicCamera();
        viewport = new FitViewport(TankGame.V_WIDTH, TankGame.V_HEIGHT, camera);

        map = new TmxMapLoader().load("playmap.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);

        world = new World(new Vector2(0, 0), true);
        world.setContactListener(new WorldContactListener());
        b2dr = new Box2DDebugRenderer();

        TankInputConfig rightPlayer = new TankInputConfig(Input.Keys.W, Input.Keys.S, Input.Keys.A, Input.Keys.D, Input.Keys.Q);
        TankInputConfig leftPlayer = new TankInputConfig(Input.Keys.UP, Input.Keys.DOWN, Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.SLASH);

        tank = new Tank(new Vector2(0, 0), world, rightPlayer);
        tank1 = new Tank(new Vector2(70, 30), world, leftPlayer);

        BodyDef bdef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fdef = new FixtureDef();
        Body body;

        for(MapObject object : map.getLayers().get(1).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set((rect.getX() + rect.getWidth() / 2), (rect.getY() + rect.getHeight() / 2));

            body = world.createBody(bdef);

            shape.setAsBox(rect.getWidth() / 2, rect.getHeight() / 2);
            fdef.shape = shape;
            body.createFixture(fdef);
        }
    }

    @Override
    public void show() {

    }

    public void update(float dt) {
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();

        for (Tank tank : Tank.getInstances()) {
            tank.update(dt);
        }

        Iterator<Bullet> bulletIterator = Bullet.getInstances().iterator();

        // Fucking ConcurrentModificationExceptions
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            if (bullet.dispose) {
                bulletIterator.remove();
            }
            bullet.update(dt);
        }

        world.step(1 / 60f, 6, 2);
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.setProjectionMatrix(camera.combined);

        for (Tank tank : Tank.getInstances()) {
            tank.render(batch);
        }

        for (Bullet bullet : Bullet.getInstances()) {
            bullet.render(batch);
        }

        //b2dr.render(world, camera.combined);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
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
        map.dispose();
    }
}
