package com.dw.backend.doablewellbeingbackend;

import org.springframework.boot.SpringApplication;

public class TestDoableWellbeingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.from(DoableWellbeingBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
