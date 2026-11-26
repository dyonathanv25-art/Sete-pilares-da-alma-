package com.example.ui.combat3d

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

data class Vec3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(o: Vec3) = Vec3(x + o.x, y + o.y, z + o.z)
    operator fun minus(o: Vec3) = Vec3(x - o.x, y - o.y, z - o.z)
    operator fun times(s: Float) = Vec3(x * s, y * s, z * s)

    fun rotateY(angleRad: Float): Vec3 {
        val c = cos(angleRad)
        val s = sin(angleRad)
        return Vec3(x * c + z * s, y, -x * s + z * c)
    }

    fun rotateX(angleRad: Float): Vec3 {
        val c = cos(angleRad)
        val s = sin(angleRad)
        return Vec3(x, y * c - z * s, y * s + z * c)
    }

    fun rotateZ(angleRad: Float): Vec3 {
        val c = cos(angleRad)
        val s = sin(angleRad)
        return Vec3(x * c - y * s, x * s + y * c, z)
    }

    fun cross(o: Vec3) = Vec3(
        y * o.z - z * o.y,
        z * o.x - x * o.z,
        x * o.y - y * o.x
    )

    fun normalized(): Vec3 {
        val len = sqrt(x * x + y * y + z * z)
        return if (len > 0.0001f) Vec3(x / len, y / len, z / len) else Vec3(0f, 1f, 0f)
    }

    fun dot(o: Vec3) = x * o.x + y * o.y + z * o.z

    fun distanceXZ(o: Vec3): Float {
        val dx = x - o.x
        val dz = z - o.z
        return sqrt(dx * dx + dz * dz)
    }
}

data class Face3D(
    val vertices: List<Vec3>,
    val color: Color,
    val emissive: Boolean = false,
    val outlineColor: Color? = null,
    val neonGlow: Boolean = false
)

data class ProjectedFace(
    val points: List<Offset>,
    val avgDepth: Float,
    val shadedColor: Color,
    val outlineColor: Color?,
    val neonGlow: Boolean = false
)

object Arena3DMath {
    private val lightDir = Vec3(-0.45f, 0.85f, -0.55f).normalized()

    fun projectPoint(
        v: Vec3,
        camTarget: Vec3,
        camYaw: Float,
        camPitch: Float,
        camDistance: Float,
        width: Float,
        height: Float,
        fovScale: Float = 1.08f
    ): Pair<Offset, Float>? {
        val rel = v - camTarget
        val r1 = rel.rotateY(camYaw)
        val r2 = r1.rotateX(camPitch)
        val z = r2.z + camDistance
        if (z <= 0.35f) return null
        val scale = (minOf(width, height) * fovScale) / z
        val sx = width * 0.5f + r2.x * scale
        val sy = height * 0.54f - r2.y * scale
        return Offset(sx, sy) to z
    }

    fun shadeColor(base: Color, normal: Vec3, emissive: Boolean): Color {
        if (emissive) return base
        val ndotl = max(0f, normal.normalized().dot(lightDir))
        val intensity = 0.44f + 0.56f * ndotl
        return Color(
            red = (base.red * intensity).coerceIn(0f, 1f),
            green = (base.green * intensity).coerceIn(0f, 1f),
            blue = (base.blue * intensity).coerceIn(0f, 1f),
            alpha = base.alpha
        )
    }

    fun projectAndSortFaces(
        faces: List<Face3D>,
        camTarget: Vec3,
        camYaw: Float,
        camPitch: Float,
        camDistance: Float,
        width: Float,
        height: Float
    ): List<ProjectedFace> {
        val result = ArrayList<ProjectedFace>(faces.size)
        for (face in faces) {
            if (face.vertices.size < 3) continue
            val projected = ArrayList<Offset>(face.vertices.size)
            var depthSum = 0f
            var valid = true
            for (v in face.vertices) {
                val p = projectPoint(v, camTarget, camYaw, camPitch, camDistance, width, height)
                if (p == null) {
                    valid = false
                    break
                }
                projected.add(p.first)
                depthSum += p.second
            }
            if (!valid) continue

            val e1 = face.vertices[1] - face.vertices[0]
            val e2 = face.vertices[2] - face.vertices[0]
            val normal = e1.cross(e2)
            val shaded = shadeColor(face.color, normal, face.emissive)
            result.add(
                ProjectedFace(
                    points = projected,
                    avgDepth = depthSum / face.vertices.size,
                    shadedColor = shaded,
                    outlineColor = face.outlineColor,
                    neonGlow = face.neonGlow
                )
            )
        }
        result.sortByDescending { it.avgDepth }
        return result
    }

