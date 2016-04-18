/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testing;

import core.WatchDir;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.gephi.JSONSender;

/**
 *
 * @author jose
 */
public class testrecursive {

    public static void main(String[] args) throws IOException {
        // parse arguments

        // Initialize graph
        Graph graph = new MultiGraph("G", false, true);

        // Initialize JSON sender
        JSONSender sender = new JSONSender("localhost", 8080, "workspace0");
        sender.setDebug(true);
        graph.addSink(sender);

        Path dir = Paths.get("output/");
        WatchDir watchdir = new WatchDir(dir, true);
        watchdir.processEvents(graph);

//        if (args.length == 0 || args.length > 2)
//            watchdir.usage();
//        boolean recursive = false;
//        int dirArg = 0;
//        if (args[0].equals("-r")) {
//            if (args.length < 2)
//                watchdir.usage();
//            watchdir.recursive = true;
//            dirArg++;
//        }
    }

}
