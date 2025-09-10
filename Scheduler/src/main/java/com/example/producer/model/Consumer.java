package com.example.producer.model;

import lombok.Data;

@Data
public class Consumer {
    private String id;
    private boolean waiting;
    private int itemsConsumed;

    public Consumer(String id) {
        this.id = id;
        this.waiting = false;
        this.itemsConsumed = 0;
    }
}