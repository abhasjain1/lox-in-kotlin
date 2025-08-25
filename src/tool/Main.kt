package tool

import java.io.PrintWriter

fun main(args: Array<String>) {
    defineAst("Expr", arrayOf(
        "Binary   ->  left : Expr, operator : scanner.Token, right : Expr",
        "Grouping ->  expression : Expr",
        "Unary    ->  operator : scanner.Token, right : Expr",
        "Literal  ->  value : Any?",
        "Variable ->  name : Token",
        "Assign   ->  name : Token, value : Expr"
    ))
    defineAst("Stmt", arrayOf(
        "Expression -> expression : Expr",
        "Print      -> expression : Expr",
        "Var        -> name : Token, initializer : Expr?", // The expression can be null (to represent nil)
        "Block      -> statements : List<Stmt>",
        "If         -> condition : Expr, thenBranch : Stmt, elseBranch : Stmt?"
    ))
}

private fun defineAst(baseClassName : String, types: Array<String>) {

    val printWriter = PrintWriter("src/expr/$baseClassName.kt")

    printWriter.println("package expr")
    printWriter.println()
    printWriter.println("import scanner.Token")
    printWriter.println()
    printWriter.println("abstract class $baseClassName {\n")

    defineVisitor(baseClassName, types.map { it.split("->")[0].trim() }, printWriter)

    types.forEach {
        defineType(baseClassName, it.split("->")[0].trim(), it.split("->")[1].trim(), printWriter)
    }
    printWriter.println("  abstract fun <R> accept(visitor: Visitor<R>) : R")
    printWriter.println("}")
    printWriter.close()
}

private fun defineVisitor(baseClassName : String, classNames: List<String>, printWriter: PrintWriter) {
    printWriter.println("  interface Visitor<R> {")
    classNames.forEach {
        printWriter.println("    fun visit${it}${baseClassName}(expr : $it) : R")
    }
    printWriter.println("  }\n")
}

// Maybe I should've let auto-format do the formatting for me
private fun defineType(baseClassName : String, className: String, fields: String, printWriter: PrintWriter) {
    printWriter.print("  data class $className(")
    val fieldPrinter = mutableListOf<String>()
    fields.split(",").forEach {
        fieldPrinter.add("val $it")
    }
    printWriter.print(fieldPrinter.joinToString(", "))
    printWriter.println(") : $baseClassName() {")
    printWriter.println("    override fun <R> accept(visitor: Visitor<R>): R = visitor.visit$className$baseClassName(this)")
    printWriter.println("  }\n")
}
