package com.example.producer.dto;

import lombok.Data;

@Data
public class InitRequestDTO {
    private int bufferSize;
    private int producerCount;
    private int consumerCount;
    private int simulationSpeed;
    private int productionSpeed;
    private int consumptionSpeed;
}