package com.furniture.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.furniture.entity.Employee;
import com.furniture.entity.Project;
import com.furniture.entity.TaskPriority;
import com.furniture.entity.TaskStatus;
import com.furniture.entity.WorkTask;
import com.furniture.exception.BadRequestException;
import com.furniture.exception.ResourceNotFoundException;
import com.furniture.repository.EmployeeRepository;
import com.furniture.repository.ProjectEmployeeRepository;
import com.furniture.repository.ProjectRepository;
import com.furniture.repository.WorkTaskRepository;

@Service
public class WorkTaskService {
    private final WorkTaskRepository workTaskRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectEmployeeRepository projectEmployeeRepository;

public WorkTaskService(WorkTaskRepository workTaskRepository,
                        ProjectRepository projectRepository,
                        EmployeeRepository employeeRepository,
                        ProjectEmployeeRepository projectEmployeeRepository) {
                this.workTaskRepository = workTaskRepository;
                this.projectRepository = projectRepository;
                this.employeeRepository = employeeRepository;
                this.projectEmployeeRepository = projectEmployeeRepository;
    }

    public WorkTask createTask(WorkTask task) {
        if (task.getTaskName() == null || task.getTaskName().isBlank()) {
            throw new BadRequestException("taskName is required");
        }
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.PENDING);
        }
        if (task.getPriority() == null) {
            task.setPriority(TaskPriority.MEDIUM);
        }
        if (task.getProject() == null || task.getProject().getId() == null) {
            throw new BadRequestException("project.id is required");
        }

        Long projectId = task.getProject().getId();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + projectId));

        if (task.getAssignedEmployee() != null && task.getAssignedEmployee().getId() != null) {
            Long employeeId = task.getAssignedEmployee().getId();
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id " + employeeId));
            if (!projectEmployeeRepository.existsByProjectIdAndEmployeeId(projectId, employeeId)) {
                throw new BadRequestException("Employee is not assigned to this project");
            }
            task.setAssignedEmployee(employee);
        } else {
            task.setAssignedEmployee(null);
        }

        task.setProject(project);
        return workTaskRepository.save(task);
    }

    public List<WorkTask> getAllTask() {
        return workTaskRepository.findAll();
    }

    public WorkTask getTaskById(Long id) {
        return workTaskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found with id "+ id));
    }
    public Map<String, Object> getProjectProgress(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id " + projectId);
        }
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
        WorkTask task = workTaskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found with id "+ id));
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
