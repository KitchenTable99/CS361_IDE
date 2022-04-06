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

    /**
     * flag to identify if error occured
     */
    private boolean currentTokenError;

    /**
     * holds Tokens that delimit
     */
    private char skippedLastToken;

    /**
     * keeps track if next character escaped in string
     * default false
     */
    private boolean charIsEscaped = false;

    /**
     * Holds legal escaped characters in strings
     */
    private final HashSet<Character> validEscapedCharacter = new HashSet<>(){{
        add('n');
        add('t');
        add('"');
        add('\\');
        add('f');
    }};

    /**
     * Holds Tokens that can be one character
     */
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

    /**
     * Holds Characters that indicate Math Tokens
     */
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
                        errorHandler.register(Error.Kind.LEX_ERROR,
                            sourceFile.getFilename(), sourceFile.getCurrentLineNumber(),
                            "Unsupported Character " + letter);
                    }
                    spellingStack.push(letter);
                }
            } catch (IOException e) {
                // if there are no more character then check to see if the final token is invalid
                e.printStackTrace(); // TODO: make this elegant
            }
        }
        return createToken(spellingStack);
    }

    /**
     * Checks if the character is legal in Bantam Java
     * @param symbol the character being checked
     * @return true if legal
     */
    private boolean isUnsupportedCharacter(Character symbol){
        return !(Character.isLetterOrDigit(symbol) 
                || Character.isWhitespace(symbol)
                || validSolo.contains(symbol)
                || leadingMathChars.contains(symbol)
                || symbol == '\n'
                || symbol == '\r'
                || symbol == '\u0000'
                || symbol == '\"'
                || symbol == '_'
                || symbol == '=');
    }

    /**
     * Checks if token accepts all character symbols
     * 
     * @param spellingStack a stack of characters for the token spelling
     * @return true if the first symbol is / or "
     */
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

    /**
     * Checks if the token is an End Of File (EOF) Token
     * @param spellingStack a stack of characters for the token spelling
     * @return true if EOF
     */
    private boolean isEOF(Stack<Character> spellingStack) {
        return spellingStack.firstElement() == '\u0000';
    }

    /**
     * Checks if a token that starts with "/" is complete
     * @param spellingStack a stack of characters for the token spelling
     * @return true if a complete comment or math token
     */
    private boolean isCompleteSlash(Stack<Character> spellingStack) {
        if (spellingStack.size() >= 2 && spellingStack.get(1) == '/') {
            return isCompleteComment(spellingStack);
        } else if (spellingStack.size() >= 2 && spellingStack.get(1) == '*') {
            return isCompleteComment(spellingStack);
        } else {
            return isCompleteMath(spellingStack);
        }
    }

    /**
     * Checks if the token spelling has a complete single line or multiline
     * comment
     * @param spellingStack a stack of characters for the token spelling
     * @return true if comment is complete
     */
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
                                "Unterminated Block Comment!");
            }
            if (secondToLastChar == '*' && lastChar == '/') {
                skippedLastToken = '\0';
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the token spelling contains a complete math statement
     * 
     * @param spellingStack a stack of characters for the token spelling
     * @return true if a complete math statement
     */
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

    /**
     * Checks if tokens starting with = are complete
     * 
     * @param spellingStack a stack of characters for the token spelling
     * @return true if the equals statement is complete
     */
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

    /**
     * Checks if Int has finished
     * 
     * @param spellingStack a stack of characters for the token spelling
     * @return true if the int has finished
     */
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
        // TODO: check for illegal characters
        int stackSize = spellingStack.size();
        if (stackSize > 1 && stackSize <= 5000) {
            if (spellingStack.peek() == '"' && !charIsEscaped) {
                skippedLastToken = '\0';
                return true;
            }
            if (spellingStack.peek() == '\u0000'){
                currentTokenError = true;
                errorHandler.register(Error.Kind.LEX_ERROR,
                                sourceFile.getFilename(), sourceFile.getCurrentLineNumber(),
                                "Unterminated String Constant!");
                return true;
            }
            if (spellingStack.peek() == '\\'){
                charIsEscaped = true;
                return false;
            }
            if (charIsEscaped && !validEscapedCharacter.contains(spellingStack.peek())){
                errorHandler.register(Error.Kind.LEX_ERROR,
                                sourceFile.getFilename(), sourceFile.getCurrentLineNumber(),
                                "Invalid Escaped Character \\" + spellingStack.peek());
                charIsEscaped = false;
                return false;
            } else{
                charIsEscaped = false;
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

    /**
     * Creates a token of the right kind
     * 
     * @param spellingStack a stack of characters for the token spelling
     * @return The new token
     */
    private Token createToken(Stack<Character> spellingStack) {
        Kind tokenKind = getTokenKind(spellingStack);
        return new Token(tokenKind, makeStackString(spellingStack, false), sourceFile.getCurrentLineNumber());
    }

    /**
     * Finds the token type based on the token spelling
     * 
     * @param spellingStack a stack of characters for the token spelling
     * @return the correct token kind
     */
    private Kind getTokenKind(Stack<Character> spellingStack) {
        char leadingChar = spellingStack.firstElement();
        if(!currentTokenError){
            if (validSolo.contains(leadingChar)) {
                return getSoloTokenKind(leadingChar);
            } else if (isEOF(spellingStack)) {
                return Kind.EOF;
            } else if (leadingChar == '/' && spellingStack.size() != 1) {
                return Kind.COMMENT;
            } else if (leadingMathChars.contains(leadingChar)) {
                String tokenString = makeStackString(spellingStack, true);
                return getMathTokenKind(tokenString);
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
                            "Integer Constant too large.");
            } else if (leadingChar == '"') {
                return Kind.STRCONST;
            }else if (Character.isAlphabetic(leadingChar)){
                return Kind.IDENTIFIER;
            }
        }
        return Kind.ERROR;
    }

    /**
     * helper method for finding token kind for 
     * "solo" tokens
     * 
     * @param spelling the token spelling
     * @return the correct token kind
     */
    private Kind getSoloTokenKind(Character spelling){
        switch (spelling) {
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
            case '!':
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
    }

    /**
     * helper method to find the token kind of math tokens
     * 
     * @param tokenString the token spelling
     * @return the correct token kind
     */
    private Kind getMathTokenKind(String tokenString){
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
                return Kind.BINARYLOGIC;
            case "++":
                return Kind.UNARYINCR;
            case "--":
                return Kind.UNARYDECR;
            default:
                return null;
        }
    }

    /**
     * Checks if int is too large
     * 
     * @param spellingStack the token spelling
     * @return returns true if less than the max integer value
     */
    private boolean isValidIntegerSize(Stack<Character> spellingStack){
        Long currentNumber = Long.parseLong(makeStackString(spellingStack, true));
        return currentNumber <= Integer.MAX_VALUE;
    }

    /**
     * Converts the spelling stack to a string
     * 
     * @param spellingStack the token spelling stack
     * @param copyStack wether or not to empty the stack
     * @return a string of the spelling stack
     */
    private String makeStackString(Stack<Character> spellingStack, boolean copyStack) {
        if (copyStack) {
            Stack<Character> spellingStackCopy = (Stack<Character>) spellingStack.clone();
            return emptyStackToString(spellingStackCopy);
        } else {
            return emptyStackToString(spellingStack);
        }
    }

    /**
     * Converts a stack to a string and empties stack
     * 
     * @param spellingStack the spelling stack to be converted/ emptied
     * @return The spelling stack as a string
     */
    private String emptyStackToString(Stack<Character> spellingStack) {
        char[] charArray = new char[spellingStack.size()];
        for (int i = spellingStack.size() - 1; i >= 0; i--) {
            charArray[i] = spellingStack.pop();
        }

        return new String(charArray);
    }
}
