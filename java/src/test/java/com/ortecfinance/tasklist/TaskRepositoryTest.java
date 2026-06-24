package com.ortecfinance.tasklist;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public final class TaskRepositoryTest {
    @Test
    void it_adds_a_project() {
        TaskRepository repository = new TaskRepository();

        repository.addProject("training");

        assertThat(repository.findAll().containsKey("training"), is(true));
        assertThat(repository.findAll().get("training"), is(empty()));
    }

    @Test
    void it_checks_whether_a_project_exists() {
        TaskRepository repository = new TaskRepository();
        repository.addProject("training");

        assertThat(repository.projectExists("training"), is(true));
        assertThat(repository.projectExists("missing"), is(false));
    }

    @Test
    void it_adds_a_task_to_a_project() {
        TaskRepository repository = new TaskRepository();
        repository.addProject("training");

        Task task = repository.addTask("training", "SOLID");

        assertThat(task.getId(), is(1L));
        assertThat(task.getDescription(), is("SOLID"));
        assertThat(task.isDone(), is(false));
        assertThat(repository.findAll().get("training"), contains(task));
    }

    @Test
    void it_finds_a_task_by_id() {
        TaskRepository repository = new TaskRepository();
        repository.addProject("training");
        repository.addTask("training", "SOLID");
        Task expectedTask = repository.addTask("training", "Coupling and Cohesion");

        Task task = repository.findTaskById(2L);

        assertThat(task, is(expectedTask));
    }

    @Test
    void it_returns_null_when_a_task_id_does_not_exist() {
        TaskRepository repository = new TaskRepository();

        Task task = repository.findTaskById(99L);

        assertThat(task, is(nullValue()));
    }
}
