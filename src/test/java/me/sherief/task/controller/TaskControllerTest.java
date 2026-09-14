package me.sherief.task.controller;

import me.sherief.task.domain.CreateTaskRequest;
import me.sherief.task.domain.UpdateTaskRequest;
import me.sherief.task.domain.dto.CreateTaskRequestDto;
import me.sherief.task.domain.dto.UpdateTaskRequestDto;
import me.sherief.task.domain.entity.Task;
import me.sherief.task.domain.entity.TaskPriority;
import me.sherief.task.domain.entity.TaskStatus;
import me.sherief.task.exception.TaskNotFoundException;
import me.sherief.task.mapper.impl.TaskMapperImpl;
import me.sherief.task.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import(TaskMapperImpl.class)
class TaskControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    TaskService taskService;

    @Test
    void givenValidCreateRequest_whenCreateTask_thenReturnsCreatedTask() throws Exception {
        UUID taskId = UUID.randomUUID();
        CreateTaskRequestDto requestDto = new CreateTaskRequestDto(
                "API Test Task",
                "Testing the endpoint",
                null,
                TaskPriority.HIGH
        );

        Task mockedSavedTask = new Task(
                taskId,
                "API Test Task",
                "Testing the endpoint",
                null,
                TaskStatus.OPEN,
                TaskPriority.HIGH,
                Instant.now(),
                Instant.now()
        );

        given(taskService.createTask(any(CreateTaskRequest.class))).willReturn(mockedSavedTask);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("API Test Task"))
                .andExpect(jsonPath("$.id").exists());

        then(taskService).should().createTask(any(CreateTaskRequest.class));
    }

    @Test
    void givenMissingTitleInCreateRequest_whenCreateTask_thenReturnsBadRequest() throws Exception {
        CreateTaskRequestDto badRequestDto = new CreateTaskRequestDto(
                "",
                "Missing Title",
                null,
                TaskPriority.HIGH
        );

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Title must be between 1 and 255 characters"));

        then(taskService).shouldHaveNoInteractions();
    }

    @Test
    void givenExceedingMaxLengthTitle_whenCreateTask_thenReturnsBadRequest() throws Exception {
        String excessivelyLongTitle = "A".repeat(256);
        CreateTaskRequestDto badRequest = new CreateTaskRequestDto(
                excessivelyLongTitle, "Description", null, TaskPriority.MEDIUM);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Title must be between 1 and 255 characters"));

    }

    @Test
    void givenExceedingMaxLengthDescription_whenCreateTask_thenReturnsBadRequest() throws Exception {
        String excessivelyLongDescription = "A".repeat(1001);
        CreateTaskRequestDto badRequest = new CreateTaskRequestDto(
                "Title", excessivelyLongDescription, null, TaskPriority.MEDIUM);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Description must be less than 1000 characters"));

    }

    @Test
    void givenMissingPriorityInCreateTask_whenCreateTask_thenReturnsBadRequest() throws Exception {
        CreateTaskRequestDto badRequestDto = new CreateTaskRequestDto(
                "Missing Priority",
                "Description",
                null,
                null
        );

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Task priority must be provided"));
    }

    @Test
    void givenPastDueDateInCreateTask_whenCreateTask_thenReturnsBadRequest() throws Exception {
        CreateTaskRequestDto badRequestDto = new CreateTaskRequestDto(
                "Invalid Due Date",
                "Description",
                LocalDate.now().minusDays(1),
                TaskPriority.LOW
        );

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Due date must be in the future"));

    }

    @Test
    public void givenMultipleTasksExist_whenListTasks_thenReturnsAllTasks() throws Exception {

        UUID taskId1 = UUID.randomUUID();
        UUID taskId2 = UUID.randomUUID();

        Task mockTask1 = new Task(
                taskId1,
                "Task One",
                "Description",
                null,
                TaskStatus.OPEN,
                TaskPriority.MEDIUM,
                Instant.now(),
                Instant.now()
        );
        Task mockTask2 = new Task(
                taskId2,
                "Task Two",
                "Description",
                null,
                TaskStatus.OPEN,
                TaskPriority.MEDIUM,
                Instant.now(),
                Instant.now()
        );

        given(taskService.listTasks()).willReturn(List.of(mockTask1, mockTask2));

        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(taskId1.toString()))
                .andExpect(jsonPath("$[0].title").value("Task One"))
                .andExpect(jsonPath("$[0].priority").value("MEDIUM"))
                .andExpect(jsonPath("$[1].id").value(taskId2.toString()))
                .andExpect(jsonPath("$[1].title").value("Task Two"))
                .andExpect(jsonPath("$[1].status").value("OPEN"));

        then(taskService).should().listTasks();
    }

    @Test
    void givenTaskIdDoesNotExist_whenUpdateTask_thenReturnsNotFound() throws Exception {
        UUID notExistID = UUID.randomUUID();

        UpdateTaskRequest request = new UpdateTaskRequest(
                "Task",
                "New Description",
                null,
                TaskStatus.COMPLETE,
                TaskPriority.HIGH
        );

        given(taskService.updateTask(
                eq(notExistID),
                any(UpdateTaskRequest.class)
        )).willThrow(new TaskNotFoundException(notExistID));

        mockMvc.perform(put("/api/v1/tasks/{taskId}", notExistID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").value(org.hamcrest.Matchers.startsWith("Task with ID ")))
                .andExpect(jsonPath("$.errorMessage", containsString(notExistID.toString())))
                .andExpect(jsonPath("$.errorMessage").value(org.hamcrest.Matchers.endsWith(" not found")));

        then(taskService).should(times(1)).updateTask(notExistID, request);
    }

    @Test
    void givenValidUpdateRequest_whenUpdateTask_thenReturnsUpdatedTask() throws Exception {
        UpdateTaskRequestDto requestDto = new UpdateTaskRequestDto(
                "API Test Task",
                "Testing the endpoint",
                null,
                TaskStatus.OPEN,
                TaskPriority.HIGH
        );

        UUID taskId = UUID.randomUUID();

        Task mockedUpdatedTask = new Task(
                taskId,
                "Task",
                "Description",
                null,
                TaskStatus.COMPLETE,
                TaskPriority.HIGH,
                Instant.now().minusSeconds(5000),
                Instant.now()
        );


        given(taskService.updateTask(eq(taskId), any(UpdateTaskRequest.class))).willReturn(mockedUpdatedTask);

        mockMvc.perform(put("/api/v1/tasks/{taskId}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("Task"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.status").value("COMPLETE"))
                .andExpect(jsonPath("$.priority").value("HIGH"));

        then(taskService).should().updateTask(eq(taskId), any(UpdateTaskRequest.class));
    }

    @Test
    void givenMissingTitleInUpdateRequest_whenUpdateTask_thenReturnsBadRequest() throws Exception {
        UUID taskId = UUID.randomUUID();

        UpdateTaskRequestDto badRequestDto = new UpdateTaskRequestDto(
                "",
                "Missing Title",
                null,
                TaskStatus.OPEN,
                TaskPriority.HIGH
        );

        mockMvc.perform(put("/api/v1/tasks/{taskId}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Title must be between 1 and 255 characters"));

        then(taskService).shouldHaveNoInteractions();
    }

    @Test
    void givenExceedingMaxLengthTitle_whenUpdateTask_thenReturnsBadRequest() throws Exception {
        UUID taskId = UUID.randomUUID();
        String excessivelyLongTitle = "A".repeat(256);
        UpdateTaskRequestDto badRequest = new UpdateTaskRequestDto(
                excessivelyLongTitle, "Description", null, TaskStatus.COMPLETE, TaskPriority.MEDIUM);

        mockMvc.perform(put("/api/v1/tasks/{taskId}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Title must be between 1 and 255 characters"));

    }

    @Test
    void givenExceedingMaxLengthDescription_whenUpdateTask_thenReturnsBadRequest() throws Exception {
        UUID taskId = UUID.randomUUID();

        String excessivelyLongDescription = "A".repeat(1001);
        UpdateTaskRequestDto badRequest = new UpdateTaskRequestDto(
                "Title", excessivelyLongDescription, null, TaskStatus.OPEN, TaskPriority.MEDIUM);

        mockMvc.perform(put("/api/v1/tasks/{taskId}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Description must be less than 1000 characters"));

    }

    @Test
    void givenMissingStatusInUpdateTask_whenUpdateTask_thenReturnsBadRequest() throws Exception {
        UUID taskId = UUID.randomUUID();

        UpdateTaskRequestDto badRequestDto = new UpdateTaskRequestDto(
                "Missing Status",
                "Description",
                null,
                null,
                TaskPriority.LOW
        );

        mockMvc.perform(put("/api/v1/tasks/{taskId}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Task status must be provided"));
    }

    @Test
    void givenMissingPriorityInUpdateTask_whenUpdateTask_thenReturnsBadRequest() throws Exception {
        UUID taskId = UUID.randomUUID();

        UpdateTaskRequestDto badRequestDto = new UpdateTaskRequestDto(
                "Missing Priority",
                "Description",
                null,
                TaskStatus.OPEN,
                null
        );

        mockMvc.perform(put("/api/v1/tasks/{taskId}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Task priority must be provided"));
    }

    @Test
    void givenPastDueDateInCreateTask_whenUpdateTask_thenReturnsBadRequest() throws Exception {
        UUID taskId = UUID.randomUUID();

        UpdateTaskRequestDto badRequestDto = new UpdateTaskRequestDto(
                "Invalid Due Date",
                "Description",
                LocalDate.now().minusDays(1),
                TaskStatus.OPEN,
                TaskPriority.LOW
        );

        mockMvc.perform(put("/api/v1/tasks/{taskId}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Due date must be in the future"));

    }

    @Test
    void givenExistingTask_whenDeleteTask_thenReturnsNoContent() throws Exception {
        UUID taskId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/tasks/{taskId}", taskId))
                .andExpect(status().isNoContent());

        then(taskService)
                .should()
                .deleteTask(taskId);
    }

}