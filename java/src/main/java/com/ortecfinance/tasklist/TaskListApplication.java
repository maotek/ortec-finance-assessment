package com.ortecfinance.tasklist;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Map;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TaskListApplication {
    private static final String WEB = "web";
    private static final String BOTH = "both";
    private static final String CONSOLE_ENABLED_PROPERTY = "task-list.console.enabled";

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Starting console Application");
            TaskList.startConsole();
            return;
        }

        SpringApplication application = new SpringApplication(TaskListApplication.class);
        application.setDefaultProperties(Map.of(CONSOLE_ENABLED_PROPERTY, String.valueOf(BOTH.equals(args[0]))));
        application.run(args);
        System.out.println("localhost:8080/projects");
    }

    @Bean
    @ConditionalOnProperty(name = CONSOLE_ENABLED_PROPERTY, havingValue = "true")
    ApplicationRunner startConsole(TaskRepository taskRepository, TaskService taskService) {
        return args -> {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter out = new PrintWriter(System.out, true);

            TaskList taskList = new TaskList(in, out, taskRepository, taskService);
            Thread consoleThread = new Thread(taskList, "task-list-console");
            consoleThread.start();
        };
    }
}
