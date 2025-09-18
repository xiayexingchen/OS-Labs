package com.example.producer.model;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Data
public class BufferItem {
    private int value;
    private String producerId;
    private LocalDateTime timestamp;
    private long waitTime; // 等待被消费的时间
    private boolean isConsumed; // 是否被消费
    private String consumerId; // 消费者ID
    private String state; // 状态字段（字符串）
    private long remainingTime; // 剩余时间
    @JsonIgnore
    private final Object slotLock = new Object(); // 每个槽位的单独锁
    
    public BufferItem() {
        this.state = "空"; // 初始状态为空
    }
}