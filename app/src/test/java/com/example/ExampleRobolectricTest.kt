package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.catalog.ClassAndWorldCatalog
import com.example.data.catalog.SkillCatalog
import com.example.domain.model.HeroClass
import com.example.domain.model.SkillCategory
import org.junit.Assert.assertEquals
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
}
