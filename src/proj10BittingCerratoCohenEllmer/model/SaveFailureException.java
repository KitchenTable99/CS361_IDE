/*
 * File: SaveFailureException.java
 * Names: Caleb Bitting, Matt Cerrato, Erik Cohen, Ian Ellmer
 * Class: CS 361
 * Project 6
 * Date: March 18
 */
package proj10BittingCerratoCohenEllmer.model;

/**
 * Specialized Exception for CS361 GUI. Whenever a file cannot be saved to disk due to a permissions error or something
 * similar, this will be thrown instead of an IO error.
 */
public class SaveFailureException extends Exception{
    public SaveFailureException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
