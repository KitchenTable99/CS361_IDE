/*
 * File: Main.java
 * Names: Caleb Bitting, Matt Cerrato, Erik Cohen, Ian Ellmer
 * Class: CS 361
 * Project 7
 * Date: March 30
 */

package proj7BittingCerratoCohenEllmer.bantam;

import proj7BittingCerratoCohenEllmer.bantam.lexer.Scanner;
import proj7BittingCerratoCohenEllmer.bantam.lexer.Token;
import proj7BittingCerratoCohenEllmer.bantam.lexer.Token.Kind;
import proj7BittingCerratoCohenEllmer.bantam.util.CompilationException;
import proj7BittingCerratoCohenEllmer.bantam.util.ErrorHandler;

public class Main {
    public static void main(String[] args) {
        if(args.length > 0){
            Scanner bantamScanner;
            ErrorHandler bantamErrorHandler = new ErrorHandler();
            Token currentToken;
            for(String filename : args){
                int fileErrorCount = 0;
                try {
                    bantamScanner = new Scanner(filename, bantamErrorHandler);
                    do{
                        currentToken = bantamScanner.scan();
                    }while(currentToken.kind != Kind.EOF); 
                    // file status check
                    if (fileErrorCount == 0){
                        System.out.println(
                            "*** Scanning file " + filename 
                                + " was Successfull! ***");
                    }else{
                        System.out.println(
                            "*** " + filename + " had " 
                                + fileErrorCount + " errors! ***");
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
