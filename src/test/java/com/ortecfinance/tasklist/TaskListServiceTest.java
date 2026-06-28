package com.ortecfinance.tasklist;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;

public final class TaskListServiceTest {

    @Test
    void it_adds_projects_and_tasks() {
        TaskListService service = new TaskListService();

        service.addProject("secrets");
        boolean added = service.addTask("secrets", "Eat more donuts.");

        Map<String, List<Task>> projects = service.getProjects();

        assertThat(added, is(true));
        assertThat(projects, hasKey("secrets"));
        assertThat(projects.get("secrets").size(), is(1));
        assertThat(projects.get("secrets").getFirst().getId(), is(1L));
        assertThat(projects.get("secrets").getFirst().getDescription(), is("Eat more donuts."));
    }

    @Test
    void it_does_not_add_task_to_unknown_project() {
        TaskListService service = new TaskListService();

        boolean added = service.addTask("unknown", "Eat more donuts.");

        assertThat(added, is(false));
        assertThat(service.getProjects().isEmpty(), is(true));
    }

    @Test
    void it_marks_tasks_as_done_and_not_done() {
        TaskListService service = new TaskListService();
        service.addProject("training");
        service.addTask("training", "SOLID");

        boolean checked = service.setDone(1, true);
        Task task = service.getProjects().get("training").getFirst();

        assertThat(checked, is(true));
        assertThat(task.isDone(), is(true));

        boolean unchecked = service.setDone(1, false);

        assertThat(unchecked, is(true));
        assertThat(task.isDone(), is(false));
    }

    @Test
    void it_returns_false_when_marking_unknown_task() {
        TaskListService service = new TaskListService();

        boolean updated = service.setDone(999, true);

        assertThat(updated, is(false));
    }

    @Test
    void it_sets_task_deadline() {
        TaskListService service = new TaskListService();
        service.addProject("training");
        service.addTask("training", "Interaction-Driven Design");

        LocalDate deadline = LocalDate.of(2024, 11, 25);
        boolean updated = service.setDeadline(1, deadline);

        Task task = service.getProjects().get("training").getFirst();

        assertThat(updated, is(true));
        assertThat(task.getDeadline(), is(deadline));
    }

    @Test
    void it_groups_tasks_by_deadline_with_no_deadline_tasks_last() {
        TaskListService service = new TaskListService();

        service.addProject("secrets");
        service.addTask("secrets", "Eat more donuts.");
        service.addTask("secrets", "Refactor the codebase");

        service.addProject("training");
        service.addTask("training", "Interaction-Driven Design");

        LocalDate laterDeadline = LocalDate.of(2021, 11, 13);
        LocalDate earlierDeadline = LocalDate.of(2021, 11, 11);

        service.setDeadline(1, laterDeadline);
        service.setDeadline(3, earlierDeadline);

        TaskListService.DeadlineView deadlineView = service.getDeadlineView();

        assertThat(deadlineView.tasksByDeadline().keySet(), contains(earlierDeadline, laterDeadline));
        assertThat(deadlineView.tasksByDeadline().get(earlierDeadline).getFirst().getDescription(), is("Interaction-Driven Design"));
        assertThat(deadlineView.tasksByDeadline().get(laterDeadline).getFirst().getDescription(), is("Eat more donuts."));
        assertThat(deadlineView.tasksWithoutDeadline().getFirst().getDescription(), is("Refactor the codebase"));
    }
}