package com.ortecfinance.tasklist;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
public final class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskListService taskListService;

    @Test
    void it_creates_a_project() throws Exception {
        mockMvc.perform(post("/projects")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "secrets"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("secrets"))
                .andExpect(jsonPath("$.tasks").isArray())
                .andExpect(jsonPath("$.tasks").isEmpty());

        verify(taskListService).addProject("secrets");
    }

    @Test
    void it_rejects_project_without_name() throws Exception {
        mockMvc.perform(post("/projects")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void it_returns_projects_with_tasks() throws Exception {
        Map<String, List<Task>> projects = new LinkedHashMap<>();
        projects.put("secrets", List.of(new Task(1, "Eat more donuts.", false)));
        projects.put("training", List.of(new Task(2, "SOLID", true)));

        when(taskListService.getProjects()).thenReturn(projects);

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("secrets"))
                .andExpect(jsonPath("$[0].tasks[0].id").value(1))
                .andExpect(jsonPath("$[0].tasks[0].description").value("Eat more donuts."))
                .andExpect(jsonPath("$[0].tasks[0].done").value(false))
                .andExpect(jsonPath("$[1].name").value("training"))
                .andExpect(jsonPath("$[1].tasks[0].id").value(2))
                .andExpect(jsonPath("$[1].tasks[0].description").value("SOLID"))
                .andExpect(jsonPath("$[1].tasks[0].done").value(true));
    }

    @Test
    void it_creates_a_task_in_a_project() throws Exception {
        Task task = new Task(1, "Eat more donuts.", false);

        when(taskListService.createTask("secrets", "Eat more donuts."))
                .thenReturn(Optional.of(task));

        mockMvc.perform(post("/projects/secrets/tasks")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Eat more donuts."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Eat more donuts."))
                .andExpect(jsonPath("$.done").value(false));

        verify(taskListService).createTask("secrets", "Eat more donuts.");
    }

    @Test
    void it_rejects_task_without_description() throws Exception {
        mockMvc.perform(post("/projects/secrets/tasks")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "description": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void it_returns_not_found_when_creating_task_for_unknown_project() throws Exception {
        when(taskListService.createTask("unknown", "Eat more donuts."))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/projects/unknown/tasks")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Eat more donuts."
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void it_updates_a_task_deadline() throws Exception {
        LocalDate deadline = LocalDate.of(2024, 11, 25);
        Task task = new Task(1, "Eat more donuts.", false, deadline);

        when(taskListService.setDeadline("secrets", 1, deadline))
                .thenReturn(Optional.of(task));

        mockMvc.perform(put("/projects/secrets/tasks/1")
                        .param("deadline", "25-11-2024"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Eat more donuts."))
                .andExpect(jsonPath("$.done").value(false))
                .andExpect(jsonPath("$.deadline").value("2024-11-25"));

        verify(taskListService).setDeadline("secrets", 1, deadline);
    }

    @Test
    void it_rejects_invalid_task_deadline() throws Exception {
        mockMvc.perform(put("/projects/secrets/tasks/1")
                        .param("deadline", "invalid-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void it_returns_not_found_when_updating_unknown_task_deadline() throws Exception {
        LocalDate deadline = LocalDate.of(2024, 11, 25);

        when(taskListService.setDeadline("secrets", 999, deadline))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/projects/secrets/tasks/999")
                        .param("deadline", "25-11-2024"))
                .andExpect(status().isNotFound());
    }

    @Test
    void it_returns_projects_grouped_by_deadline() throws Exception {
        LocalDate earlierDeadline = LocalDate.of(2021, 11, 11);
        LocalDate laterDeadline = LocalDate.of(2021, 11, 13);

        Map<LocalDate, List<Task>> tasksByDeadline = new LinkedHashMap<>();
        tasksByDeadline.put(
                earlierDeadline,
                List.of(new Task(3, "Interaction-Driven Design", false, earlierDeadline))
        );
        tasksByDeadline.put(
                laterDeadline,
                List.of(new Task(1, "Eat more donuts.", false, laterDeadline))
        );

        TaskListService.DeadlineView deadlineView = new TaskListService.DeadlineView(
                tasksByDeadline,
                List.of(new Task(2, "Refactor the codebase", false))
        );

        when(taskListService.getDeadlineView()).thenReturn(deadlineView);

        mockMvc.perform(get("/projects/view_by_deadline"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.deadlineGroups[0].deadline").value("2021-11-11"))
                .andExpect(jsonPath("$.deadlineGroups[0].tasks[0].id").value(3))
                .andExpect(jsonPath("$.deadlineGroups[0].tasks[0].description").value("Interaction-Driven Design"))
                .andExpect(jsonPath("$.deadlineGroups[1].deadline").value("2021-11-13"))
                .andExpect(jsonPath("$.deadlineGroups[1].tasks[0].id").value(1))
                .andExpect(jsonPath("$.deadlineGroups[1].tasks[0].description").value("Eat more donuts."))
                .andExpect(jsonPath("$.tasksWithoutDeadline[0].id").value(2))
                .andExpect(jsonPath("$.tasksWithoutDeadline[0].description").value("Refactor the codebase"));
    }
}