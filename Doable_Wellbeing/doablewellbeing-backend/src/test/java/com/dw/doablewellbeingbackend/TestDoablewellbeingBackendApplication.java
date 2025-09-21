package com.dw.doablewellbeingbackend;

import org.springframework.boot.SpringApplication;

public class TestDoablewellbeingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.from(DoablewellbeingBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
