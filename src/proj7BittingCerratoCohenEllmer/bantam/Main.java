package proj7BittingCerratoCohenEllmer.bantam;
import proj7BittingCerratoCohenEllmer.bantam.lexer.Scanner;
import proj7BittingCerratoCohenEllmer.bantam.lexer.Token;
import proj7BittingCerratoCohenEllmer.bantam.util.CompilationException;
import proj7BittingCerratoCohenEllmer.bantam.util.ErrorHandler;
//TODO : should this be renamed to "ScannerTest" ?
public class Main { 
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
                System.out.println(filename);

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
