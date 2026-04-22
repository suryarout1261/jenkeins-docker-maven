package com.example.TestingJenkinsMavenDocker.controller;

import com.example.TestingJenkinsMavenDocker.model.Task;
import com.example.TestingJenkinsMavenDocker.service.TaskService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<Task> listTasks() {
        return taskService.listTasks();
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody TaskCreateRequest request) {
        Task createdTask = taskService.createTask(request.title().trim());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleInvalidTask(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    public record TaskCreateRequest(
            @NotBlank(message = "title is required")
            @Size(max = 120, message = "title must be at most 120 characters")
            String title
    ) {
    }
}

