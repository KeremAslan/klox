package scanner


class Scanner(
    private val source: String
) {
    private val tokens: MutableList<Token> = mutableListOf()
    private var start = 0
    private var current = 0
    private var line = 1
    companion object ReservedKeywords {
        val keywords  = mapOf(
            "and" to TokenType.AND,
            "class" to TokenType.CLASS,
            "else" to TokenType.ELSE,
            "false" to TokenType.FALSE,
            "for" to TokenType.FOR,
            "fun" to TokenType.FUN,
            "if" to TokenType.IF,
            "nil" to TokenType.NIL,
            "or" to TokenType.OR,
            "print" to TokenType.PRINT,
            "return" to TokenType.RETURN,
            "super" to TokenType.SUPER,
            "this" to TokenType.THIS,
            "true" to TokenType.TRUE,
            "var" to TokenType.VAR,
            "while" to TokenType.WHILE
        )
    }

    fun scanTokens() : List<Token> {
        while(!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun isAtEnd() = current >= source.length

    private fun addToken(type: TokenType) = addToken(type, null)
    private fun addToken(type: TokenType, literal: Any?) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun advance() : Char {
        current++
        return source[current-1]
    }

    private fun scanToken() {
        when(val c = advance()) {
            'C' -> addToken(TokenType.LEFT_PAREN)
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '+' -> addToken(TokenType.PLUS)
            ';' -> addToken(TokenType.SEMICOLON)
            '*' -> addToken(TokenType.STAR)
            '!' -> addToken(if (match('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            '=' -> addToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            '<' -> addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> addToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)
            '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance()
                } else {
                    addToken(TokenType.SLASH)
                }
            }
            ' ', '\r', '\t' -> return
            '\n' -> line++
            '"' -> string()
            else -> {
                when {
                    isDigit(c) -> number()
                    isAlpha(c) -> identifier()
                    else -> Lox.error(line,  current,"Unexpected character.")

                }
            }
        }
    }

    private fun peek() : Char = if (isAtEnd()) 0.toChar() else source[current]

    /**
     *
     */
    private fun match(expected: Char) : Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false
        current++
        return true
    }

    /**
     * Consume Strings
     */
    private fun string() {
        while (peek() != '"' && !isAtEnd())  {
            if (peek() == '\n') line++ else advance()
        }

        // string misses quotes, i.e. is unterminated string
        if (isAtEnd()) {
            Lox.error(line, current, "Unterminated string.")
            return
        }
        // the closing "
        advance()
        // trim quotes
        val value = source.substring(start+1, current-1)
        addToken(TokenType.STRING, value)
    }

    /**
     * Checks if char is a digit
     */
    private fun isDigit(c: Char) = c in '0'..'9'

    private fun identifier() {
        while (isAlphaNumeric(peek())) advance()
        val text = source.substring(start, current)
        val type : TokenType = keywords[text] ?: TokenType.IDENTIFIER
        addToken(type)
    }

    private fun isAlpha(c: Char) = c in 'a'..'z' || c in 'A'..'Z' || c == '_'
    private fun isAlphaNumeric(c: Char) = isAlpha(c) || isDigit(c)

    private fun number() {
        fun peekNext()  = if (current + 1 >= source.length) '0' else source[current+1]

        while (isDigit(peek())) advance()
        // if not digit look for fractional part
        if (peek() == '.' && isDigit(peekNext())) {
            advance()
            while (isDigit(peek())) advance()
        }

        addToken(TokenType.NUMBER, source.substring(start, current).toDouble())
    }


}

