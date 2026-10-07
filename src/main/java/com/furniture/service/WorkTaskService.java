package com.furniture.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.furniture.entity.TaskStatus;
import com.furniture.entity.WorkTask;
import com.furniture.repository.WorkTaskRepository;

@Service
public class WorkTaskService {
    private final WorkTaskRepository wtr;

    public WorkTaskService(WorkTaskRepository wtr) {
        this.wtr = wtr;
    }

    public WorkTask createTask(WorkTask worktask) {
        if(worktask.getStatus() == null){
            worktask.setStatus(TaskStatus.PENDING);
        }
        return wtr.save(worktask);
    }

    public List<WorkTask> getAllTask() {
        return wtr.findAll();
    }

    public Optional<WorkTask> getTaskById(Long id) {
        return wtr.findById(id);
    }

    public WorkTask updateTaskStatus(Long id, TaskStatus status){

        WorkTask worktask  = wtr.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));

        worktask.setStatus(status);

        if(status == TaskStatus.COMPLETED){
            worktask.setCompletedAt(LocalDateTime.now());
        }else{
            worktask.setCompletedAt(null);
        }
        return wtr.save(worktask);
    }
}
