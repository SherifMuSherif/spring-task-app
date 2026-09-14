package me.sherief.task.mapper.impl;

import me.sherief.task.domain.CreateTaskRequest;
import me.sherief.task.domain.UpdateTaskRequest;
import me.sherief.task.domain.dto.CreateTaskRequestDto;
import me.sherief.task.domain.dto.TaskDto;
import me.sherief.task.domain.dto.UpdateTaskRequestDto;
import me.sherief.task.domain.entity.Task;
import me.sherief.task.mapper.TaskMapper;
import org.springframework.stereotype.Component;

@Component
public class TaskMapperImpl implements TaskMapper {
    @Override
    public CreateTaskRequest fromDto(CreateTaskRequestDto dto) {
        return new CreateTaskRequest(
                dto.title(),
                dto.description(),
                dto.dueDate(),
                dto.priority()
        );
    }

    @Override
    public UpdateTaskRequest fromDto(UpdateTaskRequestDto dto) {
        return new UpdateTaskRequest(
                dto.title(),
                dto.description(),
                dto.dueDate(),
                dto.status(),
                dto.priority()
        );
    }

    @Override
    public TaskDto toDto(Task task) {
        return new TaskDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getDueDate(),
                task.getStatus(),
                task.getPriority()
        );
    }
}
