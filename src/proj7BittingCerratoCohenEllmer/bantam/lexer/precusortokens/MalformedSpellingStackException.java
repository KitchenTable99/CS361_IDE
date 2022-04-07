/*
 * File: MalformedSpellingStackException.java
 * Author: cbitting
 * Date: 4/8/2021
 */
package proj7BittingCerratoCohenEllmer.bantam.lexer.precusortokens;

/**
 * This error will be thrown if any PrecursorToken is asked to create a final Token
 * object, but the PrecursorToken has an internal stack that needs to be popped.
 */
public class MalformedSpellingStackException extends Exception {
    public MalformedSpellingStackException(String errorMessage) {
        super(errorMessage);
    }
}
