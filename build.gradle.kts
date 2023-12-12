plugins {
    kotlin("jvm") version "1.9.21"
}

sourceSets {
    main {
        kotlin.srcDir("src")
    }
}

dependencies {
    // https://mvnrepository.com/artifact/org.apache.commons/commons-math3
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.7.3"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
}

tasks {
    wrapper {
        gradleVersion = "8.5"
    }
}