    fun DrawScope.drawProjectedFaces(faces: List<ProjectedFace>, timeSec: Float = 0f) {
        val path = Path()
        val pulse = EmissiveGlowShader.computeNeonPulse(timeSec)
        for (f in faces) {
            path.reset()
            path.moveTo(f.points[0].x, f.points[0].y)
            for (i in 1 until f.points.size) {
                path.lineTo(f.points[i].x, f.points[i].y)
            }
            path.close()
            drawPath(path = path, color = f.shadedColor)
            if (f.neonGlow) {
                EmissiveGlowShader.run {
                    drawEmissivePathGlow(path = path, baseColor = f.shadedColor, pulse = pulse)
                }
            }
            f.outlineColor?.let { border ->
                drawPath(path = path, color = border, style = Stroke(width = 1.1f))
            }
        }
    }

    fun addBox(
        out: MutableList<Face3D>,
        center: Vec3,
        halfSize: Vec3,
        color: Color,
        rotY: Float = 0f,
        rotX: Float = 0f,
        rotZ: Float = 0f,
        emissive: Boolean = false,
        outline: Color? = null,
        neonGlow: Boolean = false
    ) {
        val hx = halfSize.x
        val hy = halfSize.y
        val hz = halfSize.z
        val local = arrayOf(
            Vec3(-hx, -hy, -hz), Vec3(hx, -hy, -hz), Vec3(hx, hy, -hz), Vec3(-hx, hy, -hz),
            Vec3(-hx, -hy, hz), Vec3(hx, -hy, hz), Vec3(hx, hy, hz), Vec3(-hx, hy, hz)
        )
        val world = local.map { p ->
            p.rotateX(rotX).rotateZ(rotZ).rotateY(rotY) + center
        }
        out += Face3D(listOf(world[0], world[1], world[2], world[3]), color, emissive, outline, neonGlow)
        out += Face3D(listOf(world[5], world[4], world[7], world[6]), color, emissive, outline, neonGlow)
        out += Face3D(listOf(world[4], world[0], world[3], world[7]), color, emissive, outline, neonGlow)
        out += Face3D(listOf(world[1], world[5], world[6], world[2]), color, emissive, outline, neonGlow)
        out += Face3D(listOf(world[3], world[2], world[6], world[7]), color, emissive, outline, neonGlow)
    }

    fun addTaperedBox(
        out: MutableList<Face3D>,
        center: Vec3,
        halfSize: Vec3,
        topScaleX: Float = 1f,
        topScaleZ: Float = 1f,
        color: Color,
        rotY: Float = 0f,
        rotX: Float = 0f,
        rotZ: Float = 0f,
        emissive: Boolean = false,
        outline: Color? = null,
        neonGlow: Boolean = false
    ) {
        val hx = halfSize.x
        val hy = halfSize.y
        val hz = halfSize.z
        val thx = hx * topScaleX
        val thz = hz * topScaleZ
        val local = arrayOf(
            Vec3(-hx, -hy, -hz), Vec3(hx, -hy, -hz), Vec3(thx, hy, -thz), Vec3(-thx, hy, -thz),
            Vec3(-hx, -hy, hz), Vec3(hx, -hy, hz), Vec3(thx, hy, thz), Vec3(-thx, hy, thz)
        )
        val world = local.map { p ->
            p.rotateX(rotX).rotateZ(rotZ).rotateY(rotY) + center
        }
        out += Face3D(listOf(world[0], world[1], world[2], world[3]), color, emissive, outline, neonGlow)
        out += Face3D(listOf(world[5], world[4], world[7], world[6]), color, emissive, outline, neonGlow)
        out += Face3D(listOf(world[4], world[0], world[3], world[7]), color, emissive, outline, neonGlow)
        out += Face3D(listOf(world[1], world[5], world[6], world[2]), color, emissive, outline, neonGlow)
        out += Face3D(listOf(world[3], world[2], world[6], world[7]), color, emissive, outline, neonGlow)
    }

