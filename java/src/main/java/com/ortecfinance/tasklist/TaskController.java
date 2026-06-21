package com.ortecfinance.tasklist;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/projects")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createProject(@RequestBody CreateProjectRequest request) {
        taskService.addProject(request.name());
    }

    @GetMapping
    public Map<String, List<Task>> getProjects() {
        return taskService.getProjects();
    }

    @PostMapping("/{project}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public Task createTask(@PathVariable String project, @RequestBody CreateTaskRequest request) {
        Task task = taskService.addTask(project, request.description());
        if (task == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }
        return task;
    }

    public record CreateProjectRequest(String name) {
    }

    public record CreateTaskRequest(String description) {
    }
}
