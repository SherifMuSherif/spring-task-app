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
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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

}
