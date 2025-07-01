package com.example.scheduler.algorithm;

import com.example.scheduler.model.Core;
import com.example.scheduler.model.ProcessControlBlock;
import com.example.scheduler.model.ProcessStatus;

import java.util.*;

public class RoundRobinAlgorithm implements ISchedulingAlgorithm {
    private final int timeSlice;
    private final Queue<ProcessControlBlock> rrQueue = new LinkedList<>();

    public RoundRobinAlgorithm(int timeSlice) {
        this.timeSlice = timeSlice;
    }

    @Override
    public List<ProcessControlBlock> selectProcesses(List<ProcessControlBlock> readyQueue, List<Core> cores, int currentTime) {
        // 1. 保证rrQueue有所有readyQueue中的READY进程
        for (ProcessControlBlock pcb : readyQueue) {
            if (!rrQueue.contains(pcb) && pcb.getStatus() == ProcessStatus.READY) {
                rrQueue.offer(pcb);
            }
        }
        // 2. 移除已完成或不在readyQueue的进程
        rrQueue.removeIf(pcb -> pcb.getStatus() != ProcessStatus.READY || !readyQueue.contains(pcb));

        // 3. 选出空闲核心数量
        int freeCores = 0;
        for (Core core : cores) {
            if (core.getRunningProcess() == null || core.getRunningProcess().getStatus() != ProcessStatus.RUNNING) {
                freeCores++;
            }
        }

        // 4. 轮转选出可分配的进程
        List<ProcessControlBlock> selected = new ArrayList<>();
        Iterator<ProcessControlBlock> it = rrQueue.iterator();
        while (it.hasNext() && selected.size() < freeCores) {
            ProcessControlBlock pcb = it.next();
            if (pcb.getStatus() == ProcessStatus.READY) {
                selected.add(pcb);
            }
        }

        return selected;
    }

    @Override
    public String getName() {
        return "Round Robin";
    }

    public int getTimeSlice() {
        return timeSlice;
    }

    /**
     * 在SchedulerService主流程中：
     * - 每个进程需有已用时间片计数（如 pcb.setTimeSliceUsed()）
     * - 达到timeSlice时未完成则抢占回到rrQueue队尾
     * - 进程完成则移出队列
     */
}