package javara.world;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import javara.world.environment.Celestial;
import javara.world.physical.Block;
import javara.world.physical.Domeoid;
import javara.world.physical.Ramp;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class WorldLoader {
	public static World load(String fileName, Node rootNode, PhysicsSpace physicsSpace, AssetManager assets) throws IOException {
		InputStream is = ClassLoader.getSystemResourceAsStream(fileName);
		InputSource src = new InputSource(is);
		MapParser parser = new MapParser(rootNode, physicsSpace, assets);
		try {
			XMLReader reader = XMLReaderFactory.createXMLReader();
			reader.setContentHandler(parser);
			reader.parse(src);
			return parser.get();
		}
		catch (SAXException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	protected static class MapParser extends DefaultHandler {
		protected World world;
		protected StringBuilder chars;
		protected int numCelestials = 0;

		public MapParser(Node root, PhysicsSpace space, AssetManager assets) {
			world = new World(root, space, assets);
			chars = new StringBuilder();
		}

		public static boolean parseBoolean(String s) {
			if (s.equalsIgnoreCase("true") || s.equals("1") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("t") || s.equalsIgnoreCase("y")) {
				return true;
			} else if (s.equalsIgnoreCase("false") || s.equals("0") || s.equalsIgnoreCase("no") || s.equalsIgnoreCase("f") || s.equalsIgnoreCase("n")) {
				return false;
			}
			return false;
		}

		public static ColorRGBA parseColor(String s) {
			if (s == null) {
				return null;
			}
			String parts[] = s.split(",");
			float r = Float.parseFloat(parts[0]);
			float g = Float.parseFloat(parts[1]);
			float b = Float.parseFloat(parts[2]);
			return new ColorRGBA(r, g, b, 1);
		}

		public static Float parseCompass(String s) {
			if (s == null) {
				return null;
			}
			return (Float.parseFloat(s) * -FastMath.DEG_TO_RAD) + FastMath.PI;
		}

		public static Vector3f parseVector(String s) {
			if (s == null) {
				return null;
			}
			String parts[] = s.split(",");
			float x = Float.parseFloat(parts[0]);
			float y = Float.parseFloat(parts[1]);
			float z = Float.parseFloat(parts[2]);
			return new Vector3f(x, y, z);
		}

		@Override
		public void characters(char[] ch, int start, int length) {
			chars.append(ch, start, length);
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attrs) {
			if (localName.equalsIgnoreCase("map")) {
				world.setName(attrs.getValue("name"));
				world.setAuthor(attrs.getValue("author"));
			} else if (localName.equalsIgnoreCase("sky")) {
				ColorRGBA skyColor = parseColor(attrs.getValue("color"));
				ColorRGBA horizonColor = parseColor(attrs.getValue("horizon"));
				ColorRGBA ambientColor = parseColor(attrs.getValue("ambient"));

				if (skyColor != null) {
					world.getSky().setColor(skyColor);
				}
				if (horizonColor != null) {
					world.getSky().setHorizonColor(horizonColor);
				}
				if (ambientColor != null) {
					world.getSky().setAmbientColor(ambientColor);
				}

				if (attrs.getValue("horizonScale") != null) {
					float horizonScale = Float.parseFloat(attrs.getValue("horizonScale"));
					world.getSky().setHorizonScale(horizonScale);
				}
			} else if (localName.equalsIgnoreCase("ground")) {
				ColorRGBA groundColor = parseColor(attrs.getValue("color"));

				if (groundColor != null) {
					world.getGround().setColor(groundColor);
				}
			} else if (localName.equalsIgnoreCase("celestial")) {
				ColorRGBA color = Celestial.DEFAULT_COLOR;
				float azimuth = 90.0f, elevation = 45.0f, intensity = 0.4f, radius = 1.0f;
				boolean visible = false;

				if (attrs.getValue("color") != null) {
					color = parseColor(attrs.getValue("color"));
				}
				if (attrs.getValue("azimuth") != null) {
					azimuth = parseCompass(attrs.getValue("azimuth"));
				}
				if (attrs.getValue("elevation") != null) {
					elevation = Float.parseFloat(attrs.getValue("elevation")) * FastMath.DEG_TO_RAD;
				}
				if (attrs.getValue("intensity") != null) {
					intensity = Float.parseFloat(attrs.getValue("intensity"));
				}
				if (attrs.getValue("size") != null) {
					radius = Float.parseFloat(attrs.getValue("size"));
				}
				if (attrs.getValue("visible") != null) {
					visible = parseBoolean(attrs.getValue("visible"));
				}

				Celestial cel = new Celestial(color, intensity, azimuth, elevation, radius, visible);
				world.addWorldObject(cel);

				if (intensity > 0) {
					numCelestials++;
				}
			} else if (localName.equalsIgnoreCase("starfield")) {
				long seed = world.getName().hashCode();
				int count = 500;
				boolean monochrome = false;
				ColorRGBA minColor = ColorRGBA.White;
				ColorRGBA maxColor = ColorRGBA.White;
				float minSize = 0.025f;
				float maxSize = 0.025f;

				if (attrs.getValue("seed") != null) {
					seed = Long.parseLong(attrs.getValue("seed"));
				}
				if (attrs.getValue("count") != null) {
					count = Integer.parseInt(attrs.getValue("count"));
				}
				if (attrs.getValue("monochrome") != null) {
					monochrome = parseBoolean(attrs.getValue("monochrome"));
				}
				if (attrs.getValue("minColor") != null) {
					minColor = parseColor(attrs.getValue("minColor"));
				}
				if (attrs.getValue("maxColor") != null) {
					maxColor = parseColor(attrs.getValue("maxColor"));
				}
				if (attrs.getValue("minSize") != null) {
					minSize = Float.parseFloat(attrs.getValue("minSize"));
				}
				if (attrs.getValue("maxSize") != null) {
					maxSize = Float.parseFloat(attrs.getValue("maxSize"));
				}

				Random rand = new Random(seed);

				float minR = minColor.r;
				float deltaR = maxColor.r - minR;
				float minG = minColor.g;
				float deltaG = maxColor.g - minG;
				float minB = minColor.b;
				float deltaB = maxColor.b - minB;

				for (int i = 0; i < count; i++) {
					// Note: in order to end up with an even distribution of
					// stars, we can't just use two raw random numbers.
					// We need to scale them such that there are fewer stars
					// near the poles than near the equator.
					float theta = FastMath.TWO_PI * rand.nextFloat();
					float phi = FastMath.abs(FastMath.HALF_PI - FastMath.acos(rand.nextFloat()));
					float r, g, b;

					if (monochrome) {
						float dice = rand.nextFloat();
						r = minR + dice * deltaR;
						g = minG + dice * deltaG;
						b = minB + dice * deltaB;
					} else {
						r = minR + rand.nextFloat() * deltaR;
						g = minG + rand.nextFloat() * deltaG;
						b = minB + rand.nextFloat() * deltaB;
					}
					ColorRGBA color = new ColorRGBA(r, g, b, (phi / FastMath.HALF_PI));
					float size = minSize + rand.nextFloat() * (maxSize - minSize);

					Celestial cel = new Celestial(color, 0.0f, theta, phi, size, true);
					world.addWorldObject(cel);
				}
			} else if (localName.equalsIgnoreCase("incarnator")) {
				float angle = 0.0f;
				int order = 0;
				Vector3f location = new Vector3f(0.0f, 30.0f, 0.0f);

				if (attrs.getValue("location") != null) {
					location = parseVector(attrs.getValue("location"));
				}
				if (attrs.getValue("angle") != null) {
					angle = parseCompass(attrs.getValue("angle"));
				}
				if (attrs.getValue("order") != null) {
					order = Integer.parseInt(attrs.getValue("order"));
				}

				world.addIncarnator(location, angle, order);
			} else if (localName.equalsIgnoreCase("block")) {
				boolean isHologram = false;
				ColorRGBA color = Block.DEFAULT_COLOR;
				float mass = Block.DEFAULT_MASS;
				float pitch = Block.DEFAULT_PITCH;
				float yaw = Block.DEFAULT_YAW;
				float roll = Block.DEFAULT_ROLL;
				Vector3f center = new Vector3f(0.0f, 0.0f, 0.0f);
				Vector3f size = Block.DEFAULT_SIZE;

				if (attrs.getValue("color") != null) {
					color = parseColor(attrs.getValue("color"));
				}
				if (attrs.getValue("mass") != null) {
					mass = Float.parseFloat(attrs.getValue("mass"));
				}
				if (attrs.getValue("hologram") != null) {
					isHologram = parseBoolean(attrs.getValue("hologram"));
				}
				if (attrs.getValue("center") != null) {
					center = parseVector(attrs.getValue("center"));
				}
				if (attrs.getValue("size") != null) {
					size = parseVector(attrs.getValue("size"));
				}
				if (attrs.getValue("pitch") != null) {
					pitch = Float.parseFloat(attrs.getValue("pitch")) * FastMath.DEG_TO_RAD;
				}
				if (attrs.getValue("yaw") != null) {
					yaw = Float.parseFloat(attrs.getValue("yaw")) * FastMath.DEG_TO_RAD;
				}
				if (attrs.getValue("roll") != null) {
					roll = Float.parseFloat(attrs.getValue("roll")) * FastMath.DEG_TO_RAD;
				}
				if (attrs.getValue("opacity") != null) {
					color.set(color.r, color.g, color.b, Float.parseFloat(attrs.getValue("opacity")));
				}

				Block block = new Block(color, mass, isHologram, center, size, pitch, yaw, roll);
				world.addWorldObject(block);
			}
			else if (localName.equalsIgnoreCase("ramp")) {
				boolean isHologram = false;
				ColorRGBA color = Ramp.DEFAULT_COLOR;
				float mass = Ramp.DEFAULT_MASS;
				float width = Ramp.DEFAULT_WIDTH;
				float thickness = Ramp.DEFAULT_THICKNESS;
				Vector3f base = new Vector3f(-2.0f, 0.0f, 0.0f);
				Vector3f top = new Vector3f(2.0f, 4.0f, 0.0f);

				if (attrs.getValue("color") != null) {
					color = parseColor(attrs.getValue("color"));
				}
				if (attrs.getValue("mass") != null) {
					mass = Float.parseFloat(attrs.getValue("mass"));
				}
				if (attrs.getValue("hologram") != null) {
					isHologram = parseBoolean(attrs.getValue("hologram"));
				}
				if (attrs.getValue("base") != null) {
					base = parseVector(attrs.getValue("base"));
				}
				if (attrs.getValue("top") != null) {
					top = parseVector(attrs.getValue("top"));
				}
				if (attrs.getValue("width") != null) {
					width = Float.parseFloat(attrs.getValue("width"));
				}
				if (attrs.getValue("thickness") != null) {
					thickness = Float.parseFloat(attrs.getValue("thickness"));
				}
				if (attrs.getValue("opacity") != null) {
					color.set(color.r, color.g, color.b, Float.parseFloat(attrs.getValue("opacity")));
				}

				Ramp ramp = new Ramp(color, mass, isHologram, base, top, width, thickness);
				world.addWorldObject(ramp);
			} else if (localName.equalsIgnoreCase("dome")) {
				boolean outsideView = true, isHologram = false;
				ColorRGBA color = Domeoid.DEFAULT_COLOR;
				int planes = Domeoid.DEFAULT_PLANES;
				int radialSamples = Domeoid.DEFAULT_RADIAL_SAMPLES;
				float mass = Domeoid.DEFAULT_MASS;
				float radius = Domeoid.DEFAULT_RADIUS;
				float pitch = Domeoid.DEFAULT_PITCH;
				float yaw = Domeoid.DEFAULT_YAW;
				float roll = Domeoid.DEFAULT_ROLL;
				Vector3f center = new Vector3f(0.0f, 0.0f, 0.0f);

				if (attrs.getValue("color") != null) {
					color = parseColor(attrs.getValue("color"));
				}
				if (attrs.getValue("mass") != null) {
					mass = Float.parseFloat(attrs.getValue("mass"));
				}
				if (attrs.getValue("hologram") != null) {
					isHologram = parseBoolean(attrs.getValue("hologram"));
				}
				if (attrs.getValue("center") != null) {
					center = parseVector(attrs.getValue("center"));
				}
				if (attrs.getValue("planes") != null) {
					planes = Integer.parseInt(attrs.getValue("planes"));
				}
				if (attrs.getValue("radialSamples") != null) {
					radialSamples = Integer.parseInt(attrs.getValue("radialSamples"));
				}
				if (attrs.getValue("radius") != null) {
					radius = Float.parseFloat(attrs.getValue("radius"));
				}
				if (attrs.getValue("outsideView") != null) {
					outsideView = parseBoolean(attrs.getValue("outsideView"));
				}
				if (attrs.getValue("pitch") != null) {
					pitch = Float.parseFloat(attrs.getValue("pitch")) * FastMath.DEG_TO_RAD;
				}
				if (attrs.getValue("yaw") != null) {
					yaw = Float.parseFloat(attrs.getValue("yaw")) * FastMath.DEG_TO_RAD;
				}
				if (attrs.getValue("roll") != null) {
					roll = Float.parseFloat(attrs.getValue("roll")) * FastMath.DEG_TO_RAD;
				}
				if (attrs.getValue("opacity") != null) {
					color.set(color.r, color.g, color.b, Float.parseFloat(attrs.getValue("opacity")));
				}

				Domeoid dome = new Domeoid(color, mass, isHologram, center, planes, radialSamples, radius, outsideView, pitch, yaw, roll);
				world.addWorldObject(dome);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) {
			// If the map has no celestials, add the defaults.
			if (localName.equalsIgnoreCase("map")) {
				if (numCelestials == 0) {
					Celestial c1 = new Celestial(Celestial.DEFAULT_COLOR, 0.4f, parseCompass("20"), 45 * FastMath.DEG_TO_RAD, 1.0f, false);
					Celestial c2 = new Celestial(Celestial.DEFAULT_COLOR, 0.3f, parseCompass("200"), 20 * FastMath.DEG_TO_RAD, 1.0f, false);
					world.addWorldObject(c1);
					world.addWorldObject(c2);
				}
			}
			chars = new StringBuilder();
		}

		public World get() {
			return world;
		}
	}
}
