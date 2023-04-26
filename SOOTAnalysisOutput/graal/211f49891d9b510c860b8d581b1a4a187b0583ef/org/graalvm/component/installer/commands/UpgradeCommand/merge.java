package org.graalvm.component.installer.commands;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.graalvm.component.installer.CommandInput;
import org.graalvm.component.installer.Commands;
import org.graalvm.component.installer.ComponentCollection;
import org.graalvm.component.installer.ComponentInstaller;
import org.graalvm.component.installer.ComponentParam;
import org.graalvm.component.installer.Feedback;
import org.graalvm.component.installer.InstallerCommand;
import org.graalvm.component.installer.Version;
import org.graalvm.component.installer.model.ComponentInfo;

public class UpgradeCommand implements InstallerCommand {

    private static final Map<String, String> options = new HashMap<>();

    private final boolean allowDistUpgrades;

    private CommandInput input;

    private Feedback feedback;

    private UpgradeProcess helper;

    private boolean verifyJars = true;

    static {
        options.put(Commands.OPTION_IGNORE_MISSING_COMPONENTS, "");
        options.put(Commands.LONG_OPTION_IGNORE_MISSING_COMPONENTS, Commands.OPTION_IGNORE_MISSING_COMPONENTS);
        options.put(Commands.OPTION_NO_VERIFY_JARS, "");
        options.put(Commands.OPTION_NO_DOWNLOAD_PROGRESS, "");
        options.put(Commands.LONG_OPTION_NO_DOWNLOAD_PROGRESS, Commands.OPTION_NO_DOWNLOAD_PROGRESS);
        options.put(Commands.LONG_OPTION_NO_VERIFY_JARS, Commands.OPTION_NO_VERIFY_JARS);
        options.put(Commands.OPTION_USE_EDITION, "s");
        options.put(Commands.LONG_OPTION_USE_EDITION, Commands.OPTION_USE_EDITION);
        options.put(Commands.OPTION_TARGET_DIRECTORY, "s");
        options.put(Commands.LONG_OPTION_TARGET_DIRECTORY, Commands.OPTION_TARGET_DIRECTORY);
        options.put(Commands.OPTION_NO_SYMLINK, "");
        options.put(Commands.LONG_OPTION_NO_SYMLINK, Commands.OPTION_NO_SYMLINK);
        options.putAll(ComponentInstaller.componentOptions);
    }

    public UpgradeCommand(boolean allowDistUpgrades) {
        this.allowDistUpgrades = allowDistUpgrades;
    }

    public UpgradeCommand() {
        this(true);
    }

    @Override
    public Map<String, String> supportedOptions() {
        return allowDistUpgrades ? options : Collections.emptyMap();
    }

    protected boolean allowDistUpgrades() {
        return allowDistUpgrades;
    }

    UpgradeProcess getProcess() {
        return helper;
    }

    protected void initUpgradeOptions() {
        ComponentCollection coll = input.getRegistry();
        coll.setAllowDistUpdate(allowDistUpgrades());
        if (input.optValue(Commands.OPTION_IGNORE_MISSING_COMPONENTS) != null) {
            helper.setAllowMissing(true);
        }
        if (input.optValue(Commands.OPTION_NO_VERIFY_JARS) != null) {
            verifyJars = false;
        }
        String ed = input.optValue(Commands.OPTION_USE_EDITION);
        if (ed != null) {
            helper.setEditionUpgrade(ed);
        }
    }

    @Override
    public void init(CommandInput commandInput, Feedback feedBack) {
        this.input = commandInput;
        this.feedback = feedBack.withBundle(UpgradeCommand.class);
        helper = new UpgradeProcess(input, feedBack, commandInput.getRegistry());
        initUpgradeOptions();
    }

    ComponentInfo configureProcess() throws IOException {
        input.existingFiles().setVerifyJars(verifyJars);
        Version min = input.getLocalRegistry().getGraalVersion();
        String s = input.peekParameter();
        Version v = min;
        Version.Match filter = min.match(allowDistUpgrades() ? Version.Match.Type.MOSTRECENT : Version.Match.Type.COMPATIBLE);
        if (s != null) {
            try {
                Version.Match.Type mt = Version.Match.Type.COMPATIBLE;
                if (s.startsWith("=")) {
                    mt = Version.Match.Type.EXACT;
                    s = s.substring(1);
                } else if (s.startsWith("+")) {
                    mt = Version.Match.Type.INSTALLABLE;
                    s = s.substring(1);
                }
                v = Version.fromUserString(s);
                filter = v.match(mt);
                if (min.compareTo(v) > 0) {
                    throw feedback.failure("UPGRADE_CannotDowngrade", null, v.displayString());
                }
                input.nextParameter();
                input.existingFiles().matchVersion(filter);
            } catch (IllegalArgumentException ex) {
            }
        }
        for (ComponentParam p : input.existingFiles()) {
            helper.addComponent(p);
        }
        ComponentInfo info = helper.findGraalVersion(filter);
        return info;
    }

    @Override
    public int execute() throws IOException {
        input.getLocalRegistry().verifyAdministratorAccess();
        if (input.optValue(Commands.OPTION_HELP) != null) {
            feedback.output(allowDistUpgrades ? "UPGRADE_Help" : "UPDATE_Help");
            return 0;
        }
        try (UpgradeProcess h = this.helper) {
            ComponentInfo info = configureProcess();
            boolean workDone;
            if (allowDistUpgrades) {
                workDone = h.installGraalCore(info);
            } else {
                workDone = false;
            }
            h.installAddedComponents();
            if (h.addedComponents().isEmpty()) {
                return workDone ? 0 : 1;
            }
        }
        return 0;
    }
}
