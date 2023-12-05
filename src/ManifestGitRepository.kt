import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import java.io.FileWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class ManifestGitRepository {
    private val executor = Executor()
    private val environment = Environment()

    fun exists(): Boolean {
        return environment.manifest.exists() && File(environment.manifest, ".git").exists()
    }

    @Throws(Exception::class)
    fun initialize() {
        banner("Initialize manifest repository", "=")
        val dir = environment.manifest.getParentFile()
        if (!dir.exists() && !dir.mkdirs()) {
            throw Exception("Failed to create directory")
        }
        val commands = ArrayList<String>()
        commands.add("git")
        commands.add("clone")
        commands.add("ssh://git@github.com/couchbase/manifest")
        executor.execute(commands, dir)
    }

    fun update() {
        try {
            banner("Sync manifest repository", "=")
            syncWithUpstreamRepository()
            banner("Rewrite manifest files", "=")
            rewriteFiles(getInterestingVersions(environment.manifest))
            banner("Commit manifest files", "=")
            commitChanges()
        } catch (e: Exception) {
            System.err.println("Failed to update manifest repo: $e")
        }
    }

    val interestingVersions: ArrayList<File>
        get() = getInterestingVersions(environment.manifest)

    private fun getInterestingVersions(root: File): ArrayList<File> {
        val ret = ArrayList<File>()
        val files = root.listFiles() ?: return ret
        for (file in files) {
            if (isInteresting(file)) {
                if (file.isDirectory()) {
                    ret.addAll(getInterestingVersions(file))
                } else {
                    ret.add(file)
                }
            }
        }
        return ret
    }

    @Throws(Exception::class)
    private fun syncWithUpstreamRepository() {
        executor.execute(listOf("git", "reset", "--hard", "origin/master"), environment.manifest)
        executor.execute(listOf("git", "remote", "update"), environment.manifest)
        executor.execute(listOf("git", "reset", "--hard", "origin/master"), environment.manifest)
    }

    @Throws(Exception::class)
    private fun commitChanges() {
        executor.execute(listOf("git", "add", "."), environment.manifest)
        executor.execute(
            listOf("git", "commit", "--allow-empty", "--no-verify", "--no-gpg-sign", "-m", "Rewrite manifest files"),
            environment.manifest
        )
    }

    companion object {
        private val eol = listOf(
            "1.6.5.1.xml",
            "1.6.5.2.xml",
            "1.6.5.3.xml",
            "1.6.5.xml",
            "1.7.0.xml",
            "1.7.1.xml",
            "1.7.2.xml",
            "1.8.0.xml",
            "1.8.1.xml",
            "2.0.0-beta-linux.xml",
            "2.0.0-beta-mac.xml",
            "2.0.0-beta-win.xml",
            "2.0.0-dp4-linux-730.xml",
            "2.0.0-dp4-linux.xml",
            "2.0.0-dp4-win.xml",
            "2.0.0.xml",
            "2.0.1-linux.xml",
            "2.0.1-macosx.xml",
            "2.0.1-windows.xml",
            "2.1.0.xml",
            "2.1.1.xml",
            "2.2.0.xml",
            "2.5.0-dp1.xml",
            "2.5.0.xml",
            "2.5.1.1.xml",
            "2.5.1-MP1-A.xml",
            "2.5.1.xml",
            "3.0.0-beta1.xml",
            "3.0.0-beta2.xml",
            "3.0.0-beta3.xml",
            "3.0.0.xml",
            "3.0.1.xml",
            "3.0.2-MP2.xml",
            "3.0.2-MP3.xml",
            "3.0.2.xml",
            "3.0.3-GA.xml",
            "3.0.3-MP1.xml",
            "3.0.3.xml",
            "3.0.4.xml",
            "3.1.0-MP1.xml",
            "3.1.0.xml",
            "3.1.1.xml",
            "3.1.2.xml",
            "3.1.3-MP1.xml",
            "3.1.3.xml",
            "3.1.4-MP1.xml",
            "3.1.4.xml",
            "3.1.5.xml",
            "3.1.6-MP1.xml",
            "3.1.6.xml",
            "4.0.0-beta.xml",
            "4.0.0-dp.xml",
            "4.0.0-rc0.xml",
            "4.0.0.xml",
            "4.1.0-dp.xml",
            "4.1.0.xml",
            "4.1.1-MP1.xml",
            "4.1.1.xml",
            "4.1.2-MP1.xml",
            "4.1.2-MP2.xml",
            "4.1.2.xml",
            "4.5.0-beta.xml",
            "4.5.0-DP1.xml",
            "4.5.0-MP1.xml",
            "4.5.0.xml",
            "4.5.1-MP1.xml",
            "4.5.1-MP2.xml",
            "4.5.1-MP3.xml",
            "4.5.1-MP4.xml",
            "4.5.1-MP5.xml",
            "4.5.1-Win10DP.xml",
            "4.5.1.xml",
            "4.6.0-DP.xml",
            "4.6.0-MP1.xml",
            "4.6.0-MP2.xml",
            "4.6.0.xml",
            "4.6.1-MP1.xml",
            "4.6.1.xml",
            "4.6.2-MP1.xml",
            "4.6.2-MP2.xml",
            "4.6.2-MP3.xml",
            "4.6.2.xml",
            "4.6.3.xml",
            "4.6.4-MP1.xml",
            "4.6.4-MP2.xml",
            "4.6.4.xml",
            "4.6.5.xml",
            "5.0.0-beta2.xml",
            "5.0.0-beta.xml",
            "5.0.0.xml",
            "5.0.1.xml",
            "5.1.0-MP1.xml",
            "5.1.0-MP2.xml",
            "5.1.0-MP3.xml",
            "5.1.0.xml",
            "5.1.1-MP1.xml",
            "5.1.1-SB1.xml",
            "5.1.1.xml",
            "5.1.2.xml",
            "5.1.3-MP1.xml",
            "5.1.3.xml",
            "5.5.0-beta.xml",
            "5.5.0.xml",
            "5.5.1.xml",
            "5.5.2-MP1.xml",
            "5.5.2.xml",
            "5.5.3.xml",
            "5.5.4-MP1.xml",
            "5.5.4.xml",
            "5.5.5-MP1.xml",
            "5.5.5-MP2.xml",
            "5.5.5.xml",
            "5.5.6.xml",
            "6.0.0-beta.xml",
            "6.0.0.xml",
            "6.0.1-MP1.xml",
            "6.0.1.xml",
            "6.0.2.xml",
            "6.0.3-MP1.xml",
            "6.0.3.xml",
            "6.0.4-MP1.xml",
            "6.0.4-MP2.xml",
            "6.0.4.xml",
            "6.0.5.xml",
            "6.5.0-beta2.xml",
            "6.5.0-beta.xml",
            "6.5.0-CE.xml",
            "6.5.0-MP1.xml",
            "6.5.0.xml",
            "6.5.1-MP1.xml",
            "6.5.1-MP2.xml",
            "6.5.1-MP3.xml",
            "6.5.1-MP4.xml",
            "6.5.1-MP5.xml",
            "6.5.1-MP6.xml",
            "6.5.1-MP7.xml",
            "6.5.1-MP8.xml",
            "6.5.1.xml",
            "6.5.2.xml",
            "6.6.0-MP1.xml",
            "6.6.0-MP2.xml",
            "6.6.0-MP3.xml",
            "6.6.0.xml",
            "6.6.1-MP1.xml",
            "6.6.1-MP2.xml",
            "6.6.1.xml",
            "6.6.2-MP1.xml",
            "6.6.2-MP2.xml",
            "6.6.2-MP3.xml",
            "6.6.2.xml",
            "6.6.3-MP1.xml",
            "6.6.3-MP2.xml",
            "6.6.3-MP3.xml",
            "6.6.3-MP4.xml",
            "6.6.3-MP5.xml",
            "6.6.3.xml",
            "6.6.4.xml",
            "6.6.5-MP1.xml",
            "6.6.5-MP10.xml",
            "6.6.5-MP11.xml",
            "6.6.5-MP2.xml",
            "6.6.5-MP3.xml",
            "6.6.5-MP5.xml",
            "6.6.5-MP6.xml",
            "6.6.5-MP7.xml",
            "6.6.5-MP8.xml",
            "6.6.5-MP9.xml",
            "6.6.5-mp4.xml",
            "6.6.5.xml",
            "6.6.6-MP1.xml",
            "6.6.6.xml",
            "7.0.0-beta.xml",
            "alice.xml",
            "basestar-a1.xml",
            "basestar-a2.xml",
            "basestar-b1.xml",
            "branch-1.8-mb-4738.xml",
            "branch-1.8-mb-4901.xml",
            "branch-1.8-relocatable.xml",
            "branch-2.0c-linux.xml",
            "elsa.xml",
            "elixir.xml",
            "sherlock.xml",
            "spock.xml",
            "vulcan.xml",
            "watson.xml"
        )

        private fun isInteresting(file: File): Boolean {
            val name = file.getName()
            if (file.isDirectory()) {
                return name == "released" || name == "couchbase-server"
            }
            if (!name.endsWith(".xml")) {
                return false
            }

            return !eol.contains(name)
        }

        @Throws(Exception::class)
        private fun getDocument(inputFile: File): Document {
            val dbFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = dbFactory.newDocumentBuilder()
            val doc = dBuilder.parse(inputFile)
            doc.documentElement.normalize()
            return doc
        }

        @Throws(Exception::class)
        private fun writeFile(file: File, doc: Document) {
            val domSource = DOMSource(doc)
            val writer = FileWriter(file)
            val result = StreamResult(writer)
            val tf = TransformerFactory.newInstance()
            val transformer = tf.newTransformer()
            transformer.transform(domSource, result)
            writer.write('\n'.code)
            writer.flush()
            writer.close()
        }

        @Throws(Exception::class)
        private fun rewriteFile(file: File) {
            val doc = getDocument(file)
            var modified = false
            val nList = doc.getElementsByTagName("project")
            for (ii in 0 until nList.length) {
                val node = nList.item(ii)
                if (node.nodeName == "project" && node.nodeType == Node.ELEMENT_NODE) {
                    val element = node as Element
                    if (element.hasAttribute("remote") && (element.getAttribute("remote") == "couchbasedeps" || element.getAttribute(
                            "remote"
                        ) == "blevesearch")
                    ) {
                        var groups = "thirdparty"
                        if (element.hasAttribute("groups")) {
                            // We need to add it to the group
                            groups = element.getAttribute("groups") + ",thirdparty"
                        }
                        element.setAttribute("groups", groups)
                        modified = true
                    }
                }
            }
            if (modified) {
                writeFile(file, doc)
            }
        }

        @Throws(Exception::class)
        private fun rewriteFiles(files: ArrayList<File>) {
            for (file in files) {
                rewriteFile(file)
            }
        }
    }
}
