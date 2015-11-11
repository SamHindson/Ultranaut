package com.semdog.ultranaut.vehicles;

/**
 * A simple Docking Port class whose main purpose is to store a reference to
 * which ship owns the docking port.
 * 
 * When two Fixtures with DockingPorts as their UserDatas collide, the dock
 * sequence is initiated.
 * 
 * @author Sam
 */

public class DockingPort {
	private Ship owner;

	private float offsetX, offsetY;

	public DockingPort(Ship owner, float offsetX, float offsetY) {
		this.owner = owner;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	public Ship getOwner() {
		return owner;
	}
	
	public float getOffsetX() {
		return offsetX;
	}
	
	public float getOffsetY() {
		return offsetY;
	}
}
