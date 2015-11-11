package com.semdog.ultranaut.vehicles;

/**
 * This class serves as the UserData to a collider surrounding the OdysseyLander,
 * upon contact with which the player is eligible to enter the ship.
 * 
 * This class stores a reference to that ship.
 * 
 * @author Sam
 */

public class PlayerSensor {
	private Ship ship;
	
	public PlayerSensor(Ship ship) {
		this.ship = ship;
	}
	
	public Ship getShip() {
		return ship;
	}
}
