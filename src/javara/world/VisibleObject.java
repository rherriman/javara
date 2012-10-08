package javara.world;

import com.jme3.light.Light;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

public class VisibleObject extends WorldObject {
	public static ColorRGBA DEFAULT_COLOR = new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f);
	public static float DEFAULT_PITCH = 0.0f, DEFAULT_YAW = 0.0f, DEFAULT_ROLL = 0.0f;

	protected ColorRGBA color;
	protected Material material;
	protected Spatial spatial;

	public VisibleObject(ColorRGBA color) {
		this.color = color;
	}

	public Material materialForColor(ColorRGBA c) {
		Material mat;

		if (c.getAlpha() < 1.0f) {
			mat = new Material(world.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
			mat.setColor("Color", c);
			mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
		} else {
			ColorRGBA ambient = c.clone();

			ambient.interpolate(ColorRGBA.Black, 0.5f);

			mat = new Material(world.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
			mat.setFloat("Shininess", Float.MAX_VALUE);
			mat.setBoolean("UseMaterialColors", true);
			mat.setColor("Ambient", ambient);
			mat.setColor("Diffuse", c);
			mat.setColor("Specular", ColorRGBA.Gray);
		}

		return mat;
	}

	@Override
	public void attachedToWorld() {
		material = materialForColor(color);
		spatial.setMaterial(material);
	}

	public Vector3f getLocalTranslation() {
		return spatial.getLocalTranslation();
	}

	public void setLocalTranslation(Vector3f vec) {
		spatial.setLocalTranslation(vec);
	}

	public Quaternion getLocalRotation() {
		return spatial.getLocalRotation();
	}

	public void setLocalRotation(Quaternion rot) {
		spatial.setLocalRotation(rot);
	}

	public ColorRGBA getColor() {
		return color;
	}

	public void setColor(ColorRGBA c) {
		color = c;
	}

	public Spatial getSpatial() {
		return spatial;
	}

	public Light getLight() {
		return null;
	}
}
