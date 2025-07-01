package com.example.scheduler.model;

import lombok.Data;

@Data
public class ProcessControlBlock {
    private int pid;                // 进程ID
    private int arrivalTime;        // 进入内存时间
    private int burstTime;          // 要求服务时间
    private int remainingTime;      // 剩余服务时间（用于RR等算法）
    private int jobSize;            // 作业大小
    private int priority;           // 静态优先级
    private ProcessStatus status;   // 进程状态
    private int startTime;          // 实际开始时间
    private int finishTime;         // 完成时间
    private int waitingTime;        // 等待时间
    private int turnaroundTime;     // 周转时间
    private double weightedTurnaroundTime; // 带权周转时间
    private int timeSliceUsed ; // 已用时间片



    public ProcessControlBlock(int pid, int arrivalTime, int burstTime, int jobSize, int priority) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.jobSize = jobSize;
        this.priority = priority;
        this.status = ProcessStatus.READY;
        this.startTime = -1;
        this.finishTime = -1;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
        this.weightedTurnaroundTime = 0.0;
        this.timeSliceUsed = 0;
    }

    // getter/setter 省略，可用IDE自动生成   就是供外部获取内部信息的函数

}
