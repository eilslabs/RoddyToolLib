/*
 * Copyright (c) 2017 eilslabs.
 *
 * Distributed under the MIT License (license terms are at https://www.github.com/eilslabs/Roddy/LICENSE.txt).
 */

package de.dkfz.roddy.execution.io

import de.dkfz.roddy.core.InfoObject
import de.dkfz.roddy.tools.LoggerWrapper
import groovy.transform.CompileStatic

import java.lang.reflect.Field
import java.nio.charset.Charset
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.function.Supplier

@CompileStatic
class LocalExecutionHelper {

    private static final LoggerWrapper logger = LoggerWrapper.getLogger(LocalExecutionHelper.class.name);

    private static final ExecutorService executorService = Executors.newCachedThreadPool()

    static Integer getProcessID(Process process) {
        Field f = process.getClass().getDeclaredField("pid");
        f.setAccessible(true);
        Integer processID = f.get(process) as Integer
        return processID
    }

    /**
     * Use ExecutionResult instead. Deprecated and kept for compatibility.
     */
    @Deprecated
    static class ExtendedProcessExecutionResult extends InfoObject {
        final int exitValue;
        final String processID;
        final List<String> lines = [];

        ExtendedProcessExecutionResult(int exitValue, String processID, List<String> lines) {
            this.exitValue = exitValue
            this.processID = processID
            this.lines = lines
        }

        boolean isSuccessful() {
            return exitValue == 0
        }
    }

    public static String executeSingleCommand(String command) {
        //TODO What an windows systems?
        //Process process = Roddy.getLocalCommandSet().getShellExecuteCommand(command).execute();
        Process process = (["bash", "-c", "${command}"]).execute();

        final String separator = System.getProperty("line.separator");
        process.waitFor();
        if (process.exitValue()) {
            throw new RuntimeException("Process could not be run" + separator + "\tCommand: bash -c " + command + separator + "\treturn code is: " + process.exitValue())
        }

        def text = process.text
        return chomp(text) //Cut off trailing "\n"
    }

    static String chomp(String text) {
        text.length() >= 2 ? text[0..-2] : text
    }

    /** Read from an InputStream asynchronously using the executorService.
     *  May throw an UncheckedIOException.
     */
    static CompletableFuture<List<String>> asyncReadStringStream(
            InputStream inputStream,
            ExecutorService executorService) {
        return CompletableFuture.supplyAsync({
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))
            reader.lines().toArray() as List<String>
        } as Supplier<List<String>>, executorService)
    }

    /** This method is like asyncReadStringStream, but additionally copies the inputStream content
     *  to the outputStream.
     */
    static CompletableFuture<List<String>> asyncReadStringStream(
            InputStream inputStream,
            ExecutorService executorService,
            OutputStream outputStream) {
        return CompletableFuture.supplyAsync({
            // Make sure the same charset is used for input and output. This is what
            // InputStreamReader(InputStream) uses internally.
            String charsetName = Charset.defaultCharset().name()
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charsetName))
            List<String> result = []
            reader.eachLine {line ->
                result.add(line)
                // The reader removes the newlines, so we add them back here. UNIX only.
                byte[] bytes = (line + "\n").getBytes(charsetName)
                outputStream.write(bytes, 0, bytes.size())
            }
        } as Supplier<List<String>>, executorService)
    }

    /**
     * Execute a command using the local command interpreter Bash (currently fixed).
     *
     * If outputStream is set, the full output is going to this stream. Otherwise it is stored
     * in the returned object.
     *
     * @param command
     * @param outputStream
     * @return
     */
    static ExecutionResult executeCommandWithExtendedResult(
            String command,
            OutputStream outputStream = null,
            ExecutorService executorService = executorService) {
        List<String> bashCommand = ["bash", "-c", command]
        logger.postRareInfo("Executing the command ${command} locally.")
        ProcessBuilder processBuilder = new ProcessBuilder(bashCommand)
        Process process = processBuilder.start()
        Future<List<String>> stdout
        if (outputStream == null)
            stdout = asyncReadStringStream(process.inputStream, executorService)
        else
            stdout = asyncReadStringStream(process.inputStream, executorService, outputStream)
        Future<List<String>> stderr = asyncReadStringStream(process.errorStream, executorService)
        Future<Integer> exitCode = CompletableFuture.supplyAsync({
            process.waitFor()
        } as Supplier<Integer>, executorService)
        return new AsyncExecutionResult(bashCommand, getProcessID(process),
                exitCode, stdout, stderr).asExecutionResult()
    }

    static Process executeNonBlocking(String command) {
        Process process = ("sleep 1; " + command).execute();
        return process;
    }


    static String execute(String cmd) {
        def proc = cmd.execute();
        int res = proc.waitFor();
        if (res == 0) {
            return proc.in.text;
        }
        return "";
    }
}
