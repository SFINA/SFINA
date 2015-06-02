/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sefina2gephi;

import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;


import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.gephi.JSONSender;

import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author jose
 */
public class Sfina2gephi {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // Watch service for monitoring the folders
 
        WatchService watcher = FileSystems.getDefault().newWatchService();
        
        // Initialize graph

        Graph graph = new MultiGraph("G", false, true);
        
        // Initialize JSON sender

        JSONSender sender = new JSONSender("localhost", 8080, "workspace0");
        sender.setDebug(true);
        graph.addSink(sender);

        CSVReader reader = null;

        Path dir = Paths.get("branch/");

        try {
            WatchKey key = dir.register(watcher,
                    ENTRY_CREATE);
        } catch (IOException x) {
            System.err.println(x);
        }

        for (;;) {

            // wait for key to be signaled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                if (kind == OVERFLOW) {
                    continue;
                }

                //The filename is the context of the event.
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path filename = ev.context();

                //Read the CSV file.
//                System.out.format("Emailing file %s%n", dir.toString()+"/"+filename.toString());
                try {
                    //Get the CSVReader instance with specifying the delimiter to be used
                    reader = new CSVReader(new FileReader(dir.toString() + "/" + filename.toString()), ',');
                    String[] nextLine;
                    //Read one line at a time
                    // Skip first line
                    reader.readNext();
                    while ((nextLine = reader.readNext()) != null) {

                        //Add branches if status = 1
                    
                        if ("1".equals(nextLine[3])) {
                            graph.addEdge(nextLine[0], nextLine[1], nextLine[2], true);
//                            System.out.println(nextLine[0]);
                        sleep();
                        // Remove branches if Status = 0
                        } else {
                            graph.removeEdge(nextLine[1], nextLine[2]);
                            sleep();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                sleep();

            }

            //Reset the key -- this step is critical if you want to receive
            //further watch events. If the key is no longer valid, the directory
            //is inaccessible so exit the loop.
            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }

//        graph.clear();
    }

    protected static void sleep() {
        try {
            Thread.sleep(200);
        } catch (Exception e) {
        }
    }

}
