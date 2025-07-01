package com.example.scheduler.algorithm;

import com.example.scheduler.model.Core;
import com.example.scheduler.model.ProcessControlBlock;
import com.example.scheduler.model.ProcessStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PriorityAlgorithm implements ISchedulingAlgorithm {

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

        // 3. 按优先级降序排列（优先级大的优先），如果优先级相同可按到达时间升序
        readyList.sort(Comparator
                .comparingInt(ProcessControlBlock::getPriority).reversed()
                .thenComparingInt(ProcessControlBlock::getArrivalTime));

        // 4. 取前freeCores个
        List<ProcessControlBlock> selected = new ArrayList<>();
        for (int i = 0; i < Math.min(freeCores, readyList.size()); i++) {
            selected.add(readyList.get(i));
        }

        return selected;
    }

    @Override
    public String getName() {
        return "Static Priority";
    }
}