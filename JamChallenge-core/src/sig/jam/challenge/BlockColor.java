package sig.jam.challenge;

import com.badlogic.gdx.graphics.Color;

public enum BlockColor {
	BLUE(Color.NAVY),
	GREEN(Color.FOREST),
	RED(Color.MAROON),
	YELLOW(Color.GOLD),
	TEAL(Color.TEAL),
	PURPLE(Color.PURPLE),
	ORANGE(Color.CORAL),
	BROWN(Color.BROWN),
	INVISIBLE(new Color(0,0,0,0));
	Color col;
	
	BlockColor(Color col) {
		this.col=col;
	}
	
	public Color getDrawColor() {
		return col;
	}
	
	public static BlockColor getRandomColor() {
		return getRandomColor(3);
	}
	
	public static BlockColor getRandomColor(int variety) {
		return BlockColor.values()[(int)(JamChallenge.r.nextInt(variety))];
	}
	
}
