?/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.nio.file.*;

import org.graphstream.graph.Graph;
import org.graphstream.stream.gephi.JSONSender;
import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author jose
 */
public class CSVSender {

    public Graph graph;
    public CSVReader reader;

    public CSVSender(Graph graph) throws IOException {
        this.graph = graph;
        this.reader = null;
    }

    public void send(Path dir) throws IOException {

        if (dir.endsWith("nodes.csv"))
        {
            try {
                //Get the CSVReader instance with specifying the delimiter to be used
                reader = new CSVReader(new FileReader(dir.toString()), ',');
                String[] nextLine;
                //Read one line at a time
                // Skip first line
                reader.readNext();
                while ((nextLine = reader.readNext()) != null) {

                    //Add branches if status = 1
//                System.out.println(nextLine[3]);
                    if ("1".equals(nextLine[3])) {
                        graph.addEdge(nextLine[0], nextLine[1], nextLine[2], true);
//                            System.out.println(nextLine[0]);
                        sleep();
                        // Remove branches if Status = 0
                    } else {
//                    System.out.println(nextLine[3]);
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
        else {
            
        }
    }

    protected static void sleep() {
        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }
    }
}
