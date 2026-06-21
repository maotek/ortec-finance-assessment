package com.ortecfinance.tasklist;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Map<String, List<Task>> getTasksDueToday() {
        LocalDate today = LocalDate.now();
        Map<String, List<Task>> tasksDueToday = new LinkedHashMap<>();
        for (Map.Entry<String, List<Task>> project : taskRepository.findAll().entrySet()) {
            List<Task> tasksForToday = project.getValue().stream()
                    .filter(task -> today.equals(task.getDeadline()))
                    .toList();
            if (!tasksForToday.isEmpty()) {
                tasksDueToday.put(project.getKey(), tasksForToday);
            }
        }

        return tasksDueToday;
    }
}
