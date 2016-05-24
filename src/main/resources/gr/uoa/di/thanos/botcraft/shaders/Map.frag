uniform sampler2D terrains[TERRAINS];
in vec2 _texture;
out vec4 color;

void main() {
	color = texture(terrains[1], _texture);
}
