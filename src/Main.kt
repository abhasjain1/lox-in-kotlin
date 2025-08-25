import expr.Stmt
import interpreter.Interpreter
import parser.Parser
import scanner.Scanner
import scanner.Token
import java.io.File
import kotlin.system.exitProcess

private var hadError = false

fun main(args: Array<String>) {
    runFile("src/bro.lox")
}

private fun runFile(path : String) {
    run(File(path).readText())
    if (hadError) exitProcess(65)
}

private fun run(source : String) {
    val scanner = Scanner(source)
    val tokens : Array<Token> = scanner.scanTokens()
    // println(tokens.joinToString("\n"))
    val expression : List<Stmt> = Parser(tokens).parse()
    val interpreter : Interpreter = Interpreter()
    interpreter.interpret(expression)
}
