package com.semdog.ultranaut.universe;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.semdog.ultranaut.player.Foot;
import com.semdog.ultranaut.player.Player;
import com.semdog.ultranaut.vehicles.DockingPort;
import com.semdog.ultranaut.vehicles.PlayerSensor;
import com.semdog.ultranaut.vehicles.Ship;

/**
 * This class implements the Box2D interface ContactListener, which is
 * responsible for dealing with collisions and what happens when they
 * occur.
 * 
 * @author Sam
 */

public class UniverseContactListener implements ContactListener {

	/**
	 * This method is called by the Box2D world when two bodies collide.
	 */
	public void beginContact(Contact contact) {
		Object a = contact.getFixtureA().getUserData();
		Object b = contact.getFixtureB().getUserData();
		
		if(a instanceof DockingPort && b instanceof DockingPort) {
			Ship ship1 = ((DockingPort)a).getOwner();
			Ship ship2 = ((DockingPort)b).getOwner();
			
			ship1.dockedWithShip(ship2);
			ship2.dockedWithShip(ship1);
		}
		
		if(b instanceof Foot && a instanceof CelestialBody)
			((Foot)b).getPlayer().setOnSurface(true);
		
		if(b instanceof Ship && a instanceof CelestialBody) {
			((Ship)b).setOnSurface(true);
		}
		
		if(a instanceof Mass)
			((Mass)a).recalculateOrbit();

		if(b instanceof Mass)
			((Mass)b).recalculateOrbit();
		
		if(a instanceof Player && b instanceof PlayerSensor)
			((Player) a).setNearbyShip(((PlayerSensor) b).getShip(), true);
		
		if (b instanceof Player) {
			if (a instanceof CelestialBody || a instanceof Ship) {
				((Player) b).setOnSurface(true);
			}
			else if (a instanceof PlayerSensor)
				((Player) b).setNearbyShip(((PlayerSensor) a).getShip(), true);
		}
	}
	
	/**
	 * This method is called by the Box2D world when two bodies come
	 * apart.
	 */
	public void endContact(Contact contact) {
		Object a = contact.getFixtureA().getUserData();
		Object b = contact.getFixtureB().getUserData();
		
		if(b instanceof Foot && a instanceof CelestialBody) {
			((Foot)b).getPlayer().setOnSurface(false);
		}
		
		if(b instanceof Ship && a instanceof CelestialBody)
			((Ship)b).setOnSurface(false);
		
		if(a instanceof Player && b instanceof PlayerSensor)
			((Player) a).setNearbyShip(((PlayerSensor) b).getShip(), false);

		if (b instanceof Player) {
			if (a instanceof CelestialBody || a instanceof Ship) {
				((Player) b).setOnSurface(false);
			}
			else if (a instanceof PlayerSensor)
				((Player) b).setNearbyShip(((PlayerSensor) a).getShip(), false);
		}
	}

	public void preSolve(Contact contact, Manifold oldManifold) {}

	public void postSolve(Contact contact, ContactImpulse impulse) {}

}
