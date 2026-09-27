package dev.guilherme.tarefas.exception;

/** Lançada quando uma tarefa referenciada por id não existe. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
