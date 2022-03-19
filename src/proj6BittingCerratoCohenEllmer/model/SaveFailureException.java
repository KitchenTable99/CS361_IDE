/*
 * File: SaveFailureException.java
 * Names: Caleb Bitting, Matt Cerrato, Erik Cohen, Ian Ellmer
 * Class: CS 361
 * Project 6
 * Date: March 18
 */
package proj6BittingCerratoCohenEllmer.model;

/**
 * Specialized Exception for CS361 GUI
 */
public class SaveFailureException extends Exception{
    public SaveFailureException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
