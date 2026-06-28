package com.ortecfinance.tasklist;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
public class TaskController {
    private final TaskListService taskListService;

    public TaskController(TaskListService taskListService) {
        this.taskListService = taskListService;
    }

    @PostMapping("/projects")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(@RequestBody CreateProjectRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project name is required.");
        }

        String projectName = request.name().trim();
        taskListService.addProject(projectName);

        return new ProjectResponse(projectName, List.of());
    }

    @GetMapping("/projects")
    public List<ProjectResponse> getProjects() {
        return taskListService.getProjects()
                .entrySet()
                .stream()
                .map(this::toProjectResponse)
                .toList();
    }

    private ProjectResponse toProjectResponse(Map.Entry<String, List<Task>> project) {
        List<TaskResponse> tasks = project.getValue()
                .stream()
                .map(TaskResponse::from)
                .toList();

        return new ProjectResponse(project.getKey(), tasks);
    }

    public record CreateProjectRequest(String name) {
    }

    public record ProjectResponse(String name, List<TaskResponse> tasks) {
    }

    public record TaskResponse(
            long id,
            String description,
            boolean done,
            LocalDate deadline
    ) {
        public static TaskResponse from(Task task) {
            return new TaskResponse(
                    task.getId(),
                    task.getDescription(),
                    task.isDone(),
                    task.getDeadline()
            );
        }
    }
}