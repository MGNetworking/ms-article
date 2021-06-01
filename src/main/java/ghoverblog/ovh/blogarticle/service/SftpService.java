package ghoverblog.ovh.blogarticle.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public interface SftpService {


    /**
     * L'envoie de fichier sur server local a un server distant.
     * Il faut donnée le path locale et path distant.
     *
     * @param pathLocalFile
     * @param pathRemoteFile
     * @throws Exception
     */
    void uploadFile(String pathLocalFile, String pathRemoteFile) throws Exception;

    /**
     * Téléchargement de fichier d'un server local a un autre server distant.
     * Il faut donnée le path locale et path distant.
     *
     * @param pathLocalFile
     * @param pathRemoteFile
     * @throws Exception
     */
    void dowloadFile(String pathLocalFile, String pathRemoteFile) throws Exception;

    /**
     * Permet de créer un cannale SFTP
     *
     * @return objet ChannelSftp
     * @throws JSchException
     */
    ChannelSftp createChannelSftp() throws JSchException;

    /**
     * Deconnection du canale SFTP
     *
     * @param channelSftp
     * @throws Exception
     */
    void disconnectChannelSftp(ChannelSftp channelSftp) throws Exception;


}
