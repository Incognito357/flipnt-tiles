#import "Common/ShaderLib/GLSLCompat.glsllib"
#import "assets/Shaders/FastNoiseLite.glsl"

uniform float g_Time;
uniform vec4 m_Color;

in vec4 gl_FragCoord;

float fbm(in fnl_state _state, in fnl_state _state2, in vec2 _st, in float _t) {
    fnlDomainWarp3D(_state2, _st.x, _t, _st.y);
    return fnlGetNoise3D(_state, _st.x, _t, _st.y) / 2.0f + 0.5f;
}

void main() {
    fnl_state state = fnlCreateState(1337);
    state.noise_type = FNL_NOISE_PERLIN;
    state.fractal_type = FNL_FRACTAL_FBM;
    state.frequency = .0075f;
    state.octaves = 5;
    state.lacunarity = 2.0f;
    state.gain = .5f;

    fnl_state state2 = fnlCreateState(1357);
    state2.noise_type = FNL_NOISE_OPENSIMPLEX2;
    state2.fractal_type = FNL_FRACTAL_DOMAIN_WARP_PROGRESSIVE;
    state2.domain_warp_type= FNL_DOMAIN_WARP_OPENSIMPLEX2;
    state2.frequency = .01f;
    state2.octaves = 4;
    state2.lacunarity = 2.0f;
    state2.gain = .5f;

    //float noise = fnlGetNoise3D(state, gl_FragCoord.x, g_Time * 10.0, gl_FragCoord.y) / 2.f + 0.5f;
    float f = fbm(state, state2, gl_FragCoord.xy, g_Time * 7.5f);
    //gl_FragColor = vec4(noise, noise, noise, 1.0) * m_Color;

//    vec2 st = gl_FragCoord.xy;
//    vec3 color = vec3(0.0);
//
//    vec2 q = vec2(0.0);
//    q.x = fbm(state, st);
//    q.y = fbm(state, st + vec2(1.0));
//
//    vec2 r = vec2(0.0);
//    r.x = fbm(state, st + 1.0 * q + vec2(1.7, 9.2) + .15 * g_Time);
//    r.y = fbm(state, st + 1.0 * q + vec2(8.3, 2.8) + .126 * g_Time);
//
//    float f = fbm(state, st + r);
//
//    vec3 color = mix(vec3(0.101961, 0.619608, 0.666667),
//                vec3(0.666667, 0.666667, 0.498039),
//                clamp((f * f) * 4.0, 0.0, 1.0));
//    color = mix(color,
//                vec3(0, 0, 0.164706),
//                clamp(length(q), 0.0, 1.0));
//    color = mix(color,
//                vec3(0.666667, 1, 1),
//                clamp(length(r.x), 0.0, 1.0));

    gl_FragColor = vec4((f * f * f + 0.6 * f * f + 0.5 * f) * m_Color.rgb, 1.);

    // Output to screen
    //gl_FragColor = vec4(noise, noise, noise, 1.0) * m_Color;
}