package util

final class ProcessRunner {

    public static Process runProcess(String directory, String... command) {
        return buildProcess(directory, command).start()
    }

    public static ProcessBuilder buildProcess(String directory, String... initialCommand) {
        return new ProcessBuilder(initialCommand)
                .directory(new File(directory))
                .redirectErrorStream(true)
    }

    public static void addCommand(ProcessBuilder builder, String command) {
        builder.command().add(command)
    }

    public static Process startProcess(ProcessBuilder builder) {
        return builder.start()
    }



}