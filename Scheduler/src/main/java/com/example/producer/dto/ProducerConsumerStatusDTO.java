package com.example.producer.dto;

import com.example.producer.model.BufferItem;
import com.example.producer.model.Consumer;
import com.example.producer.model.Producer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProducerConsumerStatusDTO {
    private List<BufferItem> buffer;
    private int headPointer;
    private int tailPointer;
    private int itemCount;
    private int bufferSize;
    private List<Producer> producers;
    private List<Consumer> consumers;
    private Stats stats;
    private List<String> logs;

    @Data
    @AllArgsConstructor
    public static class Stats {
        private int totalProduced;
        private int totalConsumed;
        private int bufferFullCount;
        private int bufferEmptyCount;
    }
}