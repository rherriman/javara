package javara.world.logical;

import com.jme3.math.Vector3f;

public class Incarnator {
	protected int order = 0;
	protected float startAngle = 0.0f;
	protected Vector3f location;

	public Incarnator(Vector3f location, float angle, int order) {
		this.order = order;
		this.startAngle = angle;
		this.location = location.clone();
	}

	public Vector3f getLocation() {
		return location;
	}

	public float getStartAngle() {
		return startAngle;
	}

	public int getOrder() {
		return order;
	}
}