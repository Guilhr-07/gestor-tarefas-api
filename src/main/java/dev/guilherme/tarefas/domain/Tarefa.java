package dev.guilherme.tarefas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "tarefas")
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusTarefa status = StatusTarefa.PENDENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Prioridade prioridade = Prioridade.MEDIA;

    private LocalDate prazo;

    @Column(nullable = false, updatable = false)
    private Instant criadaEm;

    @Column(nullable = false)
    private Instant atualizadaEm;

    protected Tarefa() {
        // exigido pelo JPA
    }

    public Tarefa(String titulo, String descricao, Prioridade prioridade, LocalDate prazo) {
        this.titulo = titulo;
        this.descricao = descricao;
        if (prioridade != null) {
            this.prioridade = prioridade;
        }
        this.prazo = prazo;
    }

    @PrePersist
    void aoCriar() {
        Instant agora = Instant.now();
        this.criadaEm = agora;
        this.atualizadaEm = agora;
    }

    @PreUpdate
    void aoAtualizar() {
        this.atualizadaEm = Instant.now();
    }

    /** Regra de negócio: só conclui uma vez; concluir uma tarefa já concluída é no-op idempotente. */
    public void concluir() {
        this.status = StatusTarefa.CONCLUIDA;
    }

    public void atualizar(String titulo, String descricao, StatusTarefa status,
                          Prioridade prioridade, LocalDate prazo) {
        this.titulo = titulo;
        this.descricao = descricao;
        if (status != null) {
            this.status = status;
        }
        if (prioridade != null) {
            this.prioridade = prioridade;
        }
        this.prazo = prazo;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public StatusTarefa getStatus() {
        return status;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public LocalDate getPrazo() {
        return prazo;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }

    public Instant getAtualizadaEm() {
        return atualizadaEm;
    }
}
