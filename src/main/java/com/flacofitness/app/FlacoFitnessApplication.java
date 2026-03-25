package com.flacofitness.app;

import com.flacofitness.app.config.DatabaseConnectionFailureListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FlacoFitnessApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(FlacoFitnessApplication.class);
        application.addListeners(new DatabaseConnectionFailureListener());
        application.run(args);
    }
}
