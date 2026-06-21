package com.ortecfinance.tasklist;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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


    public DeadlineView getTasksGroupedByDeadline() {
        // TreeMap to keep keys sorted automatically
        Map<LocalDate, Map<String, List<Task>>> tasksByDeadline = new TreeMap<>();
        Map<String, List<Task>> tasksWithoutDeadline = new LinkedHashMap<>();

        for (Map.Entry<String, List<Task>> project : taskRepository.findAll().entrySet()) {
            for (Task task : project.getValue()) {
                if (task.getDeadline() == null) {
                    tasksWithoutDeadline
                            .computeIfAbsent(project.getKey(), projectName -> new ArrayList<>()).add(task);
                } else {
                    tasksByDeadline
                            .computeIfAbsent(task.getDeadline(), deadline -> new LinkedHashMap<>())
                            .computeIfAbsent(project.getKey(), projectName -> new ArrayList<>())
                            .add(task);
                }
            }
        }

        return new DeadlineView(tasksByDeadline, tasksWithoutDeadline);
    }
}
