package ghoverblog.ovh.blogarticle.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FileSftp {

    @Value("${sftp.host}")
    private String hostRemote;

    @Value("${sftp.username")
    private String username;

    @Value("${sftp.password")
    private String password;

    @Value("${sftp.port")
    private int port;

    @Value("${sftp.sessionTimeout")
    private Integer sessionTimeout;

    @Value("${sftp.channelTimeout")
    private Integer channelTimeout;

    @Value("${sftp.channel")
    private String channel;

}
