import java.io.BufferedInputStream
import java.io.File
import java.io.InputStreamReader
import java.io.StringWriter

class Executor {
    @Throws(Exception::class)
    fun execute(commands: List<String>, directory: File): Int {
        print("Execute: ")
        for (c in commands) {
            if (c.indexOf(' ') > 0) {
                print("'$c' ")
            } else {
                print("$c ")
            }
        }
        println("in $directory")
        if (!directory.exists() && !directory.mkdir()) {
            throw Exception("Failed to create directory $directory")
        }
        val builder = ProcessBuilder()
        builder.command(commands)
        builder.directory(directory)
        val process = builder.start()
        val output = StringWriter()
        val error = StringWriter()
        val os = InputStreamReader(BufferedInputStream(process.inputStream))
        val es = InputStreamReader(BufferedInputStream(process.errorStream))
        while (process.isAlive) {
            // we don't want to busy-wait
            Thread.sleep(1000)
            while (os.ready()) {
                output.write(os.read())
            }
            while (es.ready()) {
                error.write(es.read())
            }
        }
        if (output.buffer.isNotEmpty()) {
            println("Process output: ")
            println(output.buffer.toString())
            println("EOF")
        }
        if (error.buffer.isNotEmpty()) {
            System.err.println("Process error: ")
            System.err.println(error.buffer.toString())
            println("EOF")
        }
        return process.waitFor()
    }
}