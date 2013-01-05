package eu.whrl.lsystemwallpaper;

public class DrawingPosition {
	float x;
	float y;
	float angle;
	public DrawingPosition() {
		x = 0;
		y = 0;
		angle = 0;
	}
	public DrawingPosition(float x, float y, float a) {
		this.x = x;
		this.y = y;
		this.angle = a;
	}
	public DrawingPosition copy() {
		return new DrawingPosition(x, y, angle);
	}
}