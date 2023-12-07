import java.io.File

class CouchbaseServer(manifestFile: File) {
    @JvmField
    var directory: File
    @JvmField
    val name: String

    private var relativeManifestFilename: String
    private val environment = Environment()
    private val released : Boolean
    private val repoRepository : RepoRepository

    fun exists(): Boolean {
        return repoRepository.exists()
    }

    @Throws(Exception::class)
    fun initialize() {
        banner("Running repo init for $name", "=")
        repoRepository.init(
            environment.manifest,
            environment.repo_cache,
            relativeManifestFilename
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
        directory.mkdirs()
        repoRepository = RepoRepository(directory)
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
        repoRepository.syncWithUpstreamRepository()
        if (released) {
            if (!File(directory, ".locked").createNewFile()) {
                System.err.println("Failed to create lock file for $name")
            }
        }
    }
}
