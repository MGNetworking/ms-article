package ghoverblog.ovh.blogarticle.service;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.event.annotation.BeforeTestClass;

import static org.junit.jupiter.api.Assertions.*;

public class SftpServiceImplTest {


    @BeforeTestClass
    void setUp() {
    }

    @Test
    @DisplayName("test de téléchargement de fichier ")
    void uploadFile() {

        // Resource rs = resourceLoader.getResource("classpath:/static/blog/101.jpg");
        // assertTrue(rs.exists());


    }

    @Test
    void dowloadFile() {
    }

    @Test
    void createChannelSftp() {
    }

    @Test
    void disconnectChannelSftp() {
    }
}
