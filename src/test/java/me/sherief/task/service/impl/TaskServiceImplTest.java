package me.sherief.task.service.impl;

import me.sherief.task.domain.CreateTaskRequest;
import me.sherief.task.domain.entity.Task;
import me.sherief.task.domain.entity.TaskPriority;
import me.sherief.task.domain.entity.TaskStatus;
import me.sherief.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Test
    void createTask_withValidTask_persistsAndCanBeFoundById() {
        // Arrange
        String title = "New Task";

        CreateTaskRequest request = new CreateTaskRequest(
                title,
                "New Description",
                null,
                TaskPriority.HIGH
        );

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task taskToSave = invocation.getArgument(0);
            taskToSave.setId(UUID.randomUUID());
            return taskToSave;
        });

        // Act
        Task result = taskService.createTask(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(title);
        verify(taskRepository,times(1)).save(any(Task.class));

    }

    @Test
    void createTask_savesAndReturnsTaskWithCorrectDefaults() {
        // Arrange
        String title = "New Task";

        CreateTaskRequest request = new CreateTaskRequest(
                title,
                "New Description",
                null,
                TaskPriority.HIGH
        );

        UUID generatedId = UUID.randomUUID();
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task taskToSave = invocation.getArgument(0);
            return new Task(
                    generatedId,
                    taskToSave.getTitle(),
                    taskToSave.getDescription(),
                    taskToSave.getDueDate(),
                    taskToSave.getStatus(),
                    taskToSave.getPriority(),
                    taskToSave.getCreated(),
                    taskToSave.getUpdated()
            );
        });

        // Act
        Task result = taskService.createTask(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(generatedId);
        assertThat(result.getTitle()).isEqualTo(title);

        assertThat(result.getStatus()).isEqualTo(TaskStatus.OPEN);
        assertThat(result.getCreated()).isBeforeOrEqualTo(Instant.now());
        assertThat(result.getUpdated()).isEqualTo(result.getCreated());

        verify(taskRepository, times(1)).save(any(Task.class));
    }
}