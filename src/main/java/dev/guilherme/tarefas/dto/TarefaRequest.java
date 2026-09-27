package dev.guilherme.tarefas.dto;

import dev.guilherme.tarefas.domain.Prioridade;
import dev.guilherme.tarefas.domain.StatusTarefa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Payload de entrada para criar/atualizar uma tarefa.
 * {@code status} é ignorado na criação (toda tarefa nasce PENDENTE).
 */
public record TarefaRequest(
        @NotBlank(message = "titulo é obrigatório")
        @Size(min = 3, max = 120, message = "titulo deve ter entre 3 e 120 caracteres")
        String titulo,

        @Size(max = 500, message = "descricao deve ter no máximo 500 caracteres")
        String descricao,

        StatusTarefa status,

        Prioridade prioridade,

        LocalDate prazo
) {
}
