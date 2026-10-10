package com.furniture.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.furniture.entity.TaskStatus;
import com.furniture.entity.WorkTask;
import com.furniture.repository.WorkTaskRepository;

@Service
public class WorkTaskService {
    private final WorkTaskRepository workTaskRepository;

    public WorkTaskService(WorkTaskRepository workTaskRepository) {
        this.workTaskRepository = workTaskRepository;
    }

    public WorkTask createTask(WorkTask worktask) {
        if(worktask.getStatus() == null){
            worktask.setStatus(TaskStatus.PENDING);
        }
        return workTaskRepository.save(worktask);
    }

    public List<WorkTask> getAllTask() {
        return workTaskRepository.findAll();
    }

    public Optional<WorkTask> getTaskById(Long id) {
        return workTaskRepository.findById(id);
    }
    public Map<String, Object> getProjectProgress(Long projectId) {
        long total = workTaskRepository.countByProjectId(projectId);
        long completed = workTaskRepository.countByProjectIdAndStatus(projectId, TaskStatus.COMPLETED);

        double progress = 0;
        if (total > 0) {
            progress = (completed * 100.0) / total;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectId", projectId);
        result.put("totalTasks", total);
        result.put("completedTasks", completed);
        result.put("progress", progress);
        return result;
    }

    public WorkTask updateTaskStatus(Long id, TaskStatus status) {
        WorkTask task = workTaskRepository.findById(id).orElse(null);
        task.setStatus(status);

        if (status == TaskStatus.COMPLETED) {
            task.setCompletedAt(LocalDateTime.now());
        }
        else {
            task.setCompletedAt(null);
        }

        return workTaskRepository.save(task);
    }
}
