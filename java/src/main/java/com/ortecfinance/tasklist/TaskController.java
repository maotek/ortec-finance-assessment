package com.ortecfinance.tasklist;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/projects")
public class TaskController {
    private static final String DEADLINE_PATTERN = "dd-MM-yyyy";
    private static final DateTimeFormatter DEADLINE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

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

    @PutMapping("/{project}/tasks/{taskId}")
    public Task updateTaskDeadline(
            @PathVariable String project,
            @PathVariable long taskId,
            @RequestParam String deadline
    ) {
        Task task = taskService.setDeadline(taskId, parseDeadline(deadline));
        if (task == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
        }
        return task;
    }

    private LocalDate parseDeadline(String deadline) {
        try {
            return LocalDate.parse(deadline, DEADLINE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid deadline \"" + deadline + "\". Use format " + DEADLINE_PATTERN + "."
            );
        }
    }

    public record CreateProjectRequest(String name) {
    }

    public record CreateTaskRequest(String description) {
    }
}
