package me.sherief.task.domain;

import me.sherief.task.domain.entity.TaskPriority;
import me.sherief.task.domain.entity.TaskStatus;

import java.time.LocalDate;

public record UpdateTaskRequest(
        String title,
        String description,
        LocalDate dueDate,
        TaskStatus status,
        TaskPriority priority
) {
}
