import scanner.Scanner
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

object Lox {
    var hadError: Boolean = false

    fun main(args: Array<String>) {
        when {
            args.size > 1 -> println("Usage: jlox [script]")
            args.size == 1 -> runFile(args[0])
            else -> runPrompt()
        }
    }

    fun runFile(path: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()))
        if (hadError) System.exit(65)
    }

    fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)
        while (true) {
            print("> ")
            val line = reader.readLine() ?: break
            run(line)
            hadError = false
        }
    }

    fun run(source: String) {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()
        for (token in tokens) {
            println(token)
        }
    }

    fun error(line: Int, where: Int = 0,  message: String = "") {
        report(line, where.toString(), message)
    }

    fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] Error at `$where`: $message")
        hadError = true
    }
}

