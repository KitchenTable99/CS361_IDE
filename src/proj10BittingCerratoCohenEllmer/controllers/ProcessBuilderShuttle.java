/*
 * File: ProcessBuilderShuttle.java
 * Names: Caleb Bitting, Matt Cerrato, Erik Cohen, Ian Ellmer
 * Class: CS 361
 * Project 6
 * Date: March 18
 */
package proj10BittingCerratoCohenEllmer.controllers;

public class ProcessBuilderShuttle {

    private ProcessBuilder processBuilder;

    public ProcessBuilderShuttle() {
        processBuilder = null;
    }

    public void setProcessBuilder(ProcessBuilder pb) {
        processBuilder = pb;
    }

    public ProcessBuilder getProcessBuilder() {
        return processBuilder;
    }
}
