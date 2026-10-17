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

        // badge: wght 850, wdth 110, opsz null
        val badge = EmberbyteType.axes(EmberbyteType.Role.Badge)
        assertEquals(850, badge.wght)
        assertEquals(110f, badge.wdth, 0.001f)
        assertNull(badge.opsz)

        // title: wght 800, wdth 40, opsz 144
        val title = EmberbyteType.axes(EmberbyteType.Role.Title)
        assertEquals(800, title.wght)
        assertEquals(40f, title.wdth, 0.001f)
        assertEquals(144f, title.opsz ?: 0f, 0.001f)

        // heading: wght 800, wdth 105, opsz null
        val heading = EmberbyteType.axes(EmberbyteType.Role.Heading)
        assertEquals(800, heading.wght)
        assertEquals(105f, heading.wdth, 0.001f)
        assertNull(heading.opsz)

        // value: wght 850, wdth 75, opsz null
        val value = EmberbyteType.axes(EmberbyteType.Role.Value)
        assertEquals(850, value.wght)
        assertEquals(75f, value.wdth, 0.001f)
        assertNull(value.opsz)

        // Also verify string-based overload matches
        assertEquals(hero, EmberbyteType.axes("hero"))
        assertEquals(badge, EmberbyteType.axes("badge"))
        assertEquals(title, EmberbyteType.axes("title"))
        assertEquals(heading, EmberbyteType.axes("heading"))
        assertEquals(value, EmberbyteType.axes("value"))
    }
}
