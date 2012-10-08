package javara.world.physical;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import javara.world.PhysicalObject;

public class Ramp extends PhysicalObject {
	public static final float DEFAULT_WIDTH = 8.0f, DEFAULT_THICKNESS = 0.0f;

	public Ramp(ColorRGBA color, float mass, boolean isHologram, Vector3f base, Vector3f top, float width, float thickness) {
		super(color, mass, isHologram);

		float length = top.subtract(base).length();
		Quaternion rotation = new Quaternion();
		rotation.lookAt(base.subtract(top), Vector3f.UNIT_Y);

		// These points are an approximation. They should be extended down half
		// the thickness perpendicular to the plane of the ramp.
		Vector3f baseCenter = base.clone().subtractLocal(0.0f, thickness / 2.0f, 0.0f);
		Vector3f topCenter = top.clone().subtractLocal(0.0f, thickness / 2.0f, 0.0f);
		Vector3f midPoint = baseCenter.interpolate(topCenter, 0.5f);

		spatial = new Geometry(identifier, new Box(Vector3f.ZERO, width / 2.0f, thickness / 2.0f, length / 2.0f));
		spatial.setLocalTranslation(midPoint);
		spatial.setLocalRotation(rotation);

		initializePhysics(spatial);
	}
}
