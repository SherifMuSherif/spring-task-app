package me.sherief.task.mapper.impl;

import me.sherief.task.domain.CreateTaskRequest;
import me.sherief.task.domain.dto.CreateTaskRequestDto;
import me.sherief.task.domain.dto.TaskDto;
import me.sherief.task.domain.entity.Task;
import me.sherief.task.domain.entity.TaskPriority;
import me.sherief.task.domain.entity.TaskStatus;
import me.sherief.task.mapper.TaskMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TaskMapperImplTest {
    private final TaskMapper taskMapper = new TaskMapperImpl();

    @Test
    void givenValidCreateTaskRequestDto_whenFromDto_thenAllFieldsAreMappedToEntity() {
        CreateTaskRequestDto dto = new CreateTaskRequestDto(
                "Title",
                "Desc",
                LocalDate.now(),
                TaskPriority.HIGH
        );

        CreateTaskRequest entity = taskMapper.fromDto(dto);

        assertThat(entity.title()).isEqualTo(dto.title());
        assertThat(entity.description()).isEqualTo(dto.description());
        assertThat(entity.dueDate()).isEqualTo(dto.dueDate());
        assertThat(entity.priority()).isEqualTo(dto.priority());
    }

    @Test
    void givenValidTaskEntity_whenToDto_thenAllFieldsAreMappedToDto() {
        Task entity = new Task(
                UUID.randomUUID(),
                "Title",
                "Desc",
                LocalDate.now(),
                TaskStatus.OPEN,
                TaskPriority.HIGH,
                Instant.now(),
                Instant.now()
        );

        TaskDto dto = taskMapper.toDto(entity);

        assertThat(dto.id()).isEqualTo(entity.getId());
        assertThat(dto.title()).isEqualTo(entity.getTitle());
        assertThat(dto.description()).isEqualTo(entity.getDescription());
        assertThat(dto.dueDate()).isEqualTo(entity.getDueDate());
        assertThat(dto.status()).isEqualTo(entity.getStatus());
        assertThat(dto.priority()).isEqualTo(entity.getPriority());

    }
}