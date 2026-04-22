package com.example.TestingJenkinsMavenDocker.controller;

import com.example.TestingJenkinsMavenDocker.model.Task;
import com.example.TestingJenkinsMavenDocker.service.TaskService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskControllerTest {

    private final TaskService taskService = new TaskService();
    private final TaskController taskController = new TaskController(taskService);

    @Test
    void createTaskReturnsCreatedTask() {
        ResponseEntity<Task> response = taskController.createTask(new TaskController.TaskCreateRequest("Check pipeline by API"));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Check pipeline by API", response.getBody().title());
    }

    @Test
    void listTasksReturnsCreatedTasks() {
        taskController.createTask(new TaskController.TaskCreateRequest("Task A"));
        taskController.createTask(new TaskController.TaskCreateRequest("Task B"));

        List<Task> tasks = taskController.listTasks();

        assertEquals(2, tasks.size());
        assertEquals("Task A", tasks.get(0).title());
        assertEquals("Task B", tasks.get(1).title());
    }

    @Test
    void createTaskRejectsBlankTitle() {
        assertThrows(IllegalArgumentException.class,
                () -> taskController.createTask(new TaskController.TaskCreateRequest("   ")));
    }
}

