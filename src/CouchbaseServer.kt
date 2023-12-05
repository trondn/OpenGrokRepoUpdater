import java.io.File

class CouchbaseServer(manifestFile: File) {
    @JvmField
    var directory: File
    @JvmField
    val name: String

    private var relativeManifestFilename: String
    private val environment = Environment()
    private val released : Boolean
    private val executor = Executor()

    fun exists(): Boolean {
        return directory.exists() && File(directory, ".repo").exists()
    }

    @Throws(Exception::class)
    fun initialize() {
        banner("Running repo init for $name", "=")
        if (!directory.exists() && !directory.mkdirs()) {
            throw Exception("Failed to create directory $name")
        }
        executor.execute(
            listOf(
                "repo",
                "init",
                "-u",
                environment.manifest.absolutePath,
                "-g",
                "default,-thirdparty",
                "--reference=" + environment.repo_cache.absolutePath,
                "-b",
                "master",
                "-m",
                relativeManifestFilename,
                "--no-repo-verify",
                "--quiet"
            ), directory
        )
    }

    val isLocked: Boolean
        get() = File(directory, ".locked").exists()

    init {
        val absolutePath = manifestFile.absolutePath
        var index = absolutePath.indexOf("released/")
        if (index == -1) {
            index = absolutePath.indexOf("couchbase-server")
            relativeManifestFilename = if (index == -1) {
                manifestFile.name
            } else {
                absolutePath.substring(index)
            }
            released = false
        } else {
            released = true
            relativeManifestFilename = absolutePath.substring(index)
        }

        // The name of the repository should be the name of the manifest file
        // (without the extension; and "trunk" if the file is branch-master.xml)
        name = if (manifestFile.name == "branch-master.xml") {
            "trunk"
        } else {
            manifestFile.name.substring(0, manifestFile.name.indexOf(".xml"))
        }
        directory = File(environment.source, name)
    }

    fun update() {
        try {
            banner("Update $name", "=")
            syncWithUpstreamRepository()
        } catch (e: Exception) {
            System.err.println("Failed to update $name: $e")
        }
    }

    @Throws(Exception::class)
    private fun syncWithUpstreamRepository() {
        executor.execute(listOf("git", "reset", "--hard", "origin/master"), File(File(directory, ".repo"), "manifests"))
        executor.execute(listOf("git", "remote", "update"), File(File(directory, ".repo"), "manifests"))
        executor.execute(listOf("git", "reset", "--hard", "origin/master"), File(File(directory, ".repo"), "manifests"))
        executor.execute(listOf("repo", "sync", "--jobs=10", "--quiet", "--force-sync"), directory)
        if (released) {
            if (!File(directory, ".locked").createNewFile()) {
                System.err.println("Failed to create lock file for $name")
            }
        }
    }
}
