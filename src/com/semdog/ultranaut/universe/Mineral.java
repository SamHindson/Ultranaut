package com.semdog.ultranaut.universe;

import com.badlogic.gdx.graphics.Color;

/**
 * This class' purpose is to provide instantializable data
 * which represents the mineral content of a planet.
 * 
 * It stores which type of mineral it is as well as its
 * percentage composition in the surface.
 * 
 * @author Sam
 */

public class Mineral {
	
	public static final MineralType[] ELEMENTS = {
			MineralType.URANIUM,
			MineralType.PLUTONIUM,
			MineralType.CARBON,
			MineralType.HYDROCARBONS,
			MineralType.WATER,
			MineralType.SILICON,
			MineralType.GOLD,
			MineralType.IRON,
			MineralType.ALUMINIUM
	};
	
	private int percent;
	private MineralType type;
	
	public Mineral(MineralType type, int percent) {
		this.type = type;
		this.percent = percent;
	}
	
	public MineralType getType() {
		return type;
	}
	
	public int getPercent() {
		return percent;
	}
	
	public Color getColor() {
		return type.getColor();
	}
}

enum MineralType {
	
	URANIUM(new Color(0x59684EFF)),
	PLUTONIUM(new Color(0x925C5CFF)),
	CARBON(new Color(0xFFAC3DFF)),
	HYDROCARBONS(new Color(0x00D587FF)),
	WATER(new Color(0x58CEFFFF)),
	SILICON(new Color(0xFFC0E5FF)),
	GOLD(new Color(0xFFE200FF)),
	IRON(new Color(0xFF2222FF)),
	ALUMINIUM(new Color(0xFFFFFFFF));
	
	private Color color;
	
	MineralType(Color what) {
		color = what;
	}
	
	public Color getColor() {
		return color;
	}
}
