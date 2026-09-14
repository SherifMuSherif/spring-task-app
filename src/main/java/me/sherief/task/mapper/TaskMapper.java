package me.sherief.task.mapper;

import me.sherief.task.domain.CreateTaskRequest;
import me.sherief.task.domain.UpdateTaskRequest;
import me.sherief.task.domain.dto.CreateTaskRequestDto;
import me.sherief.task.domain.dto.TaskDto;
import me.sherief.task.domain.dto.UpdateTaskRequestDto;
import me.sherief.task.domain.entity.Task;

public interface TaskMapper {
    CreateTaskRequest fromDto(CreateTaskRequestDto dto);

    UpdateTaskRequest fromDto(UpdateTaskRequestDto dto);

    TaskDto toDto(Task task);
}
