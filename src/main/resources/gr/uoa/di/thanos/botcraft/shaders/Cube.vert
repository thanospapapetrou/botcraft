uniform mat4 transformation;
in vec3 position;
in vec4 color;
out vec4 _color;

void main() {
	gl_Position = transformation * vec4(position, 1.0f);
	_color = color;
}
