package interpreter

import environment.Environment
import expr.Expr
import expr.Stmt
import scanner.TokenType.*

class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {

    private var environment = Environment()

    fun interpret(statements : List<Stmt>) {
        statements.forEach { execute(it) }
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Any {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        return when(expr.operator.tokenType) {
            MINUS -> left as Double - right as Double
            SLASH -> left as Double / right as Double
            STAR -> left as Double * right as Double
            PLUS -> {
                if (right is Double && left is Double) return left + right
                else if (right is String && left is String) return left + right
                else throw VerifyError("+ operation is invalid. Invalid data-types")
            }
            GREATER_EQUAL -> left as Double >= right as Double
            EQUAL_EQUAL -> {
                left == right
            }
            LESS_EQUAL -> left as Double <= right as Double
            BANG_EQUAL -> left !== right
            else -> throw VerifyError("Expression ${expr.operator.tokenType} not supported")
        }
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any? {
        return evaluate(expr.expression)
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Any {
       val right = evaluate(expr.right)
       when (expr.operator.tokenType) {
           BANG -> isTruth(right)
           MINUS -> ((right as Double) * -1)
           else -> throw VerifyError("This is unreachable")
       }
        throw VerifyError("This is unreachable")
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Any? {
        return expr.value
    }

    override fun visitVariableExpr(expr: Expr.Variable): Any? {
        return environment.get(expr.name)
    }

    override fun visitAssignExpr(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)
        environment.assign(expr.name, value)
        return value
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        println(evaluate(stmt.expression))
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        environment.define(stmt.name.lexeme, stmt.initializer?.let { evaluate(it) })
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        // This environment is created for the current block.
        executeBlock(stmt.statements, Environment(environment))
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        if (isTruth(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
    }

    private fun isTruth(expr : Any?) : Boolean {
        return when (expr) {
            is Boolean -> expr
            null -> false
            else -> true
        }
    }

    private fun evaluate(expression: Expr) : Any? {
        return expression.accept(this)
    }

    private fun execute(stmt : Stmt) : Any {
        return stmt.accept(this)
    }

    private fun executeBlock(statements: List<Stmt>, environment: Environment) {
        val previousEnv = this.environment
        try {
            // Run statements in the new environment
            this.environment = environment
            statements.forEach { execute(it) }
        } finally {
            // Revert back to previous environment
            this.environment = previousEnv
        }
    }
}