package com.example.scheduler.algorithm;

import com.example.scheduler.model.Core;
import com.example.scheduler.model.ProcessControlBlock;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FCFSAlgorithm implements ISchedulingAlgorithm {

    @Override
    public List<ProcessControlBlock> selectProcesses(List<ProcessControlBlock> readyQueue, List<Core> cores, int currentTime) {
        // FCFS: 按到达时间排序，最多选4个进程
        readyQueue.sort(Comparator.comparingInt(ProcessControlBlock::getArrivalTime));
        List<ProcessControlBlock> selected = new ArrayList<>();
        for (ProcessControlBlock pcb : readyQueue) {
            if (selected.size() < 4) {
                selected.add(pcb);
            } else {
                break;
            }
        }
        return selected;
    }

    @Override
    public String getName() {
        return "First Come First Serve";
    }
}
