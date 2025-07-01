package com.example.scheduler.algorithm;

import com.example.scheduler.model.Core;
import com.example.scheduler.model.ProcessControlBlock;
import java.util.List;

public interface ISchedulingAlgorithm {
    /**
     * 选择当前时刻可运行的进程列表（最多4个），按算法规则排序/选取
     * @param readyQueue 当前就绪队列
     * @param cores 当前核的状态（可选）
     * @param currentTime 当前时间
     * @return 被选中的进程列表
     */
    List<ProcessControlBlock> selectProcesses(List<ProcessControlBlock> readyQueue, List<Core> cores, int currentTime);

    /**
     * 算法名称
     */
    String getName();
}
