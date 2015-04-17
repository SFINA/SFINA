/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network;

import dsutil.generic.state.State;
import dsutil.protopeer.FingerDescriptor;
import java.util.UUID;

/**
 *
 * @author evangelospournaras
 */
public class Link {
    
    public static String id=UUID.randomUUID().toString();
    private String index;
    private String startNodeID;
    private String endNodeID;
    public State state;
    
    public Link(String index){
        this.index=index;
    }
    
}
