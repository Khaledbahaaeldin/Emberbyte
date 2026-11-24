package io.github.khaledbahaaeldin.emberbyte.ui.design.theme

import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class EmberbyteTypeTest {

    @Test
    fun hero_style_has_the_design_axes() {
        val size = 96.sp
        val style = EmberbyteType.hero(size)
        assertNotNull(style.fontFamily)
        assertEquals((-0.04).em, style.letterSpacing)
        assertEquals(size, style.fontSize)
    }

    @Test
    fun axis_table_is_exact() {
        // hero: wght 800, wdth 36, opsz 144
        val hero = EmberbyteType.axes(EmberbyteType.Role.Hero)
        assertEquals(800, hero.wght)
        assertEquals(36f, hero.wdth, 0.001f)
        assertEquals(144f, hero.opsz ?: 0f, 0.001f)

        // badge: wght 700, wdth 105, opsz null
        val badge = EmberbyteType.axes(EmberbyteType.Role.Badge)
        assertEquals(700, badge.wght)
        assertEquals(105f, badge.wdth, 0.001f)
        assertNull(badge.opsz)

        // title: wght 600, wdth 100, opsz 28
        val title = EmberbyteType.axes(EmberbyteType.Role.Title)
        assertEquals(600, title.wght)
        assertEquals(100f, title.wdth, 0.001f)
        assertEquals(28f, title.opsz ?: 0f, 0.001f)

        // heading: wght 600, wdth 100, opsz null
        val heading = EmberbyteType.axes(EmberbyteType.Role.Heading)
        assertEquals(600, heading.wght)
        assertEquals(100f, heading.wdth, 0.001f)
        assertNull(heading.opsz)

        // value: wght 700, wdth 80, opsz null
        val value = EmberbyteType.axes(EmberbyteType.Role.Value)
        assertEquals(700, value.wght)
        assertEquals(80f, value.wdth, 0.001f)
        assertNull(value.opsz)

        // Also verify string-based overload matches
        assertEquals(hero, EmberbyteType.axes("hero"))
        assertEquals(badge, EmberbyteType.axes("badge"))
        assertEquals(title, EmberbyteType.axes("title"))
        assertEquals(heading, EmberbyteType.axes("heading"))
        assertEquals(value, EmberbyteType.axes("value"))
    }

    @Test
    fun grotesk_style_uses_the_bundled_family() {
        val style = EmberbyteType.grotesk(44.sp, 600)
        assertNotNull(style.fontFamily)
        assertEquals(44.sp, style.fontSize)
        assertEquals("tnum", style.fontFeatureSettings)
        assertEquals(style.fontFamily, EmberbyteType.grotesk(20.sp, 600).fontFamily)
        assertEquals(false, style.fontFamily == EmberbyteType.hero(44.sp).fontFamily)
    }

    @Test
    fun grotesk_weight_is_clamped_to_the_font_range() {
        assertEquals(androidx.compose.ui.text.font.FontWeight(400), EmberbyteType.grotesk(20.sp, 100).fontWeight)
        assertEquals(androidx.compose.ui.text.font.FontWeight(700), EmberbyteType.grotesk(20.sp, 900).fontWeight)
    }
}
