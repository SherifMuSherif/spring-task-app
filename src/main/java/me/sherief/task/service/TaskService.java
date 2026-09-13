package me.sherief.task.service;

import me.sherief.task.domain.CreateTaskRequest;
import me.sherief.task.domain.entity.Task;

import java.util.List;

public interface TaskService {

    Task createTask(CreateTaskRequest request);

    List<Task> listTasks();

}
