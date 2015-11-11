package com.semdog.ultranaut.meta;

import java.io.Serializable;

/**
 * The serializable class that is saved and read from ".up" files.
 * It stores the settings for ULTRANAUT.
 * 
 * @author Sam
 *
 */

public class UltranautPref implements Serializable {

	private static final long serialVersionUID = -6876215693228748763L;
	
	private byte masterVolume, sfxVolume, musicVolume;
	private boolean fullScreen, antialiasing;
	
	public UltranautPref(byte masterVolume, byte sfxVolume, byte musicVolume, boolean fullScreen, boolean antialiasing) {
		this.masterVolume = masterVolume;
		this.sfxVolume = sfxVolume;
		this.musicVolume = musicVolume;
		this.fullScreen = fullScreen;
		this.antialiasing = antialiasing;
	}
	
	public byte getMasterVolume() {
		return masterVolume;
	}
	
	public byte getSfxVolume() {
		return sfxVolume;
	}
	
	public byte getMusicVolume() {
		return musicVolume;
	}
	
	public boolean isAntialiasing() {
		return antialiasing;
	}
	
	public boolean isFullscreen() {
		return fullScreen;
	}
	
	public void setFullScreen(boolean fullScreen) {
		this.fullScreen = fullScreen;
	}
	
	public void setAntialiasing(boolean antialiasing) {
		this.antialiasing = antialiasing;
	}
}
