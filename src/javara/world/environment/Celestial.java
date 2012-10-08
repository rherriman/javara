package javara.world.environment;

import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import javara.world.VisibleObject;
import javara.world.World;

public class Celestial extends VisibleObject {
	public static final float celestialScale = 45.0f;
	public static final float celestialDistance = 1000.0f * 255.0f / 256.0f;

	protected boolean visible;
	protected DirectionalLight light;
	protected float azimuth, elevation, radius;

	public Celestial(ColorRGBA color, float intensity, float azimuth, float elevation, float radius, boolean visible) {
		super(color);

		this.azimuth = azimuth;
		this.elevation = elevation;
		this.radius = radius;
		this.visible = visible;

		if (intensity > 0) {
			light = new DirectionalLight();
			light.setColor(color.mult(intensity));
			light.setDirection(World.toCartesian(azimuth, elevation, 1).negate());
		}

		spatial = new Node(identifier);
	}

	// Visible Celestials, like the Sky, are slightly different from regular
	// VisibleObjects. They are not added to World.rootNode directly like other
	// objects. Instead, they should be added as children of Sky. They also
	// utilize a different Material type than Blocks, Ramps, and so forth.
	@Override
	public void attachedToWorld() {
		if (visible) {
			material = new Material(world.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
			material.setColor("Color", color);
			material.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

			Geometry geom = new Geometry(identifier, new Sphere(16, 16, celestialScale * radius));
			geom.setMaterial(material);
			geom.setLocalTranslation(World.toCartesian(azimuth, elevation, celestialDistance));

			world.getSky().addCelestialSpatial(geom);
		}
	}

	@Override
	public Light getLight() {
		return light;
	}
}
