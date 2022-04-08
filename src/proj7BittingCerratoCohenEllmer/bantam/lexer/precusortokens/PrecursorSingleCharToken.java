package proj7BittingCerratoCohenEllmer.bantam.lexer.precusortokens;

import proj7BittingCerratoCohenEllmer.bantam.lexer.Token;

import java.util.Stack;

public class PrecursorSingleCharToken extends AbstractPrecursorToken {

    public PrecursorSingleCharToken(Stack<Character> sc, int n, String s) {
        super(sc, n, s);
    }

    @Override
    public void pushChar(char c) {
        spellingStack.push(c);
        containsCompleteToken = true;
    }

    @Override
    public Token getFinalToken(int currentLineNumber) {
        return new Token(getTokenType(), makeStackString(false), currentLineNumber);
    }

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
            case '!':
                return Token.Kind.UNARYNOT;
            case '.':
                return Token.Kind.DOT;
            case ':':
                return Token.Kind.COLON;
            case ',':
                return Token.Kind.COMMA;
            default:
                return null;
        }
    }
}
