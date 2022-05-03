/*
 * @(#)SourceFile.java                        2.0 1999/08/11
 *
 * Copyright (C) 1999 D.A. Watt and D.F. Brown
 * Dept. of Computing Science, University of Glasgow, Glasgow G12 8QQ Scotland
 * and School of Computer and Math Sciences, The Robert Gordon University,
 * St. Andrew Street, Aberdeen AB25 1HG, Scotland.
 * All rights reserved.
 *
 * This software is provided free for educational use only. It may
 * not be used for commercial purposes without the prior written permission
 * of the authors.
 *
 * Modified by Dale Skrien, Fall 2021
 */

package proj10BittingCerratoCohenEllmer.bantam.lexer;

import proj10BittingCerratoCohenEllmer.bantam.util.CompilationException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * A class for extracting the characters, one at a time, from a text file or a Reader.
 */
public class SourceFile {
    public static final char EOL = '\n';         // end of line character
    public static final char CR = '\r';  // carriage return character
    public static final char EOF = '\u0000';     // end of file character

    private Reader sourceReader;   // the reader of the data
    private int currentLineNumber; // for bantam error messages
    private int prevChar;          // the previous character read
    private String filename;       // the file currently being scanned.

    /**
     * creates a new SourceFile object for the file with the given name
     *
     * @param filename the name of the file to be read.
     * @throws CompilationException if the file is not found
     */
    public SourceFile(String filename) {
        try {
            sourceReader = new FileReader(filename);
        } catch (FileNotFoundException e) {
            throw new CompilationException("File " + filename + " not found.", e);
        }
        currentLineNumber = 1;
        prevChar = -1;
        this.filename = filename;
    }

    /**
     * creates a new SourceFile object for the given Reader
     *
     * @param in the Reader that provides the characters to be processes
     */
    public SourceFile(Reader in) {
        sourceReader = in;
        currentLineNumber = 1;
        prevChar = -1;
    }

    int getCurrentLineNumber() {
        return currentLineNumber;
    }

    String getFilename() {
        return filename;
    }

    /**
     * Finds and returns the next character in the source file.
     * The current line number is incremented if the end of a line is reached
     *
     * @return the next character in the source file
     */
    private char getNextChar() throws IOException {
        int c = sourceReader.read();

        if (c == -1) {
            c = EOF;
        } else if (c == CR || (c == EOL && prevChar != CR)) {
            currentLineNumber++;
        }
        prevChar = c;
        return (char) c;
    }

    /**
     * Helper method to get the next character in the file. Different from
     * getNextChar() in two significant ways. <p> First, if there is a char
     * stored in the currentChar field, this method will attempt to return that
     * char and reset the field. <p> Second, the caller can request a non-whitespace char
     *
     * @param ignoreWhitespace whether to ignore whitespace characters
     * @return the next possibly-non-whitespace char in the file including the
     * currentChar field
     */
    public char getNextChar(boolean ignoreWhitespace) {
        // get the next character no matter what it is
        char nextChar = 0;
        try {
            nextChar = getNextChar();
        } catch (IOException e) {
            throw new CompilationException("Ran out of characters before" +
                    " program scanning done", new Throwable());
        }

        // get the next character if we didn't want this whitespace character
        if (ignoreWhitespace && Character.isWhitespace(nextChar)) {
            return getNextChar(true);
        } else {
            return nextChar;
        }
    }
}

