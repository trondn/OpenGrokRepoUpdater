import java.io.File

class CouchbaseServerRepositoryCache {
    private val executor = Executor()
    private val environment = Environment()

    fun exists(): Boolean {
        return environment.repo_cache.exists() && File(environment.repo_cache, ".repo").exists()
    }

    @Throws(Exception::class)
    fun initialize() {
        banner("Initialize repo cache", "=")
        if (!environment.repo_cache.mkdirs()) {
            throw Exception("Failed to create directory")
        }

        executor.execute(
            listOf(
                "repo",
                "init",
                "-u",
                environment.manifest.absolutePath,
                "-g",
                "default,-thirdparty",
                "--mirror",
                "-b",
                "master",
                "-m",
                "branch-master.xml",
                "--no-repo-verify",
                "--quiet"
            ), environment.repo_cache
        )
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
        executor.execute(listOf("git", "remote", "update"), File(File(environment.repo_cache, ".repo"), "manifests"))
        executor.execute(
            listOf("git", "reset", "--hard", "origin/master"),
            File(File(environment.repo_cache, ".repo"), "manifests")
        )
        executor.execute(
            listOf("repo", "sync", "--jobs=32", "--quiet"), environment.repo_cache
        )
    }
}
