package de.dkfz.roddy.execution.io

import java.util.concurrent.Future
import java.util.concurrent.CompletableFuture

/** Like ExecutionResult, but taking Futures of the exit code (usually the actual process) and the standard output and
 *  error streams. Calling any of the methods will block until the process is finished!
 *
 *  TODO Make ExecutionResult and AsyncExecutionResult common subclasses of an IExecutionResult interface.
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
     * @param stdout
     * @param stderr
     * @param successful   Can be a future. By default (when set to null), it will be set success if exitCode == 0.
     */
    AsyncExecutionResult(CompletableFuture<Integer> process,
                         CompletableFuture<List<String>> stdout,
                         CompletableFuture<List<String>> stderr,
                         CompletableFuture<Boolean> successful = null) {
        super(true, 0, [], "") // These are only stub-values.
        this.exitCodeF = process
        this.stderrF = stderr
        this.stdoutF = stdout
        if (null == successful) {
            this.successfulF = process.thenApply {exitCode ->
                exitCode == 0
            }
        } else {
            this.successfulF = successful
        }
    }

    @Override
    int getExitCode() {
        return exitCodeF.get()
    }

    @Override
    List<String> getResultLines() {
        return stdoutF.get()
    }

    @Override
    String getFirstLine() {
        return stdoutF.get()
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
