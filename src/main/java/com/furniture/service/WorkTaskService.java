package com.furniture.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.furniture.dto.CreateTaskRequest;
import com.furniture.dto.WorkTaskResponse;
import com.furniture.entity.Employee;
import com.furniture.entity.Project;
import com.furniture.entity.TaskPriority;
import com.furniture.entity.TaskStatus;
import com.furniture.entity.User;
import com.furniture.entity.WorkTask;
import com.furniture.exception.BadRequestException;
import com.furniture.exception.ResourceNotFoundException;
import com.furniture.repository.EmployeeRepository;
import com.furniture.repository.ProjectEmployeeRepository;
import com.furniture.repository.ProjectRepository;
import com.furniture.repository.UserRepository;
import com.furniture.repository.WorkTaskRepository;

@Service
public class WorkTaskService {

    private final WorkTaskRepository workTaskRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectEmployeeRepository projectEmployeeRepository;
    private final UserRepository userRepository;

    public WorkTaskService(
            WorkTaskRepository workTaskRepository,
            ProjectRepository projectRepository,
            EmployeeRepository employeeRepository,
            ProjectEmployeeRepository projectEmployeeRepository,
            UserRepository userRepository) {
        this.workTaskRepository = workTaskRepository;
        this.projectRepository = projectRepository;
        this.employeeRepository = employeeRepository;
        this.projectEmployeeRepository = projectEmployeeRepository;
        this.userRepository = userRepository;
    }

    private Employee getEmployeeByEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return employeeRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee profile not found"));
    }

    public WorkTask createTask(CreateTaskRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found with id " + request.getProjectId()));

        Employee employee = null;
        if (request.getAssignedEmployeeId() != null) {
            Long employeeId = request.getAssignedEmployeeId();
            employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employee not found with id " + employeeId));

            if (!projectEmployeeRepository.existsByProjectIdAndEmployeeId(
                    request.getProjectId(), employeeId)) {
                throw new BadRequestException("Employee is not assigned to this project");
            }
        }

        WorkTask task = new WorkTask();
        task.setTaskName(request.getTaskName().trim());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority() != null
                ? request.getPriority()
                : TaskPriority.MEDIUM);
        task.setStatus(TaskStatus.PENDING);
        task.setProject(project);
        task.setAssignedEmployee(employee);

        return workTaskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public List<WorkTask> getAllTask() {
        return workTaskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public WorkTask getTaskById(Long id) {
        return workTaskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Task not found with id " + id));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProjectProgress(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id " + projectId);
        }

        long total = workTaskRepository.countByProjectId(projectId);
        long completed = workTaskRepository.countByProjectIdAndStatus(
                projectId, TaskStatus.COMPLETED);

        double progress = total > 0 ? (completed * 100.0) / total : 0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("projectId", projectId);
        result.put("totalTasks", total);
        result.put("completedTasks", completed);
        result.put("progress", progress);

        return result;
    }

    @Transactional
    public WorkTaskResponse updateTaskStatus(
            Long id,
            TaskStatus status,
            String email,
            boolean isAdmin) {

        if (status == null) {
            throw new BadRequestException("Task status cannot be null");
        }

        WorkTask task;
        if (isAdmin) {
            task = workTaskRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Task not found with id " + id));
        } else {
            Employee employee = getEmployeeByEmail(email);

            task = workTaskRepository
                    .findByIdAndAssignedEmployeeId(id, employee.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Task not found or not assigned to you"));

            if (!projectEmployeeRepository.existsByProjectIdAndEmployeeId(
                    task.getProject().getId(), employee.getId())) {
                throw new BadRequestException(
                        "You are no longer assigned to this project");
            }
        }

        task.setStatus(status);
        task.setCompletedAt(
                status == TaskStatus.COMPLETED ? LocalDateTime.now() : null);

        WorkTask saved = workTaskRepository.save(task);
        return mapToResponse(saved);
    }

    private WorkTaskResponse mapToResponse(WorkTask task) {
        return new WorkTaskResponse(
                task.getId(),
                task.getTaskName(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getCompletedAt(),
                task.getProject().getProjectName());
    }

    @Transactional(readOnly = true)
    public List<WorkTaskResponse> getMyTasks(String email) {
        Employee employee = getEmployeeByEmail(email);

        List<Long> projectIds = projectEmployeeRepository
                .findByEmployeeId(employee.getId())
                .stream()
                .map(assignment -> assignment.getProject().getId())
                .toList();

        if (projectIds.isEmpty()) {
            return List.of();
        }

        return workTaskRepository
                .findByAssignedEmployeeIdAndProjectIdIn(employee.getId(), projectIds)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
}
