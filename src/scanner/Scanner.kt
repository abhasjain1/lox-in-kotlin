package scanner

import scanner.TokenType.*

class Scanner(private val source : String) {
    private var tokens = mutableListOf<Token>();

    private val keywords = mapOf(
        "and" to AND,
        "class" to CLASS,
        "else" to ELSE,
        "false" to FALSE,
        "for" to FOR,
        "fun" to FUN,
        "if" to IF,
        "nil" to NIL,
        "or" to OR,
        "print" to PRINT,
        "return" to RETURN,
        "super" to SUPER,
        "this" to THIS,
        "true" to TRUE,
        "var" to VAR,
        "while" to WHILE
    )

    private var start = 0
    private var current = 0
    private var lineNumber = 1
    private var line = 0

    fun scanTokens() : Array<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }
        tokens.add(Token(EOF, "", null, lineNumber))
        return tokens.toTypedArray()
    }

    private fun scanToken() {
        when (val c = nextChar()) {
            '(' -> addToken(LEFT_PAREN);
            ')' -> addToken(RIGHT_PAREN);
            '{' -> addToken(LEFT_BRACE);
            '}' -> addToken(RIGHT_BRACE);
            ',' -> addToken(COMMA);
            '.' -> addToken(DOT);
            '-' -> addToken(MINUS);
            '+' -> addToken(PLUS);
            ';' -> addToken(SEMICOLON);
            '*' -> addToken(STAR);
            '!' -> if (matchNext('=')) addToken(BANG_EQUAL) else addToken(BANG)
            '=' -> if (matchNext('=')) addToken(EQUAL_EQUAL) else addToken(EQUAL)
            '/' -> if (matchNext('/')) {
                        while (peek() != '\n' && !isAtEnd()) nextChar();
                    } else addToken(TokenType.SLASH)
            '<' -> if (matchNext('=')) addToken(LESS_EQUAL) else addToken(LESS)
            '>' -> if (matchNext('=')) addToken(GREATER_EQUAL) else addToken(GREATER)
            '"' -> string()
            in '0'..'9' -> number()
            ' ', '\r', '\t', '\n' -> Unit
            else -> if (isAlphabet(c)) identifier()
                    else {
                        println(c)
                        throw VerifyError("Error occured at line: $line.")
                    }
        }
    }

    private fun isAtEnd() : Boolean {
        return current >= source.length
    }

    private fun nextChar() : Char {
        return source[current++]
    }

    private fun peek() : Char {
        if (isAtEnd()) return '\n';
        return source[current]
    }

    private fun addToken(type : TokenType) {
       addToken(type, null)
    }

    private fun addToken(type: TokenType, literal : Any?) {
        tokens.add(Token(type, source.substring(start, current), literal, line))
    }

    private fun matchNext(expected : Char) : Boolean {
        if (isAtEnd() || source[current] != expected) return false
        current++
        return true
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            nextChar()
        }

        if (isAtEnd()) {
            throw VerifyError("Unterminated string..")
        }

        nextChar()
        addToken(TokenType.STRING, source.substring(start + 1, current - 1))
    }

    private fun number () {
        while (peek() in '0'..'9' || peek() == '.') {
            nextChar()
        }
        addToken(TokenType.NUMBER, source.substring(start, current).toDouble())
    }

    private fun isAlphabet(char : Char) : Boolean {
        return char == '_' || char.lowercaseChar() in 'a'..'z'
    }

    private fun identifier() {
        while (isAlphanumeric(peek())) nextChar()
        val text = source.substring(start, current)
        addToken(keywords[text] ?: IDENTIFIER)
    }

    private fun isAlphanumeric(char : Char) : Boolean {
        return isAlphabet(char) || char in '0'..'9'
    }
}

