package com.example.scheduler.algorithm;

import com.example.scheduler.model.Core;
import com.example.scheduler.model.ProcessControlBlock;
import com.example.scheduler.model.ProcessStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SJFAlgorithm implements ISchedulingAlgorithm {

    @Override
    public List<ProcessControlBlock> selectProcesses(List<ProcessControlBlock> readyQueue, List<Core> cores, int currentTime) {
        // 1. 统计空闲核心数量
        int freeCores = 0;
        for (Core core : cores) {
            if (core.getRunningProcess() == null || core.getRunningProcess().getStatus() != ProcessStatus.RUNNING) {
                freeCores++;
            }
        }

        // 2. 过滤出所有READY进程
        List<ProcessControlBlock> readyList = new ArrayList<>();
        for (ProcessControlBlock pcb : readyQueue) {
            if (pcb.getStatus() == ProcessStatus.READY) {
                readyList.add(pcb);
            }
        }

        // 3. 按剩余运行时间升序排列
        readyList.sort(Comparator.comparingInt(ProcessControlBlock::getRemainingTime));

        // 4. 取前freeCores个分配
        List<ProcessControlBlock> selected = new ArrayList<>();
        for (int i = 0; i < Math.min(freeCores, readyList.size()); i++) {
            selected.add(readyList.get(i));
        }

        return selected;
    }

    @Override
    public String getName() {
        return "Shortest Job First";
    }
}
