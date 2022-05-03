/*
 * File: PrecursorSlashToken.java
 * Author: cbitting
 * Date: 4/8/2021
 */
package proj10BittingCerratoCohenEllmer.bantam.lexer.precusortokens;

import proj10BittingCerratoCohenEllmer.bantam.lexer.Token;
import proj10BittingCerratoCohenEllmer.bantam.util.Error;

import java.util.Stack;

/**
 * If some token starts with a slash, this PrecursorToken contains all the logic needed
 * to tokenize that string.
 */
public class PrecursorSlashToken extends AbstractPrecursorToken {

    private boolean typeKnown = false;
    private boolean isComment = false;

    public PrecursorSlashToken(Stack<Character> sc, int l, String s) {
        super(sc, l, s);
    }

    @Override
    public void pushChar(char c) {
        spellingStack.push(c);

        if (!typeKnown && spellingStack.size() >= 2) {
            isComment = spellingStack.get(1) == '/' || spellingStack.get(1) == '*';
            typeKnown = true;
        }

        if (spellingStack.size() >= 2 && isComment) {
            handleIsComment();
        } else if (spellingStack.size() >= 2) {
            // this MUST be a divided sign followed by some character in Bantam Java
            popLastBeforeCreation = true;
            containsCompleteToken = true;
        }
    }

    /**
     * Updates the flags popLastBeforeCreation and containsCompleteToken if the internal
     * stack represents a comment. This method will also set the internal error field
     * if there is an unterminated block comment.
     */
    private void handleIsComment() {
        char secondChar = spellingStack.get(1);
        char secondToLastChar = spellingStack.get(spellingStack.size() - 2);
        char lastChar = spellingStack.peek();

        if (secondChar == '/' && (lastChar == '\n' || lastChar == '\r')) {
            popLastBeforeCreation = true;
            containsCompleteToken = true;
        } else {
            if (lastChar == '\u0000') {
                tokenError.add(new Error(Error.Kind.LEX_ERROR,
                        filename,
                        startingLineNumber,
                        "Unterminated Block Comment!"));
                containsCompleteToken = true;
            } else if (spellingStack.size() > 3 // /*/ is not valid
                    && secondToLastChar == '*' && lastChar == '/') {
                containsCompleteToken = true;
            }
        }
    }

    @Override
    public Token getFinalToken(int currentLineNumber)
            throws MalformedSpellingStackException {
        if (popLastBeforeCreation) {
            throw new MalformedSpellingStackException("You need to pop the stack first");
        }

        Token.Kind tokenKind;
        if (tokenError.size() != 0) {
            tokenKind = Token.Kind.ERROR;
        } else if (isComment) {
            tokenKind = Token.Kind.COMMENT;
        } else {
            tokenKind = Token.Kind.MULDIV;
        }

        return new Token(tokenKind, makeStackString(false), currentLineNumber);

    }
}
