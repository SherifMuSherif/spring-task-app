package me.sherief.task.service.impl;

import me.sherief.task.domain.CreateTaskRequest;
import me.sherief.task.domain.entity.Task;
import me.sherief.task.domain.entity.TaskPriority;
import me.sherief.task.domain.entity.TaskStatus;
import me.sherief.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Test
    void givenValidCreateTaskRequest_whenCreateTask_thenTaskIsCreatedWithCorrectValuesAndDefaults() {
        // Given
        CreateTaskRequest request = new CreateTaskRequest(
                "New Task",
                "New Description",
                null,
                TaskPriority.HIGH
        );

        UUID generatedId = UUID.randomUUID();

        Task mockSavedTask = new Task();
        mockSavedTask.setId(generatedId);

        given(taskRepository.save(any(Task.class))).willReturn(mockSavedTask);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);

        // When
        Instant before = Instant.now();
        Task result = taskService.createTask(request);
        Instant after = Instant.now();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(generatedId);

        then(taskRepository).should(times(1)).save(taskCaptor.capture());

        Task capturedTask = taskCaptor.getValue();

        assertThat(capturedTask.getTitle()).isEqualTo(request.title());
        assertThat(capturedTask.getDescription()).isEqualTo(request.description());
        assertThat(capturedTask.getPriority()).isEqualTo(request.priority());

        assertThat(capturedTask.getStatus()).isEqualTo(TaskStatus.OPEN);
        assertThat(capturedTask.getCreated()).isBetween(before, after);
        assertThat(capturedTask.getUpdated()).isEqualTo(capturedTask.getCreated());
    }


}