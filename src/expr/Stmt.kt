package expr

import scanner.Token

abstract class Stmt {

  interface Visitor<R> {
    fun visitExpressionStmt(expr : Expression) : R
    fun visitPrintStmt(expr : Print) : R
    fun visitVarStmt(expr : Var) : R
    fun visitBlockStmt(expr : Block) : R
    fun visitIfStmt(expr : If) : R
  }

  data class Expression(val expression : Expr) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R = visitor.visitExpressionStmt(this)
  }

  data class Print(val expression : Expr) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R = visitor.visitPrintStmt(this)
  }

  data class Var(val name : Token, val  initializer : Expr?) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R = visitor.visitVarStmt(this)
  }

  data class Block(val statements : List<Stmt>) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R = visitor.visitBlockStmt(this)
  }

  data class If(val condition : Expr, val  thenBranch : Stmt, val  elseBranch : Stmt?) : Stmt() {
    override fun <R> accept(visitor: Visitor<R>): R = visitor.visitIfStmt(this)
  }

  abstract fun <R> accept(visitor: Visitor<R>) : R
}
