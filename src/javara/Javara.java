package javara;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import javara.world.World;
import javara.world.WorldLoader;
import javara.world.physical.Hector;

public class Javara extends SimpleApplication {
	private BulletAppState bulletAppState;
	private World world;
	private Player player;

    public static void main(String[] args) {
        Javara app = new Javara();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
		bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
		stateManager.attach(bulletAppState);

		try {
			world = WorldLoader.load("Maps/phosphorus.xml", rootNode, bulletAppState.getPhysicsSpace(), assetManager);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Hector hector = new Hector(ColorRGBA.DarkGray, new ColorRGBA(0.9f, 0.6f, 0, 1), new ColorRGBA(0.4f, 0, 0, 1));
		player = new Player(world, inputManager, cam, listener, hector);
		world.setPlayer(player);
		world.initialize();
    }

    @Override
    public void simpleUpdate(float tpf) {
        world.update(tpf);
		player.update(tpf);
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
