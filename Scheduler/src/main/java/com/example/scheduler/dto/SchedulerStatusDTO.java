package com.example.scheduler.dto;

import com.example.scheduler.model.Core;
import com.example.scheduler.model.ProcessControlBlock;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SchedulerStatusDTO {
    private int currentTime;
    private List<ProcessControlBlock> readyQueue;
    private List<Core> cores;
    private List<ProcessControlBlock> finishedQueue;
}