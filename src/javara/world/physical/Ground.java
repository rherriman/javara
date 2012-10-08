package javara.world.physical;

import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Plane;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import javara.world.PhysicalObject;
import javara.world.environment.Sky;

public class Ground extends PhysicalObject {
	public Ground() {
		super(Sky.DEFAULT_GROUND_COLOR, 0, false);

		spatial = new Node(identifier);
		shape = new PlaneCollisionShape(new Plane(Vector3f.UNIT_Y, 0));
		physics = new RigidBodyControl(shape, 0);
	}

	// The ground color is technically part of the Sky box, so we need to change
	// it there as well.
	@Override
	public void setColor(ColorRGBA c) {
		color = c;
		world.getSky().setGroundColor(c);
	}

	@Override
	public void setHologram(boolean set) {
		if (set) {
			throw new UnsupportedOperationException();
		} else {
			super.setHologram(set);
		}
	}

	@Override
	public void update(float tpf) {
		Vector3f groundTrans = world.getPlayer().getCameraNode().getWorldTranslation().clone();
		groundTrans.y = 0;
		setLocalTranslation(groundTrans);
	}
}