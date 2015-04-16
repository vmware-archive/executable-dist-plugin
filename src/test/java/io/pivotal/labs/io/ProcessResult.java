package io.pivotal.labs.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProcessResult {

    public static ProcessResult of(String... command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        return of(processBuilder);
    }

    public static ProcessResult of(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        Process process = processBuilder.start();
        process.waitFor();
        return of(process);
    }

    public static ProcessResult of(Process process) throws IOException {
        int exitValue = process.exitValue();
        String output = readFully(process.getInputStream());
        String error = readFully(process.getErrorStream());

        return new ProcessResult(exitValue, output, error);
    }

    private static String readFully(InputStream in) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        ResourceUtils.copy(in, buf);
        return buf.toString();
    }

    private final int exitValue;
    private final String output;
    private final String error;

    public ProcessResult(int exitValue, String output, String error) {
        this.exitValue = exitValue;
        this.output = output;
        this.error = error;
    }

    public int getExitValue() {
        return exitValue;
    }

    public String getOutput() {
        return output;
    }

    public String getError() {
        return error;
    }

}
