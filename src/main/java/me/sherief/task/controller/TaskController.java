package me.sherief.task.controller;

import jakarta.validation.Valid;
import me.sherief.task.domain.CreateTaskRequest;
import me.sherief.task.domain.UpdateTaskRequest;
import me.sherief.task.domain.dto.CreateTaskRequestDto;
import me.sherief.task.domain.dto.TaskDto;
import me.sherief.task.domain.dto.UpdateTaskRequestDto;
import me.sherief.task.domain.entity.Task;
import me.sherief.task.mapper.TaskMapper;
import me.sherief.task.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "api/v1/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    public TaskController(TaskService taskService, TaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    @PostMapping
    public ResponseEntity<TaskDto> createTask(
            @Valid @RequestBody CreateTaskRequestDto createTaskRequestDto
    ) {
        CreateTaskRequest createTaskRequest = taskMapper.fromDto(createTaskRequestDto);
        Task task = taskService.createTask(createTaskRequest);
        TaskDto createdTaskDto = taskMapper.toDto(task);
        return new ResponseEntity<>(createdTaskDto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TaskDto>> listTasks() {
        List<Task> tasks = taskService.listTasks();
        List<TaskDto> taskDtos = tasks.stream().map(taskMapper::toDto).toList();

        return ResponseEntity.ok(taskDtos);
    }

    @PutMapping(path = "/{taskId}")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskRequestDto updateTaskRequestDto
    ) {
        UpdateTaskRequest updateTaskRequest = taskMapper.fromDto(updateTaskRequestDto);
        Task task = taskService.updateTask(taskId, updateTaskRequest);
        TaskDto updatedTaskDto = taskMapper.toDto(task);
        return new ResponseEntity<>(updatedTaskDto, HttpStatus.OK);
    }

}
