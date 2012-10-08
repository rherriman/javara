package javara.world.environment;

import com.jme3.asset.AssetManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.Light;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import javara.world.VisibleObject;

public class Sky extends VisibleObject {
	public static final ColorRGBA DEFAULT_SKY_COLOR = new ColorRGBA(0, 0, 0.15f, 1);
	public static final ColorRGBA DEFAULT_HORIZON_COLOR = new ColorRGBA(0, 0, 0.8f, 1);
	public static final ColorRGBA DEFAULT_AMBIENT_COLOR = new ColorRGBA(0.4f, 0.4f, 0.4f, 1);
	public static final ColorRGBA DEFAULT_GROUND_COLOR = new ColorRGBA(0, 0, 0.15f, 1);
	public static final float DEFAULT_HORIZON_SCALE = 0.05f;

	protected AmbientLight ambientLight;
	protected ColorRGBA horizonColor, groundColor;
	protected float horizonScale;
	protected Geometry skyPlane;

	public Sky() {
		super(DEFAULT_SKY_COLOR);

		ambientLight = new AmbientLight();
		ambientLight.setColor(DEFAULT_AMBIENT_COLOR);

		color = DEFAULT_SKY_COLOR;
		horizonColor = DEFAULT_HORIZON_COLOR;
		groundColor = DEFAULT_GROUND_COLOR;
		horizonScale = DEFAULT_HORIZON_SCALE;

		spatial = new Node(identifier);
	}

	protected void addCelestialSpatial(Spatial s) {
		((Node)spatial).attachChild(s);
	}

	// The Sky object is atypical for a VisibleObject, and we don't want to
	// perform the default callback once it is attached to the World.
	@Override
	public void attachedToWorld() {
	}

	@Override
	public void update(float tpf) {
		Matrix4f cameraToWorld = skyPlane.getWorldMatrix();
		Vector3f cameraPosition = skyPlane.getParent().getWorldTranslation();
		Vector3f celestialTranslation = world.getPlayer().getCameraNode().getWorldTranslation();

		material.setMatrix4("cameraToWorld", cameraToWorld);
		material.setVector3("cameraPosition", cameraPosition);

		setLocalTranslation(celestialTranslation);
	}

	@Override
	public void worldInitialized() {
		CameraNode camNode = world.getPlayer().getCameraNode();
		Camera c = camNode.getCamera();
		float width = Math.abs(c.getFrustumRight() - c.getFrustumLeft());
		float height = Math.abs(c.getFrustumTop() - c.getFrustumBottom());
		float distance = c.getFrustumFar();
		float scale = distance / c.getFrustumNear();

		distance = distance * (255f / 256f);
		width *= scale;
		height *= scale;

		material = new Material(world.getAssetManager(), "Materials/SkyDef.j3md");
		material.setColor("skyColor", color);
		material.setColor("horizonColor", horizonColor);
		material.setColor("groundColor", groundColor);
		material.setFloat("gradientHeight", horizonScale);

		skyPlane = new Geometry("SkyPlane", new Quad(width, height));
		skyPlane.setLocalRotation(Quaternion.DIRECTION_Z.opposite());
		skyPlane.setLocalTranslation(c.getDirection().mult(-distance).add(new Vector3f(-width / 2, height / 2, 0)));
		skyPlane.setMaterial(material);

		camNode.attachChild(skyPlane);
	}

	public ColorRGBA getHorizonColor() {
		return horizonColor;
	}

	public void setHorizonColor(ColorRGBA c) {
		horizonColor = c;
	}

	public float getHorizonScale() {
		return horizonScale;
	}

	public void setHorizonScale(float scale) {
		horizonScale = scale;
	}

	public ColorRGBA getAmbientColor() {
		return ambientLight.getColor();
	}

	public void setAmbientColor(ColorRGBA c) {
		ambientLight.setColor(c);
	}

	public ColorRGBA getGroundColor() {
		return groundColor;
	}

	public void setGroundColor(ColorRGBA c) {
		groundColor = c;
	}

	@Override
	public Light getLight() {
		return ambientLight;
	}
}