/*
 * Copyright (c) 2016 German Cancer Research Center (Deutsches Krebsforschungszentrum, DKFZ).
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/TheRoddyWMS/Roddy/LICENSE.txt).
 */

package de.dkfz.roddy.execution.io

import de.dkfz.roddy.core.InfoObject

/**
 * Stores the result of a command execution.
 * Commands can i.e. be executed via ssh or on the local command line.
 *
 *  TODO Make ExecutionResult and AsyncExecutionResult common subclasses of an IExecutionResult
 *       interface.
 *
 * @author michael
 */
class ExecutionResult extends InfoObject {

    /**
     * This can hold some sort of process id for a process
     */
    protected final String processID

    /**
     * Successful or not?
     */
    protected final boolean successful

    /**
     * This field is only kept for backwards-compatibility with Roddy plugins that are compiled
     * to access this field directly, instead of via the accessor. For this reason the field is
     * also kept public. Not that the changes in AsyncExecutionResult, and its late initialization
     * there is irrelevant in this context, because all old code refers to ExecutionResult.
     */
    @Deprecated
    public final List<String> resultLines

    /**
     * All result lines.
     */
    protected final List<String> stdout
    protected final List<String> stderr

    protected final int exitCode

    /**
     * Executed command that produced the result.
     */
    protected final List<String> command

    /** Contain the results of an execution. Note that the usage of this class is quite inconsistent. In particular
     *  occasionally the result indicates success, but the process actually failed.
     *
     * @param successful   Whether the execution was successful. Some tools produce an exit code == 0 but may still
     *                     have failed. A prominent example is the BWA aligner. For this reason the successful field
     *                     allows specifying success from e.g. standard output or error of the process rather than
     *                     just the exit code.
     * @param exitCode     The actual exit code.
     * @param stdout       This should be the standard output.
     * @param stderr       This should be the standard error.
     * @param processID    The process ID, if available.
     */
    ExecutionResult(List<String> command,
                    boolean successful,
                    int exitCode,
                    List<String> stdout = [],
                    List<String> stderr = [],
                    String processID = null) {
        this.command = command
        this.processID = processID
        this.successful = successful
        this.exitCode = exitCode
        this.stdout = stdout
        this.stderr = stderr
        // This will initialize the final field of ExecutionResult. Old client code using
        // ExecutionResult variables that refer to AsyncExecutionResult will retrieve this
        // value, rather than getting the maybe updated value via getResultLines()!
        this.resultLines = stdout + stderr
    }

    String getProcessID() {
        return processID
    }

    int getExitCode() {
        return exitCode
    }

    /** Convenience method */
    String getFirstStdoutLine() {
        return stdout.size() > 0 ? stdout.get(0) : null
    }

    @Deprecated
    List<String> getResultLines() {
        return getStdout() + getStderr()
    }

    List<String> getStdout() {
        return stdout
    }

    List<String> getStderr() {
        return stderr
    }

    boolean isSuccessful() {
        return successful
    }

    boolean getSuccessful() {
        return isSuccessful()
    }

    @Deprecated
    int getErrorNumber() {
        return exitCode
    }

    List<String> toStatusMessage() {
        return [successful ? "Success" : "Error" +
                " executing command (exitCode=${exitCode}, command=${command.join(" ")}):",
                "stdout={${this.stdout.join("\n")}}",
                "stderr={${stderr.join("\n")}}"]
    }

    String toStatusLine() {
        return toStatusMessage().join(" ")
    }

}
