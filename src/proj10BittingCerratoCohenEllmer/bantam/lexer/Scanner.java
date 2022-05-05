/*
 * File: Scanner.java
 * Author: cbitting
 * Date: 4/8/2021
 */
package proj10BittingCerratoCohenEllmer.bantam.lexer;

import proj10BittingCerratoCohenEllmer.bantam.lexer.precusortokens.*;
import proj10BittingCerratoCohenEllmer.bantam.util.CompilationException;
import proj10BittingCerratoCohenEllmer.bantam.util.Error;
import proj10BittingCerratoCohenEllmer.bantam.util.ErrorHandler;

import java.io.Reader;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

/**
 * This class generates precursor tokens based on a passed character.
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
     * INVARIANT: when the token is finished scanning, this field holds the first
     * character of the next token
     */
    private char currentChar;

    /**
     * creates a new scanner for the given file
     *
     * @param filename the name of the file to be scanned
     * @param handler  the ErrorHandler that collects all the errors found
     */
    public Scanner(String filename, ErrorHandler handler) {
        errorHandler = handler;
        sourceFile = new SourceFile(filename);
        currentChar = sourceFile.getNextChar(true);
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
        currentChar = sourceFile.getNextChar(true);
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
        TokenBuilder tokenBuilder = createTokenBuilder(currentChar);
        while (!tokenBuilder.isComplete()) {
            tokenBuilder.pushChar(sourceFile.getNextChar(false));
        }

        // store last char in currentChar if needed
        Optional<Character> extraChar = tokenBuilder.getExtraChar();
        extraChar.ifPresentOrElse(
                c -> currentChar = Character.isWhitespace(c)
                        ? sourceFile.getNextChar(true)
                        : c,
                () -> currentChar = sourceFile.getNextChar(true)
        );

        // create the token from the precursor token
        // create token before extracting errors as some errors occur during creation
        Token finalToken;
        try {
            finalToken = tokenBuilder.getFinalToken(sourceFile.getCurrentLineNumber());
        } catch (MalformedSpellingStackException e) {
            // this will never happen because we call getExtraChar above
            e.printStackTrace();
            finalToken = null;
        }

        // register any errors that occurred with the error handler
        Optional<List<Error>> errorList = tokenBuilder.getErrors();
        errorList.ifPresent(el -> {
            for (Error e : el) {
                errorHandler.register(e);
            }
        });

        return finalToken;
    }

    /**
     * Returns the filename of the internal source file
     *
     * @return the name of the internal source file
     */
    public String getFilename() {
        return sourceFile.getFilename();
    }

    /**
     * Creates a TokenBuilder based on the passed character
     *
     * @param initialChar the first char in the token
     * @return the appropriate TokenBuilder for the passed token
     */
    private TokenBuilder createTokenBuilder(char initialChar) {
        int lineNum = sourceFile.getCurrentLineNumber();
        String filename = sourceFile.getFilename();
        Stack<Character> spellingStack = new Stack<>();
        spellingStack.push(initialChar);

        switch (initialChar) {
            case '/':
                return new SlashTokenBuilder(spellingStack, lineNum, filename);
            case '&':
            case '|':
            case '+':
            case '-':
            case '*':
            case '%':
            case '<':
            case '>':
                return new MathTokenBuilder(spellingStack, lineNum, filename);
            case '=':
                return new EqualsTokenBuilder(spellingStack, lineNum, filename);
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return new IntegerTokenBuilder(spellingStack, lineNum, filename);
            case '"':
                return new StringTokenBuilder(spellingStack, lineNum, filename);
            case '!':
                return new ExclamationTokenBuilder(spellingStack, lineNum, filename);
            case '(':
            case ')':
            case '{':
            case '}':
            case ';':
            case '.':
            case ':':
            case ',':
            case '\u0000':
                return new SingleCharTokenBuilder(spellingStack, lineNum, filename);
            default:
                if (Character.isLetter(spellingStack.lastElement())) {
                    return new IdentifierTokenBuilder(spellingStack, lineNum, filename);
                }
                return new UnsupportedCharTokenBuilder(spellingStack, lineNum, filename);
        }
    }

    /**
     * Driver test code
     */
    public static void main(String[] args) {
        // files specified on cmd line
        args = new String[]{"Bantam.txt"};
        if (args.length > 0) {
            Scanner bantamScanner;
            ErrorHandler bantamErrorHandler;
            Token currentToken;
            // scan each file
            for (String filename : args) {

                //file may not be opened -> CompilationException
                try {
                    // initialize scanner for each file
                    bantamErrorHandler = new ErrorHandler();
                    bantamScanner = new Scanner(filename, bantamErrorHandler);

                    // move through file tokens until "End Of File" reached
                    do {
                        currentToken = bantamScanner.scan();
                        System.out.println(currentToken.toString());
                    } while (currentToken.kind != Token.Kind.EOF);

                    // Check Scanner's Error Handler
                    if (bantamErrorHandler.errorsFound()) {
                        int errorCount = bantamErrorHandler.getErrorList().size();
                        System.out.println(
                                "*** " + filename + " had "
                                        + errorCount + " errors! ***");
                        for (Error error : bantamErrorHandler.getErrorList()) {
                            System.out.println(error);
                        }
                    } else {
                        System.out.println(
                                "*** Scanning file " + filename
                                        + " was Successfull! ***");

                    }
                } catch (CompilationException e) {
                    System.out.println(e);
                }
            }
        } else {
            System.out.println("Please provide a file in the Command Line arguments!");
        }


    }
}
