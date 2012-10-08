package javara.world.physical;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import javara.world.PhysicalObject;

public class Block extends PhysicalObject {
	public static final Vector3f DEFAULT_SIZE = new Vector3f(2.0f, 2.0f, 2.0f);

	public Block(ColorRGBA color, float mass, boolean isHologram, Vector3f center, Vector3f size, float pitch, float yaw, float roll) {
		super(color, mass, isHologram);

		spatial = new Geometry(identifier, new Box(Vector3f.ZERO, size.x / 2.0f, size.y / 2.0f, size.z / 2.0f));
		spatial.rotate(yaw, pitch, roll);
		spatial.setLocalTranslation(center);
		spatial.setLocalRotation(new Quaternion().fromAngles(pitch, yaw, roll));

		initializePhysics(spatial);
	}
}
