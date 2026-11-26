package com.example.ui.combat3d

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.ui.theme.ParadoxMagenta
import kotlin.math.sin

/**
 * Shader & Glow Utility for Aeldrin's 3D Engine.
 * Supports:
 * 1. Android 13+ (API 33+) AGSL RuntimeShader for alpha-masked neon emissive bloom.
 * 2. High-FPS multi-pass additive blending (BlendMode.Plus) for 3D polygon fissures/runes
 *    and alpha-channel ImageBitmap textures across all supported Android versions (API 24+).
 */
object EmissiveGlowShader {

    /**
     * AGSL fragment shader for pulsing neon aura & alpha-channel emissive compositing (API 33+).
     */
    const val AGSL_EMISSIVE_ALPHA_SHADER: String = """
        uniform float2 uResolution;
        uniform float uTime;
        uniform half4 uGlowColor;
        uniform float uIntensity;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / max(uResolution, float2(1.0, 1.0));
            float2 centered = uv - float2(0.5, 0.5);
            float dist = length(centered);
            float pulse = 0.72 + 0.28 * sin(uTime * 3.8 + uv.y * 6.2831);
            float halo = exp(-dist * 4.4) * uIntensity * pulse;
            half3 rgb = uGlowColor.rgb * half(halo);
            half alpha = clamp(half(halo * uGlowColor.a), half(0.0), half(1.0));
            return half4(rgb, alpha);
        }
    """

    /**
     * AGSL fragment shader with explicit diffuse + emissive alpha mask sampling (API 33+).
     */
    const val AGSL_TEXTURE_ALPHA_EMISSIVE_SHADER: String = """
        uniform shader uDiffuseTex;
        uniform shader uEmissiveMaskTex;
        uniform float uTime;
        uniform half4 uNeonColor;
        uniform float uGlowStrength;

        half4 main(float2 fragCoord) {
            half4 base = uDiffuseTex.eval(fragCoord);
            half4 mask = uEmissiveMaskTex.eval(fragCoord);
            float pulse = 0.70 + 0.30 * sin(uTime * 4.2);
            half emissiveAlpha = mask.a * max(mask.r, max(mask.g, mask.b));
            half3 neonAdd = uNeonColor.rgb * emissiveAlpha * half(uGlowStrength * pulse);
            half outAlpha = max(base.a, emissiveAlpha);
            return half4(clamp(base.rgb + neonAdd, half3(0.0), half3(1.0)), outAlpha);
        }
    """

    fun isAgslSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    fun computeNeonPulse(
        timeSec: Float,
        speed: Float = 4.2f,
        minIntensity: Float = 0.68f
    ): Float {
        val span = (1f - minIntensity).coerceIn(0f, 1f)
        val wave = (sin(timeSec * speed) + 1f) * 0.5f
        return (minIntensity + span * wave).coerceIn(0f, 1.2f)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun createAgslAuraBrush(
        glowColor: Color = ParadoxMagenta,
        intensity: Float = 0.85f,
        timeSec: Float = 0f,
        width: Float,
        height: Float
    ): ShaderBrush {
        val shader = RuntimeShader(AGSL_EMISSIVE_ALPHA_SHADER).apply {
            setFloatUniform("uResolution", width.coerceAtLeast(1f), height.coerceAtLeast(1f))
            setFloatUniform("uTime", timeSec)
            setFloatUniform(
                "uGlowColor",
                glowColor.red,
                glowColor.green,
                glowColor.blue,
                glowColor.alpha
            )
            setFloatUniform("uIntensity", intensity)
        }
        return ShaderBrush(shader)
    }

    /**
     * Renders a projected 3D polygon path with multi-pass additive neon glow (BlendMode.Plus),
     * giving runes, eyes, blades, and corruption fissures a luminous halo without expensive offscreen buffers.
     */
    fun DrawScope.drawEmissivePathGlow(
        path: Path,
        baseColor: Color,
        pulse: Float = 1f,
        outerStrokePx: Float = 6.5f,
        innerStrokePx: Float = 2.4f
    ) {
        val p = pulse.coerceIn(0.35f, 1.35f)
        val outerColor = baseColor.copy(alpha = (baseColor.alpha * 0.28f * p).coerceIn(0f, 0.7f))
        val midColor = baseColor.copy(alpha = (baseColor.alpha * 0.55f * p).coerceIn(0f, 0.85f))
        val coreTint = Color(
            red = (baseColor.red + 0.25f).coerceIn(0f, 1f),
            green = (baseColor.green + 0.18f).coerceIn(0f, 1f),
            blue = (baseColor.blue + 0.25f).coerceIn(0f, 1f),
            alpha = (baseColor.alpha * 0.45f * p).coerceIn(0f, 0.8f)
        )

        drawPath(
            path = path,
            color = outerColor,
            style = Stroke(width = outerStrokePx),
            blendMode = BlendMode.Plus
        )
        drawPath(
            path = path,
            color = midColor,
            style = Stroke(width = innerStrokePx),
            blendMode = BlendMode.Plus
        )
        drawPath(
            path = path,
            color = coreTint,
            blendMode = BlendMode.Plus
        )
    }

    /**
     * Renders an alpha-channeled diffuse texture and composites a pulsing neon emissive glow pass
     * using either an optional emissive mask bitmap or the diffuse alpha channel itself.
     */
    fun DrawScope.drawAlphaTextureWithEmissiveGlow(
        diffuse: ImageBitmap,
        dstOffset: IntOffset,
        dstSize: IntSize,
        glowColor: Color = ParadoxMagenta,
        timeSec: Float = 0f,
        emissiveMask: ImageBitmap? = null,
        glowStrength: Float = 0.85f,
        alpha: Float = 1f
    ) {
        drawImage(
            image = diffuse,
            dstOffset = dstOffset,
            dstSize = dstSize,
            alpha = alpha
        )

        val pulse = computeNeonPulse(timeSec) * glowStrength
        val maskBitmap = emissiveMask ?: diffuse
        val tint = glowColor.copy(alpha = (pulse * alpha).coerceIn(0f, 1f))

        // Additive neon glow layers preserving the texture alpha silhouette
        drawImage(
            image = maskBitmap,
            dstOffset = IntOffset(dstOffset.x - 2, dstOffset.y - 2),
            dstSize = IntSize(dstSize.width + 4, dstSize.height + 4),
            alpha = (0.35f * pulse * alpha).coerceIn(0f, 1f),
            colorFilter = ColorFilter.tint(tint, BlendMode.SrcIn),
            blendMode = BlendMode.Plus
        )
        drawImage(
            image = maskBitmap,
            dstOffset = dstOffset,
            dstSize = dstSize,
            alpha = (0.75f * pulse * alpha).coerceIn(0f, 1f),
            colorFilter = ColorFilter.tint(tint, BlendMode.SrcIn),
            blendMode = BlendMode.Plus
        )
    }
}
