package com.example.scheduler.dto;

import com.example.scheduler.model.ProcessControlBlock;
import lombok.Data;

import java.util.List;

@Data
public class InitRequestDTO {
    private List<ProcessControlBlock> processes;
    private String algorithm;
    private Integer timeSlice; // 仅RR算法需要
}