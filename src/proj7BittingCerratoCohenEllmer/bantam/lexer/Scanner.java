package proj7BittingCerratoCohenEllmer.bantam.lexer;

import proj7BittingCerratoCohenEllmer.bantam.lexer.precusortokens.AbstractPrecursorToken;
import proj7BittingCerratoCohenEllmer.bantam.lexer.precusortokens.MalformedSpellingStackException;
import proj7BittingCerratoCohenEllmer.bantam.lexer.precusortokens.PrecursorTokenFactory;
import proj7BittingCerratoCohenEllmer.bantam.util.Error;
import proj7BittingCerratoCohenEllmer.bantam.util.ErrorHandler;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Optional;

/**
 * This class reads characters from a file or a Reader
 * and breaks it into Tokens.
 */
public class Scanner {
    /**
     * the source of the characters to be broken into tokens
     */
    private final SourceFile sourceFile;
    /**
     * collector of all errors that occur
     */
    private final ErrorHandler errorHandler;

    /**
     * holds Tokens that were read but not included in the previous token
     */
    private char skippedLastToken;

    /**
     * factory to generate all the precursor tokens based on the first token
     */
    private final PrecursorTokenFactory precursorTokenFactory = new PrecursorTokenFactory();

    /**
     * creates a new scanner for the given file
     *
     * @param filename the name of the file to be scanned
     * @param handler  the ErrorHandler that collects all the errors found
     */
    public Scanner(String filename, ErrorHandler handler) {
        errorHandler = handler;
        sourceFile = new SourceFile(filename);
        skippedLastToken = '\0';
    }

    /**
     * creates a new scanner for the given file
     *
     * @param reader  reader object for the file to be scanned
     * @param handler the ErrorHandler that collects all the errors found
     */
    public Scanner(Reader reader, ErrorHandler handler) {
        errorHandler = handler;
        sourceFile = new SourceFile(reader);
        skippedLastToken = '\0';
    }

    /**
     * read characters and collect them into a Token.
     * It ignores white space unless it is inside a string or a comment.
     * It returns an EOF Token if all characters from the sourceFile have
     * already been read.
     *
     * @return the Token containing the characters read
     */
    public Token scan() {

        AbstractPrecursorToken precursorToken = null;
        do {

            // create a new precursor token or push the char to the existing one
            if (precursorToken == null) {
                precursorToken = precursorTokenFactory.createPrecursorToken(getNextChar(true), sourceFile.getCurrentLineNumber(), sourceFile.getFilename());
            } else {
                precursorToken.pushChar(getNextChar(false));
            }
        } while (!precursorToken.isComplete());

        // if the precursor token needs to get rid of the last char, store it in skippedLastToken
        Optional<Character> extraChar = precursorToken.getExtraChar();
        extraChar.ifPresent(c -> skippedLastToken = c);

        Token finalToken;
        // create the token from the precursor token
        try {
            finalToken = precursorToken.getFinalToken(sourceFile.getCurrentLineNumber());
        } catch (MalformedSpellingStackException e) {
            // this will never happen because we call getExtraChar above
            e.printStackTrace();
            finalToken = null;
        }

        // register any errors that occurred with the error handler
        Optional<List<Error>> errorList = precursorToken.getErrors();
        errorList.ifPresent(el -> {
            for (Error e : el) {
                errorHandler.register(e);
            }
        });

        return finalToken;
    }

    private char getNextChar(boolean ignoreWhitespace) {
        // get the next character no matter what it is
        char nextChar;
        if (skippedLastToken != '\0') {
            nextChar = skippedLastToken;
            skippedLastToken = '\0';  // reset skippedLastToken
        } else {
            try {
                nextChar = sourceFile.getNextChar();
            } catch (IOException e) {
                e.printStackTrace();
                nextChar = '\0';
            }
        }

        // get the next character if we didn't want this whitespace character
        if (ignoreWhitespace && Character.isWhitespace(nextChar)) {
            return getNextChar(true);
        } else {
            return nextChar;
        }
    }

}