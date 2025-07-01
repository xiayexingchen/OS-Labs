package com.example.scheduler.controller;

import com.example.scheduler.algorithm.*;
import com.example.scheduler.dto.InitRequestDTO;
import com.example.scheduler.dto.SchedulerStatusDTO;
import com.example.scheduler.model.ProcessControlBlock;
import com.example.scheduler.model.ScheduleResult;
import com.example.scheduler.service.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scheduler")
public class SchedulerController {

    @Autowired
    private SchedulerService schedulerService;

    /**
     * 初始化进程列表与算法
     * POST /api/scheduler/init
     * body: { "processes": [...], "algorithm": "fcfs" }
     */
    @PostMapping("/init")
    public  SchedulerStatusDTO init(@RequestBody InitRequestDTO dto) {
        schedulerService.initProcesses(dto.getProcesses());
        ISchedulingAlgorithm algorithm;
        switch (dto.getAlgorithm().toLowerCase()) {
            case "fcfs":
                algorithm = new FCFSAlgorithm();
                break;
            case "rr":
                algorithm = new RoundRobinAlgorithm(dto.getTimeSlice() != null ? dto.getTimeSlice() : 1);
                break;
            case "sjf":
                algorithm = new SJFAlgorithm();
                break;
            case "priority":
                algorithm = new PriorityAlgorithm();
                break;
            case "hrn":
                algorithm = new HRNAlgorithm();
                break;

            default:
                algorithm = new FCFSAlgorithm();
                break;
        }
        schedulerService.setAlgorithm(algorithm);
        return new SchedulerStatusDTO(
                schedulerService.getCurrentTime(),
                schedulerService.getReadyQueue(),
                schedulerService.getCores(),
                schedulerService.getFinishedQueue()
        );
    }

    /**
     * 单步调度
     * POST /api/scheduler/step
     */
    @PostMapping("/step")
    public SchedulerStatusDTO step() {
        if (!schedulerService.isAllFinished()) {
            schedulerService.scheduleStep();
        }
        return new SchedulerStatusDTO(
                schedulerService.getCurrentTime(),
                schedulerService.getReadyQueue(),
                schedulerService.getCores(),
                schedulerService.getFinishedQueue()
        );
    }

    /**
     * 一次性运行到结束
     * POST /api/scheduler/run
     */
    @PostMapping("/run")
    public SchedulerStatusDTO runAll() {
        schedulerService.runToEnd();
        return new SchedulerStatusDTO(
                schedulerService.getCurrentTime(),
                schedulerService.getReadyQueue(),
                schedulerService.getCores(),
                schedulerService.getFinishedQueue()
        );
    }

    /**
     * 查询当前调度状态
     * GET /api/scheduler/status
     */
    @GetMapping("/status")
    public SchedulerStatusDTO status() {
        return new SchedulerStatusDTO(
                schedulerService.getCurrentTime(),
                schedulerService.getReadyQueue(),
                schedulerService.getCores(),
                schedulerService.getFinishedQueue()
        );
    }

    /**
     * 查询性能统计
     * GET /api/scheduler/performance
     */
    @GetMapping("/performance")
    public ScheduleResult performance() {
        return schedulerService.calculatePerformance();
    }

    /**
     * 重置调度系统
     * POST /api/scheduler/reset
     */
    @PostMapping("/reset")
    public String reset() {
        schedulerService.reset();
        return "已重置";
    }

    /**
     * 测试demo接口（可选）
     * GET /api/scheduler/test-demo
     * 方便你本地测试，无需前端
     */
    @GetMapping("/test-demo")
    public Object testDemo() {
        List<ProcessControlBlock> testProcs = List.of(
                new ProcessControlBlock(1, 0, 3, 10, 2),
                new ProcessControlBlock(2, 1, 2, 12, 1),
                new ProcessControlBlock(3, 2, 1, 8, 3)
        );
        schedulerService.initProcesses(testProcs);
        schedulerService.setAlgorithm(new FCFSAlgorithm());
        schedulerService.runToEnd();
        return schedulerService.calculatePerformance();
    }

