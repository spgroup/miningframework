package org.springframework.boot.actuate.system;

import java.io.File;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

@Deprecated
public class ApplicationPidFileWriter extends org.springframework.boot.system.ApplicationPidFileWriter {

    public ApplicationPidFileWriter() {
        super();
    }

    public ApplicationPidFileWriter(String filename) {
        super(filename);
    }

    public ApplicationPidFileWriter(File file) {
        super(file);
    }
}
