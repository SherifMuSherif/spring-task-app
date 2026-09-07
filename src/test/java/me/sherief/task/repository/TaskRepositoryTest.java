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
    public void findByStatus_ReturnsOnlyMatchingTasks() {
        // Arrange
        Task openTask = createTaskWith(TaskStatus.OPEN, TaskPriority.MEDIUM);
        Task closedTask = createTaskWith(TaskStatus.COMPLETE, TaskPriority.MEDIUM);

        entityManager.persist(openTask);
        entityManager.persist(closedTask);
        entityManager.flush();

        // Act
        List<Task> result = taskRepository.findByStatus(TaskStatus.OPEN);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(TaskStatus.OPEN);
    }

    @Test
    public void findByPriority_ReturnsOnlyMatchingTasks() {
        // Arrange
        Task highTask = createTaskWith(TaskStatus.OPEN, TaskPriority.HIGH);
        Task lowTask = createTaskWith(TaskStatus.OPEN, TaskPriority.LOW);

        entityManager.persist(highTask);
        entityManager.persist(lowTask);
        entityManager.flush();

        // Act
        List<Task> result = taskRepository.findByPriority(TaskPriority.HIGH);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPriority()).isEqualTo(TaskPriority.HIGH);
    }

    @Test
    public void findByDueDate_ReturnOnlyMatchingTasks() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        Task todayTask = createTaskWith(today, TaskStatus.OPEN, TaskPriority.HIGH);
        Task tomorrowTask = createTaskWith(tomorrow, TaskStatus.OPEN, TaskPriority.HIGH);

        entityManager.persist(todayTask);
        entityManager.persist(tomorrowTask);
        entityManager.flush();

        // Act
        List<Task> result = taskRepository.findByDueDate(today);

        // Assert
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
