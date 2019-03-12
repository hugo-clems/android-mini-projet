package hutoch.m2dl.miniprojet;

public class Migeon
{
	float posX;
	float posY;
	float rate;
	
	public Migeon(float x, float y)
	{
		posX = x;
		posY = y;
		rate = 10;
	}
	
	public void tick()
	{
		posY += rate;
	}
	
	public float getX()
	{
		return posX;
	}
	
	public float getY()
	{
		return posY;
	}
}
