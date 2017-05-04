/*
 * Copyright (C) 2015 SFINA Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package utilities;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.graphstream.graph.Graph;
import com.opencsv.CSVReader;

/** 
 * Visualization with Gephy. Not tested yet, probably not working.
 * @author Ben
 */
public class VisualizationAgent{
    
    private static final Logger logger = Logger.getLogger(VisualizationAgent.class);
    
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final boolean recursive;
    private boolean trace = false;
        
    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    public VisualizationAgent(Path dir, boolean recursive) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey, Path>();
        this.recursive = recursive;
        try{
            if (recursive) {
                System.out.format("Scanning %s ...\n", dir);
                registerAll(dir);
                System.out.println("Done.");
            } else {
                register(dir);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        // enable trace after initial registration
        this.trace = true;
    }
    
    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {

                String[] flow_dir = dir.toString().split("/");
//                System.out.format("Emailing file %s%n", nodes[nodes.length-1]);
                if ("flow".equals(flow_dir[flow_dir.length - 1])) {
                    return FileVisitResult.TERMINATE;
                } else {
                    register(dir);
                    return FileVisitResult.CONTINUE;
                }
            }
        });
    }

    /**
     * Process all events for keys queued to the watcher
     */
    private void processEvents(Graph graph) throws IOException {
        for (;;) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                // print out event
                System.out.format("%s: %s\n", event.kind().name(), child);
                if (child.endsWith("end")) {
                    System.out.println("Exit!!");
                    key.cancel();
                    break;

                } else {
                    send(child, graph);
                }
                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (recursive && (kind == ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            registerAll(child);
                        }
                    } catch (IOException x) {
                        // ignore to keep sample readbale
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    System.out.println("Exit222!!");
                    break;
                }
            }

        }
    }

    private static void usage() {
        System.err.println("usage: java WatchDir [-r] dir");
        System.exit(-1);
    }
    
    private void send(Path dir, Graph graph) throws IOException {
        CSVReader reader = null;
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

    private static void sleep() {
        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }
    }

}
