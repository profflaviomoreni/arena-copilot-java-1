package br.fiap.arena.api;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.fiap.arena.api.dto.TaskRequest;
import br.fiap.arena.domain.Task;
import br.fiap.arena.domain.TaskStatus;
import br.fiap.arena.service.TaskService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<Task> create(@Valid @RequestBody TaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setPriority(request.getPriority());
        task.setStatus(request.getStatus());
        task.setDueDate(request.getDueDate());
        
        Task saved = service.create(task);
        return ResponseEntity
            .created(URI.create("/api/tasks/" + saved.getId()))
            .body(saved);
    }

    @GetMapping
    public ResponseEntity<Page<Task>> list(
            @RequestParam(name = "status", required = false) TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort) {
        
        List<Sort.Order> orders = Arrays.stream(sort)
            .map(s -> {
                String[] parts = s.split(",");
                return new Sort.Order(
                    parts.length > 1 && parts[1].equalsIgnoreCase("desc") ? 
                        Sort.Direction.DESC : Sort.Direction.ASC,
                    parts[0]
                );
            })
            .collect(Collectors.toList());
            
        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));
        Page<Task> result = status == null ? 
            service.listAll(pageable) : 
            service.listByStatus(status, pageable);
            
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        if (service.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(service.stats());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getById(@PathVariable("id") Long id) {
        return service.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
