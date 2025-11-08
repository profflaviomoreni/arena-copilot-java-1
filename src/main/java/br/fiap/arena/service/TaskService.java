package br.fiap.arena.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.fiap.arena.domain.Task;
import br.fiap.arena.domain.TaskStatus;
import br.fiap.arena.repo.TaskRepository;

@Service
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) { this.repository = repository; }

    public Task create(Task t) { return repository.save(t); }

    public Page<Task> listAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Task> listByStatus(TaskStatus status, Pageable pageable) {
        return repository.findByStatus(status, pageable);
    }

    public boolean delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<Task> findById(Long id) {
        return repository.findById(id);
    }

    public Map<String, Object> stats() {
        List<Task> all = repository.findAll();
        LocalDate today = LocalDate.now();
        
        // Count overdue tasks using streams
        long overdue = all.stream()
            .filter(t -> t.getDueDate() != null && today.isAfter(t.getDueDate()))
            .count();
            
        // Priority histogram using groupingBy
        Map<Integer, Long> hist = all.stream()
            .collect(Collectors.groupingBy(
                task -> Optional.ofNullable(task.getPriority()).orElse(0),
                Collectors.counting()
            ));
            
        // Ensure all priority levels (0-5) are represented
        for (int i = 0; i <= 5; i++) {
            hist.putIfAbsent(i, 0L);
        }
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", all.size());
        result.put("overdueCount", overdue);
        result.put("priorityHistogram", hist);
        return result;
    }
}
