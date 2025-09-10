package com.example.producer.model;

import lombok.Data;

@Data
public class Producer {
    private String id;
    private boolean waiting;
    private int itemsProduced;

    public Producer(String id) {
        this.id = id;
        this.waiting = false;
        this.itemsProduced = 0;
    }
}