//-----------------------------------------------------------------------------------
//  Varying variables to pass the texture coordinates from the VS to the FS
//-----------------------------------------------------------------------------------
varying vec3 viewVector;

attribute vec4 inPosition;

uniform mat4 g_WorldViewProjectionMatrix;

uniform mat4 m_cameraToWorld;
uniform vec3 m_cameraPosition;

//-----------------------------------------------------------------------------------
//  Vertex shader entry point
//-----------------------------------------------------------------------------------
void main ()
{

	gl_Position = g_WorldViewProjectionMatrix * inPosition;
	
	viewVector = (m_cameraToWorld * inPosition).xyz - vec3(m_cameraPosition.x, 0, m_cameraPosition.z);
}
