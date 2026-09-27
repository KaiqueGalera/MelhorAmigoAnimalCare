package br.ifsp.demo.model.prescricao;

import br.ifsp.demo.model.abertura.Atendimento;
import br.ifsp.demo.model.abertura.AnimalId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PrescricaoTest {

    /*
        História do Usuário
        Como veterinário(a),
        Quero emitir uma prescrição médica contendo medicamentos, dosagem, via, frequência e duração,
        Para que eu possa registrar o tratamento indicado ao paciente durante o atendimento.

        US10 - Criação de emissão

        Dado que o atendimento está com status "Em andamento",
        Quando o(a) veterinário(a) informar um ou mais itens de prescrição (medicamentos, dosagem, via, frequência e duração),
        Então a prescrição deve ser criada com status "Aberta" e vinculada ao atendimento.
     */

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
}