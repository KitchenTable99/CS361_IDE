package proj7BittingCerratoCohenEllmer.bantam.lexer;

import proj7BittingCerratoCohenEllmer.bantam.util.ErrorHandler;
import proj7BittingCerratoCohenEllmer.bantam.lexer.Token.Kind;
import proj7BittingCerratoCohenEllmer.bantam.util.CompilationException;

import java.io.IOException;
import java.io.Reader;

/**
 * This class reads characters from a file or a Reader
 * and breaks it into Tokens.
 */
public class Scanner {
    /**
     * the source of the characters to be broken into tokens
     */
    private SourceFile sourceFile;
    /**
     * collector of all errors that occur
     */
    private ErrorHandler errorHandler;

    /**
     * creates a new scanner for the given file
     *
     * @param filename the name of the file to be scanned
     * @param handler  the ErrorHandler that collects all the errors found
     */
    public Scanner(String filename, ErrorHandler handler) {
        errorHandler = handler;
        sourceFile = new SourceFile(filename);
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
    }

    /**
     * checks if white space should be kept
     *
     * @param spelling the current spelling of the token to return
     */
    private boolean keepWhiteSpace(String spelling){
        if(spelling.startsWith("\"") 
            || spelling.startsWith("//'") 
            || spelling.startsWith("/*")){
                return true;
            }
        return false;
    }

    /**
     * checks if letter is whitesace, a carrage, or a tab
     *
     * @param letter the current letter being checked
     */
    private boolean isWhiteSpace(String letter){
        if(Character.isWhitespace(letter.charAt(0))){
                return true;
            }
        return false;
    }

    /**
     * checks if some string is an Identifier
     * 
     * @param spelling the current spelling of the token to check
     * 
     * @return returns true if an identifier 
     */
    private boolean isIdentifier(String spelling){
        //must start with a letter
        char start = spelling.charAt(0);
        if(start >= 'A' && start <= 'Z' ||
            start >= 'a' && start <= 'z'){
            // only contains letters, numbers, and underscores
            for (char character : spelling.toCharArray()) {
                if(character < '0' ||
                        character > '9' && character < 'A' ||
                        character > 'Z' && character < '_' ||
                        character > '_' && character < 'a' ||
                        character > 'z'){
                    return false;
                }
            } 
            return true;
        } // doesnt start with letter
        return false;
    }

    /**
     * checks if string contains a valid int
     * 
     * @param integer the current spelling of the token to check
     * 
     * @return returns true if an integer
     */
    private boolean isValidInt(String integer){
            // else check int
                    //Integer constants (9, 32, etc.) 0 and ((2^31)-1) INTCONST, 
        return false;
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
        String spelling = "";
        while(true){
            try {
                String letter = String.valueOf(sourceFile.getNextChar());
                // Next token is delimited by white space
                while(true){
                    //ignore white space in file
                    if(isWhiteSpace(letter) && spelling.equals("")){
                        letter = String.valueOf(sourceFile.getNextChar());
                        continue;
                    }
                    if(isWhiteSpace(letter)){
                        // keep white space in strings and comments
                        if(keepWhiteSpace(spelling)){
                            System.out.println("keep");
                            spelling += letter;
                        }else{ // stop parsing -> create token
                            break;
                        }
                    }
                    spelling += letter;
                    letter = String.valueOf(sourceFile.getNextChar());
                }

                //check if spelling is a special symbol
                // check if ){
                
                    // '(', ')', '{', '}', ';', '+', '-', '++', '==',
                    // '&', '|', '&&', '||', '--', '!', '.', ';', ':', ',', '[', ']')
                        //create token with kind

                if(isIdentifier(spelling)){
                    return new Token(Kind.IDENTIFIER, spelling, 
                                sourceFile.getCurrentLineNumber());
                }
                // if(isValidInt(spelling)){
                //     return new Token(Kind.INTCONST, spelling, 
                //                 sourceFile.getCurrentLineNumber());
                // }
                
                
                return new Token(Kind.ERROR, "missed", 999);
                    
                    //else check if spelling starts and ends with double quotes
                    /**String constants ("abc", etc.) String constants start and end with double quotes. 
                    They may contain the following special symbols: 
                    \n (newline), \t (tab), \" (double quote),
                    \\ (backslash), and \f (form feed). 
                    A string constant cannot exceed 5000 characters and cannot span multiple lines. STRCONST */
                    
                //else check if spelling is comment
                    //Comments. include the "//" or the "/*" and "*/" delimiters.
            } catch (IOException e) {
                System.out.println(e);
            }

        }

    }

}
