uniform mat4 transformation;
in vec3 position;
in vec3 normal;
in vec2 texture;
out vec2 _texture;

void main() {
	gl_Position = transformation * vec4(position + normal, 1.0f);
	_texture = vec2(position.x, position.z) + texture;
}
