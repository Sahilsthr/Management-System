package com.furniture.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.furniture.entity.TaskStatus;
import com.furniture.entity.WorkTask;
import com.furniture.service.WorkTaskService;


@RestController
public class WorkTaskController {

    private WorkTaskService wts;

    public WorkTaskController(WorkTaskService wts){
        this.wts = wts;
    }
    
    @PostMapping("/api/tasks")
    public WorkTask createTask(@RequestBody WorkTask workTask){
        return wts.createTask(workTask);
    }

    @GetMapping("/api/tasks")
    public List<WorkTask> getAllTask() {
        return wts.getAllTask();
    }

    @GetMapping("/api/tasks/{id}")
    public Optional<WorkTask> getTaskById(@PathVariable Long id) {
        return wts.getTaskById(id);
    }
    @PutMapping("/api/tasks/{id}/status")
    public WorkTask updateTaskStatus(@PathVariable Long id, @RequestBody TaskStatus status){
        return wts.updateTaskStatus(id,status);
    }
    
    

    
}
