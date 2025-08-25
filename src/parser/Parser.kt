package parser

import expr.Expr
import expr.Stmt
import scanner.Token
import scanner.TokenType
import scanner.TokenType.*

/**

Parsing is based on the following language grammar (Precedence Low to high going below):

expression     → assignment ;
assignment     → IDENTIFIER "=" assignment | equality ;
equality       → comparison ( ( "!=" | "==" ) comparison )* ;
comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term           → factor ( ( "-" | "+" ) factor )* ;
factor         → unary ( ( "/" | "*" ) unary )* ;
unary          → ( "!" | "-" ) unary
                 | primary ;
primary        → NUMBER | STRING | "true" | "false" | "nil"
                 | "(" expression ")" ;

exprStmt       → expression ";" ;
printStmt      → "print" expression ";" ;

statement      → exprStmt | printStmt | ifStmt | block ;

ifStmt         → "if" "(" expression ")" statement ( "else" statement )? ;

block          → "{" declaration "}" ;

declaration    → varDecl
                | statement ;

varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;

program        → declaration* EOF ;
 */

/**
 * A recursive descent parser to build the AST.
 */
class Parser(private val tokens: Array<Token>) {
    private var current = 0

    fun parse() : List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!isAtEnd()) statements.add(declaration())
        return statements
    }

    private fun expression() : Expr {
        return assignment()
    }

    private fun declaration() : Stmt {
        if (match(VAR)) return varDecl()
        return statement()
    }

    private fun varDecl() : Stmt {
        val name = consume(IDENTIFIER, "Expect variable name..")

        var initializer : Expr? = null
        if (match(EQUAL)) initializer = expression()
        consume(SEMICOLON, "Expect ; after variable declaration.")
        return Stmt.Var(name, initializer)
    }

    private fun equality() : Expr {
        var expr = comparison()
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator : Token = previous()
            val right : Expr = comparison()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun comparison() : Expr {
        var expr = term()
        while (match(GREATER, LESS, GREATER_EQUAL, LESS_EQUAL)) {
            val operator : Token = previous()
            val right : Expr = term()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun term() : Expr {
        var expr = factor()
        while (match(MINUS, PLUS)) {
            val operator : Token = previous()
            val right : Expr = factor()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun factor() : Expr {
        var expr = unary()
        while (match(SLASH, STAR)) {
            val operator : Token = previous()
            val right : Expr = unary()
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun unary() : Expr {
        if (match(BANG, MINUS)) {
            val operator : Token = previous()
            val right : Expr = unary()
            return Expr.Unary(operator, right)
        }
        return primary()
    }

    private fun primary() : Expr {
        if (match(FALSE)) return Expr.Literal(false)
        if (match(TRUE)) return Expr.Literal(true)
        if (match(NIL)) return Expr.Literal(null)
        if (match(NUMBER, STRING)) {
            return Expr.Literal(previous().literal)
        }
        if (match(IDENTIFIER)) {
            return Expr.Variable(previous())
        }
        if (match(LEFT_PAREN)) {
            val expr = expression()
            consume(RIGHT_PAREN, "Expected closing ')")
            return Expr.Grouping(expr)
        }
        error("Expression not valid ${peek()}")
    }

    private fun statement() : Stmt {
        if (match(IF)) return ifStatement()
        if (match(PRINT)) return printStatement()
        if (match(LEFT_BRACE)) return Stmt.Block(block())
        return expressionStatement()
    }

    private fun ifStatement() : Stmt {
        consume(LEFT_PAREN, "Left paren missing after if statement.")
        val expr : Expr = expression()
        consume(RIGHT_PAREN, "Right paren missing for an if statement.")
        val thenStatement : Stmt = statement()
        var elseStatement : Stmt? = null
        if (match(ELSE)) elseStatement = statement()
        return Stmt.If(expr, thenStatement, elseStatement)
    }

    private fun printStatement() : Stmt {
        val value = expression()
        consume(SEMICOLON, "Expected ; after value.")
        return Stmt.Print(value)
    }

    private fun expressionStatement() : Stmt {
        val value = expression()
        consume(SEMICOLON, "Expected ; after value.")
        return Stmt.Expression(value)
    }

    private fun block() : List<Stmt> {
        val statements = mutableListOf<Stmt>()
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration())
        }
        consume(RIGHT_BRACE, "Expect } after block.")
        return statements
    }

    private fun assignment() : Expr {
        val expr : Expr = equality()
        if (match(EQUAL)) {
            val equals : Token = previous()
            val value : Expr = assignment()
            if (expr is Expr.Variable) {
                val name = expr.name
                return Expr.Assign(name, value)
            }
            error(equals)
        }
        return expr
    }

    private fun consume(type : TokenType, message : String) : Token {
        if (check(type)) {
            return advance()
        }
        error("${peek()} $message")
    }

    private fun match(vararg types : TokenType) : Boolean {
        if (types.contains(tokens[current].tokenType)) {
            current++
            return true
        }
        return false
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().tokenType == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun previous() : Token {
        return tokens[current - 1]
    }

    private fun isAtEnd(): Boolean {
        return peek().tokenType == EOF
    }

    private fun peek(): Token {
        return tokens[current]
    }
}