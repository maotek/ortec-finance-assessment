package com.ortecfinance.tasklist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Map;

public final class TaskList implements Runnable {
    private static final String QUIT = "quit";
    private static final String DEADLINE_PATTERN = "dd-MM-yyyy";
    private static final DateTimeFormatter DEADLINE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    private final TaskRepository taskRepository;
    private final TaskService taskService;
    private final BufferedReader in;
    private final PrintWriter out;

    public static void startConsole() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        TaskRepository taskRepository = new TaskRepository();
        TaskService taskService = new TaskService(taskRepository);
        new TaskList(in, out, taskRepository, taskService).run();
    }

    public TaskList(BufferedReader reader, PrintWriter writer) {
        TaskRepository taskRepository = new TaskRepository();
        TaskService taskService = new TaskService(taskRepository);
        this.in = reader;
        this.out = writer;
        this.taskRepository = taskRepository;
        this.taskService = taskService;
    }

    public TaskList(BufferedReader reader, PrintWriter writer, TaskRepository taskRepository, TaskService taskService) {
        this.in = reader;
        this.out = writer;
        this.taskRepository = taskRepository;
        this.taskService = taskService;
    }

    public void run() {
        out.println("Welcome to TaskList! Type 'help' for available commands.");
        while (true) {
            out.print("> ");
            out.flush();
            String command;
            try {
                command = in.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (command.equals(QUIT)) {
                break;
            }
            execute(command);
        }
    }

    private void execute(String commandLine) {
        String[] commandRest = commandLine.split(" ", 2);
        String command = commandRest[0];
        switch (command) {
            case "show":
                show();
                break;
            case "today":
                today();
                break;
            case "view-by-deadline":
                viewByDeadline();
                break;
            case "add":
                add(commandRest[1]);
                break;
            case "check":
                check(commandRest[1]);
                break;
            case "uncheck":
                uncheck(commandRest[1]);
                break;
            case "deadline":
                deadline(commandRest[1]);
                break;
            case "help":
                help();
                break;
            default:
                error(command);
                break;
        }
    }

    private void show() {
        for (Map.Entry<String, List<Task>> project : taskRepository.findAll().entrySet()) {
            out.println(project.getKey());
            for (Task task : project.getValue()) {
                printTask(task);
            }
            out.println();
        }
    }

    private void today() {
        for (Map.Entry<String, List<Task>> tasksDueToday : taskService.getTasksDueToday().entrySet()) {
            out.println(tasksDueToday.getKey());
            for (Task task : tasksDueToday.getValue()) {
                printTask(task);
            }
            out.println();
        }
    }

    private void printTask(Task task) {
        out.printf("    [%c] %d: %s%s%n",
                (task.isDone() ? 'x' : ' '),
                task.getId(),
                task.getDescription(),
                formatDeadline(task));
    }

    private void viewByDeadline() {
        DeadlineView deadlineView = taskService.getTasksGroupedByDeadline();

        for (Map.Entry<LocalDate, Map<String, List<Task>>> deadlineProjects : deadlineView.tasksWithDeadline().entrySet()) {
            out.println(deadlineProjects.getKey().format(DEADLINE_FORMAT) + ":");
            printTasksByProject(deadlineProjects.getValue());
        }

        if (!deadlineView.tasksWithoutDeadline().isEmpty()) {
            out.println("No deadline:");
            printTasksByProject(deadlineView.tasksWithoutDeadline());
        }
    }

    private void printTasksByProject(Map<String, List<Task>> tasksByProject) {
        for (Map.Entry<String, List<Task>> project : tasksByProject.entrySet()) {
            out.println("    " + project.getKey() + ":");
            for (Task task : project.getValue()) {
                out.printf("        %d: %s%n", task.getId(), task.getDescription());
            }
        }
    }

    private void add(String commandLine) {
        String[] subcommandRest = commandLine.split(" ", 2);
        String subcommand = subcommandRest[0];
        if (subcommand.equals("project")) {
            addProject(subcommandRest[1]);
        } else if (subcommand.equals("task")) {
            String[] projectTask = subcommandRest[1].split(" ", 2);
            addTask(projectTask[0], projectTask[1]);
        }
    }

    private void addProject(String name) {
        taskService.addProject(name);
    }

    private void addTask(String project, String description) {
        Task task = taskService.addTask(project, description);
        if (task == null) {
            out.printf("Could not find a project with the name \"%s\".", project);
            out.println();
        }
    }

    private void check(String idString) {
        setDone(idString, true);
    }

    private void uncheck(String idString) {
        setDone(idString, false);
    }

    private void deadline(String commandLine) {
        String[] idDeadline = commandLine.split(" ", 2);
        long id = Long.parseLong(idDeadline[0]);

        LocalDate deadline = parseDeadline(idDeadline[1]);
        if (deadline == null) {
            return;
        }

        Task task = taskService.setDeadline(id, deadline);
        if (task == null) {
            out.printf("Could not find a task with an ID of %d.", id);
            out.println();
        }
    }

    private void setDone(String idString, boolean done) {
        long id = Long.parseLong(idString);
        Task task = taskService.setDone(id, done);
        if (task == null) {
            out.printf("Could not find a task with an ID of %d.", id);
            out.println();
        }
    }

    private String formatDeadline(Task task) {
        if (task.getDeadline() == null) {
            return "";
        }
        return " (deadline: " + task.getDeadline().format(DEADLINE_FORMAT) + ")";
    }

    private LocalDate parseDeadline(String deadline) {
        try {
            return LocalDate.parse(deadline, DEADLINE_FORMAT);
        } catch (DateTimeParseException e) {
            out.printf("Invalid deadline \"%s\". Use format %s.", deadline, DEADLINE_PATTERN);
            out.println();
            return null;
        }
    }

    private void help() {
        out.println("Commands:");
        out.println("  show");
        out.println("  add project <project name>");
        out.println("  add task <project name> <task description>");
        out.println("  check <task ID>");
        out.println("  uncheck <task ID>");
        out.println("  deadline <task ID> <deadline>");
        out.println("  today");
        out.println("  view-by-deadline");
        out.println();
    }

    private void error(String command) {
        out.printf("I don't know what the command \"%s\" is.", command);
        out.println();
    }

}
