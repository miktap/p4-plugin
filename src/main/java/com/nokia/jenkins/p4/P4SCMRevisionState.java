package com.nokia.jenkins.p4;

import hudson.scm.SCMRevisionState;

public class P4SCMRevisionState extends SCMRevisionState {
    
    private final int revision;
    
    public P4SCMRevisionState(int revision){
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + this.revision;
        return hash;
    }

    public int getRevision() {
        return revision;
    }

}
