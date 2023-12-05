import java.io.File

class Environment internal constructor() {
    val manifest: File
    val repo_cache: File
    @JvmField
    val source: File
    val data: File
    val etc: File
    val configuration: File
    val jar: File
    val logging_properties: File

    init {
        val root: File
        root = if (File("/Users/trondnorbye").exists()) {
            File("/Users/trondnorbye/tmp/opengrok")
        } else {
            File("/home/opengrok")
        }
        manifest = File(root, "manifest")
        repo_cache = File(root, "repo.cache")
        source = File(root, "src")
        data = File(root, "data")
        etc = File(root, "etc")
        configuration = File(etc, "configuration.xml")
        jar = File(File(File(root, "dist"), "lib"), "opengrok.jar")
        logging_properties = File(etc, "logging.properties")
    }
}
