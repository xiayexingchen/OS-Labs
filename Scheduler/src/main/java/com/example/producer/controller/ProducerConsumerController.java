package com.example.producer.controller;

import com.example.producer.dto.InitRequestDTO;
import com.example.producer.dto.ProducerConsumerStatusDTO;
import com.example.producer.service.ProducerConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/producer-consumer")
public class ProducerConsumerController {

    @Autowired
    private ProducerConsumerService producerConsumerService;

    /**
     * 初始化生产者消费者系统
     * POST /api/producer-consumer/init
     */
    @PostMapping("/init")
    public ProducerConsumerStatusDTO init(@RequestBody InitRequestDTO request) {
        producerConsumerService.init(
                request.getBufferSize(),
                request.getProducerCount(),
                request.getConsumerCount(),
                request.getSimulationSpeed()
        );
        return producerConsumerService.getStatus();
    }

    /**
     * 开始模拟
     * POST /api/producer-consumer/start
     */
    @PostMapping("/start")
    public ProducerConsumerStatusDTO start() {
        producerConsumerService.startSimulation();
        return producerConsumerService.getStatus();
    }

    /**
     * 停止模拟
     * POST /api/producer-consumer/stop
     */
    @PostMapping("/stop")
    public ProducerConsumerStatusDTO stop() {
        producerConsumerService.stopSimulation();
        return producerConsumerService.getStatus();
    }

    /**
     * 重置模拟
     * POST /api/producer-consumer/reset
     */
    @PostMapping("/reset")
    public String reset() {
        producerConsumerService.reset();
        return "已重置";
    }

    /**
     * 获取当前状态
     * GET /api/producer-consumer/status
     */
    @GetMapping("/status")
    public ProducerConsumerStatusDTO status() {
        return producerConsumerService.getStatus();
    }

    /**
     * 检查是否正在运行
     * GET /api/producer-consumer/is-running
     */
    @GetMapping("/is-running")
    public boolean isRunning() {
        return producerConsumerService.isRunning();
    }
}