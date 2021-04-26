package de.dkfz.roddy.execution.io

import java.util.concurrent.Future
import java.util.concurrent.CompletableFuture

/** Like ExecutionResult, but taking Futures of the exit code (usually the actual process) and the standard output and
 *  error streams. Calling any of the methods will block until the process is finished!
 *
 *  TODO Make ExecutionResult and AsyncExecutionResult common subclasses of an IExecutionResult
 *       interface.
 *
 */

class AsyncExecutionResult extends ExecutionResult {

    private final Future<Integer> exitCodeF
    private final Future<List<String>> stdoutF
    private final Future<List<String>> stderrF
    private final Future<Boolean> successfulF

    /** The AsyncExecutionResult takes Futures of a number of values. Calling any of the methods of the superclass
     *  that return the values will block until the result is available.
     *
     * @param process
     * @param stdoutF
     * @param stderrF
     * @param successful   Can be a future. By default (when set to null), it will be set success if exitCode == 0.
     */
    AsyncExecutionResult(List<String> command,
                         Integer processId,
                         CompletableFuture<Integer> exitCodeF,
                         CompletableFuture<List<String>> stdoutF,
                         CompletableFuture<List<String>> stderrF) {
        super(command, false, -1, [], [], processId as String)
        this.exitCodeF = exitCodeF
        this.stderrF = stderrF
        this.stdoutF = stdoutF
        this.successfulF = exitCodeF.thenApply { exitCode ->
            exitCode == 0
        }
    }

    /** Convert the asynchronous result into a synchronous one. This will obviously block until
     *  the referee process is finished.
     */
    ExecutionResult asExecutionResult() {
        new ExecutionResult(command, successful, exitCode, stdout, stderr, processID)
    }

    @Override
    int getExitCode() {
        return exitCodeF.get()
    }


    @Override
    List<String> getStdout() {
        return this.stdoutF.get()
    }

    @Override
    List<String> getStderr() {
        return this.stderrF.get()
    }

    @Override
    boolean isSuccessful() {
        return successfulF.get()
    }

    @Override
    boolean getSuccessful() {
        return successfulF.get()
    }

}
