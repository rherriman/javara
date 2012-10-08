varying vec3 viewVector;

uniform vec4 m_skyColor, m_horizonColor, m_groundColor;
uniform float m_gradientHeight;


//-----------------------------------------------------------------------------------
//  Fragment shader entry point
//-----------------------------------------------------------------------------------
void main ()
{
	float phi = normalize(viewVector).y;

	if (phi <= 0.0) {
		gl_FragColor = m_groundColor;
	}
	else if (phi > m_gradientHeight) {
		gl_FragColor = m_skyColor;
	}
	else {
		float gradientValue = phi / m_gradientHeight;
		gl_FragColor = m_skyColor * gradientValue + m_horizonColor * (1.0 - gradientValue);
	}
}