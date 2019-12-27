package com.jaeheonshim.tankwars;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.jaeheonshim.tankwars.screens.PlayScreen;
import com.jaeheonshim.tankwars.sprites.Bullet;
import com.jaeheonshim.tankwars.sprites.Tank;

public class WorldContactListener implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        if (fixtureA.getUserData() instanceof Tank ^ fixtureB.getUserData() instanceof Tank && fixtureA.getUserData() instanceof Bullet ^ fixtureB.getUserData() instanceof Bullet) {
            Tank tank = (Tank) (fixtureA.getUserData() instanceof Tank ? fixtureA.getUserData() : fixtureB.getUserData());
            Bullet bullet = (Bullet) (fixtureA.getUserData() instanceof Bullet ? fixtureA.getUserData() : fixtureB.getUserData());

            if (bullet.getFiredTank() != tank && tank.getState() != Tank.TankState.DESTROYED && !bullet.isCollided()) {
                tank.takeDamage(Bullet.BULLET_DAMAGE);
                bullet.handleCollision();
            }
        }
        if((fixtureA.getUserData().equals("wall") ^ fixtureB.getUserData().equals("wall")) && (fixtureA.getUserData() instanceof Bullet ^ fixtureB.getUserData() instanceof Bullet)) {
            Bullet bullet = (Bullet) (fixtureA.getUserData() instanceof Bullet ? fixtureA.getUserData() : fixtureB.getUserData());
            bullet.handleCollision();
        }

        if((fixtureA.getUserData().equals("water") ^ fixtureB.getUserData().equals("water")) && (fixtureA.getUserData() instanceof Tank ^ fixtureB.getUserData() instanceof Tank)) {
            Tank tank = (Tank) (fixtureA.getUserData() instanceof Tank ? fixtureA.getUserData() : fixtureB.getUserData());
            tank.takeWaterDamage();
        }
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        if((fixtureA.getUserData().equals("water") ^ fixtureB.getUserData().equals("water")) && (fixtureA.getUserData() instanceof Tank ^ fixtureB.getUserData() instanceof Tank)) {
            Tank tank = (Tank) (fixtureA.getUserData() instanceof Tank ? fixtureA.getUserData() : fixtureB.getUserData());
            tank.stopWaterDamage();
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
