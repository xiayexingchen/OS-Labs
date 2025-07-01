package com.example.scheduler.model;


import lombok.Data;

@Data
public class Core {
    private int coreId;                   // 核心ID
    private ProcessControlBlock runningProcess; // 当前运行的进程

    public Core(int coreId) {
        this.coreId = coreId;
        this.runningProcess = null;
    }

    public int getCoreId() {
        return coreId;
    }

    public ProcessControlBlock getRunningProcess() {
        return runningProcess;
    }

    public void setRunningProcess(ProcessControlBlock process) {
        this.runningProcess = process;
    }

    public boolean isIdle() {
        return runningProcess == null;
    }
}