    fun addQuadXZ(
        out: MutableList<Face3D>,
        center: Vec3,
        halfW: Float,
        halfD: Float,
        color: Color,
        emissive: Boolean = false,
        outline: Color? = null
    ) {
        val v0 = Vec3(center.x - halfW, center.y, center.z - halfD)
        val v1 = Vec3(center.x + halfW, center.y, center.z - halfD)
        val v2 = Vec3(center.x + halfW, center.y, center.z + halfD)
        val v3 = Vec3(center.x - halfW, center.y, center.z + halfD)
        out += Face3D(listOf(v0, v1, v2, v3), color, emissive, outline)
    }

    fun addPyramid(
        out: MutableList<Face3D>,
        baseCenter: Vec3,
        radius: Float,
        height: Float,
        color: Color,
        rotY: Float = 0f,
        rotX: Float = 0f,
        rotZ: Float = 0f,
        emissive: Boolean = false,
        outline: Color? = null,
        neonGlow: Boolean = false
    ) {
        fun transform(v: Vec3): Vec3 = v.rotateX(rotX).rotateZ(rotZ).rotateY(rotY) + baseCenter
        val p0 = transform(Vec3(-radius, 0f, -radius))
        val p1 = transform(Vec3(radius, 0f, -radius))
        val p2 = transform(Vec3(radius, 0f, radius))
        val p3 = transform(Vec3(-radius, 0f, radius))
        val tip = transform(Vec3(0f, height, 0f))
        out += Face3D(listOf(p0, p1, tip), color, emissive, outline, neonGlow)
        out += Face3D(listOf(p1, p2, tip), color, emissive, outline, neonGlow)
        out += Face3D(listOf(p2, p3, tip), color, emissive, outline, neonGlow)
        out += Face3D(listOf(p3, p0, tip), color, emissive, outline, neonGlow)
    }

    fun addOctahedron(
        out: MutableList<Face3D>,
        center: Vec3,
        radius: Float,
        height: Float,
        color: Color,
        rotY: Float = 0f,
        emissive: Boolean = true,
        outline: Color? = null,
        neonGlow: Boolean = emissive
    ) {
        val top = center + Vec3(0f, height, 0f)
        val bot = center + Vec3(0f, -height, 0f)
        val corners = (0 until 4).map { i ->
            val a = rotY + (i * PI.toFloat() / 2f)
            center + Vec3(cos(a) * radius, 0f, sin(a) * radius)
        }
        for (i in 0 until 4) {
            val next = (i + 1) % 4
            out += Face3D(listOf(corners[i], corners[next], top), color, emissive, outline, neonGlow)
            out += Face3D(listOf(corners[next], corners[i], bot), color, emissive, outline, neonGlow)
        }
    }

    fun addOrbitRing(
        out: MutableList<Face3D>,
        center: Vec3,
        radius: Float,
        thickness: Float,
        segments: Int,
        color: Color,
        rotX: Float,
        rotY: Float,
        emissive: Boolean = true,
        neonGlow: Boolean = false
    ) {
        for (i in 0 until segments) {
            val a0 = (i.toFloat() / segments) * 2f * PI.toFloat()
            val a1 = ((i + 1).toFloat() / segments) * 2f * PI.toFloat()
            val inner0 = Vec3(cos(a0) * (radius - thickness), 0f, sin(a0) * (radius - thickness)).rotateX(rotX).rotateY(rotY) + center
            val outer0 = Vec3(cos(a0) * (radius + thickness), 0f, sin(a0) * (radius + thickness)).rotateX(rotX).rotateY(rotY) + center
            val outer1 = Vec3(cos(a1) * (radius + thickness), 0f, sin(a1) * (radius + thickness)).rotateX(rotX).rotateY(rotY) + center
            val inner1 = Vec3(cos(a1) * (radius - thickness), 0f, sin(a1) * (radius - thickness)).rotateX(rotX).rotateY(rotY) + center
            out += Face3D(listOf(inner0, outer0, outer1, inner1), color, emissive, neonGlow = neonGlow)
        }
    }

    fun addFloorDisk(
        out: MutableList<Face3D>,
        center: Vec3,
        radius: Float,
        segments: Int,
        color: Color,
        outline: Color? = null
    ) {
        val verts = (0 until segments).map { i ->
            val a = (i.toFloat() / segments) * 2f * PI.toFloat()
            Vec3(center.x + cos(a) * radius, center.y, center.z + sin(a) * radius)
        }
        out += Face3D(verts, color, emissive = true, outlineColor = outline)
    }
}
