package javara.world;

public abstract class WorldObject {
	protected static int lastUniqueID = 0;

	protected String identifier;
	protected World world;

	public WorldObject() {
		identifier = getClass().getName() + ":" + (++lastUniqueID);
	}

	public WorldObject(String ident) {
		identifier = ident;
	}

	protected void attach(World w) {
		world = w;
		attachedToWorld();
	}

	protected void detach() {
		detachedFromWorld();
		world = null;
	}

	/**
	 * Called once, after this object is actually loaded into the World.
	 */
	public void attachedToWorld() {
	}

	/**
	 * Called once, after this object is removed from the World.
	 */
	public void detachedFromWorld() {
	}

	/**
	 * Called every "tick" of the game.
	 *
	 * @param tpf
	 */
	public void update(float tpf) {
	}

	/**
	 * Called once, after all world objects have been initialized.
	 */
	public void worldInitialized() {
	}

	public String getIdentifier() {
		return identifier;
	}
}
