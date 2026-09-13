package me.sherief.task;

import me.sherief.task.domain.dto.CreateTaskRequestDto;
import me.sherief.task.domain.dto.TaskDto;
import me.sherief.task.domain.entity.Task;
import me.sherief.task.domain.entity.TaskPriority;
import me.sherief.task.domain.entity.TaskStatus;
import me.sherief.task.repository.TaskRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class TaskApplicationIT {

    @LocalServerPort
    private int port;

    @Autowired
    RestTestClient restClient;

    @Autowired
    TaskRepository taskRepository;

    @BeforeEach
    public void setup() {
        restClient = RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @AfterEach
    void cleanUp() {
        taskRepository.deleteAll();
    }

    @Test
    void givenValidCreateTaskRequestDto_whenCreateTaskRequestIsSent_thenTaskIsCreatedAndPersistedToDatabase() {
        CreateTaskRequestDto requestDto = new CreateTaskRequestDto(
                "IT Test Task",
                "Validating full stack",
                null,
                TaskPriority.HIGH
        );

        TaskDto responseBody = restClient.post()
                .uri("/api/v1/tasks")
                .body(requestDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TaskDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(responseBody).isNotNull();

        UUID savedTaskId = responseBody.id();
        Optional<Task> taskInDb = taskRepository.findById(savedTaskId);

        assertThat(taskInDb).isPresent();
        assertThat(taskInDb.get().getTitle()).isEqualTo(requestDto.title());
        assertThat(taskInDb.get().getDescription()).isEqualTo(requestDto.description());
        assertThat(taskInDb.get().getPriority()).isEqualTo(requestDto.priority());
        assertThat(taskInDb.get().getStatus()).isEqualTo(TaskStatus.OPEN);

    }

    @Test
    void givenNoTasksExist_whenGetTasksRequestIsSent_thenEmptyListIsReturned() {

        List<TaskDto> responseBody = restClient.get()
                .uri("/api/v1/tasks")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<TaskDto>>() {
                })
                .returnResult()
                .getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody).isEmpty();
    }

    @Test
    void givenMultipleTasksInDatabase_whenGetTasksRequestIsSent_thenTasksAreReturnedInCreatedAscendingOrder() {

        Instant createdAt1 = Instant.parse("2026-01-01T10:00:00Z");
        Instant createdAt2 = Instant.parse("2026-01-02T10:00:00Z");

        Task olderTask = new Task(
                null,
                "Task One",
                "Description",
                null,
                TaskStatus.OPEN,
                TaskPriority.LOW,
                createdAt1,
                createdAt1
        );
        Task newerTask = new Task(
                null,
                "Task Two",
                "Description",
                null,
                TaskStatus.OPEN,
                TaskPriority.HIGH,
                createdAt2,
                createdAt2
        );

        taskRepository.saveAll(List.of(newerTask, olderTask));

        List<TaskDto> responseBody = restClient.get()
                .uri("/api/v1/tasks")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<TaskDto>>() {
                })
                .returnResult()
                .getResponseBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody).hasSize(2);
        assertThat(responseBody)
                .extracting(TaskDto::title, TaskDto::priority)
                .containsExactly(
                        tuple("Task One", TaskPriority.LOW),
                        tuple("Task Two", TaskPriority.HIGH)
                );

    }

}
