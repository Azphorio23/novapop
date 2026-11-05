package com.novapop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NovaPopApplication {

    public static void main(String[] args) throws Exception {
        // Con backend/.env se conecta a Supabase; sin él, usa una base local para poder probarlo en cualquier PC
        if (!BaseDatosLocal.hayCredenciales()) {
            BaseDatosLocal.iniciarModoLocal();
        }
        SpringApplication.run(NovaPopApplication.class, args);
    }
}
