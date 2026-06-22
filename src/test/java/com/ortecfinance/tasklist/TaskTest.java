package com.ortecfinance.tasklist;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public final class TaskTest {

    @Test
    void task_has_no_deadline_by_default() {
        Task task = new Task(1, "Eat more donuts.", false);

        assertThat(task.hasDeadline(), is(false));
        assertThat(task.getDeadline(), is(nullValue()));
    }

    @Test
    void task_deadline_can_be_set() {
        Task task = new Task(1, "Eat more donuts.", false);
        LocalDate deadline = LocalDate.of(2024, 11, 25);

        task.setDeadline(deadline);

        assertThat(task.hasDeadline(), is(true));
        assertThat(task.getDeadline(), is(deadline));
    }
}