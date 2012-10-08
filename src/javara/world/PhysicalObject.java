package javara.world;

import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Spatial;

public abstract class PhysicalObject extends VisibleObject {
	public static float DEFAULT_MASS = 0.0f;

	protected PhysicsControl physics;
	protected CollisionShape shape;
	protected float mass = 0.0f;
	protected boolean isHologram = false;

	public PhysicalObject(ColorRGBA color, float mass, boolean hologram) {
		super(color);
		this.mass = mass;
		this.isHologram = hologram;
		this.color = color;
	}

	protected void initializePhysics(Spatial s) {
		spatial = s;
		if (mass > 0) {
			shape = CollisionShapeFactory.createDynamicMeshShape(spatial);
		} else {
			// Something is broken here. Spits out some error about TerrainQuad,
			// so I've told it to use DynamicMeshShape in both cases.
			// shape = CollisionShapeFactory.createMeshShape(spatial);
			shape = CollisionShapeFactory.createDynamicMeshShape(spatial);
		}
		rebuildPhysics();
	}

	protected void rebuildPhysics() {
		if (spatial == null || shape == null) {
			return;
		}

		if (physics != null) {
			spatial.removeControl(physics);
		}

		if (isHologram) {
			spatial.setShadowMode(ShadowMode.Off);
			physics = new GhostControl(shape);
		} else {
			spatial.setShadowMode(ShadowMode.CastAndReceive);
			physics = new RigidBodyControl(shape, mass);
		}

		spatial.addControl(physics);
	}

	public void setHologram(boolean setHologram) {
		if (isHologram == setHologram) {
			return;
		}

		this.isHologram = setHologram;
		rebuildPhysics();
	}

	/*
	 * These methods are all basically convenience methods for casting the
	 * PhysicsControl appropriately. The rigid body and ghost controls share a
	 * common grandparent (PhysicsCollisionObject) and the PhysicsControl
	 * interface, but all the meaty stuff happens in their direct superclasses,
	 * RigidBodyControl and GhostControl.
	 */

	public PhysicsCollisionObject getCollisionObject() {
		return (PhysicsCollisionObject)physics;
	}

	public PhysicsControl getPhysicsControl() {
		return (PhysicsControl)physics;
	}

	public RigidBodyControl getRigidBodyControl() {
		return isHologram ? null : (RigidBodyControl)physics;
	}

	public GhostControl getGhostControl() {
		return isHologram ? (GhostControl)physics : null;
	}
}
