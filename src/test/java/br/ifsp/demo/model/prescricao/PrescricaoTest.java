package br.ifsp.demo.model.prescricao;

import br.ifsp.demo.model.abertura.Atendimento;
import br.ifsp.demo.model.abertura.AnimalId;
import br.ifsp.demo.model.abertura.AtendimentoId;
import br.ifsp.demo.model.abertura.StatusAtendimento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class PrescricaoTest {

    @Test
    @DisplayName("C1001 - deve criar nova prescricao com status ABERTA")
    void deveCriarNovaPrescricaoComStatusAberta(){
        UUID animalId = UUID.randomUUID();
        Atendimento atendimento = Atendimento.abrirProntoAtendimento(AnimalId.of(animalId));

        List<ItemPrescricao> itens = List.of(
                new ItemPrescricao("Dipirona", 1, "Oral", "12/12h", 7)
        );

        Prescricao prescricao = atendimento.emitirPrescricao(itens);

        assertEquals(StatusPrescricao.ABERTA, prescricao.getStatus());
        assertTrue(atendimento.getPrescricoes().contains(prescricao));
    }

    @Test
    @DisplayName("C1002 - deve rejeitar prescricao quando atendimento nao esta em andamento")
    void deveRejeitarPrescricaoQuandoAtendimentoNaoEstaEmAndamento(){
        UUID animalId = UUID.randomUUID();
        Atendimento atendimento = Atendimento.abrirProntoAtendimento(AnimalId.of(animalId));
        atendimento.cancelar("TESTE");

        List<ItemPrescricao> itens = List.of(
                new ItemPrescricao("Dipirona", 1, "Oral", "12/12h", 7)
        );

        assertThatThrownBy(() -> atendimento.emitirPrescricao(itens))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Emissão rejeitada: só é possível emitir uma prescrição durante um atendimento em andamento.");
    }

    @Test
    @DisplayName("C1003 - deve rejeitar emissao quando nao houver itens na prescricao")
    void deveRejeitarEmissaoQuandoNaoHouverItensNaPrescricao(){
        UUID animalId = UUID.randomUUID();
        Atendimento atendimento = Atendimento.abrirProntoAtendimento(AnimalId.of(animalId));

        List<ItemPrescricao> itens = List.of();

        assertThatThrownBy(() -> atendimento.emitirPrescricao(itens))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Emissão rejeitada: só é possível emitir uma prescrição com pelo menos um item.");
    }
}