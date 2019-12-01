package language.lexer

//Def Token
val defTokenBuilderCondition: TokenBuilderCondition = {
    val peek = this.peekIdentifier()
    val result = peek == "def"
    TokenBuilderConditionResult(peek, result)
}

val defTokenBuilder: TokenBuilderCallback = { _ ->
    buildToken(Tokens.DefToken){
        this.readIdentifier()
    }
}

//Identifier Token
val identTokenCondition: TokenBuilderCondition = {
    val identifier = this.peekIdentifier()
    val result = identifier.matches(Regex("[a-zA-Z][a-zA-Z0-9]*"))
    TokenBuilderConditionResult(identifier, result)
}

val identTokenBuilder: TokenBuilderCallback = { _ ->
    buildToken(Tokens.IdentToken){
        this.readIdentifier()
    }
}

//Equal Sign Token
val equalSignTokenCondition: TokenBuilderCondition = {
    val str = this.currentChar.toString()
    val result = str == "="
    TokenBuilderConditionResult(str, result)
}

val equalSignTokenBuilder: TokenBuilderCallback = { str ->
    buildToken(Tokens.EqualSignToken){
        str
    }
}

//Integer Literal Token
val integerLiteralCondition: TokenBuilderCondition = {
    val identifier = this.peekIdentifier()
    var result = true
    identifier.forEach {
        if(it.isLetter()){
            result = false
        }
    }
    TokenBuilderConditionResult(identifier, result)
}

val integerLiteralTokenBuilder: TokenBuilderCallback = { _ ->
    buildToken(Tokens.IntegerToken){
        this.readIdentifier()
    }
}

fun Lexer.buildToken(tokenType: Tokens, valueBlock: Lexer.()->String): Token{
    val token = Token(tokenType, this.lineIdx, this.column)
    val value = this.valueBlock()
    token.value = value
    return token
}

fun registerTokenBuilders(){
    TokenBuilder.putTokenBuilder(defTokenBuilderCondition, defTokenBuilder)
    TokenBuilder.putTokenBuilder(identTokenCondition, identTokenBuilder)
    TokenBuilder.putTokenBuilder(equalSignTokenCondition, equalSignTokenBuilder)
    TokenBuilder.putTokenBuilder(integerLiteralCondition, integerLiteralTokenBuilder)
}