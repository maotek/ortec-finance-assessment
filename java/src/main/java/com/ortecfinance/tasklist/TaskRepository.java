package com.ortecfinance.tasklist;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

/**
 * In-memory storage for now. This can be replaced by a database-backed repository later.
 */
@Repository
public class TaskRepository {
    private final Map<String, List<Task>> tasks = new LinkedHashMap<>();
    private long lastId = 0;

    public Map<String, List<Task>> findAll() {
        return tasks;
    }

    public void addProject(String name) {
        tasks.put(name, new ArrayList<>());
    }

    public boolean projectExists(String name) {
        return tasks.containsKey(name);
    }

    public Task addTask(String project, String description) {
        List<Task> projectTasks = tasks.get(project);
        Task task = new Task(nextId(), description, false);
        projectTasks.add(task);
        return task;
    }

    public Task findTaskById(long id) {
        for (List<Task> projectTasks : tasks.values()) {
            for (Task task : projectTasks) {
                if (task.getId() == id) {
                    return task;
                }
            }
        }
        return null;
    }

    private long nextId() {
        return ++lastId;
    }
}
