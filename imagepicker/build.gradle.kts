import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.vanniktech.publish)
}

android {
    namespace = "com.akshayashokcode.imagepicker"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

}

dependencies {
    api(project(":mediakit-core"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.exifinterface)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.activity.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(
        groupId = project.findProperty("GROUP_ID") as String,
        artifactId = "imagepicker",
        version = project.findProperty("VERSION_NAME") as String
    )

    pom {
        name.set("MediaKit ImagePicker")
        description.set("Lifecycle-safe gallery and camera image picker for Android. Fluent builder API built on Activity Result APIs.")
        url.set("https://github.com/AkshayAshokCode/MediaKit-android")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("AkshayAshokCode")
                name.set("Akshay Ashok")
                email.set("akshayashokan1054@gmail.com")
                url.set("https://github.com/AkshayAshokCode")
            }
        }
        scm {
            url.set("https://github.com/AkshayAshokCode/MediaKit-android")
            connection.set("scm:git:https://github.com/AkshayAshokCode/MediaKit-android.git")
            developerConnection.set("scm:git:ssh://git@github.com/AkshayAshokCode/MediaKit-android.git")
        }
    }
}
