/*
 * Copyright (c) 2016 eilslabs.
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */

package de.dkfz.roddy.execution.io;

import de.dkfz.roddy.core.InfoObject;

import java.util.Arrays;
import java.util.List;

/**
 * Stores the result of a command execution.
 * Commands can i.e. be executed via ssh or on the local command line.
 * @author michael
 */
public class ExecutionResult extends InfoObject {

    private final int exitCode;

    private final String standardOutput;

    private final String standardError;

    /**
     * This can hold some sort of process id for a process
     */
    private final String processID;


    public ExecutionResult(int exitCode, String standardOutput, String standardError, String processID) {
        this.exitCode = exitCode;
        this.standardOutput = standardOutput;
        this.standardError = standardError;
        this.processID = processID;
    }

    public ExecutionResult(int exitCode, String standardOutput, String standardError) {
        this(exitCode, standardOutput, standardError, null);
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getStandardOutput() {
        return standardOutput;
    }

    public String getStandardError() {
        return standardError;
    }

    public String getProcessID() {
        return processID;
    }


    public boolean isSuccessful() {
        return exitCode == 0;
    }

    /**
     * All result lines.
     */
    public List<String> getResultLines() {
        return Arrays.asList(getStandardOutput().split("\\R"));
    }

    /**
     * First line of the result array.
     */
    public String getFirstLine() {
        return getResultLines().get(0);
    }
}
