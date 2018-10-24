buildscript {
    extra["kotlinVersion"] = "1.3.0-rc-198"

    repositories {
        jcenter()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${extra["kotlinVersion"]}")
    }
}

// For JS projects
plugins {
    id("com.moowork.node").version("1.2.0")
}

allprojects {
    repositories {
        jcenter()
    }
}

repositories {
    jcenter()
}
