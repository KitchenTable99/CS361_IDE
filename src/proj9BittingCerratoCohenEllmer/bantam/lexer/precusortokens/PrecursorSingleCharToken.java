/*
 * File: PrecursorSingleCharToken.java
 * Author: cbitting
 * Date: 4/8/2021
 */
package proj9BittingCerratoCohenEllmer.bantam.lexer.precusortokens;

import proj9BittingCerratoCohenEllmer.bantam.lexer.Token;

import java.util.Stack;

/**
 * If some token is complete as a single symbol, this PrecursorToken contains all the
 * logic needed to tokenize that string.
 */
public class PrecursorSingleCharToken extends AbstractPrecursorToken {

    public PrecursorSingleCharToken(Stack<Character> sc, int n, String s) {
        super(sc, n, s);
        containsCompleteToken = true;
    }

    @Override
    public void pushChar(char c) {
    }

    @Override
    public Token getFinalToken(int currentLineNumber) {
        return new Token(getTokenType(),
            makeStackString(false), currentLineNumber);
    }

    /**
     * Gets the exact token type of the singular character
     *
     * @return the Kind of the token
     */
    private Token.Kind getTokenType() {
        char firstChar = spellingStack.peek();
        switch (firstChar) {
            case '(':
                return Token.Kind.LPAREN;
            case ')':
                return Token.Kind.RPAREN;
            case '{':
                return Token.Kind.LCURLY;
            case '}':
                return Token.Kind.RCURLY;
            case ';':
                return Token.Kind.SEMICOLON;
            case '.':
                return Token.Kind.DOT;
            case ':':
                return Token.Kind.COLON;
            case ',':
                return Token.Kind.COMMA;
            case '\u0000':
                return Token.Kind.EOF;
            default:
                return null;
        }
    }
}
