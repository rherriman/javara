package javara.world.physical;

import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import javara.world.VisibleObject;

public class Hector extends VisibleObject {
	protected ColorRGBA mainMatColor, secondaryMatColor, tertiaryMatColor;
	protected Material secondaryMaterial, tertiaryMaterial;
	protected Node hectorNode, headNode, legsNode;
	protected Spatial leftLegTop, leftLegBottom, rightLegTop, rightLegBottom;
	protected CollisionShape collisionShape;

	public Hector(ColorRGBA mainColor, ColorRGBA trimColor, ColorRGBA cockpitColor) {
		super(mainColor);

		collisionShape = new CapsuleCollisionShape(1.5f, 0.7f, 1);

		mainMatColor = mainColor;
		secondaryMatColor = trimColor;
		tertiaryMatColor = cockpitColor;

		hectorNode = new Node(identifier);
		headNode = new Node(identifier + "-Head");
		legsNode = new Node(identifier + "-Legs");

		hectorNode.attachChild(headNode);
		hectorNode.attachChild(legsNode);
	}

	@Override
	public void attachedToWorld() {
		material = materialForColor(mainMatColor);
		secondaryMaterial = materialForColor(secondaryMatColor);
		tertiaryMaterial = materialForColor(tertiaryMatColor);

		Node headGeo = (Node)world.getAssetManager().loadModel("Models/hull.mesh.xml");
		headGeo.rotate(0, 0, 0);
		headNode.attachChild(headGeo);

		Geometry barrelTrim = (Geometry)headGeo.getChild("hull-geom-1");
		barrelTrim.setMaterial(secondaryMaterial);

		Geometry hull = (Geometry)headGeo.getChild("hull-geom-2");
		hull.setMaterial(material);

		Geometry cockpit = (Geometry)headGeo.getChild("hull-geom-3");
		cockpit.setMaterial(tertiaryMaterial);

		Geometry crotch = (Geometry)headGeo.getChild("hull-geom-4");
		crotch.setMaterial(material);

		leftLegTop = world.getAssetManager().loadModel("Models/legHigh.mesh.xml");
		rightLegTop = leftLegTop.clone();

		leftLegTop.setMaterial(material);
		rightLegTop.setMaterial(material);

		leftLegBottom = world.getAssetManager().loadModel("Models/legLow.mesh.xml");
		rightLegBottom = leftLegBottom.clone();

		leftLegBottom.setMaterial(secondaryMaterial);
		rightLegBottom.setMaterial(secondaryMaterial);

		leftLegTop.setLocalTranslation(-.45f, -.45f, -.2f);
		leftLegTop.setLocalRotation(new Quaternion().fromAngles(-(FastMath.PI / 4.4f), 0, 0));

		rightLegTop.setLocalRotation(new Quaternion().fromAngles((FastMath.PI / 4.4f), 0, FastMath.PI));
		rightLegTop.setLocalTranslation(.45f, -.45f, -.2f);

		leftLegBottom.setLocalTranslation(.43f, -1.18f, -.9f);
		leftLegBottom.setLocalRotation(new Quaternion().fromAngles((FastMath.PI / 4.1f), 0, 0));

		rightLegBottom.setLocalTranslation(-.43f, -1.18f, -.9f);
		rightLegBottom.setLocalRotation(new Quaternion().fromAngles(-(FastMath.PI / 4.1f), 0, FastMath.PI));

		legsNode.attachChild(leftLegTop);
		legsNode.attachChild(rightLegTop);
		legsNode.attachChild(leftLegBottom);
		legsNode.attachChild(rightLegBottom);
	}

	public CollisionShape getCollisionShape() {
		return collisionShape;
	}

	public Node getHectorNode() {
		return hectorNode;
	}

	public Node getHeadNode() {
		return headNode;
	}

	public Node getLegsNode() {
		return legsNode;
	}
}
