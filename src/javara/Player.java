package javara;

import com.jme3.asset.AssetManager;
import com.jme3.audio.Listener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.control.CameraControl.ControlDirection;
import javara.world.World;
import javara.world.logical.Incarnator;
import javara.world.physical.Hector;

public class Player {
	protected static float PLAYER_JUMP_SPEED = 20.0f;
	protected static float PLAYER_FALL_SPEED = 30.0f;
	protected static float PLAYER_GRAVITY = 30.0f;

	protected static float BODY_DEG_PER_SECOND = 120.0f;
	protected static float HEAD_MOVE_SPEED = 30.0f;
	protected static float HEAD_MAX_ROTATION_X = 120.0f;
	protected static float HEAD_MAX_ROTATION_Y = 50.0f;
	protected static float MAX_JUMP_TIME = 0.3f;

	public float walkAngle = 0.0f, headAngleX = 0.0f, headAngleY = 0.0f;
	public boolean walking = false;
	public float walkMult = 0.1f;
	public Vector3f headDir, lastWalkDir;
	public boolean jumpCrouching = false, crouching = false;
	public float jumpCrouchTime = 0.0f;
	public boolean followCam = false;

	protected World world;
	protected Hector hector;
	protected CharacterControl player;
	protected CameraNode camNode;
	protected Node scoutNode;

	protected InputManager inputManager;
	protected AssetManager assetManager;
	protected Quaternion headRot;
	protected Listener listener;

	public Player(World world, InputManager input, Camera cam, Listener listener, Hector hull) {
		Incarnator incarn = world.getIncarnator(0);

		this.listener = listener;
		this.world = world;
		assetManager = world.getAssetManager();
		inputManager = input;
		hector = hull;

		player = new CharacterControl(hector.getCollisionShape(), .1f);
		player.setJumpSpeed(PLAYER_JUMP_SPEED);
		player.setFallSpeed(PLAYER_FALL_SPEED);
		player.setGravity(PLAYER_GRAVITY);
		player.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
		player.setUserObject(this);
		walkAngle = incarn.getStartAngle();

		hector.getHectorNode().addControl(player);

		camNode = new CameraNode("PlayerCam", cam);
		camNode.setControlDir(ControlDirection.SpatialToCamera);
		camNode.setLocalTranslation(Vector3f.ZERO);
		camNode.lookAt(Vector3f.UNIT_Z, Vector3f.UNIT_Y);
		hector.getHeadNode().attachChild(camNode);

		// aureus: temporary solution; scout should ultimately be a separate object
		scoutNode = new Node();
		scoutNode.setLocalTranslation(new Vector3f(0.0f, 3.0f, -10.0f));
		Matrix3f scoutMatrix = new Matrix3f();
		scoutMatrix.fromAngleAxis((float)Math.atan2(3.0f, 10.0f), Vector3f.UNIT_X);
		scoutNode.setLocalRotation(scoutMatrix);
		hector.getLegsNode().attachChild(scoutNode);

		player.setPhysicsLocation(incarn.getLocation());

		headRot = new Quaternion();
		setupKeys(input);
	}

	public CameraNode getCameraNode() {
		return camNode;
	}

	public Hector getHector() {
		return hector;
	}

	public CharacterControl getCharacterControl() {
		return player;
	}

	public void update(float tpf) {
		// float crouchOffset = jumpCrouching ? Math.min(((jumpCrouchTime / MAX_JUMP_TIME) * 0.25f),
		// 0.25f) : 0.0f;

		Matrix3f bodyRot = new Matrix3f();
		bodyRot.fromAngleNormalAxis(walkAngle * FastMath.DEG_TO_RAD, Vector3f.UNIT_Y);
		Vector3f bodyDir = bodyRot.mult(Vector3f.UNIT_Z.negate()).normalizeLocal();

		if (walking) {
			lastWalkDir = bodyDir.mult(walkMult * 2.0f);
			player.setWalkDirection(lastWalkDir);
			// set animation channels
		}
		else {
			player.setWalkDirection(Vector3f.ZERO);
			// set animation channels
		}

		hector.getLegsNode().setLocalRotation(new Quaternion().fromRotationMatrix(bodyRot).mult(new Quaternion().fromAngles(0, FastMath.PI, 0)));

		headRot.fromAngles(-headAngleY * FastMath.DEG_TO_RAD, FastMath.PI + ((walkAngle + headAngleX) * FastMath.DEG_TO_RAD), 0.0f);
		hector.getHeadNode().setLocalRotation(headRot);

		listener.setLocation(camNode.getCamera().getLocation());
		listener.setRotation(camNode.getCamera().getRotation());
	}

