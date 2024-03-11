#import "Common/ShaderLib/GLSLCompat.glsllib"
#import "shaders/FastNoiseLite.glsl"

uniform float g_Time;
uniform vec4 m_Color;
uniform int m_Seed;
uniform float m_Speed;
uniform float m_Scale;
uniform float m_Strength;
uniform bool m_ScreenSpace;
uniform bool m_LocalSpace;

in vec4 gl_FragCoord;
in vec4 worldPos;
in vec2 texCoord;

float fbm(in fnl_state _state, in fnl_state _state2, in vec2 _st, in float _t) {
    fnlDomainWarp3D(_state2, _st.x, _t, _st.y);
    return fnlGetNoise3D(_state, _st.x, _t, _st.y) / 2.0f + 0.5f;
}

void main() {
    fnl_state state = fnlCreateState(m_Seed);
    state.noise_type = FNL_NOISE_PERLIN;
    state.fractal_type = FNL_FRACTAL_FBM;
    state.frequency = .0075f * m_Scale;
    state.octaves = 5;
    state.lacunarity = 2.0f;
    state.gain = .5f;

    fnl_state state2 = fnlCreateState(m_Seed - 1);
    state2.noise_type = FNL_NOISE_OPENSIMPLEX2;
    state2.fractal_type = FNL_FRACTAL_DOMAIN_WARP_PROGRESSIVE;
    state2.domain_warp_type = FNL_DOMAIN_WARP_OPENSIMPLEX2;
    state2.domain_warp_amp = m_Strength;
    state2.frequency = .01f;
    state2.octaves = 4;
    state2.lacunarity = 2.0f;
    state2.gain = .5f;

    vec2 coord = m_ScreenSpace ? gl_FragCoord.xy : ((m_LocalSpace ? texCoord.xy : worldPos.xy) * 100.0f);

    float f = fbm(state, state2, coord, g_Time * m_Speed);

    gl_FragColor = vec4((f * f * f + 0.6 * f * f + 0.5 * f) * m_Color.rgb, m_Color.a);
}