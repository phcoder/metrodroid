package au.id.micolous.metrodroid.test

import au.id.micolous.metrodroid.card.Card
import au.id.micolous.metrodroid.serializers.XmlFormat
import au.id.micolous.metrodroid.util.ConcurrentFileReader
import au.id.micolous.metrodroid.util.Input
import au.id.micolous.metrodroid.util.Preferences
import platform.Foundation.NSBundle
import platform.Foundation.NSFileManager
import platform.Foundation.NSLocale
import platform.Foundation.NSURL
import kotlin.test.assertNotNull

private fun fullPathForAsset(path: String): String = NSBundle.mainBundle.resourcePath + "/$path"

actual fun loadCardXml(path: String): Card {
    var ret: Card? = null
    println("path = $path")
    val fullPath = fullPathForAsset(path)
    println("fullPath = $fullPath")
    val url = NSURL(fileURLWithPath = fullPath)
    println("url = $url")
    XmlFormat.readXmlFromUrl(url) {
        card -> if(ret == null)
            ret = card
    }
    assertNotNull(ret)
    return ret!!
}

actual abstract class BaseInstrumentedTestPlatform actual constructor() {
    actual fun setLocale(languageTag: String) {
        Preferences.languageOverrideForTest.value = languageTag.substringBefore("-")
        Preferences.localeOverrideForTest.value = NSLocale(localeIdentifier = languageTag)
    }

    actual fun loadAssetSafe(path: String): Input? =
        ConcurrentFileReader.openFile(fullPathForAsset(path))?.makeInput()
}
