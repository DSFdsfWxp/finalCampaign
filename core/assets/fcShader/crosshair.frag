
uniform float color_a;
uniform sampler2D u_texture;
varying vec2 v_texCoords;
varying vec4 v_color;

void main() {
    vec2 p = vec2(v_texCoords.x, 1.0 - v_texCoords.y);
    vec4 c = texture2D(u_texture, p);
    gl_FragColor = vec4(1.0 - c.r, 1.0 - c.g, 1.0 - c.b, c.a * color_a);
}