    @GetMapping("/test-rr")
    public SchedulerStatusDTO testRR() {
        List<ProcessControlBlock> testProcs = List.of(
                new ProcessControlBlock(1, 0, 3, 10, 2),
                new ProcessControlBlock(2, 1, 2, 12, 1),
                new ProcessControlBlock(3, 2, 1, 8, 3)
        );
        schedulerService.initProcesses(testProcs);
        // 使用RR算法，时间片设为2
        schedulerService.setAlgorithm(new RoundRobinAlgorithm(2));
        schedulerService.runToEnd();
        return new SchedulerStatusDTO(
                schedulerService.getCurrentTime(),
                schedulerService.getReadyQueue(),
                schedulerService.getCores(),
                schedulerService.getFinishedQueue()
        );
    }
    @GetMapping("/test-sjf")
    public SchedulerStatusDTO testSJF() {
        List<ProcessControlBlock> testProcs = List.of(
                new ProcessControlBlock(1, 0, 5, 10, 2), // 到达时间0，运行时间5
                new ProcessControlBlock(2, 0, 2, 12, 1), // 到达时间1，运行时间2
                new ProcessControlBlock(3, 0, 1, 8, 3),  // 到达时间2，运行时间1
                new ProcessControlBlock(4, 0, 4, 15, 2), // 到达时间3，运行时间4
                new ProcessControlBlock(5, 0, 3, 11, 1)  // 到达时间4，运行时间3
        );
        schedulerService.initProcesses(testProcs);
        // 使用SJF算法
        schedulerService.setAlgorithm(new SJFAlgorithm());
        schedulerService.runToEnd();
        return new SchedulerStatusDTO(
                schedulerService.getCurrentTime(),
                schedulerService.getReadyQueue(),
                schedulerService.getCores(),
                schedulerService.getFinishedQueue()
        );
    }
    @GetMapping("/test-priority")
    public SchedulerStatusDTO testPriority() {
        List<ProcessControlBlock> testProcs = List.of(
                new ProcessControlBlock(1, 0, 5, 10, 1), // 到达时间0，运行时间5
                new ProcessControlBlock(2, 0, 2, 12, 2), // 到达时间1，运行时间2
                new ProcessControlBlock(3, 0, 1, 8, 3),  // 到达时间2，运行时间1
                new ProcessControlBlock(4, 0, 4, 15, 2), // 到达时间3，运行时间4
                new ProcessControlBlock(5, 0, 3, 11, 3)  // 到达时间4，运行时间3
        );
        schedulerService.initProcesses(testProcs);
        // 使用SJF算法
        schedulerService.setAlgorithm(new PriorityAlgorithm());
        schedulerService.runToEnd();
        return new SchedulerStatusDTO(
                schedulerService.getCurrentTime(),
                schedulerService.getReadyQueue(),
                schedulerService.getCores(),
                schedulerService.getFinishedQueue()
        );
    }
    @GetMapping("/test-HRN")
    public SchedulerStatusDTO testHRN() {
        List<ProcessControlBlock> testProcs = List.of(
                new ProcessControlBlock(1, 0, 5, 10, 2), // 到达时间0，运行时间5
                new ProcessControlBlock(2, 1, 2, 12, 1), // 到达时间1，运行时间2
                new ProcessControlBlock(3, 2, 1, 8, 3),  // 到达时间2，运行时间1
                new ProcessControlBlock(4, 3, 4, 15, 2), // 到达时间3，运行时间4
                new ProcessControlBlock(5, 4, 3, 11, 1)  // 到达时间4，运行时间3
        );
        schedulerService.initProcesses(testProcs);
        // 使用SJF算法
        schedulerService.setAlgorithm(new HRNAlgorithm());
        schedulerService.runToEnd();
        return new SchedulerStatusDTO(
                schedulerService.getCurrentTime(),
                schedulerService.getReadyQueue(),
                schedulerService.getCores(),
                schedulerService.getFinishedQueue()
        );
    }
}