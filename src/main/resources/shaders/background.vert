#import "Common/ShaderLib/GLSLCompat.glsllib"

//the global uniform World view projection matrix
//(more on global uniforms below)
uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldMatrix;
//The attribute inPosition is the Object space position of the vertex
in vec3 inPosition;
out vec4 worldPos;
void main(){
    //Transformation of the object space coordinate to projection space
    //coordinates.
    //- gl_Position is the standard GLSL variable holding projection space
    //position. It must be filled in the vertex shader
    //- To convert position we multiply the worldViewProjectionMatrix by
    //by the position vector.
    //The multiplication must be done in this order.
    worldPos = vec4(inPosition, 1.0);
    gl_Position = g_WorldViewProjectionMatrix * worldPos;
    worldPos = g_WorldMatrix * worldPos;
}