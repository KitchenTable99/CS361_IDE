package proj10BittingCerratoCohenEllmer.bantam;

import proj10BittingCerratoCohenEllmer.bantam.ast.Program;
import proj10BittingCerratoCohenEllmer.bantam.parser.Parser;
import proj10BittingCerratoCohenEllmer.bantam.treedrawer.Drawer;
import proj10BittingCerratoCohenEllmer.bantam.util.CompilationException;
import proj10BittingCerratoCohenEllmer.bantam.util.ErrorHandler;
import proj10BittingCerratoCohenEllmer.bantam.util.Error;

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
            Parser bantamParser;
            ErrorHandler bantamErrorHandler;
            Program currentProgram;
            // scan each file
            for(String filename : args){
                //System.out.println(filename);

                //file may not be opened -> CompilationException
                try {
                    // initialize scanner for each file
                    bantamErrorHandler = new ErrorHandler();
                    bantamParser = new Parser(bantamErrorHandler);
                    currentProgram = bantamParser.parse(filename);
                    
                    // Check Scanner's Error Handler
                    if (bantamErrorHandler.errorsFound()){
                        int errorCount = bantamErrorHandler.getErrorList().size();
                        System.out.println(
                            "*** " + filename + " had " 
                                + errorCount + " errors! ***");
                        // (cmd + /) uncomment to view individual errors
                        for (Error error : bantamErrorHandler.getErrorList()) {
                            System.out.println(error);
                        }
                    }else{
                        System.out.println(
                            "*** Scanning file " + filename 
                                + " was Successfull! ***");
                        new Drawer().draw(filename, currentProgram);
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
