import java.io.File

class CouchbaseServerRepositoryCache {
    private val environment = Environment()
    private val repoRepository = RepoRepository(environment.repo_cache)

    fun exists(): Boolean {
        return repoRepository.exists()
    }

    @Throws(Exception::class)
    fun initialize() {
        banner("Initialize repo cache", "=")
        repoRepository.initMirror(environment.manifest, "branch-master.xml")
    }

    fun update() {
        try {
            banner("Sync repo cache with upstream repo", "=")
            syncWithUpstreamRepository()
        } catch (e: Exception) {
            System.err.println("Failed to update cache: $e")
        }
    }

    @Throws(Exception::class)
    private fun syncWithUpstreamRepository() {
        repoRepository.syncWithUpstreamRepository()
    }
}
