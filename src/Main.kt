import kotlin.system.exitProcess

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val manager = RepositoryManager()
            manager.refresh()
        } catch (exception: Exception) {
            System.err.println("FATAL ERROR: $exception")
            exitProcess(1)
        }
    }
}
