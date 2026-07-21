plugins {
    id("maven-publish")
    id("java")
}

group = "com.tonic.jaloc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:26.0.2")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Javadoc>("docSite") {
    group = "documentation"
    description = "Generates the public API docsite into docs/"
    dependsOn(tasks.compileJava)
    source = sourceSets.main.get().allJava
    exclude("com/tonic/jaloc/demo/**", "com/tonic/jaloc/memory/core/**", "com/tonic/jaloc/memory/internal/**")
    classpath = sourceSets.main.get().output.classesDirs + sourceSets.main.get().compileClasspath
    setDestinationDir(file("docs"))
    outputs.upToDateWhen { false }
    (options as StandardJavadocDocletOptions).apply {
        memberLevel = JavadocMemberLevel.PUBLIC
        windowTitle = "Jaloc"
        docTitle = "Jaloc $version"
        encoding = "UTF-8"
    }
    doFirst {
        delete("docs")
    }
    doLast {
        copy {
            from("doc-assets/javadoc-dark.css")
            into("docs")
        }
        val docsDir = file("docs")
        docsDir.walkTopDown().filter { it.isFile && it.extension == "html" }.forEach { page ->
            val depth = generateSequence(page.parentFile) { it.parentFile }
                .takeWhile { it != docsDir }
                .count()
            val href = "../".repeat(depth) + "javadoc-dark.css"
            val content = page.readText(Charsets.UTF_8)
            if (content.contains("</head>") && !content.contains("javadoc-dark.css")) {
                page.writeText(
                    content.replace("</head>", "<link rel=\"stylesheet\" type=\"text/css\" href=\"$href\">\n</head>"),
                    Charsets.UTF_8
                )
            }
        }
    }
}