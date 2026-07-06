# Installation

Configory is a single-module Java library. It requires **Java 21** and pulls in **Gson** as an
`api` dependency (so Gson is on your compile classpath transitively).

Configory is published to the [Modrinth Maven](https://support.modrinth.com/en/articles/8801191-modrinth-maven).
Add the repository and depend on it with the `maven.modrinth` group:

## Gradle

```groovy
repositories {
    mavenCentral()
    maven { url 'https://api.modrinth.com/maven' }
}

dependencies {
    implementation 'maven.modrinth:configory:%%VERSION%%'
}
```

### Kotlin DSL

```kotlin
repositories {
    mavenCentral()
    maven("https://api.modrinth.com/maven")
}

dependencies {
    implementation("maven.modrinth:configory:%%VERSION%%")
}
```

Browse all versions on the [releases page](https://github.com/Indemnity83/configory/releases).

## Requirements

- **Java 21.** Configory targets the Java 21 toolchain.
- **Gson.** Provided transitively; you do not need to declare it yourself unless you use Gson
  directly elsewhere.

## Building from source

Clone and build with the Gradle wrapper:

```bash
git clone https://github.com/Indemnity83/configory.git
cd configory
./gradlew build
```

The wrapper is pinned to JDK 21 for its own build, so `./gradlew` works regardless of your
default `java`.

## Next steps

Head to the [Quick Start](getting-started/quick-start.md) to define and use your first value.
