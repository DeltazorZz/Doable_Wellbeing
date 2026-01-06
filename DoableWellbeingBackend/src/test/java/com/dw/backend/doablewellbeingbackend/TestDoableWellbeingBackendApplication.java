package com.dw.backend.doablewellbeingbackend;

import com.dw.backend.doablewellbeingbackend.it.IntegrationTestBase;
import org.springframework.boot.SpringApplication;

public class TestDoableWellbeingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.from(DoableWellbeingBackendApplication::main).with(IntegrationTestBase.class).run(args);
    }

}
