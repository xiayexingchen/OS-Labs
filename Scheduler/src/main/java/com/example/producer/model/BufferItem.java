package com.example.producer.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BufferItem {
    private int value;
    private String producerId;
    private LocalDateTime timestamp;
    private long waitTime; // 等待被消费的时间
}