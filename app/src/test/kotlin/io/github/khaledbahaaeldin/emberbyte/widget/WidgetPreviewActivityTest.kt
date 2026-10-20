package io.github.khaledbahaaeldin.emberbyte.widget

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

class WidgetPreviewActivityTest {
    @Test
    fun widget_preview_activity_is_not_in_the_main_manifest() {
        val manifestFile = listOf(
            File("src/main/AndroidManifest.xml"),
            File("app/src/main/AndroidManifest.xml"),
            File("../app/src/main/AndroidManifest.xml")
        ).firstOrNull { it.exists() }
        assertNotNull("src/main/AndroidManifest.xml must exist", manifestFile)
        val content = manifestFile!!.readText()
        assertFalse(
            "WidgetPreviewActivity must not be declared in main AndroidManifest.xml",
            content.contains("WidgetPreviewActivity")
        )
    }
}
