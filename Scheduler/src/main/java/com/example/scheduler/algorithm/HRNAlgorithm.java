package com.example.scheduler.algorithm;

import com.example.scheduler.model.Core;
import com.example.scheduler.model.ProcessControlBlock;
import com.example.scheduler.model.ProcessStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HRNAlgorithm implements ISchedulingAlgorithm {

    @Override
    public List<ProcessControlBlock> selectProcesses(List<ProcessControlBlock> readyQueue, List<Core> cores, int currentTime) {
        // 1. 统计空闲核心数量
        int freeCores = 0;
        for (Core core : cores) {
            if (core.getRunningProcess() == null || core.getRunningProcess().getStatus() != ProcessStatus.RUNNING) {
                freeCores++;
            }
        }

        // 2. 过滤所有READY进程
        List<ProcessControlBlock> readyList = new ArrayList<>();
        for (ProcessControlBlock pcb : readyQueue) {
            if (pcb.getStatus() == ProcessStatus.READY) {
                readyList.add(pcb);
            }
        }

        // 3. 按HRN响应比降序排列
        readyList.sort((a, b) -> {
            double r1 = calcResponseRatio(a, currentTime);
            double r2 = calcResponseRatio(b, currentTime);
            // 响应比大的优先
            return Double.compare(r2, r1);
        });

        // 4. 取前freeCores个
        List<ProcessControlBlock> selected = new ArrayList<>();
        for (int i = 0; i < Math.min(freeCores, readyList.size()); i++) {
            selected.add(readyList.get(i));
        }
        return selected;
    }

    private double calcResponseRatio(ProcessControlBlock pcb, int currentTime) {
        int waitingTime = currentTime - pcb.getArrivalTime();
        int burstTime = pcb.getRemainingTime(); // 或 pcb.getBurstTime()，要看你的实现，通常是剩余时间
        return ((double) waitingTime + burstTime) / burstTime;
    }

    @Override
    public String getName() {
        return "Highest Response Ratio Next";
    }
}
