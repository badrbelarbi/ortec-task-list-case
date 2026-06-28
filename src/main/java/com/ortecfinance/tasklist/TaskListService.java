package com.ortecfinance.tasklist;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class TaskListService {
    private final Map<String, List<Task>> tasks = new LinkedHashMap<>();
    private long lastId = 0;

    public void addProject(String name) {
        tasks.put(name, new ArrayList<>());
    }

    public boolean addTask(String project, String description) {
        List<Task> projectTasks = tasks.get(project);

        if (projectTasks == null) {
            return false;
        }

        projectTasks.add(new Task(nextId(), description, false));
        return true;
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

    public Map<String, List<Task>> getProjects() {
        Map<String, List<Task>> projects = new LinkedHashMap<>();

        for (Map.Entry<String, List<Task>> project : tasks.entrySet()) {
            projects.put(project.getKey(), List.copyOf(project.getValue()));
        }

        return Collections.unmodifiableMap(projects);
    }

    public DeadlineView getDeadlineView() {
        Map<LocalDate, List<Task>> tasksByDeadline = new TreeMap<>();
        List<Task> tasksWithoutDeadline = new ArrayList<>();

        for (List<Task> projectTasks : tasks.values()) {
            for (Task task : projectTasks) {
                if (task.hasDeadline()) {
                    tasksByDeadline
                            .computeIfAbsent(task.getDeadline(), deadline -> new ArrayList<>())
                            .add(task);
                } else {
                    tasksWithoutDeadline.add(task);
                }
            }
        }

        Map<LocalDate, List<Task>> orderedTasksByDeadline = new LinkedHashMap<>();

        for (Map.Entry<LocalDate, List<Task>> deadlineGroup : tasksByDeadline.entrySet()) {
            orderedTasksByDeadline.put(
                    deadlineGroup.getKey(),
                    List.copyOf(deadlineGroup.getValue())
            );
        }

        return new DeadlineView(
                Collections.unmodifiableMap(orderedTasksByDeadline),
                List.copyOf(tasksWithoutDeadline)
        );
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
            Map<LocalDate, List<Task>> tasksByDeadline,
            List<Task> tasksWithoutDeadline
    ) {
    }
}