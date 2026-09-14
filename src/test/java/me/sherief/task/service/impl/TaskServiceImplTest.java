package me.sherief.task.service.impl;

import me.sherief.task.domain.CreateTaskRequest;
import me.sherief.task.domain.UpdateTaskRequest;
import me.sherief.task.domain.entity.Task;
import me.sherief.task.domain.entity.TaskPriority;
import me.sherief.task.domain.entity.TaskStatus;
import me.sherief.task.exception.TaskNotFoundException;
import me.sherief.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

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

    @Test
    void givenMultipleTasksExist_whenListTasks_thenReturnsAllTasksWithCreatedAscendingSort() {
        // Given
        Task task1 = new Task(
                UUID.randomUUID(),
                "Task 1",
                null,
                null,
                TaskStatus.OPEN,
                TaskPriority.MEDIUM,
                Instant.now(),
                Instant.now()
        );
        Task task2 = new Task(
                UUID.randomUUID(),
                "Task 2",
                null,
                null,
                TaskStatus.COMPLETE,
                TaskPriority.HIGH,
                Instant.now(),
                Instant.now()
        );
        given(taskRepository.findAll(any(Sort.class))).willReturn(List.of(task1, task2));

        // When
        List<Task> result = taskService.listTasks();

        // Then
        assertThat(result).hasSize(2);

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        then(taskRepository).should().findAll(sortCaptor.capture());

        Sort capturedSort = sortCaptor.getValue();
        Sort.Order sortOrder = capturedSort.getOrderFor("created");

        assertThat(sortOrder).isNotNull();
        assertThat(sortOrder.getDirection()).isEqualTo(Sort.Direction.ASC);

    }

    @Test
    void givenTaskIdDoesNotExist_whenUpdateTask_thenThrowsTaskNotFoundException() {
        UUID notExistID = UUID.randomUUID();

        UpdateTaskRequest request = new UpdateTaskRequest(
                "Task",
                "New Description",
                null,
                TaskStatus.COMPLETE,
                TaskPriority.HIGH
        );

        given(taskRepository.findById(notExistID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTask(notExistID, request))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining(notExistID.toString());

        then(taskRepository).should(times(1)).findById(notExistID);
        then(taskRepository).shouldHaveNoMoreInteractions();

    }

    @Test
    void givenValidUpdateRequest_whenUpdateTask_thenStatusIsOkAndTaskIsUpdated() {

        UpdateTaskRequest request = new UpdateTaskRequest(
                "Updated Task",
                "New Description",
                null,
                TaskStatus.COMPLETE,
                TaskPriority.LOW
        );

        UUID id = UUID.randomUUID();
        Instant originalUpdated = Instant.now().minusSeconds(3000);

        Task existingTask = new Task(
                id,
                "Old Task",
                "Description",
                null,
                TaskStatus.OPEN,
                TaskPriority.HIGH,
                Instant.now().minusSeconds(5000),
                originalUpdated
        );

        given(taskRepository.findById(id)).willReturn(Optional.of(existingTask));
        given(taskRepository.save(any(Task.class))).willAnswer(invocation -> invocation.getArgument(0));

        Instant beforeUpdate = Instant.now();

        Task updatedTask = taskService.updateTask(id, request);

        Instant afterUpdate = Instant.now();

        assertThat(updatedTask.getUpdated()).isBetween(beforeUpdate, afterUpdate);
        assertThat(updatedTask.getTitle()).isEqualTo("Updated Task");
        assertThat(updatedTask.getDescription()).isEqualTo("New Description");
        assertThat(updatedTask.getPriority()).isEqualTo(TaskPriority.LOW);
        assertThat(updatedTask.getStatus()).isEqualTo(TaskStatus.COMPLETE);

        then(taskRepository).should(times(1)).findById(id);
        then(taskRepository).should(times(1)).save(existingTask);
    }

    @Test
    void givenTaskExists_whenDeleteTask_thenTaskIsDeletedSuccessfully(){
        UUID taskId = UUID.randomUUID();

        taskService.deleteTask(taskId);

        then(taskRepository).should(times(1)).deleteById(taskId);
    }

}