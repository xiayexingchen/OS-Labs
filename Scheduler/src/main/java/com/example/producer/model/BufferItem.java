package com.example.producer.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BufferItem {
    private int value;
    private String producerId;
    private LocalDateTime timestamp;
    private long waitTime; // 等待被消费的时间
    private boolean isConsumed; // 是否被消费
    private String consumerId; // 消费者ID
}