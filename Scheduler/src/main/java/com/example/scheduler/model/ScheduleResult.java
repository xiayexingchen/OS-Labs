package com.example.scheduler.model;

import lombok.Data;

import java.util.List;


@Data
public class ScheduleResult {
    private List<ProcessControlBlock> processes;
    private double avgWaitingTime;
    private double avgTurnaroundTime;
    private double avgWeightedTurnaroundTime;

    public ScheduleResult(List<ProcessControlBlock> processes, double avgWaitingTime, double avgTurnaroundTime, double avgWeightedTurnaroundTime) {
        this.processes = processes;
        this.avgWaitingTime = avgWaitingTime;
        this.avgTurnaroundTime = avgTurnaroundTime;
        this.avgWeightedTurnaroundTime = avgWeightedTurnaroundTime;
    }

    // getter/setter
}
