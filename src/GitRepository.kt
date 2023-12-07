import java.io.File

class GitRepository(dir: File) {
    private val executor = Executor()
    private val directory : File = dir

    fun exists() : Boolean {
        return directory.exists() && File(directory, ".git").exists()
    }

    fun clone(upstream: String) {
        val commands = ArrayList<String>()
        commands.add("git")
        commands.add("clone")
        commands.add("ssh://git@github.com/couchbase/manifest")
        executor.execute(
            listOf(
                "git",
                "clone",
                upstream
            ), directory.parentFile
        )
    }

    fun reset(branchName: String = "origin/master") {
        executor.execute(listOf("git", "reset", "--quiet", "--hard", branchName), directory)
    }

    fun update() {
        executor.execute(listOf("git", "remote", "update"), directory)
    }

    fun resetToUpstream() {
        reset()
        update()
        reset()
    }

    fun commitChanges(commitMessage: String) {
        executor.execute(listOf("git", "add", "."), directory)
        executor.execute(
            listOf("git", "commit", "--allow-empty", "--no-verify", "--no-gpg-sign", "-m", commitMessage),
            directory
        )
    }
}