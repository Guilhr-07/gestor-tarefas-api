package dev.guilherme.tarefas.dto;

import dev.guilherme.tarefas.domain.Prioridade;
import dev.guilherme.tarefas.domain.StatusTarefa;
import dev.guilherme.tarefas.domain.Tarefa;
import java.time.Instant;
import java.time.LocalDate;

/** Representação de saída de uma tarefa. */
public record TarefaResponse(
        Long id,
        String titulo,
        String descricao,
        StatusTarefa status,
        Prioridade prioridade,
        LocalDate prazo,
        Instant criadaEm,
        Instant atualizadaEm
) {
    public static TarefaResponse de(Tarefa tarefa) {
        return new TarefaResponse(
                tarefa.getId(),
                tarefa.getTitulo(),
                tarefa.getDescricao(),
                tarefa.getStatus(),
                tarefa.getPrioridade(),
                tarefa.getPrazo(),
                tarefa.getCriadaEm(),
                tarefa.getAtualizadaEm()
        );
    }
}
