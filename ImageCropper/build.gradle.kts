plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
    signing
}

android {
    namespace = "com.akshayashokcode.imagecropper"
    compileSdk = 35

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

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    api(project(":imagepicker"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = project.findProperty("GROUP_ID") as String
                artifactId = "imagecropper"
                version = project.findProperty("VERSION_NAME") as String

                pom {
                    name.set("MediaKit ImageCropper")
                    description.set("Touch-driven image cropping for Android. Custom CropperView with rule-of-thirds grid, corner handles, and state restoration. Integrates with MediaKit ImagePicker or standalone.")
                    url.set("https://github.com/AkshayAshokCode/MediaKit-android")
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    developers {
                        developer {
                            id.set("akshayashokcode")
                            name.set("Akshay Ashok")
                            email.set("akshayashokan1054@gmail.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:github.com/AkshayAshokCode/MediaKit-android.git")
                        developerConnection.set("scm:git:ssh://github.com/AkshayAshokCode/MediaKit-android.git")
                        url.set("https://github.com/AkshayAshokCode/MediaKit-android/tree/main")
                    }
                }
            }
        }

        repositories {
            maven {
                name = "sonatype"
                url = uri(
                    if ((project.findProperty("VERSION_NAME") as String).endsWith("SNAPSHOT"))
                        "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                    else
                        "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                )
                credentials {
                    username = project.findProperty("OSSRH_USERNAME") as String? ?: ""
                    password = project.findProperty("OSSRH_PASSWORD") as String? ?: ""
                }
            }
        }
    }

    signing {
        val signingKeyId = project.findProperty("SIGNING_KEY_ID") as String?
        val signingKey = project.findProperty("SIGNING_KEY") as String?
        val signingPassword = project.findProperty("SIGNING_PASSWORD") as String?
        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
        sign(publishing.publications["release"])
    }
}
