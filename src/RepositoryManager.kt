import java.io.File
import kotlin.system.exitProcess

class RepositoryManager internal constructor() {
    private val manifestGitRepository = ManifestGitRepository()
    private var repositories: ArrayList<CouchbaseServer>? = arrayListOf()
    private val couchbaseServerRepositoryCache = CouchbaseServerRepositoryCache()
    private val environment = Environment()
    private val indexer = Indexer()

    init {
        if (!manifestGitRepository.exists()) {
            try {
                manifestGitRepository.initialize()
                manifestGitRepository.update()
            } catch (exception: Exception) {
                System.err.println("Fatal error. Failed to create system repository: $exception")
                exitProcess(1)
            }
        }
        if (!couchbaseServerRepositoryCache.exists()) {
            try {
                couchbaseServerRepositoryCache.initialize()
            } catch (exception: Exception) {
                System.err.println("Fatal error. Failed to create repo.cache: $exception")
                exitProcess(1)
            }
        }
    }

    fun refresh() {
        manifestGitRepository.update()
        couchbaseServerRepositoryCache.update()
        banner("Detect repositories")
        repositories = arrayListOf()
        for (file in manifestGitRepository.interestingVersions) {
            val r = CouchbaseServer(file)
            try {
                if (!r.exists()) {
                    r.initialize()
                }
                repositories!!.add(r)
            } catch (exception: Exception) {
                System.err.println("Failed to initialize " + r.name + ": " + exception)
            }
        }

        pruneObsoleteRepositories()
        banner("Update repositories")
        for (r in repositories!!.sortedBy { it.directory.name }) {
            if (r.isLocked) {
                println(r.name + " - skipping")
            } else {
                r.update()
            }
        }

        banner("Running indexer")
        indexer.update()
    }

    private fun containsRepo(file: File): Boolean {
        for (r in repositories!!) {
            if (file.name == r.directory.name) {
                return true
            }
        }
        return false
    }

    private fun pruneObsoleteRepositories() {
        banner("Prune old directories")
        val files = environment.source.listFiles() ?: return
        for (f in files) {
            if (!containsRepo(f)) {
                println("  " + f.name + " - Removing obsolete")
                f.walkBottomUp().forEach{it.delete()}
            }
        }
    }
}
