import java.io.File

class RepoRepository(dir: File) {
    private val executor = Executor()
    private val directory: File = dir
    private val manifestGitRepository = GitRepository(File(File(dir, ".repo"), "manifests"))

    fun exists(): Boolean {
        return directory.exists() && File(directory, ".repo").exists()
    }

    private fun doInit(manifestDirectory: File,
                       mirrorOption: String,
                       manifestFile: String) {
        executor.execute(
            listOf(
                "repo",
                "init",
                "-u",
                manifestDirectory.absolutePath,
                "-g",
                "default,-thirdparty",
                mirrorOption,
                "-b",
                "master",
                "-m",
                manifestFile,
                "--no-repo-verify",
                "--quiet"
            ), directory
        )
    }

    fun initMirror(manifestDirectory: File, manifestFile: String) {
        doInit(manifestDirectory, "--mirror", manifestFile)
    }

    fun init(
        manifestDirectory: File,
        mirrorLocation: File,
        manifestFile: String
    ) {
        doInit(
            manifestDirectory, "--reference=" + mirrorLocation.absolutePath,
            manifestFile
        )
    }


    fun syncWithUpstreamRepository() {
        manifestGitRepository.resetToUpstream()
        executor.execute(
            listOf("repo", "sync", "--jobs=32", "--quiet", "--force-sync"), directory
        )
    }


}