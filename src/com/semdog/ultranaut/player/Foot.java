package com.semdog.ultranaut.player;

/**
 * A class that serves only as being the Userdata of the
 * player's foot fixture.
 * 
 * @author Sam
 */

public class Foot {
	private Player player;
	
	public Foot(Player player) {
		this.player = player;
	}
	
	public Player getPlayer() {
		return player;
	}
}
