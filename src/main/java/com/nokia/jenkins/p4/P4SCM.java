package com.nokia.jenkins.p4;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.exception.RequestException;
import com.perforce.p4java.impl.mapbased.client.Client;
import com.perforce.p4java.impl.mapbased.client.ClientSummary;
import com.perforce.p4java.server.IOptionsServer;
import com.perforce.p4java.server.ServerFactory;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogParser;
import hudson.scm.PollingResult;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import hudson.scm.SCMRevisionState;

public class P4SCM extends SCM {

    private String p4Port;
    
    private String p4User;
    
    private String p4Passwd;
    
    private String p4Client;
    
    private String p4Stream;
    
    private static final Logger LOGGER = Logger.getLogger(P4SCM.class.getName());
    
    @DataBoundConstructor
    public P4SCM(
            String p4Port,
            String p4User,
            String p4Passwd,
            String p4Client,
            String p4Stream) {
        
        this.p4Port = p4Port;
        this.p4User = p4User;
        this.p4Passwd = p4Passwd;
        this.p4Client = p4Client;
        this.p4Stream = p4Stream;
    }
    
    
    /**
     * @return the p4Port
     */
    public String getP4Port() {
        return p4Port;
    }


    /**
     * @param p4Port the p4Port to set
     */
    public void setP4Port(String p4Port) {
        this.p4Port = p4Port;
    }


    /**
     * @return the p4User
     */
    public String getP4User() {
        return p4User;
    }


    /**
     * @param p4User the p4User to set
     */
    public void setP4User(String p4User) {
        this.p4User = p4User;
    }


    /**
     * @return the p4Passwd
     */
    public String getP4Passwd() {
        return p4Passwd;
    }


    /**
     * @param p4Passwd the p4Passwd to set
     */
    public void setP4Passwd(String p4Passwd) {
        this.p4Passwd = p4Passwd;
    }


    /**
     * @return the p4Client
     */
    public String getP4Client() {
        return p4Client;
    }


    /**
     * @param p4Client the p4Client to set
     */
    public void setP4Client(String p4Client) {
        this.p4Client = p4Client;
    }


    /**
     * @return the p4Stream
     */
    public String getP4Stream() {
        return p4Stream;
    }


    /**
     * @param p4Stream the p4Stream to set
     */
    public void setP4Stream(String p4Stream) {
        this.p4Stream = p4Stream;
    }


    @Override
    public SCMRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> build,
            Launcher launcher, TaskListener listener) throws IOException,
            InterruptedException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected PollingResult compareRemoteRevisionWith(
            AbstractProject<?, ?> project, Launcher launcher,
            FilePath workspace, TaskListener listener, SCMRevisionState baseline)
            throws IOException, InterruptedException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean checkout(AbstractBuild<?, ?> build, Launcher launcher,
            FilePath workspace, BuildListener listener, File changelogFile)
            throws IOException, InterruptedException {
        
        PrintStream log = listener.getLogger();
        String serverUriString = "p4java://" + p4Port;
        
        try {
            IOptionsServer server = ServerFactory.getOptionsServer(serverUriString, null);
            server.connect();
            server.setUserName(p4User);
            server.login(p4Passwd);
            
            ClientSummary client = new ClientSummary();
            client.setName(p4Client);
            client.setRoot(workspace.getRemote());
        } catch (RequestException rexc) {
            log.println(rexc.getDisplayString());
            rexc.printStackTrace();
        } catch (P4JavaException exc) {
            log.println(exc.getLocalizedMessage());
            exc.printStackTrace();
        } catch (URISyntaxException e) {
            log.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public ChangeLogParser createChangeLogParser() {
        // TODO Auto-generated method stub
        return null;
    }

    @Extension
    public static final class P4SCMDescriptor extends SCMDescriptor<P4SCM> {
        public P4SCMDescriptor() {
            super(P4SCM.class, null);
            load();
        }

        public String getDisplayName() {
            return "P4";
        }

        @Override
        public SCM newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            P4SCM newInstance = (P4SCM)super.newInstance(req, formData);
            newInstance.setP4Port(formData.getString("p4Port"));
            newInstance.setP4User(formData.getString("p4User"));
            newInstance.setP4Passwd(formData.getString("p4Passwd"));
            newInstance.setP4Client(formData.getString("p4Client"));
            newInstance.setP4Stream(formData.getString("p4Stream"));
            return newInstance;
        }
        
        public String getAppName() {
            return Hudson.getInstance().getDisplayName();
        }
    }
}
