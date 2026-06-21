package com.ortecfinance.tasklist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TaskControllerTest {
    private MockMvc mockMvc;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(new TaskRepository());
        mockMvc = MockMvcBuilders.standaloneSetup(new TaskController(taskService)).build();
    }

    @Test
    void it_creates_a_project() throws Exception {
        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "training"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.training", hasSize(0)));
    }

    @Test
    void it_gets_projects_with_their_tasks() throws Exception {
        taskService.addProject("training");
        taskService.addTask("training", "SOLID");

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.training", hasSize(1)))
                .andExpect(jsonPath("$.training[0].description", is("SOLID")))
                .andExpect(jsonPath("$.training[0].done", is(false)));
    }

    @Test
    void it_creates_a_task_for_a_project() throws Exception {
        taskService.addProject("training");

        mockMvc.perform(post("/projects/training/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "SOLID"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("SOLID")))
                .andExpect(jsonPath("$.done", is(false)));

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.training", hasSize(1)))
                .andExpect(jsonPath("$.training[0].description", is("SOLID")));
    }

    @Test
    void it_returns_not_found_when_creating_a_task_for_an_unknown_project() throws Exception {
        mockMvc.perform(post("/projects/missing/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "SOLID"
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void it_updates_a_task_deadline() throws Exception {
        taskService.addProject("training");
        Task task = taskService.addTask("training", "SOLID");

        mockMvc.perform(put("/projects/training/tasks/" + task.getId())
                        .param("deadline", "25-11-2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("SOLID")))
                .andExpect(jsonPath("$.deadline", is("2024-11-25")));
    }

    @Test
    void it_returns_not_found_when_updating_a_deadline_for_an_unknown_task() throws Exception {
        taskService.addProject("training");

        mockMvc.perform(put("/projects/training/tasks/99")
                        .param("deadline", "25-11-2024"))
                .andExpect(status().isNotFound());
    }

    @Test
    void it_returns_bad_request_when_updating_a_deadline_with_an_invalid_date() throws Exception {
        taskService.addProject("training");
        Task task = taskService.addTask("training", "SOLID");

        mockMvc.perform(put("/projects/training/tasks/" + task.getId())
                        .param("deadline", "31-02-2024"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(
                        result.getResolvedException().getMessage(),
                        containsString("Invalid deadline \"31-02-2024\". Use format dd-MM-yyyy.")
                ));
    }
}
