package com.furniture.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

import com.furniture.entity.TaskStatus;
import com.furniture.entity.WorkTask;
import com.furniture.service.WorkTaskService;


@RestController
public class WorkTaskController {

    private final WorkTaskService workTaskService;

    public WorkTaskController(WorkTaskService workTaskService){
        this.workTaskService = workTaskService;
    }
    
    @PostMapping("/api/tasks")
    public WorkTask createTask(@RequestBody WorkTask workTask){
        return workTaskService.createTask(workTask);
    }

    @GetMapping("/api/tasks")
    public List<WorkTask> getAllTask() {
        return workTaskService.getAllTask();
    }

    @GetMapping("/api/tasks/{id}")
    public Optional<WorkTask> getTaskById(@PathVariable Long id) {
        return workTaskService.getTaskById(id);
    }
    @PutMapping("/api/tasks/{id}/status")
    public WorkTask updateTaskStatus(@PathVariable Long id, @RequestBody TaskStatus status){
        return workTaskService.updateTaskStatus(id,status);
    }

    @GetMapping("/api/projects/{projectId}/progress")
    public Map<String, Object> getProjectProgress(@PathVariable Long projectId) {
        return workTaskService.getProjectProgress(projectId);
    }
        
        

    
}
