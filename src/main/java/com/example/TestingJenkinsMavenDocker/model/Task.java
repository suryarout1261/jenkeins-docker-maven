package com.example.TestingJenkinsMavenDocker.model;

import java.time.Instant;

public record Task(long id, String title, boolean completed, Instant createdAt) {
}

