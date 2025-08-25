package environment

import scanner.Token

class Environment(private val enclosing : Environment? = null) {
    private val values = hashMapOf<String, Any?>()

    fun define(name : String, value: Any?) {
        values[name] = value
    }

    fun assign(token : Token, value: Any?) {
        if (values.containsKey(token.lexeme)) {
            values[token.lexeme] = value;
            return;
        }
        enclosing?.assign(token, value)
        throw RuntimeException("Undefined variable ${token.lexeme} .")
    }

    fun get(token : Token) : Any? {
        if (values.containsKey(token.lexeme)) {
            return values[token.lexeme]
        }
        if (enclosing != null) return enclosing.get(token)
        throw RuntimeException("Undefined variable ${token.lexeme}.")
    }
}