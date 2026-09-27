package dev.guilherme.tarefas.repository;

import dev.guilherme.tarefas.domain.StatusTarefa;
import dev.guilherme.tarefas.domain.Tarefa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    Page<Tarefa> findByStatus(StatusTarefa status, Pageable pageable);
}
