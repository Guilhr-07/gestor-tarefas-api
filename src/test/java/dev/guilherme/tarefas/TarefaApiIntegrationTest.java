package dev.guilherme.tarefas;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class TarefaApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void criaTarefaERetorna201ComLocation() throws Exception {
        String body = """
                { "titulo": "Escrever testes", "prioridade": "ALTA" }
                """;

        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status", is("PENDENTE")))
                .andExpect(jsonPath("$.prioridade", is("ALTA")));
    }

    @Test
    void rejeitaTituloCurtoCom400EListaDeErros() throws Exception {
        String body = """
                { "titulo": "ab" }
                """;

        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Erro de validação")))
                .andExpect(jsonPath("$.erros.titulo").exists());
    }

    @Test
    void retorna404AoBuscarIdInexistente() throws Exception {
        mockMvc.perform(get("/api/tarefas/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Recurso não encontrado")));
    }

    @Test
    void concluirMudaStatusParaConcluida() throws Exception {
        MvcResult criada = mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"titulo\": \"Tarefa a concluir\" }"))
                .andExpect(status().isCreated())
                .andReturn();

        URI location = URI.create(criada.getResponse().getHeader("Location"));

        mockMvc.perform(patch(location.getPath() + "/concluir"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONCLUIDA")));
    }
}
