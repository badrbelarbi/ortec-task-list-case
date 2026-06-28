package com.ortecfinance.tasklist;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Map;

@RestController
public class TaskController {
    private static final DateTimeFormatter DEADLINE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

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

    @PostMapping("/projects/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(
            @PathVariable String projectId,
            @RequestBody CreateTaskRequest request
    ) {
        if (request == null || request.description() == null || request.description().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task description is required.");
        }

        String description = request.description().trim();

        return taskListService.createTask(projectId, description)
                .map(TaskResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found."));
    }

    @PutMapping("/projects/{projectId}/tasks/{taskId}")
    public TaskResponse updateTaskDeadline(
            @PathVariable String projectId,
            @PathVariable long taskId,
            @RequestParam String deadline
    ) {
        LocalDate parsedDeadline = parseDeadline(deadline);

        return taskListService.setDeadline(projectId, taskId, parsedDeadline)
                .map(TaskResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found."));
    }

    @GetMapping("/projects/view_by_deadline")
    public DeadlineViewResponse getProjectsByDeadline() {
        TaskListService.DeadlineView deadlineView = taskListService.getDeadlineView();

        List<DeadlineGroupResponse> deadlineGroups = deadlineView.tasksByDeadline()
                .entrySet()
                .stream()
                .map(this::toDeadlineGroupResponse)
                .toList();

        List<TaskResponse> tasksWithoutDeadline = deadlineView.tasksWithoutDeadline()
                .stream()
                .map(TaskResponse::from)
                .toList();

        return new DeadlineViewResponse(deadlineGroups, tasksWithoutDeadline);
    }

    private ProjectResponse toProjectResponse(Map.Entry<String, List<Task>> project) {
        List<TaskResponse> tasks = project.getValue()
                .stream()
                .map(TaskResponse::from)
                .toList();

        return new ProjectResponse(project.getKey(), tasks);
    }

    private DeadlineGroupResponse toDeadlineGroupResponse(Map.Entry<LocalDate, List<Task>> deadlineGroup) {
        List<TaskResponse> tasks = deadlineGroup.getValue()
                .stream()
                .map(TaskResponse::from)
                .toList();

        return new DeadlineGroupResponse(deadlineGroup.getKey(), tasks);
    }

    private LocalDate parseDeadline(String deadline) {
        if (deadline == null || deadline.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Deadline is required.");
        }

        try {
            return LocalDate.parse(deadline, DEADLINE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid deadline. Use dd-MM-yyyy.");
        }
    }

    public record CreateProjectRequest(String name) {
    }

    public record CreateTaskRequest(String description) {
    }

    public record ProjectResponse(String name, List<TaskResponse> tasks) {
    }

    public record DeadlineViewResponse(
            List<DeadlineGroupResponse> deadlineGroups,
            List<TaskResponse> tasksWithoutDeadline
    ) {
    }

    public record DeadlineGroupResponse(
            LocalDate deadline,
            List<TaskResponse> tasks
    ) {
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