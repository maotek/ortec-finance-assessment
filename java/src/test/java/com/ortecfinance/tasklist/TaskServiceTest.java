package com.ortecfinance.tasklist;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public final class TaskServiceTest {
    @Test
    void it_adds_a_project() {
        TaskRepository repository = new TaskRepository();
        TaskService service = new TaskService(repository);

        service.addProject("training");

        assertThat(repository.findAll().containsKey("training"), is(true));
        assertThat(repository.findAll().get("training"), is(empty()));
    }

    @Test
    void it_adds_a_task_to_a_project() {
        TaskRepository repository = new TaskRepository();
        TaskService service = new TaskService(repository);
        service.addProject("training");

        Task task = service.addTask("training", "SOLID");

        assertThat(task.getDescription(), is("SOLID"));
        assertThat(repository.findAll().get("training"), contains(task));
    }

    @Test
    void it_returns_null_when_adding_a_task_to_an_unknown_project() {
        TaskRepository repository = new TaskRepository();
        TaskService service = new TaskService(repository);

        Task task = service.addTask("missing", "SOLID");

        assertThat(task, is(nullValue()));
    }

    @Test
    void it_gets_tasks_due_today() {
        TaskRepository repository = new TaskRepository();
        TaskService service = new TaskService(repository);
        repository.addProject("secrets");
        Task taskDueToday = repository.addTask("secrets", "Eat more donuts.");
        Task taskDueTomorrow = repository.addTask("secrets", "Destroy all humans.");
        taskDueToday.setDeadline(LocalDate.now());
        taskDueTomorrow.setDeadline(LocalDate.now().plusDays(1));

        Map<String, List<Task>> tasksDueToday = service.getTasksDueToday();

        assertThat(tasksDueToday.keySet(), contains("secrets"));
        assertThat(tasksDueToday.get("secrets"), contains(taskDueToday));
    }

    @Test
    void it_skips_projects_without_tasks_due_today() {
        TaskRepository repository = new TaskRepository();
        TaskService service = new TaskService(repository);
        repository.addProject("secrets");
        Task taskDueTomorrow = repository.addTask("secrets", "Eat more donuts.");
        taskDueTomorrow.setDeadline(LocalDate.now().plusDays(1));
        repository.addProject("training");
        Task taskDueToday = repository.addTask("training", "SOLID");
        taskDueToday.setDeadline(LocalDate.now());

        Map<String, List<Task>> tasksDueToday = service.getTasksDueToday();

        assertThat(tasksDueToday.containsKey("secrets"), is(false));
        assertThat(tasksDueToday.get("training"), contains(taskDueToday));
    }

    @Test
    void it_gets_tasks_grouped_by_deadline() {
        TaskRepository repository = new TaskRepository();
        TaskService service = new TaskService(repository);
        repository.addProject("secrets");
        Task firstTask = repository.addTask("secrets", "Eat more donuts.");
        Task secondTask = repository.addTask("secrets", "Destroy all humans.");
        repository.addProject("training");
        Task thirdTask = repository.addTask("training", "Four Elements of Simple Design");
        firstTask.setDeadline(LocalDate.of(2024, 11, 11));
        secondTask.setDeadline(LocalDate.of(2024, 11, 13));
        thirdTask.setDeadline(LocalDate.of(2024, 11, 11));

        DeadlineView deadlineView = service.getTasksGroupedByDeadline();

        assertThat(deadlineView.tasksWithDeadline().keySet(),
                contains(LocalDate.of(2024, 11, 11), LocalDate.of(2024, 11, 13)));
        assertThat(deadlineView.tasksWithDeadline().get(LocalDate.of(2024, 11, 11)).get("secrets"),
                contains(firstTask));
        assertThat(deadlineView.tasksWithDeadline().get(LocalDate.of(2024, 11, 11)).get("training"),
                contains(thirdTask));
        assertThat(deadlineView.tasksWithDeadline().get(LocalDate.of(2024, 11, 13)).get("secrets"),
                contains(secondTask));
    }

    @Test
    void it_gets_tasks_without_a_deadline() {
        TaskRepository repository = new TaskRepository();
        TaskService service = new TaskService(repository);
        repository.addProject("training");
        Task taskWithoutDeadline = repository.addTask("training", "Refactor the codebase");
        Task taskWithDeadline = repository.addTask("training", "Interaction-Driven Design");
        taskWithDeadline.setDeadline(LocalDate.of(2024, 11, 13));

        DeadlineView deadlineView = service.getTasksGroupedByDeadline();

        assertThat(deadlineView.tasksWithoutDeadline().get("training"), contains(taskWithoutDeadline));
    }
}
