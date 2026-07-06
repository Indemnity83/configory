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

## As a dependency mod

Configory ships as an **installed library mod**: one combined jar that loads on both **Fabric** and
**NeoForge** and provides its classes to any mod that depends on it. Users install the Configory jar
alongside your mod (installing it separately is preferred over bundling, so several mods can share a
single copy).

Declare the runtime dependency so the loader ensures Configory is present:

**Fabric** — in your `fabric.mod.json`:

```json
"depends": {
  "configory": "*"
}
```

**NeoForge** — in your `META-INF/neoforge.mods.toml`:

```toml
[[dependencies.yourmodid]]
modId = "configory"
type = "required"
versionRange = "[0.1,)"
ordering = "NONE"
side = "BOTH"
```

Configory has no Minecraft dependency and no version-specific code, so a single jar works across all
supported Minecraft versions on both loaders.

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
