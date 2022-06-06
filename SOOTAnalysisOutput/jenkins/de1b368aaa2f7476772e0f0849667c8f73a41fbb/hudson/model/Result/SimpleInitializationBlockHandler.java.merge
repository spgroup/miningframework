package hudson.model;

import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import hudson.cli.declarative.OptionHandlerExtension;
import hudson.init.Initializer;
import hudson.util.EditDistance;
import org.apache.commons.beanutils.Converter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.*;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.export.CustomExportedBean;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class Result implements Serializable, CustomExportedBean {

    public static final Result SUCCESS = new Result("SUCCESS", BallColor.BLUE, 0, true);

    public static final Result UNSTABLE = new Result("UNSTABLE", BallColor.YELLOW, 1, true);

    public static final Result FAILURE = new Result("FAILURE", BallColor.RED, 2, true);

    public static final Result NOT_BUILT = new Result("NOT_BUILT", BallColor.NOTBUILT, 3, false);

    public static final Result ABORTED = new Result("ABORTED", BallColor.ABORTED, 4, false);

    private final String name;

    public final int ordinal;

    public final BallColor color;

    public final boolean completeBuild;

    private Result(String name, BallColor color, int ordinal, boolean complete) {
        this.name = name;
        this.color = color;
        this.ordinal = ordinal;
        this.completeBuild = complete;
    }

    public Result combine(Result that) {
        if (this.ordinal < that.ordinal)
            return that;
        else
            return this;
    }

    public boolean isWorseThan(Result that) {
        return this.ordinal > that.ordinal;
    }

    public boolean isWorseOrEqualTo(Result that) {
        return this.ordinal >= that.ordinal;
    }

    public boolean isBetterThan(Result that) {
        return this.ordinal < that.ordinal;
    }

    public boolean isBetterOrEqualTo(Result that) {
        return this.ordinal <= that.ordinal;
    }

    public boolean isCompleteBuild() {
        return this.completeBuild;
    }

    @Override
    public String toString() {
        return name;
    }

    public String toExportedObject() {
        return name;
    }

    public static Result fromString(String s) {
        for (Result r : all) if (s.equalsIgnoreCase(r.name))
            return r;
        return FAILURE;
    }

    private static List<String> getNames() {
        List<String> l = new ArrayList<String>();
        for (Result r : all) l.add(r.name);
        return l;
    }

    private Object readResolve() {
        for (Result r : all) if (ordinal == r.ordinal)
            return r;
        return FAILURE;
    }

    private static final long serialVersionUID = 1L;

    private static final Result[] all = new Result[] { SUCCESS, UNSTABLE, FAILURE, NOT_BUILT, ABORTED };

    public static final SingleValueConverter conv = new AbstractSingleValueConverter() {

        public boolean canConvert(Class clazz) {
            return clazz == Result.class;
        }

        public Object fromString(String s) {
            return Result.fromString(s);
        }
    };

    @OptionHandlerExtension
    public static final class OptionHandlerImpl extends OptionHandler<Result> {

        public OptionHandlerImpl(CmdLineParser parser, OptionDef option, Setter<? super Result> setter) {
            super(parser, option, setter);
        }

        @Override
        public int parseArguments(Parameters params) throws CmdLineException {
            String param = params.getParameter(0);
            Result v = fromString(param.replace('-', '_'));
            if (v == null)
                throw new CmdLineException(owner, "No such status '" + param + "'. Did you mean " + EditDistance.findNearest(param.replace('-', '_').toUpperCase(), getNames()));
            setter.addValue(v);
            return 1;
        }

        @Override
        public String getDefaultMetaVariable() {
            return "STATUS";
        }
    }

    @Initializer
    public static void init() {
        Stapler.CONVERT_UTILS.register(new Converter() {

            public Object convert(Class type, Object value) {
                return Result.fromString(value.toString());
            }
        }, Result.class);
    }
}