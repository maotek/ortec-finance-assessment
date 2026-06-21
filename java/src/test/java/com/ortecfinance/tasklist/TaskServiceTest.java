package com.ortecfinance.tasklist;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

public final class TaskServiceTest {
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
}
