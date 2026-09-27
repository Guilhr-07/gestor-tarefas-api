package dev.guilherme.tarefas.service;

import dev.guilherme.tarefas.domain.StatusTarefa;
import dev.guilherme.tarefas.domain.Tarefa;
import dev.guilherme.tarefas.dto.TarefaRequest;
import dev.guilherme.tarefas.dto.TarefaResponse;
import dev.guilherme.tarefas.exception.RecursoNaoEncontradoException;
import dev.guilherme.tarefas.repository.TarefaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TarefaService {

    private final TarefaRepository repository;

    public TarefaService(TarefaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TarefaResponse criar(TarefaRequest request) {
        Tarefa tarefa = new Tarefa(request.titulo(), request.descricao(),
                request.prioridade(), request.prazo());
        return TarefaResponse.de(repository.save(tarefa));
    }

    @Transactional(readOnly = true)
    public Page<TarefaResponse> listar(StatusTarefa status, Pageable pageable) {
        Page<Tarefa> pagina = (status == null)
                ? repository.findAll(pageable)
                : repository.findByStatus(status, pageable);
        return pagina.map(TarefaResponse::de);
    }

    @Transactional(readOnly = true)
    public TarefaResponse buscarPorId(Long id) {
        return TarefaResponse.de(buscarEntidade(id));
    }

    @Transactional
    public TarefaResponse atualizar(Long id, TarefaRequest request) {
        Tarefa tarefa = buscarEntidade(id);
        tarefa.atualizar(request.titulo(), request.descricao(), request.status(),
                request.prioridade(), request.prazo());
        // flush dispara o @PreUpdate agora; sem ele a resposta sairia com o atualizadaEm antigo
        return TarefaResponse.de(repository.saveAndFlush(tarefa));
    }

    @Transactional
    public TarefaResponse concluir(Long id) {
        Tarefa tarefa = buscarEntidade(id);
        tarefa.concluir();
        return TarefaResponse.de(repository.saveAndFlush(tarefa));
    }

    @Transactional
    public void remover(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Tarefa " + id + " não encontrada");
        }
        repository.deleteById(id);
    }

    private Tarefa buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tarefa " + id + " não encontrada"));
    }
}
