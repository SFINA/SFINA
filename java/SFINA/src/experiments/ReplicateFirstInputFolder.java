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
package experiments;

import java.io.File;

/**
 *
 * @author Ben
 */
public class ReplicateFirstInputFolder {
    int runDuration;
    String experimentConfigurationFilesLocation;
    String timeTokenName;
    
    public ReplicateFirstInputFolder(int runDuration, String experimentConfigurationFilesLocation, String timeTokenName){
        this.runDuration=runDuration;
        this.experimentConfigurationFilesLocation=experimentConfigurationFilesLocation;
        this.timeTokenName=timeTokenName;
        this.run();
    }
    
    private void run(){
        for(int i=1; i<runDuration; i++){
            File nextTimeFolderLocation = new File(experimentConfigurationFilesLocation+timeTokenName+(i+1));
            nextTimeFolderLocation.mkdirs();
//            try{
//                File sourceTop = new File(experimentConfigurationFilesLocation+timeTokenName+i+"/"+topologyDirectoryName);
//                File target = new File(experimentConfigurationFilesLocation+timeTokenName+(i+1)+"/");
//                System.out.println(sourceTop.toString());
//                System.out.println(target.toString());
//                Files.copy(sourceTop.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
//            }
//            catch(IOException ex){
//                logger.debug("Error copying input files.");
//            }
        }
    }
}
