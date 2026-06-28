package com.ortecfinance.tasklist;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}