package hutoch.m2dl.miniprojet.utils;

public class Migeon {
	private float posX;
	private float posY;
	private float rate;
	
	public Migeon(float x, float y) {
		posX = x;
		posY = y;
		rate = 0;
	}
	
	public void tick() {
		posY += rate;
	}
	
	public float getX() {
		return posX;
	}
	
	public float getY() {
		return posY;
	}
}
