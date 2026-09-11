package me.sherief.task.repository;

import me.sherief.task.domain.entity.Task;
import me.sherief.task.domain.entity.TaskPriority;
import me.sherief.task.domain.entity.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void givenTasksWithDifferentStatuses_whenFindByStatus_thenReturnsOnlyMatchingTasks() {
        // Given
        Task openTask = createTaskWith(TaskStatus.OPEN, TaskPriority.MEDIUM);
        Task closedTask = createTaskWith(TaskStatus.COMPLETE, TaskPriority.MEDIUM);

        entityManager.persist(openTask);
        entityManager.persist(closedTask);
        entityManager.flush();

        // When
        List<Task> result = taskRepository.findByStatus(TaskStatus.OPEN);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(TaskStatus.OPEN);
    }

    @Test
    void givenTasksWithDifferentPriorities_whenFindByPriority_thenReturnsOnlyMatchingTasks() {
        // Given
        Task highTask = createTaskWith(TaskStatus.OPEN, TaskPriority.HIGH);
        Task lowTask = createTaskWith(TaskStatus.OPEN, TaskPriority.LOW);

        entityManager.persist(highTask);
        entityManager.persist(lowTask);
        entityManager.flush();

        // When
        List<Task> result = taskRepository.findByPriority(TaskPriority.HIGH);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPriority()).isEqualTo(TaskPriority.HIGH);
    }

    @Test
    void givenTasksWithDifferentDueDates_whenFindByDueDate_thenReturnsOnlyMatchingTasks() {
        // Given
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        Task todayTask = createTaskWith(today, TaskStatus.OPEN, TaskPriority.HIGH);
        Task tomorrowTask = createTaskWith(tomorrow, TaskStatus.OPEN, TaskPriority.HIGH);

        entityManager.persist(todayTask);
        entityManager.persist(tomorrowTask);
        entityManager.flush();

        // When
        List<Task> result = taskRepository.findByDueDate(today);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDueDate()).isToday();

    }

    private Task createTaskWith(TaskStatus status, TaskPriority priority) {
        return new Task(null, "Task", "Desc", null, status, priority, Instant.now(), Instant.now());
    }

    private Task createTaskWith(LocalDate dueDate, TaskStatus status, TaskPriority priority) {
        return new Task(null, "Task", "Desc", dueDate, status, priority, Instant.now(), Instant.now());
    }
}
