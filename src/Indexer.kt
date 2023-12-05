import java.io.File

class Indexer {
    private val executor = Executor()
    private val environment = Environment()

    fun update() {
        try {
            val commands = listOf(
                "java",
                "-XX:-UseGCOverheadLimit",
                "-Xmx6g",
                "-Xms6g",
                "-server",
                "-Djava.util.logging.config.file=" + environment.logging_properties.absolutePath,
                "-jar",
                environment.jar.absolutePath,
                "--source",
                environment.source.absolutePath,
                "--dataRoot",
                environment.data.absolutePath,
                "--ignore",
                ".need_sync",
                "--ignore",
                ".locked",
                "--ignore",
                "travel_sample.json",
                "--ignore",
                "compress.js",
                "-m",
                "256",
                "-H",
                "--projects",
                "--defaultProject",
                "/trunk",
                "-S",
                "-G",
                "--writeConfig",
                environment.configuration.absolutePath,
                "-U",
                "http://localhost:8080/source"
            )
            executor.execute(commands, environment.data)
            val timestamp = File(environment.data, "timestamp")
            if (timestamp.exists()) {
                timestamp.delete()
            }
            timestamp.createNewFile()
        } catch (e: Exception) {
            System.err.println("Failed to update index: $e")
        }
    }
}
