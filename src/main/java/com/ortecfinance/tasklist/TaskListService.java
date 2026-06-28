package com.ortecfinance.tasklist;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Service
public final class TaskListService {
    private final Map<String, List<Task>> tasks = new LinkedHashMap<>();
    private long lastId = 0;

    public void addProject(String name) {
        tasks.put(name, new ArrayList<>());
    }

    public boolean addTask(String project, String description) {
        return createTask(project, description).isPresent();
    }

    public Optional<Task> createTask(String project, String description) {
        List<Task> projectTasks = tasks.get(project);

        if (projectTasks == null) {
            return Optional.empty();
        }

        Task task = new Task(nextId(), description, false);
        projectTasks.add(task);

        return Optional.of(task);
    }

    public boolean setDone(long id, boolean done) {
        Task task = findTask(id);

        if (task == null) {
            return false;
        }

        task.setDone(done);
        return true;
    }

    public boolean setDeadline(long id, LocalDate deadline) {
        Task task = findTask(id);

        if (task == null) {
            return false;
        }

        task.setDeadline(deadline);
        return true;
    }

    public Optional<Task> setDeadline(String project, long taskId, LocalDate deadline) {
        Task task = findTask(project, taskId);

        if (task == null) {
            return Optional.empty();
        }

        task.setDeadline(deadline);
        return Optional.of(task);
    }

    public Map<String, List<Task>> getProjects() {
        return copyProjects(tasks);
    }

    public Map<String, List<Task>> getProjectsWithTasksDueOn(LocalDate date) {
        Map<String, List<Task>> projectsWithTasksDueOnDate = new LinkedHashMap<>();

        for (Map.Entry<String, List<Task>> project : tasks.entrySet()) {
            List<Task> tasksDueOnDate = new ArrayList<>();

            for (Task task : project.getValue()) {
                if (task.hasDeadline() && task.getDeadline().equals(date)) {
                    tasksDueOnDate.add(task);
                }
            }

            if (!tasksDueOnDate.isEmpty()) {
                projectsWithTasksDueOnDate.put(project.getKey(), tasksDueOnDate);
            }
        }

        return copyProjects(projectsWithTasksDueOnDate);
    }

    public DeadlineView getDeadlineView() {
        Map<LocalDate, Map<String, List<Task>>> tasksByDeadline = new TreeMap<>();
        Map<String, List<Task>> tasksWithoutDeadline = new LinkedHashMap<>();

        for (Map.Entry<String, List<Task>> project : tasks.entrySet()) {
            String projectName = project.getKey();

            for (Task task : project.getValue()) {
                if (task.hasDeadline()) {
                    tasksByDeadline
                            .computeIfAbsent(task.getDeadline(), deadline -> new LinkedHashMap<>())
                            .computeIfAbsent(projectName, name -> new ArrayList<>())
                            .add(task);
                } else {
                    tasksWithoutDeadline
                            .computeIfAbsent(projectName, name -> new ArrayList<>())
                            .add(task);
                }
            }
        }

        Map<LocalDate, Map<String, List<Task>>> orderedTasksByDeadline = new LinkedHashMap<>();

        for (Map.Entry<LocalDate, Map<String, List<Task>>> deadlineGroup : tasksByDeadline.entrySet()) {
            orderedTasksByDeadline.put(
                    deadlineGroup.getKey(),
                    copyProjects(deadlineGroup.getValue())
            );
        }

        return new DeadlineView(
                Collections.unmodifiableMap(orderedTasksByDeadline),
                copyProjects(tasksWithoutDeadline)
        );
    }

    private Map<String, List<Task>> copyProjects(Map<String, List<Task>> projectsToCopy) {
        Map<String, List<Task>> projects = new LinkedHashMap<>();

        for (Map.Entry<String, List<Task>> project : projectsToCopy.entrySet()) {
            projects.put(project.getKey(), List.copyOf(project.getValue()));
        }

        return Collections.unmodifiableMap(projects);
    }

    private Task findTask(String project, long taskId) {
        List<Task> projectTasks = tasks.get(project);

        if (projectTasks == null) {
            return null;
        }

        for (Task task : projectTasks) {
            if (task.getId() == taskId) {
                return task;
            }
        }

        return null;
    }

    private Task findTask(long id) {
        for (List<Task> projectTasks : tasks.values()) {
            for (Task task : projectTasks) {
                if (task.getId() == id) {
                    return task;
                }
            }
        }

        return null;
    }

    private long nextId() {
        return ++lastId;
    }

    public record DeadlineView(
            Map<LocalDate, Map<String, List<Task>>> tasksByDeadline,
            Map<String, List<Task>> tasksWithoutDeadline
    ) {
    }
}