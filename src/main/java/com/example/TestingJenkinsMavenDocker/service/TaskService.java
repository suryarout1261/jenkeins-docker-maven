package com.example.TestingJenkinsMavenDocker.service;

import com.example.TestingJenkinsMavenDocker.model.Task;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

	private final AtomicLong idGenerator = new AtomicLong(0);
	private final List<Task> tasks = new CopyOnWriteArrayList<>();

	public Task createTask(String title) {
		if (title == null || title.isBlank()) {
			throw new IllegalArgumentException("title must not be blank");
		}

		Task task = new Task(idGenerator.incrementAndGet(), title, false, Instant.now());
		tasks.add(task);
		return task;
	}

	public List<Task> listTasks() {
		return List.copyOf(new ArrayList<>(tasks));
	}
}

