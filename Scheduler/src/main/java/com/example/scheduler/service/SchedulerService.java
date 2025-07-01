package com.example.scheduler.service;

import com.example.scheduler.algorithm.RoundRobinAlgorithm;
import com.example.scheduler.model.*;
import com.example.scheduler.algorithm.ISchedulingAlgorithm;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.*;

@Data
@Service
public class SchedulerService {

    @Getter
    private List<ProcessControlBlock> processList;   // 所有进程
    @Getter
    private List<ProcessControlBlock> readyQueue;    // 就绪队列
    @Getter
    private List<ProcessControlBlock> finishedQueue; // 完成队列

    @Getter
    private List<Core> cores;                        // 4核模拟

    @Getter
    @Setter
    private int currentTime;                         // 当前时刻

    @Getter
    @Setter
    private ISchedulingAlgorithm algorithm;          // 当前调度算法

    public SchedulerService() {
        this.processList = new ArrayList<>();
        this.readyQueue = new ArrayList<>();
        this.finishedQueue = new ArrayList<>();
        this.cores = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            cores.add(new Core(i));
        }
        this.currentTime = 0;
        this.algorithm = null;
    }

    // 初始化进程列表（可从前端表单或文件获取）
    // 初始化进程列表（可从前端表单或文件获取）
    public void initProcesses(List<ProcessControlBlock> processes) {
        this.processList.clear();
        this.processList.addAll(processes);
        this.readyQueue.clear();
        this.finishedQueue.clear();
        for (Core core : cores) {
            core.setRunningProcess(null);
        }
        this.currentTime = 0;
        // 补充：初始化时把到达时间<=0的进程加入readyQueue
        for (ProcessControlBlock pcb : processList) {
            if (pcb.getArrivalTime() <= currentTime && pcb.getStatus() == ProcessStatus.READY) {
                readyQueue.add(pcb);
            }
        }
    }



    // 调度一步（模拟一个时间片/时钟周期）
    public void scheduleStep() {
        // 1. 更新已到达的进程到readyQueue
        for (ProcessControlBlock pcb : processList) {
            if (pcb.getArrivalTime() <= currentTime && pcb.getStatus() == ProcessStatus.READY && !readyQueue.contains(pcb)) {
                readyQueue.add(pcb);
            }
        }

        // 2. 回收已完成进程
        for (Core core : cores) {
            ProcessControlBlock running = core.getRunningProcess();
            if (running != null && running.getRemainingTime() <= 0 && running.getStatus() != ProcessStatus.FINISHED) {
                running.setStatus(ProcessStatus.FINISHED);
                running.setFinishTime(currentTime);
                finishedQueue.add(running);
                core.setRunningProcess(null);
            }
        }

        // 3. 空闲核分配进程
        List<ProcessControlBlock> runnable = algorithm.selectProcesses(readyQueue, cores, currentTime);
        int coreIndex = 0;
        for (ProcessControlBlock pcb : runnable) {
            while (coreIndex < 4 && !cores.get(coreIndex).isIdle()) coreIndex++;
            if (coreIndex >= 4) break;
            if (pcb.getStatus() == ProcessStatus.READY) {
                pcb.setStatus(ProcessStatus.RUNNING);
                if (pcb.getStartTime() == -1) pcb.setStartTime(currentTime);
                cores.get(coreIndex).setRunningProcess(pcb);
            }
        }
        // 4. 运行中的进程减少剩余时间
        for (Core core : cores) {
            ProcessControlBlock running = core.getRunningProcess();
            if (running != null && running.getStatus() == ProcessStatus.RUNNING) {
                running.setRemainingTime(running.getRemainingTime() - 1);

                // ========== RR算法时间片处理 ==========
                if (algorithm instanceof RoundRobinAlgorithm) {
                    running.setTimeSliceUsed(running.getTimeSliceUsed() + 1);
                    int timeSlice = ((RoundRobinAlgorithm) algorithm).getTimeSlice();

                    if (running.getRemainingTime() > 0 && running.getTimeSliceUsed() >= timeSlice) {
                        // 时间片到期但未完成，让出CPU
                        running.setStatus(ProcessStatus.READY);
                        running.setTimeSliceUsed(0);
                        core.setRunningProcess(null);
                        if (!readyQueue.contains(running)) {
                            readyQueue.add(running);
                        }
                        // rrQueue不需要手动操作，selectProcesses下一步会处理
                        continue; // 跳过下方统计（让出CPU不再算周转、等待，避免重复）
                    }
                }
                // ========== RR算法结束 ==========

                // 统计等待和周转
                running.setTurnaroundTime(currentTime + 1 - running.getArrivalTime());
                running.setWaitingTime(running.getTurnaroundTime() - running.getBurstTime());
                running.setWeightedTurnaroundTime((double) running.getTurnaroundTime() / running.getBurstTime());
            }
        }
        // 5. 就绪队列去除已分配/已完成进程
        readyQueue.removeIf(pcb -> pcb.getStatus() != ProcessStatus.READY);
        currentTime++;
    }

    // 判断是否全部完成
    public boolean isAllFinished() {
        return finishedQueue.size() == processList.size();
    }

    // 计算性能指标
    public ScheduleResult calculatePerformance() {
        double avgWait = finishedQueue.stream().mapToInt(ProcessControlBlock::getWaitingTime).average().orElse(0);
        double avgTurn = finishedQueue.stream().mapToInt(ProcessControlBlock::getTurnaroundTime).average().orElse(0);
        double avgWTurn = finishedQueue.stream().mapToDouble(ProcessControlBlock::getWeightedTurnaroundTime).average().orElse(0);
        return new ScheduleResult(finishedQueue, avgWait, avgTurn, avgWTurn);
    }

    // 一次性跑完全部调度
    public void runToEnd() {
        while (!isAllFinished()) {
            scheduleStep();
        }
    }

    // 重置
    public void reset() {
        for (ProcessControlBlock pcb : processList) {
            pcb.setStatus(ProcessStatus.READY);
            pcb.setRemainingTime(pcb.getBurstTime());
            pcb.setStartTime(-1);
            pcb.setFinishTime(-1);
            pcb.setWaitingTime(0);
            pcb.setTurnaroundTime(0);
            pcb.setWeightedTurnaroundTime(0);
        }
        readyQueue.clear();
        finishedQueue.clear();
        for (Core core : cores) core.setRunningProcess(null);
        currentTime = 0;
    }
}