package com.nokia.jenkins.p4;

import com.perforce.p4java.core.IChangelistSummary;
import hudson.scm.SCMRevisionState;

/**
 * This object stores Perforce workspace state as a {@link IChangelistSummary}.
 * 
 * When attached to a build, one can use it for polling operations and 
 * changelist calculations.
 * 
 * @author mitapani
 *
 */
public class P4SCMRevisionState extends SCMRevisionState {
    
    private final IChangelistSummary revision;
    
    public P4SCMRevisionState(IChangelistSummary revision){
        this.revision = revision;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof P4SCMRevisionState){
            P4SCMRevisionState comp = (P4SCMRevisionState) obj;
            return comp.revision == this.revision;
        } else {
            return false;
        }
    }

//    @Override
//    public int hashCode() {
//        int hash = 7;
//        hash = 23 * hash + this.revision;
//        return hash;
//    }

    public IChangelistSummary getRevision() {
        return revision;
    }

}