	private void setupKeys(InputManager input) {
		input.addMapping("left", new KeyTrigger(KeyInput.KEY_A));
		input.addMapping("right", new KeyTrigger(KeyInput.KEY_D));
		input.addMapping("forward", new KeyTrigger(KeyInput.KEY_W));
		input.addMapping("backward", new KeyTrigger(KeyInput.KEY_S));
		input.addMapping("jump", new KeyTrigger(KeyInput.KEY_SPACE));
		input.addMapping("center", new KeyTrigger(KeyInput.KEY_2));
		input.addMapping("camera", new KeyTrigger(KeyInput.KEY_F));
		input.addMapping("shoot", new MouseButtonTrigger(0));
		input.addMapping("head_left", new MouseAxisTrigger(0, true));
		input.addMapping("head_right", new MouseAxisTrigger(0, false));
		input.addMapping("head_up", new MouseAxisTrigger(1, false));
		input.addMapping("head_down", new MouseAxisTrigger(1, true));
		input.setCursorVisible(false);
		input.addListener(analogListener, "left", "right", "jump", "crouch", "head_left", "head_right", "head_up", "head_down");
		input.addListener(actionListener, "forward", "backward", "crouch", "jump", "shoot", "center", "camera");
	}

	private AnalogListener analogListener = new AnalogListener() {
		public void onAnalog(String name, float value, float tpf) {
			if (name.equals("left")) {
				walkAngle += BODY_DEG_PER_SECOND * value;
			} else if (name.equals("right")) {
				walkAngle -= BODY_DEG_PER_SECOND * value;
			} else if (name.equals("jump")) {
				jumpCrouchTime += tpf;
			} else if (name.equals("head_left")) {
				headAngleX += HEAD_MOVE_SPEED * value;
				if (headAngleX > HEAD_MAX_ROTATION_X) {
					headAngleX = HEAD_MAX_ROTATION_X;
				}
			} else if (name.equals("head_right")) {
				headAngleX -= HEAD_MOVE_SPEED * value;
				if (headAngleX < -HEAD_MAX_ROTATION_X) {
					headAngleX = -HEAD_MAX_ROTATION_X;
				}
			} else if (name.equals("head_up")) {
				headAngleY += HEAD_MOVE_SPEED * value;
				if (headAngleY > HEAD_MAX_ROTATION_Y) {
					headAngleY = HEAD_MAX_ROTATION_Y;
				}
			} else if (name.equals("head_down")) {
				headAngleY -= HEAD_MOVE_SPEED * value;
				if (headAngleY < -HEAD_MAX_ROTATION_Y) {
					headAngleY = -HEAD_MAX_ROTATION_Y;
				}
			}
		}
	};

	private ActionListener actionListener = new ActionListener() {
		public void onAction(String name, boolean keyPressed, float tpf) {
			if (name.equals("forward")) {
				walking = keyPressed;
				walkMult = 0.1f;
			} else if (name.equals("backward")) {
				walking = keyPressed;
				walkMult = -0.1f;
			} else if (name.equals("camera") && keyPressed) {
				followCam = !followCam;

				camNode.removeFromParent();

				if (followCam) {
					scoutNode.attachChild(camNode);
				} else {
					hector.getHeadNode().attachChild(camNode);
				}
			} else if (name.equals("jump")) {
				if (keyPressed) {
					jumpCrouching = true;
					jumpCrouchTime = 0.0f;
				} else {
					jumpCrouching = false;
					float jumpSpeed = Math.min(((jumpCrouchTime / MAX_JUMP_TIME) * 400.0f) + 1.0f, 10.0f);
					player.setJumpSpeed(jumpSpeed);
					player.jump();
					// jump(jumpSpeed);
				}
			} else if (name.equals("center") && keyPressed) {
				headAngleX = 0.0f;
				headAngleY = 0.0f;
			}
		}
	};
}
