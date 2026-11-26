package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.catalog.ClassAndWorldCatalog
import com.example.data.catalog.OpenWorldCatalog
import com.example.data.catalog.SkillCatalog
import com.example.domain.model.HeroClass
import com.example.domain.model.SkillCategory
import com.example.domain.model.WorldZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Aeldrin", appName)
  }

  @Test
  fun `verify 300 skill bank and 30 subclasses`() {
    assertEquals(300, SkillCatalog.allSkills.size)
    assertEquals(50, SkillCatalog.countByCategory(SkillCategory.COMUM))
    assertEquals(50, SkillCatalog.countByCategory(SkillCategory.RARA))
    assertEquals(50, SkillCatalog.countByCategory(SkillCategory.LENDARIA))
    assertEquals(50, SkillCatalog.countByCategory(SkillCategory.MITICA))
    assertEquals(50, SkillCatalog.countByCategory(SkillCategory.SECRETA))
    assertEquals(40, SkillCatalog.countByCategory(SkillCategory.EVOLUTIVA))
    assertEquals(10, SkillCatalog.countByCategory(SkillCategory.PARADOXAL))

    assertEquals(5, HeroClass.entries.size)
    assertEquals(30, ClassAndWorldCatalog.subclasses.size)
    assertEquals(21, ClassAndWorldCatalog.subclasses.count { !it.isSecret })
    assertEquals(9, ClassAndWorldCatalog.subclasses.count { it.isSecret })
  }

  @Test
  fun `verify open world zones camps and monsters`() {
    assertEquals(WorldZoneId.VILA_CENTRAL, OpenWorldCatalog.zoneAt(0f, 0f))
    assertEquals(WorldZoneId.FLORESTA_SOMBRIA, OpenWorldCatalog.zoneAt(-20f, 0f))
    assertEquals(WorldZoneId.RUINAS_PILARES, OpenWorldCatalog.zoneAt(10f, 20f))
    assertEquals(WorldZoneId.PENHASCOS_CINZAS, OpenWorldCatalog.zoneAt(20f, -15f))
    assertEquals(4, OpenWorldCatalog.camps.size)
    assertTrue(OpenWorldCatalog.spawnAllWorldMonsters(1).size >= 12)
    assertTrue(OpenWorldCatalog.spawnInvasionWave(1, 1).isNotEmpty())
  }

  @Test
  fun `verify all 10 hero skins build detailed 3d geometry`() {
    assertEquals(10, com.example.domain.model.HeroSkin.entries.size)
    for (skin in com.example.domain.model.HeroSkin.entries) {
      val faces = mutableListOf<com.example.ui.combat3d.Face3D>()
      com.example.ui.combat3d.Arena3DMeshes.buildHero3D(
        heroClass = HeroClass.GUERREIRO,
        skin = skin,
        pos = com.example.ui.combat3d.Vec3(0f, 0f, 0f),
        facingYaw = 0.5f,
        walkPhase = 1.2f,
        isMoving = true,
        isDashing = false,
        attackProgress = 0.5f,
        time = 2.0f,
        shieldActive = true,
        out = faces
      )
      assertTrue("Skin ${skin.id} should generate detailed 3D faces", faces.size >= 40)
    }
  }
}
