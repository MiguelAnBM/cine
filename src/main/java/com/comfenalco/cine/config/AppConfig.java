package com.comfenalco.cine.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    // Reloj del sistema: permite a los servicios usar hora actual y a las pruebas inyectar un reloj fijo
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
