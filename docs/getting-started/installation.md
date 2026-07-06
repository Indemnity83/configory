# Installation

Configory is a single-module Java library. It requires **Java 21** and pulls in **Gson** as an
`api` dependency (so Gson is on your compile classpath transitively).

> [!WARNING]
> Configory is not yet published to Maven Central. Until a release is cut, the supported way to
> depend on it is a source build via [JitPack](https://jitpack.io) against the `main` branch.
> A published, versioned artifact is tracked in the project's issue tracker.

## Gradle (JitPack)

Add the JitPack repository and the dependency:

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.Indemnity83:configory:main-SNAPSHOT'
}
```

Once tagged releases exist, replace `main-SNAPSHOT` with the tag, for example
`com.github.Indemnity83:configory:v0.1.0`.

### Kotlin DSL

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.Indemnity83:configory:main-SNAPSHOT")
}
```

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
