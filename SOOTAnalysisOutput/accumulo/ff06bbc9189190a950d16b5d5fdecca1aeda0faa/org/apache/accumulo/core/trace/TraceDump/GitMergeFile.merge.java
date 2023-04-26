package org.apache.accumulo.core.trace;

import static java.lang.Math.min;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.accumulo.cloudtrace.thrift.RemoteSpan;
import org.apache.accumulo.core.cli.ClientOnDefaultTable;
import org.apache.accumulo.core.cli.ScannerOpts;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.hadoop.io.Text;
import com.beust.jcommander.Parameter;

public class TraceDump {

    static final long DEFAULT_TIME_IN_MILLIS = 10 * 60 * 1000l;

    static class Opts extends ClientOnDefaultTable {

        @Parameter(names = { "-l", "--list" }, description = "List recent traces")
        boolean list = false;

        @Parameter(names = { "-s", "--start" }, description = "The start time of traces to display")
        String start;

        @Parameter(names = { "-e", "--end" }, description = "The end time of traces to display")
        String end;

        @Parameter(names = { "-d", "--dump" }, description = "Dump the traces")
        boolean dump = false;

        @Parameter(names = { "-i", "--instance" }, description = "URL to point to accumulo.")
        String instance;

        @Parameter(description = " <trace id> { <trace id> ... }")
        List<String> traceIds = new ArrayList<String>();

        Opts() {
            super("trace");
        }
    }

    public static void main(String[] args) throws Exception {
        Opts opts = new Opts();
        ScannerOpts scanOpts = new ScannerOpts();
        opts.parseArgs(TraceDump.class.getName(), args, scanOpts);
        int code = 0;
        if (opts.list) {
            code = listSpans(opts, scanOpts);
        }
        if (code == 0 && opts.dump) {
            code = dumpTrace(opts, scanOpts);
        }
        System.exit(code);
    }

    public static List<RemoteSpan> sortByStart(Collection<RemoteSpan> spans) {
        List<RemoteSpan> spanList = new ArrayList<RemoteSpan>(spans);
        Collections.sort(spanList, new Comparator<RemoteSpan>() {

            @Override
            public int compare(RemoteSpan o1, RemoteSpan o2) {
                return (int) (o1.start - o2.start);
            }
        });
        return spanList;
    }

    private static int listSpans(Opts opts, ScannerOpts scanOpts) throws Exception {
        PrintStream out = System.out;
        long endTime = System.currentTimeMillis();
        long startTime = endTime - DEFAULT_TIME_IN_MILLIS;
        Connector conn = opts.getConnector();
        Scanner scanner = conn.createScanner(opts.getTableName(), opts.auths);
        scanner.setBatchSize(scanOpts.scanBatchSize);
        Range range = new Range(new Text("start:" + Long.toHexString(startTime)), new Text("start:" + Long.toHexString(endTime)));
        scanner.setRange(range);
        out.println("Trace            Day/Time                 (ms)  Start");
        for (Entry<Key, Value> entry : scanner) {
            RemoteSpan span = TraceFormatter.getRemoteSpan(entry);
            out.println(String.format("%016x %s %5d %s", span.traceId, TraceFormatter.formatDate(new Date(span.getStart())), span.stop - span.start, span.description));
        }
        return 0;
    }

    public interface Printer {

        void print(String line);
    }

    private static int dumpTrace(Opts opts, ScannerOpts scanOpts) throws Exception {
        final PrintStream out = System.out;
        Connector conn = opts.getConnector();
        int count = 0;
        for (String traceId : opts.traceIds) {
            Scanner scanner = conn.createScanner(opts.getTableName(), opts.auths);
            scanner.setBatchSize(scanOpts.scanBatchSize);
            Range range = new Range(new Text(traceId.toString()));
            scanner.setRange(range);
            count = printTrace(scanner, new Printer() {

                @Override
                public void print(String line) {
                    out.println(line);
                }
            });
        }
        return count > 0 ? 0 : 1;
    }

    public static int printTrace(Scanner scanner, final Printer out) {
        int count = 0;
        SpanTree tree = new SpanTree();
        long start = Long.MAX_VALUE;
        for (Entry<Key, Value> entry : scanner) {
            RemoteSpan span = TraceFormatter.getRemoteSpan(entry);
            tree.addNode(span);
            start = min(start, span.start);
            if (span.parentId <= 0)
                count++;
        }
        out.print(String.format("Trace started at %s", TraceFormatter.formatDate(new Date(start))));
        out.print("Time  Start  Service@Location       Name");
        final long finalStart = start;
        Set<Long> visited = tree.visit(new SpanTreeVisitor() {

            @Override
            public void visit(int level, RemoteSpan parent, RemoteSpan node, Collection<RemoteSpan> children) {
                String fmt = "%5d+%-5d %" + (level * 2 + 1) + "s%s@%s %s";
                out.print(String.format(fmt, node.stop - node.start, node.start - finalStart, "", node.svc, node.sender, node.description));
            }
        });
        tree.nodes.keySet().removeAll(visited);
        if (!tree.nodes.isEmpty()) {
            out.print("Warning: the following spans are not rooted!");
            for (RemoteSpan span : sortByStart(tree.nodes.values())) {
                out.print(String.format("%s %s %s", Long.toHexString(span.spanId), Long.toHexString(span.parentId), span.description));
            }
        }
        return count;
    }
}
