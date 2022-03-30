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
            || spelling.startsWith("\'") 
            || spelling.startsWith("/*")){
                return true;
            }
        return false;
    }

    private boolean isWhiteSpace(String letter){
        if(letter.equals(" ")
            || letter.equals("\t") 
            || letter.equals("\n")){
                return true;
            }
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
                if(isWhiteSpace(letter) && keepWhiteSpace(spelling)){
                    spelling += letter;
                }
            } catch (IOException e) {
                System.out.println(e);
            }

        }

    }

    /**
     * Test function for scanner code
     * Called when scanner.java is run on command line
     *
     * @param args a list of files to be scanned
     */
    public static void main(String[] args) {
        // files specified on cmd line
        if(args.length > 0){
            Scanner bantamScanner;
            ErrorHandler bantamErrorHandler;
            Token currentToken;
            // scan each file
            for(String filename : args){
                //file may not be opened -> CompilationException
                try {
                    // initialize scanner for each file
                    bantamErrorHandler = new ErrorHandler();
                    bantamScanner = new Scanner(filename, bantamErrorHandler);
                    // move through file tokens until "End Of File" reached
                    do{
                        currentToken = bantamScanner.scan();
                    }while(currentToken.kind != Kind.EOF); 
                    // Check Scanner's Error Handler
                    if (bantamErrorHandler.errorsFound()){
                        int errorCount = bantamErrorHandler.getErrorList().size();
                        System.out.println(
                            "*** " + filename + " had " 
                                + errorCount + " errors! ***");
                    }else{
                        System.out.println(
                            "*** Scanning file " + filename 
                                + " was Successfull! ***");
                        
                    }
                } catch (CompilationException e) {
                    System.out.println(e);
                }
            }
        }else{
            System.out.println("Please provide a file in the Command Line arguments!");
        }
        
    }

}
