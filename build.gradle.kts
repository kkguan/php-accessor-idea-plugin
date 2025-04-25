fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.13.3"
    id("org.jetbrains.changelog") version "2.0.0"
}

group = "com.free2one"
version = properties("pluginVersion")

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set(properties("intellijVersion"))
    type.set("PS") // Target IDE Platform

    plugins.set(listOf("com.jetbrains.php"))
}

changelog {
    version.set(properties("pluginVersion"))
    groups.set(emptyList())
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    patchPluginXml {
        sinceBuild.set("231")
        untilBuild.set("251.*")
    }

    patchPluginXml {
        version.set(properties("pluginVersion"))
        changeNotes.set(
            file("src/main/resources/META-INF/change-notes.html").readText().replace("<html>", "")
                .replace("</html>", "")
        )
//        changeNotes.set(provider {
//            changelog.run {
//                getOrNull(properties("pluginVersion")) ?: getLatest()
//            }.toHTML()
//        })
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}


