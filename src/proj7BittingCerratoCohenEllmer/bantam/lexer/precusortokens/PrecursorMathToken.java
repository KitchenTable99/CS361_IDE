package proj7BittingCerratoCohenEllmer.bantam.lexer.precusortokens;

import proj7BittingCerratoCohenEllmer.bantam.lexer.Token;

import java.util.Stack;

public class PrecursorMathToken extends AbstractPrecursorToken {

    public PrecursorMathToken(Stack<Character> sc, int n, String s) {
        super(sc, n, s);
    }

    @Override
    public void pushChar(char c) {
        spellingStack.push(c);

        if (Character.isWhitespace(c) ||
                Character.isAlphabetic(c) ||
                Character.isDigit(c) ||
                c == ';') {
            popLastBeforeCreation = true;
            containsCompleteToken = true;
        } else if (makeStackString(true).equals("++")
                || makeStackString(true).equals("--")
                || c == '=') {
            containsCompleteToken = true;
        }
    }

    @Override
    public Token getFinalToken(int currentLineNumber) throws MalformedSpellingStackException {
        if (popLastBeforeCreation) {
            throw new MalformedSpellingStackException("You need to pop the stack first");
        }

        return new Token(getTokenKind(), makeStackString(false), currentLineNumber);
    }

    private Token.Kind getTokenKind() {
        String tokenString = makeStackString(true);
        switch (tokenString) {
            case "+":
            case "-":
                return Token.Kind.PLUSMINUS;
            case "*":
                return Token.Kind.MULDIV;
            case "%":
            case ">":
            case "<":
            case "&&":
            case ">=":
            case "<=":
            case "||":
                return Token.Kind.BINARYLOGIC;
            case "++":
                return Token.Kind.UNARYINCR;
            case "--":
                return Token.Kind.UNARYDECR;
            default:
                return null;
        }
    }
}
