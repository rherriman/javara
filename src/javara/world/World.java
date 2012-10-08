package javara.world;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.light.Light;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import javara.Player;
import javara.world.environment.Sky;
import javara.world.logical.Incarnator;
import javara.world.physical.Ground;

public final class World {
	public static final String DEFAULT_MAP_NAME = "unnamed";
	public static final String DEFAULT_MAP_AUTHOR = "unknown";

	protected String name, author;
	protected Node rootNode;
	protected PhysicsSpace physicsSpace;
	protected AssetManager assetManager;
	protected Sky sky;
	protected Ground ground;
	protected ArrayList<Incarnator> incarnators = new ArrayList<Incarnator>();
	protected ArrayList<WorldObject> deadObjects = new ArrayList<WorldObject>();
	protected HashMap<String, WorldObject> worldObjects = new HashMap<String, WorldObject>();
	protected Player player;

	public World(Node rootNode, PhysicsSpace physicsSpace, AssetManager assetManager) {
		this.rootNode = rootNode;
		this.physicsSpace = physicsSpace;
		this.assetManager = assetManager;

		name = DEFAULT_MAP_NAME;
		author = DEFAULT_MAP_AUTHOR;

		sky = new Sky();
		ground = new Ground();

		addWorldObject(sky);
		addWorldObject(ground);
	}

	/**
	 * Where azimuth is an angle relative to north and elevation is an angle
	 * relative to the horizon, return a vector a particular distance away at
	 * those angles.
	 *
	 * Angles must be given in radians.
	 *
	 * @param azimuth Radians relative to north.
	 * @param elevation Radians relative to the horizon.
	 * @param distance Distance away in meters.
	 * @return A vector at the provided angles and distance.
	 */
	public static Vector3f toCartesian(float azimuth, float elevation, float distance) {
		float x, y, z;
		Vector3f coordinates;

		x = FastMath.sin(azimuth) * FastMath.cos(elevation);
		y = FastMath.sin(elevation);
		z = FastMath.cos(azimuth) * FastMath.cos(elevation);
		coordinates = new Vector3f(x, y, z).mult(distance);

		return coordinates;
	}

	public void show(WorldObject obj) {
		if (obj instanceof VisibleObject) {
			VisibleObject vo = (VisibleObject)obj;

			Spatial vos = vo.getSpatial();
			if (vos != null) {
				rootNode.attachChild(vos);
			}

			Light vol = vo.getLight();
			if (vol != null) {
				rootNode.addLight(vol);
			}
		}

		if (obj instanceof PhysicalObject) {
			PhysicalObject po = (PhysicalObject)obj;
			if (po.physics != null) {
				physicsSpace.add(po.physics);
				po.getCollisionObject().setUserObject(obj);
			}
		}
	}

	public void hide(WorldObject obj) {
		if (obj instanceof VisibleObject) {
			VisibleObject vo = (VisibleObject)obj;

			Spatial vos = vo.getSpatial();
			if (vos != null) {
				rootNode.detachChild(vos);
			}

			Light vol = vo.getLight();
			if (vol != null) {
				rootNode.removeLight(vol);
			}
		}

		if (obj instanceof PhysicalObject) {
			PhysicalObject po = (PhysicalObject)obj;
			if (po.physics != null) {
				physicsSpace.remove(po.physics);
			}
		}
	}

	public void initialize() {
		Iterator<Entry<String, WorldObject>> it = worldObjects.entrySet().iterator();
		while (it.hasNext()) {
			it.next().getValue().worldInitialized();
		}
	}

	public void update(float tpf) {
		if (!deadObjects.isEmpty()) {
			for (WorldObject obj : deadObjects) {
				worldObjects.remove(obj.getIdentifier());
				obj.detach();
			}
			deadObjects.clear();
		}

		Vector3f celestialTranslation = player.getCameraNode().getWorldTranslation();
		Vector3f groundTranslation = new Vector3f(celestialTranslation);
		groundTranslation.y = 0;

		sky.setLocalTranslation(celestialTranslation);
		ground.setLocalTranslation(groundTranslation);

		Iterator<Entry<String, WorldObject>> it = worldObjects.entrySet().iterator();
		while (it.hasNext()) {
			it.next().getValue().update(tpf);
		}
	}

	public AssetManager getAssetManager() {
		return assetManager;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Sky getSky() {
		return sky;
	}

	public Ground getGround() {
		return ground;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player p) {
		this.player = p;

		Vector3f celestialTranslation = player.getCameraNode().getWorldTranslation();
		Vector3f groundTranslation = new Vector3f(celestialTranslation);
		groundTranslation.y = 0;

		sky.setLocalTranslation(celestialTranslation);
		ground.setLocalTranslation(groundTranslation);

		rootNode.attachChild(player.getHector().getHectorNode());
		physicsSpace.add(player.getCharacterControl());
	}

	public Incarnator getIncarnator(int playerNumber) {
		int idx = (int)(Math.random() * incarnators.size());
		return incarnators.get(idx);
	}

	public void addIncarnator(Vector3f location, float angle, int order) {
		incarnators.add(new Incarnator(location, angle, order));
	}

	public int getMaximumPopulation() {
		return incarnators.size();
	}

	public WorldObject getWorldObject(String name) {
		return worldObjects.get(name);
	}

	public void addWorldObject(WorldObject obj) {
		worldObjects.put(obj.getIdentifier(), obj);
		obj.attach(this);
		show(obj);
	}

	public void removeWorldObject(WorldObject obj) {
		hide(obj);
		deadObjects.add(obj);
	}
}
