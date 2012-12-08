package com.nokia.jenkins.p4;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.client.IClientSummary;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.AccessException;
import com.perforce.p4java.exception.ConnectionException;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.exception.RequestException;
import com.perforce.p4java.impl.generic.client.ClientOptions;
import com.perforce.p4java.impl.mapbased.client.Client;
import com.perforce.p4java.option.client.SyncOptions;
import com.perforce.p4java.server.IServer;
import com.perforce.p4java.server.IServerInfo;
import com.perforce.p4java.server.ServerFactory;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Computer;
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

        if (p4Port==null || p4Port.equals("")) {
            log.println("*** ERROR: P4PORT missing!");
            return false;
        }
        if (p4User==null || p4User.equals("")) {
            log.println("*** ERROR: P4USER missing!");
            return false;
        }
        if (p4Client==null || p4Client.equals("")) {
            log.println("*** ERROR: P4CLIENT missing!");
            return false;
        }
        if (p4Stream==null || p4Stream.equals("")) {
            log.println("*** ERROR: Stream missing!");
            return false;
        }

        try {
            LOGGER.finest("Connecting to server: '" + serverUriString + "'.");
            IServer server = ServerFactory.getServer(serverUriString, null);
            server.connect();
            
            LOGGER.finest("Login as user '" + p4User + "'.");
            server.setUserName(p4User);
            server.login(p4Passwd);
            
            IClient currentClient = getClient(server, workspace);
            server.setCurrentClient(currentClient);

            IServerInfo info = server.getServerInfo();
            if (info != null) {
                LOGGER.finest("Info from Perforce server at URI '"
                                    + serverUriString + "':\n" 
                                    + formatInfo(info));
            }
            
            log.println("Using P4PORT: '" + p4Port + "'.");
            log.println("Syncing P4CLIENT: '" + currentClient.getName() + "' to revision: '");
            List<IFileSpec> syncList = currentClient.sync(
                    FileSpecBuilder.makeFileSpecList("//..."),
                    new SyncOptions());

            for (IFileSpec fileSpec : syncList) {
                if (fileSpec != null) {
                    if (fileSpec.getOpStatus() == FileSpecOpStatus.VALID) {
                        LOGGER.finest("Sync'd: "
                                            + fileSpec.getDepotPath()
                                            + "#" + fileSpec.getEndRevision()
                                            + " " + fileSpec.getClientPath()
                                            + " " + fileSpec.getLocalPath());
                    } else {
                        LOGGER.finest(fileSpec.getStatusMessage());
                    }
                }
                

            }
            
            if (server != null) {
                server.disconnect();
            }
        } catch (ConnectionException cexc) {
            log.println(cexc.getLocalizedMessage());
            LOGGER.finest("*** ERROR: " + cexc.getMessage());
            cexc.printStackTrace();
            return false;
        } catch (RequestException rexc) {
            log.println(rexc.getDisplayString());
            LOGGER.finest("*** ERROR: " + rexc.getMessage());
            rexc.printStackTrace();
            return false;
        } catch (P4JavaException exc) {
            log.println(exc.getLocalizedMessage());
            LOGGER.finest("*** ERROR: " + exc.getMessage());
            exc.printStackTrace();
            return false;
        } catch (URISyntaxException e) {
            log.println(e.getLocalizedMessage());
            LOGGER.finest("*** ERROR: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public ChangeLogParser createChangeLogParser() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Get the existing p4 client or create a new one.
     * 
     * @param server Perforce server
     * @param workspace Jenkins workspace
     * @return Current p4 client for the build
     * @throws IOException
     * @throws InterruptedException
     * @throws AccessException
     * @throws RequestException
     * @throws ConnectionException
     */
    private IClient getClient(IServer server, FilePath workspace) 
            throws IOException, InterruptedException, AccessException, 
            RequestException, ConnectionException {
        
        IClient client;
        // Use unique client for each node and eliminate spaces.
        String p4ClientEffective = p4Client.replaceAll(" ", "_");
        if (Computer.currentComputer() != null) {
            if (Computer.currentComputer().getName() != null && !Computer.currentComputer().getName().equals("")) {
                p4ClientEffective = p4Client.replaceAll(" ", "_") + "_" + Computer.currentComputer().getName();
            }
        }
        
        List<IClientSummary> clientList = server.getClients(p4User, p4ClientEffective, 0);
        
        if (clientList != null) {
            for (IClientSummary clientSummary : clientList) {
                if (clientSummary.getName().equals(p4ClientEffective)) {
                    LOGGER.finest("Found existing p4 client '" + p4ClientEffective + "'.");
                    client = server.getClient(clientSummary);
                    safeClientIfDirty(client, workspace);
                    return client;
                }
            }
        }
        
        LOGGER.finest("Creating new p4 client '" + p4ClientEffective + "'.");
        client = new Client();
        client.setName(p4ClientEffective);
        client.setAccessed(new Date());
        client.setUpdated(new Date());
        client.setDescription("Created by Jenkins");
        client.setHostName(getEffectiveHostName());
        client.setOwnerName(p4User);
        client.setRoot(workspace.getRemote());
        client.setLineEnd(IClientSummary.ClientLineEnd.LOCAL);
        client.setOptions(new ClientOptions(true,true,false,false,false,true));
        client.setServer(server);
        client.setStream(p4Stream);

        server.createClient(client);
        
        return client;
    }
    
    /**
     * Get the effective host name.
     * 
     * @return Host name without the domain name or UNKNOWNHOST if Jenkins is
     *          unable to retrieve the host name.
     * @throws IOException
     * @throws InterruptedException
     */
    private String getEffectiveHostName()
            throws IOException, InterruptedException {

        String host = Computer.currentComputer().getHostName();
        
        if (host == null) {
            LOGGER.finest("Could not get host name for slave " + Computer.currentComputer().getDisplayName());
            host = "UNKNOWNHOST";
        }

        if (host.contains(".")) {
            host = String.valueOf(host.subSequence(0, host.indexOf('.')));
        }
        LOGGER.finest("Using host '" + host + "'.");
        return host;
    }
    
    /**
     * Do modifications to the client if needed.
     * 
     * @param client The client to be modified.
     */
    private void safeClientIfDirty(IClient client, FilePath workspace) {
        if (!p4Stream.equals(client.getStream())) {
            client.setStream(p4Stream);
        }
        if (!p4User.equals(client.getOwnerName())) {
            client.setOwnerName(p4User);
        }
        if (!workspace.getRemote().equals(client.getRoot())) {
            client.setRoot(workspace.getRemote());
        }
    }
    
    /**
     * Create formatted p4 server information.
     * 
     * @param info Perforce server information
     * @return Formatted server information
     */
    private static String formatInfo(IServerInfo info) {
        return "\tserver address: " + info.getServerAddress() + "\n"
                + "\tserver version" + info.getServerVersion() + "\n"
                + "\tclient address: " + info.getClientAddress() + "\n"
                + "\tclient working directory: " + info.getClientCurrentDirectory() + "\n"
                + "\tclient name: " + info.getClientName() + "\n"
                + "\tuser name: " + info.getUserName();
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
