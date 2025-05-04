plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("kapt") version "1.9.22"
    id("io.freefair.aspectj.post-compile-weaving") version "8.4"
    java
    `maven-publish`
    signing
}

group = "io.github.daniloassuncao"
version = "0.1.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.11")

    // AOP
    implementation("org.aspectj:aspectjweaver:1.9.21")
    implementation("org.aspectj:aspectjrt:1.9.21")
    kapt("org.aspectj:aspectjweaver:1.9.21")

    // Jakarta
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.8.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("ch.qos.logback:logback-classic:1.4.14")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    compileJava {
        targetCompatibility = "17"
        sourceCompatibility = "17"
    }

    test {
        useJUnitPlatform()
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            pom {
                name.set("Aspect Log Interceptor")
                description.set("A Kotlin library that provides AOP-based method logging using annotations")
                url.set("https://github.com/daniloassuncao/aspect-log-interceptor")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("daniloassuncao")
                        name.set("Danilo Assunção")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/daniloassuncao/aspect-log-interceptor.git")
                    developerConnection.set("scm:git:ssh://github.com/daniloassuncao/aspect-log-interceptor.git")
                    url.set("https://github.com/daniloassuncao/aspect-log-interceptor")
                }
            }
        }
    }
} 