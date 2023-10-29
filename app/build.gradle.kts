plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")

}

android {
    namespace = "org.im97mori.myapplication"
    compileSdk = 33

    defaultConfig {
        minSdk = 14

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    api("androidx.fragment:fragment-testing:1.6.1")
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "org.im97mori"
            artifactId = "test-android-fragment"
            version = "0.1.0"

            afterEvaluate {
                from(components["release"])
            }
            pom {
                name.set("Fragment Scenario2(test-android-fragment)")
                url.set("https://github.com/im97mori-github/FragmentScenario2")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        email.set("github@im97mori.org")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            setUrl(System.getenv("MAVEN_REPOSITORY_URL"))
        }
    }
}