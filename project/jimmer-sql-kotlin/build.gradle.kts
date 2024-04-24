plugins {
    `kotlin-convention`
    `dokka-convention`
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    api(projects.jimmerCoreKotlin)
    api(projects.jimmerSql)
    implementation(libs.apache.commons.lang3)

    testImplementation(libs.kotlin.test)
    kspTest(projects.jimmerKsp)
    testAnnotationProcessor(projects.jimmerKsp)

    testImplementation(libs.h2)
    testImplementation(libs.javax.validation.api)
    testImplementation(libs.jackson.datatype.jsr310)
    testImplementation(libs.postgresql)
    testImplementation(libs.spring.jdbc)
    testImplementation(libs.jackson.module.kotlin)
}

ksp {
    arg("jimmer.immutable.isModuleRequired", "true")
    arg("jimmer.dto.mutable", "true")
}

kotlin {
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}

tasks.test {
    useJUnit()
}