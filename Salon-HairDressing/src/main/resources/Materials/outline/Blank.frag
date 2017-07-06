varying vec2 texCoord;

uniform sampler2D m_Texture;
uniform sampler2D m_DepthTexture;

void main(){
    vec4 color = texture2D(m_DepthTexture, texCoord);
	gl_FragColor=color;
}