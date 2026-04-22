package com.example.TestingJenkinsMavenDocker.service;

import com.example.TestingJenkinsMavenDocker.model.Task;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TaskServiceTest {

    private final TaskService taskService = new TaskService();

    @Test
    void createTaskAddsTaskWithIncrementingId() {
        Task first = taskService.createTask("First task");
        Task second = taskService.createTask("Second task");

        assertEquals(1L, first.id());
        assertEquals(2L, second.id());
        assertFalse(first.completed());
    }

    @Test
    void listTasksReturnsCreatedTasks() {
        taskService.createTask("Pipeline check task");

        List<Task> tasks = taskService.listTasks();

        assertEquals(1, tasks.size());
        assertEquals("Pipeline check task", tasks.getFirst().title());
    }
}

