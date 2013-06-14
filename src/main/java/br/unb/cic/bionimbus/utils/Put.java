package br.unb.cic.bionimbus.utils;

import br.unb.cic.bionimbus.p2p.Host;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class Put {

        private JSch jsch = new JSch();
        private Session session = null;
        private Host host;
        private String address;
        private String USER="zoonimbus";
        private String PASSW="zoonimbus";
        private int PORT=22;
        private  Channel channel;
        private String path;
        
        public  Put(String address, String path)
        {
            this.address=address;
            this.path=path;
        }

    public Put() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
        public boolean startSession() throws JSchException{
            session = jsch.getSession(USER, host.getAddress(), PORT); 
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(PASSW);
            if(session.isConnected())
                return true;
            return false;
        }
        public boolean startChannel() throws JSchException{
            this.channel = session.openChannel("sftp");
            channel.connect();
            if(channel.isConnected())
                return true;
            return false;
        }
        
        public boolean transferData() throws SftpException{
            ChannelSftp sftpChannel = (ChannelSftp) channel; 
            sftpChannel.put(path, path.substring(1+path.lastIndexOf("/")).trim());
            sftpChannel.exit();
            session.disconnect();
            
            return true;
        }
}