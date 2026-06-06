import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `java-platform`
    alias(libs.plugins.vanniktech.publish)
}

val GROUP = project.findProperty("GROUP_ID") as String
val VERSION = project.findProperty("VERSION_NAME") as String

dependencies {
    constraints {
        api("$GROUP:mediakit-core:$VERSION")
        api("$GROUP:imagepicker:$VERSION")
        api("$GROUP:imagecropper:$VERSION")
        api("$GROUP:mediapicker:$VERSION")
        api("$GROUP:imagecompressor:$VERSION")
        api("$GROUP:audiorecorder:$VERSION")
        api("$GROUP:videocompressor:$VERSION")
        api("$GROUP:mediapreviewer:$VERSION")
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates(groupId = GROUP, artifactId = "mediakit-bom", version = VERSION)
    pom {
        name.set("MediaKit BOM")
        description.set("Bill of Materials for MediaKit-Android. Import this platform to align all MediaKit module versions automatically.")
        url.set("https://github.com/AkshayAshokCode/MediaKit-android")
        licenses { license { name.set("MIT License"); url.set("https://opensource.org/licenses/MIT") } }
        developers { developer { id.set("AkshayAshokCode"); name.set("Akshay Ashok"); email.set("akshayashokan1054@gmail.com"); url.set("https://github.com/AkshayAshokCode") } }
        scm { url.set("https://github.com/AkshayAshokCode/MediaKit-android"); connection.set("scm:git:https://github.com/AkshayAshokCode/MediaKit-android.git"); developerConnection.set("scm:git:ssh://git@github.com/AkshayAshokCode/MediaKit-android.git") }
    }
}
