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

    /**
     * This can hold some sort of process id for a process
     */
    private final String processID;

    private final String standardOutput;

    private final String standardError;

    private final int exitCode;


    public ExecutionResult(int exitCode, String standardOutput, String standardError, String processID) {
        this.processID = processID;
        this.exitCode = exitCode;
        this.standardOutput = standardOutput;
        this.standardError = standardError;
    }

    public String getProcessID() {
        return processID;
    }

    public String getStandardOutput() {
        return standardOutput;
    }

    public String getStandardError() {
        return standardError;
    }

    public int getExitCode() {
        return exitCode;
    }

    /**
     * All result lines.
     */
    public List<String> getResultLines() {
        return Arrays.asList(getStandardOutput().split("\n"));
    }

    /**
     * First line of the result array.
     * Null if no entries are in the array.
     */
    public String getFirstLine() {
        if(getResultLines().size() > 0)
            return getResultLines().get(0);
        else
            return null;
    }

    public boolean isSuccessful() {
        return exitCode == 0;
    }

    /*@Deprecated
    public boolean getSuccessful() {
        return isSuccessful();
    }
*/
    @Deprecated
    public int getErrorNumber() { return exitCode; }
}
