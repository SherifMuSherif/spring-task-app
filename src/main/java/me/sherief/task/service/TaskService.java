package me.sherief.task.service;

import me.sherief.task.domain.CreateTaskRequest;
import me.sherief.task.domain.UpdateTaskRequest;
import me.sherief.task.domain.entity.Task;

import java.util.List;
import java.util.UUID;

public interface TaskService {

    Task createTask(CreateTaskRequest request);

    List<Task> listTasks();

    Task updateTask(UUID id, UpdateTaskRequest request);

}
