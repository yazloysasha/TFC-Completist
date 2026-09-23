plugins {
  id("net.neoforged.moddev") version "2.0.107"
}

val minecraftVersion: String = "1.21.1"
val neoForgeVersion: String = "21.1.234"
val minTfcVersion: String = "4.1.0"
val maxTfcVersion: String = "4.2.10"

val modId: String = "tfc_completist"
val modVersion: String = System.getenv("VERSION") ?: "0.0.0-indev"
val modJavaVersion: String = "21"

val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
  val modReplacementProperties = mapOf(
    "modId" to modId,
    "modVersion" to modVersion,
    "minecraftVersionRange" to "[$minecraftVersion]",
    "neoForgeVersionRange" to "[$neoForgeVersion,)",
    "tfcVersionRange" to "[$minTfcVersion,)",
  )
  inputs.properties(modReplacementProperties)
  expand(modReplacementProperties)
  from("src/main/templates")
  into(layout.buildDirectory.dir("generated/sources/modMetadata"))
}

neoForge {
  version = neoForgeVersion
}

base {
  archivesName.set("TFC-Completist-NeoForge-$minecraftVersion")
  group = "net.yazloysasha.tfccompletist"
  version = modVersion
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(modJavaVersion))
}

repositories {
  mavenCentral()
  ivy {
    url = uri("https://github.com/TerraFirmaCraft/TerraFirmaCraft/releases/download")
    patternLayout {
      artifact("/v[revision]/[artifact]-[revision].[ext]")
    }
    metadataSources {
      artifact()
    }
  }
}

sourceSets {
  main {
    resources {
      srcDir(generateModMetadata)
    }
  }
}

neoForge {
  validateAccessTransformers = true

  runs {
    configureEach {
      jvmArguments.addAll("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition", "-ea")
    }
    register("client") {
      client()
      gameDirectory = file("run/client")
    }
    register("server") {
      server()
      gameDirectory = file("run/server")
      programArgument("--nogui")
    }
  }

  mods {
    create(modId) {
      sourceSet(sourceSets.main.get())
    }
  }

  ideSyncTask(generateModMetadata)
}

dependencies {
  compileOnly("net.dries007.tfc:TerraFirmaCraft-NeoForge-$minecraftVersion:$maxTfcVersion@jar")
}

tasks {
  jar {
    manifest {
      attributes["Implementation-Version"] = project.version
    }
  }

  named("neoForgeIdeSync") {
    dependsOn(generateModMetadata)
  }
}
