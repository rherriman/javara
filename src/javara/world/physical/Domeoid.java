package javara.world.physical;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import javara.world.PhysicalObject;
import javara.world.World;

public class Domeoid extends PhysicalObject {
	public static int DEFAULT_PLANES = 5, DEFAULT_RADIAL_SAMPLES = 8;
	public static float DEFAULT_RADIUS = 2.0f;

	public Domeoid(ColorRGBA color, float mass, boolean isHologram, Vector3f center, int planes, int radialSamples, float radius, boolean outsideView, float pitch, float yaw, float roll) {
		super(color, mass, isHologram);

		spatial = new Geometry(identifier, generateMesh(Vector3f.ZERO, planes, radialSamples, radius, outsideView));
		spatial.rotate(yaw, pitch, roll);
		spatial.setLocalTranslation(center);
		spatial.setLocalRotation(new Quaternion().fromAngles(pitch, yaw, roll));

		initializePhysics(spatial);
	}

	public static Mesh generateMesh(Vector3f center, int planes, int radialSamples, float radius, boolean outsideView) {
		// If the number of planes or radialSamples are not valid,
		// force them back to the "default" values.
		if (planes < 2) {
			planes = DEFAULT_PLANES;
		}
		if (radialSamples < 3) {
			radialSamples = DEFAULT_RADIAL_SAMPLES;
		}

		Mesh m = new Mesh();
		float[] azimuth = new float[radialSamples + 1];
		float[] elevation = new float[planes];
		int numPolygons, numVertices, i_index, v_index;
		int[] indexes;
		Vector2f[] texCoord;
		Vector3f temp_normal;
		Vector3f[] vertices, normals;

		// Determine the angles (in radians) that we need.
		for (int i = 0; i < (azimuth.length - 1); i++) {
			azimuth[i] = FastMath.TWO_PI * ((float)i / radialSamples);
		}
		for (int i = 0; i < (elevation.length - 1); i++) {
			elevation[i] = FastMath.HALF_PI * ((float)i / (planes - 1));
		}

		// Determine the number of polygons, vertices, and indexes we need.
		numPolygons = ((radialSamples * 2) * (planes - 2)) + radialSamples;
		numVertices = ((radialSamples * 4) * (planes - 2)) + (radialSamples * 3);
		indexes = new int[numPolygons * 3];
		texCoord = new Vector2f[numVertices];
		vertices = new Vector3f[numVertices];
		normals = new Vector3f[numVertices];

		// Calculate vertices and determine indexes for all but the top tier.
		i_index = 0;
		v_index = 0;
		for (int i = 0; i < (planes - 2); i++) {
			for (int j = 0; j < radialSamples; j++) {
				vertices[v_index] = World.toCartesian(azimuth[j], elevation[i], radius);
				vertices[v_index + 1] = World.toCartesian(azimuth[j], elevation[i + 1], radius);
				vertices[v_index + 2] = World.toCartesian(azimuth[j + 1], elevation[i + 1], radius);
				vertices[v_index + 3] = World.toCartesian(azimuth[j + 1], elevation[i], radius);

				temp_normal = vertices[v_index].subtract(vertices[v_index + 1]).cross(vertices[v_index + 1].subtract(vertices[v_index + 2])).normalize();
				if (outsideView) {
					temp_normal = temp_normal.negate();
				}
				normals[v_index] = normals[v_index + 1] = normals[v_index + 2] = normals[v_index + 3] = temp_normal;

				texCoord[v_index] = new Vector2f(0, 0);
				texCoord[v_index + 1] = new Vector2f(0, 1);
				texCoord[v_index + 2] = new Vector2f(1, 1);
				texCoord[v_index + 3] = new Vector2f(1, 0);

				if (outsideView) {
					indexes[i_index] = v_index;
					indexes[i_index + 1] = v_index + 3;
					indexes[i_index + 2] = v_index + 1;
					indexes[i_index + 3] = v_index + 1;
					indexes[i_index + 4] = v_index + 3;
					indexes[i_index + 5] = v_index + 2;
				} else {
					indexes[i_index] = v_index;
					indexes[i_index + 1] = v_index + 1;
					indexes[i_index + 2] = v_index + 2;
					indexes[i_index + 3] = v_index + 2;
					indexes[i_index + 4] = v_index + 3;
					indexes[i_index + 5] = v_index;
				}

				i_index += 6;
				v_index += 4;
			}
		}

		// Calculate vertices and determine indexes for the top tier.
		for (int i = 0; i < radialSamples; i++) {
			vertices[v_index] = World.toCartesian(azimuth[i], elevation[elevation.length - 2], radius);
			vertices[v_index + 1] = new Vector3f(0, radius, 0);
			vertices[v_index + 2] = World.toCartesian(azimuth[i + 1], elevation[elevation.length - 2], radius);

			temp_normal = vertices[v_index].subtract(vertices[v_index + 1]).cross(vertices[v_index + 1].subtract(vertices[v_index + 2])).normalize();
			if (outsideView) {
				temp_normal = temp_normal.negate();
			}
			normals[v_index] = normals[v_index + 1] = normals[v_index + 2] = temp_normal;

			texCoord[v_index] = new Vector2f(0, 0);
			texCoord[v_index] = new Vector2f(0.5f, 1);
			texCoord[v_index] = new Vector2f(1, 0);

			if (outsideView) {
				indexes[i_index] = v_index;
				indexes[i_index + 1] = v_index + 2;
				indexes[i_index + 2] = v_index + 1;
			} else {
				indexes[i_index] = v_index;
				indexes[i_index + 1] = v_index + 1;
				indexes[i_index + 2] = v_index + 2;
			}

			i_index += 3;
			v_index += 3;
		}

		// Assign the indexes and vertices to the mesh.
		m.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
		m.setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normals));
		m.setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoord));
		m.setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(indexes));
		m.updateBound();

		return m;
	}
}