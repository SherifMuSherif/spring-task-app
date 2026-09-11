package me.sherief.task.controller;

import me.sherief.task.domain.CreateTaskRequest;
import me.sherief.task.domain.dto.CreateTaskRequestDto;
import me.sherief.task.domain.entity.Task;
import me.sherief.task.domain.entity.TaskPriority;
import me.sherief.task.domain.entity.TaskStatus;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    }

    @Test
    void givenExceedingMaxLengthTitle_whenCreateTask_thenReturnsBadRequest() throws Exception{
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
    void givenExceedingMaxLengthDescription_whenCreateTask_thenReturnsBadRequest() throws Exception{
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
    void givenMissingPriorityInCreateTask_whenCreatTask_thenReturnsBadRequest() throws Exception {
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

}