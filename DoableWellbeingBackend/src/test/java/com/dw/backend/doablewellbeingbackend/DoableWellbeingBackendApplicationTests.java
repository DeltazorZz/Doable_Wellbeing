package com.dw.backend.doablewellbeingbackend;

import com.dw.backend.doablewellbeingbackend.it.IntegrationTestBase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Disabled
@Import(IntegrationTestBase.class)
@SpringBootTest
class DoableWellbeingBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
