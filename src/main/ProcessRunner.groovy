final class ProcessRunner {

    public static runProcess(String directory, String... command) {
        return new ProcessBuilder(command)
                                .directory(new File(directory))
                                .redirectErrorStream(true)
                                .start()
    }

}