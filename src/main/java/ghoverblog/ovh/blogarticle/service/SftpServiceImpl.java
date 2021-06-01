package ghoverblog.ovh.blogarticle.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.*;
import ghoverblog.ovh.blogarticle.entities.FileSftp;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Cette classe permet la gestion d'un service SFTP
 */
@Log4j2
@Service
public class SftpServiceImpl implements SftpService {

    FileSftp fileSftp = new FileSftp();

    @Override
    public void uploadFile(String pathLocalFile,
                           String pathRemoteFile) throws Exception {

        ChannelSftp channelSftp = this.createChannelSftp();

        try {

            channelSftp.put(pathLocalFile, pathRemoteFile);

        } catch (SftpException sftpex) {

            log.error(sftpex.getMessage());
            log.error(sftpex.getCause());


            throw new RuntimeException(sftpex.getCause() + "\n" +
                    sftpex.getMessage());

        } finally {
            disconnectChannelSftp(channelSftp);
        }

    }

    @Override
    public void dowloadFile(String pathLocalFile,
                            String pathRemoteFile) throws Exception {

        ChannelSftp channelSftp = createChannelSftp();
        OutputStream outputStream;
        try {
            File file = new File(pathLocalFile);
            outputStream = new FileOutputStream(file);
            channelSftp.get(pathRemoteFile, outputStream);
            file.createNewFile();

        } catch (SftpException | IOException ex) {

            log.error(ex.getMessage());
            log.error(ex.getCause());

            throw new RuntimeException(ex.getMessage() + "\n" +
                    ex.getCause());

        } finally {
            disconnectChannelSftp(channelSftp);
        }

    }

    @Override
    public ChannelSftp createChannelSftp() throws RuntimeException {

        JSch jsch = new JSch();

        ChannelSftp channelSftp = null;

        try {
            //jsch.setKnownHosts("/Users/Maxime/.ssh/known_hosts");
            Session jschSession = jsch.getSession(
                    fileSftp.getUsername(),
                    fileSftp.getHostRemote(),
                    fileSftp.getPort());

            jschSession.setConfig("StrictHostKeyChecking", "no");
            jschSession.setPassword(fileSftp.getPassword());
            jschSession.connect(fileSftp.getSessionTimeout());

            channelSftp = (ChannelSftp) jschSession.openChannel(fileSftp.getChannel());
            channelSftp.connect(fileSftp.getChannelTimeout());

        } catch (JSchException jsc) {
            log.error(jsc.getMessage());
            log.error(jsc.getCause());

            throw new RuntimeException(jsc.getCause() + "\n" +
                    jsc.getMessage());
        }

        return channelSftp;
    }

    @Override
    public void disconnectChannelSftp(ChannelSftp channelSftp) throws Exception {

        try {
            if (channelSftp != null) {
                log.info("canale en cours de déconnection");

                if (channelSftp.isConnected()) {

                    channelSftp.disconnect();
                    log.info("canale déconnecter");
                }

                if (channelSftp.getSession() != null) {

                    channelSftp.getSession().disconnect();
                    log.info("session déconnecter");
                }
            }

        } catch (Exception ex) {
            log.error(ex.getMessage());
            log.error(ex.getCause());
        }
    }


}
