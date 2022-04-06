package proj7BittingCerratoCohenEllmer.bantam.lexer;

import proj7BittingCerratoCohenEllmer.bantam.lexer.Token.Kind;
import proj7BittingCerratoCohenEllmer.bantam.util.ErrorHandler;
import proj7BittingCerratoCohenEllmer.bantam.util.Error;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Stack;

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

    private boolean currentTokenError;

    private char skippedLastToken;
    private final HashSet<Character> validSolo = new HashSet<>() {{
        add('{');
        add('}');
        add('(');
        add(')');

        add(';');
        add(':');
        add(',');
        add('.');
        add('!');
    }};

    private final HashSet<Character> leadingMathChars = new HashSet<>() {{
        add('+');
        add('-');
        add('*');
        add('%');
        add('<');
        add('>');
        add('/');
    }};
    

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
     * @param reader reader object for the file to be scanned
     * @param handler  the ErrorHandler that collects all the errors found
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
        Stack<Character> spellingStack = new Stack<>();
        currentTokenError = false;
        if (skippedLastToken != '\0' && !Character.isWhitespace(skippedLastToken)) {
            spellingStack.push(skippedLastToken);
        }
        skippedLastToken = '\0';

        while (!isCompleteToken(spellingStack)) {
            try {
                char letter = sourceFile.getNextChar();
                if (!Character.isWhitespace(letter) || !spellingStack.empty()) {
                    if(isUnsupportedCharacter(letter)
                        && !addAllCharacters(spellingStack)){
                        currentTokenError = true;
                        errorHandler.register(Error.Kind.LEX_ERROR,
                            sourceFile.getFilename(), sourceFile.getCurrentLineNumber(),
                            "Unsupported Character" + letter);
                    }
                    spellingStack.push(letter);
                }
            } catch (IOException e) {
                // if there are no more character then check to see if the final token is invalid
                e.printStackTrace(); // TODO: make this elegant
            }
        }
        System.out.println(spellingStack.toString());
        return createToken(spellingStack);
    }

    private Boolean isUnsupportedCharacter(Character symbol){
        return !(Character.isLetterOrDigit(symbol) 
                || Character.isWhitespace(symbol)
                || validSolo.contains(symbol)
                || leadingMathChars.contains(symbol));
    }

    private boolean addAllCharacters(Stack<Character> spellingStack) {
        if(spellingStack.size() < 1){
            return false;
        }
        char leadingChar = spellingStack.firstElement();
        return leadingChar == '/' || leadingChar == '\"';
    }

    /**
     * This method checks to see if the spelling stack contains a valid token. If a valid
     * token can be constructed from the complete stack, this method return true. In the
     * case that a valid token can be made from all but the last char in the stack, this
     * method pops that character into skippedLastToken and returns true.
     *
     * @param spellingStack containing all the character read so far
     * @return whether a valid token can be created
     */
    private boolean isCompleteToken(Stack<Character> spellingStack) {
        if (spellingStack.size() == 0) {
            return false;
        }
        char leadingChar = spellingStack.firstElement();
        if (validSolo.contains(leadingChar)) {
            skippedLastToken = '\0';
            return true;
        } else if (isEOF(spellingStack)) {
            skippedLastToken = '\0';
            return true;
        } else if (leadingChar == '/') {
            return isCompleteSlash(spellingStack); // checks comments and divided by
        } else if (leadingMathChars.contains(leadingChar)) {
            return isCompleteMath(spellingStack);
        } else if (leadingChar == '=') {
            return isCompleteEquals(spellingStack);
        } else if (Character.isDigit(leadingChar)) {
            return isCompleteInt(spellingStack);
        } else if (leadingChar == '"') {
            return isCompleteString(spellingStack);
        } else {
            if(Character.isAlphabetic(leadingChar)){
                return isCompleteIdentifier(spellingStack);
            }
        return true;
        }
    }

    // TODO: add javadoc once the functionality is complete
    private boolean isEOF(Stack<Character> spellingStack) {
        return spellingStack.firstElement() == '\u0000';
    }

    private boolean isCompleteSlash(Stack<Character> spellingStack) {
        if (spellingStack.size() >= 2 && spellingStack.get(1) == '/') {
            return isCompleteComment(spellingStack);
        } else if (spellingStack.size() >= 2 && spellingStack.get(1) == '*') {
            return isCompleteComment(spellingStack);
        } else {
            return isCompleteMath(spellingStack);
        }
    }

    private boolean isCompleteComment(Stack<Character> spellingStack) {
        char secondChar = spellingStack.get(1);
        char lastChar = spellingStack.peek();
        if (secondChar == '/') {
            if (lastChar == '\n' || lastChar == '\r') {
                skippedLastToken = spellingStack.pop();
                return true;
            }
        } else if (secondChar == '*') {
            char secondToLastChar = spellingStack.get(spellingStack.size() - 2);
            if (lastChar == '\u0000'){
                currentTokenError = true;
                errorHandler.register(Error.Kind.LEX_ERROR,
                                sourceFile.getFilename(), sourceFile.getCurrentLineNumber(),
                                "Unterminated Block Comment!"); //TODO: change message
            }
            if (secondToLastChar == '*' && lastChar == '/') {
                skippedLastToken = '\0';
                return true;
            }
        }
        return false;
    }

    private boolean isCompleteMath(Stack<Character> spellingStack) {
        char lastChar = spellingStack.peek();
        if (Character.isWhitespace(lastChar)) {
            skippedLastToken = spellingStack.pop();
            return true;
        } else if (Character.isAlphabetic(lastChar) || Character.isDigit(lastChar)) {
            skippedLastToken = spellingStack.pop();
            return true;
        } else if (makeStackString(spellingStack, true).equals("++")
                || makeStackString(spellingStack, true).equals("--")
                || lastChar == '=') {
            skippedLastToken = '\0';
            return true;
        } else if (lastChar == ';') {
            // set token type
            skippedLastToken = spellingStack.pop();
            return true;
        } else {
            return false;
        }
    }

    private boolean isCompleteEquals(Stack<Character> spellingStack) {
        if (spellingStack.size() < 2) {
            return false;
        } else if (spellingStack.size() == 2 && spellingStack.peek() == '=') {
            skippedLastToken = '\0';
            return true;
        } else if (spellingStack.size() == 2 && spellingStack.peek() != '=') {
            skippedLastToken = spellingStack.pop();
            return true;
        } else {
            // something went wrong
            return false;
        }
    }

    private boolean isCompleteInt(Stack<Character> spellingStack) {
        if (Character.isDigit(spellingStack.peek())) {
            return false;
        } else {
            skippedLastToken = spellingStack.pop();
            return true;
        }
    }

    /**
     * checks if string contains valid characters
     * <p>
     * String constants start and end with double quotes.
     * They may contain the following special symbols:
     * \n (newline),
     * \t (tab),
     * \" (double quote),
     * \\ (backslash), and
     * \f (form feed).
     * A string constant cannot exceed 5000 characters and cannot span multiple lines.
     *
     * @param spellingStack the stack containing the characters
     * @return returns true if the stack contains a complete string
     */
    private boolean isCompleteString(Stack<Character> spellingStack) {
        // TODO: if EOF is the last char then this is an terminated string
        // TODO: check for illegal characters
        int stackSize = spellingStack.size();
        if (stackSize > 1 && stackSize <= 5000) {
            if (spellingStack.peek() == '\u0000'){
                currentTokenError = true;
                errorHandler.register(Error.Kind.LEX_ERROR,
                                sourceFile.getFilename(), sourceFile.getCurrentLineNumber(),
                                "Unterminated String Constant!"); //TODO: change message
            }
            
            if (spellingStack.peek() == '"' && spellingStack.get(stackSize - 2) != '\\') {
                skippedLastToken = '\0';
                return true;
            } else {
                return false;
            }
        } else if (stackSize > 5000) {
            // TODO: don't prematurely end. check string size once returning.
            //raise error;
            skippedLastToken = '\0';
            return true; // TODO: figure out if this should be true or false
        } else {
            return false;
        }
    }

    /**
     * checks if some string is an Identifier
     *
     * @param spellingStack the stack containing the characters that have been read
     * @return returns true if an identifier
     */
    private boolean isCompleteIdentifier(Stack<Character> spellingStack) {
        if (spellingStack.size() == 1) {
            return false;
        }
        Character lastChar = spellingStack.peek();
        if (Character.isAlphabetic(lastChar) ||
                Character.isDigit(lastChar) ||
                lastChar == '_') {
            return false;
        } else {
            skippedLastToken = spellingStack.pop();
            return true;
        }
    }

    private Token createToken(Stack<Character> spellingStack) {
        Kind tokenKind = getTokenKind(spellingStack);
        return new Token(tokenKind, makeStackString(spellingStack, false), sourceFile.getCurrentLineNumber());
    }


    private Kind getTokenKind(Stack<Character> spellingStack) {
        char leadingChar = spellingStack.firstElement();
        if(!currentTokenError){
            if (validSolo.contains(leadingChar)) {
                switch (leadingChar) {
                    case '(':
                        return Kind.LPAREN;
                    case ')':
                        return Kind.RPAREN;
                    case '{':
                        return Kind.LCURLY;
                    case '}':
                        return Kind.RCURLY;
                    case ';':
                        return Kind.SEMICOLON;
                    case '!': // TODO: should this be here or do we need a new method?
                        return Kind.UNARYNOT;
                    case '.':
                        return Kind.DOT;
                    case ':':
                        return Kind.COLON;
                    case ',':
                        return Kind.COMMA;
                    default:
                        return null;
                }
            } else if (isEOF(spellingStack)) {
                return Kind.EOF;
            } else if (leadingChar == '/' && spellingStack.size() != 1) {
                return Kind.COMMENT;
            } else if (leadingMathChars.contains(leadingChar)) {
                String tokenString = makeStackString(spellingStack, true);
                switch (tokenString) {
                    case "+":
                    case "-":
                        return Kind.PLUSMINUS;
                    case "*":
                    case "/":
                        return Kind.MULDIV;
                    case "%":
                    case ">":
                    case "<":
                    case "&&":
                    case ">=":
                    case "<=":
                    case "||":
                        return Kind.BINARYLOGIC; //TODO: fix modulus
                    case "++":
                        return Kind.UNARYINCR;
                    case "--":
                        return Kind.UNARYDECR;
                    default:
                        return null;
                }
            } else if (leadingChar == '=' && spellingStack.size() == 1) {
                return Kind.ASSIGN;
            } else if (leadingChar == '=') {
                return Kind.COMPARE;
            } else if (Character.isDigit(leadingChar)) {
                if (isValidIntegerSize(spellingStack)){
                    return Kind.INTCONST;
                }
                currentTokenError = true;
                errorHandler.register(Error.Kind.LEX_ERROR,
                            sourceFile.getFilename(), sourceFile.getCurrentLineNumber(),
                            "Integer Constant too large."); //TODO : update error message?
            } else if (leadingChar == '"') {
                return Kind.STRCONST;
            }else if (Character.isAlphabetic(leadingChar)){
                return Kind.IDENTIFIER;
            }
        }
        return Kind.ERROR;
    }

    private boolean isValidIntegerSize(Stack<Character> spellingStack){
        System.out.println(spellingStack.toString()); 
        return true;
    }

    private String makeStackString(Stack<Character> spellingStack, boolean copyStack) {
        if (copyStack) {
            Stack<Character> spellingStackCopy = (Stack<Character>) spellingStack.clone();
            return emptyStackToString(spellingStackCopy);
        } else {
            return emptyStackToString(spellingStack);
        }
    }

    private String emptyStackToString(Stack<Character> spellingStack) {
        char[] charArray = new char[spellingStack.size()];
        for (int i = spellingStack.size() - 1; i >= 0; i--) {
            charArray[i] = spellingStack.pop();
        }

        return new String(charArray);
    }
}
