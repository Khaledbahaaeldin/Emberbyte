package io.github.khaledbahaaeldin.emberbyte.nav

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class PredictiveBackManifestTest {

    @Test
    fun application_enables_the_on_back_invoked_callback() {
        val manifestFile = File("src/main/AndroidManifest.xml")
        assertTrue("AndroidManifest.xml should exist", manifestFile.exists())
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(manifestFile)
        val appNodes = doc.getElementsByTagName("application")
        assertTrue("Manifest must have <application>", appNodes.length > 0)
        val app = appNodes.item(0)
        val enabledAttr = app.attributes.getNamedItem("android:enableOnBackInvokedCallback")
        assertEquals(
            "<application> must declare android:enableOnBackInvokedCallback=\"true\"",
            "true",
            enabledAttr?.nodeValue
        )
    }
}
