package br.ifsp.demo.repository;

import br.ifsp.demo.exception.AtendimentoNaoEncontradoException;
import br.ifsp.demo.model.abertura.*;
import br.ifsp.demo.model.clinico.Anamnese;
import br.ifsp.demo.model.clinico.Diagnostico;
import br.ifsp.demo.model.clinico.SinaisVitais;
import br.ifsp.demo.model.clinico.TipoDiagnostico;
import br.ifsp.demo.model.exame.Exame;
import br.ifsp.demo.model.exame.ExameId;
import br.ifsp.demo.model.exame.ExameSolicitado;
import br.ifsp.demo.model.exame.ExameStatus;
import br.ifsp.demo.model.prescricao.ItemPrescricao;
import br.ifsp.demo.model.prescricao.Prescricao;
import br.ifsp.demo.model.prescricao.PrescricaoId;
import br.ifsp.demo.model.prescricao.StatusPrescricao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(SqliteAtendimentoRepository.class)
@Tag("PersistenceTest")
@Tag("IntegrationTest")
class SqliteAtendimentoRepositoryPersistenceTest {

    @Autowired
    private SqliteAtendimentoRepository repository;
    @Autowired
    private JdbcTemplate jdbc;
    private static final String INSERT_ATENDIMENTO = "INSERT INTO atendimento (id, animal_id, agendamento_id, status, data_hora_atendimento) VALUES (?,?,?,?,?)";


    private static AnimalId novoAnimal() {
        return AnimalId.of(UUID.randomUUID());
    }

    private static Atendimento atendimento(AnimalId animal, StatusAtendimento status,
                                           LocalDateTime dataHora, LocalDateTime conclusao,
                                           AgendamentoId agendamento) {
        return Atendimento.reconstituir(AtendimentoId.novo(), animal, agendamento, status,
                dataHora, conclusao, null, null, null,
                List.of(), List.of(), List.of());
    }

    private static Atendimento emAndamento(AnimalId animal) {
        return atendimento(animal, StatusAtendimento.EM_ANDAMENTO, LocalDateTime.now(), null, null);
    }

    private int contar(String tabela) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + tabela, Integer.class);
    }

    @Test
    @DisplayName("should return empty list when animal has no atendimentos")
    void shouldReturnEmptyListWhenAnimalHasNoAtendimentos() {
        assertThat(repository.buscarPorAnimalId(novoAnimal())).isEmpty();
    }

    @Test
    @DisplayName("should Persist minimal atendimento with null fields")
    void shouldPersistMinimalAtendimentoWithNullFields() {
        Atendimento original = Atendimento.abrirProntoAtendimento(novoAnimal());
        repository.salvar(original);

        Atendimento lido = repository.buscarUmPorAtendimentoId(original.getId());

        assertThat(lido.getId()).isEqualTo(original.getId());
        assertThat(lido.getStatus()).isEqualTo(StatusAtendimento.EM_ANDAMENTO);
        assertThat(lido.getAgendamentoId()).isNull();
        assertThat(lido.getSinaisVitais()).isNull();
        assertThat(lido.getAnamnese()).isNull();
        assertThat(lido.getDiagnosticos()).isEmpty();
        assertThat(lido.getPrescricoes()).isEmpty();
        assertThat(lido.getExames()).isEmpty();
    }

    @Test
    @DisplayName("should throw exception when atendimento id does not exist")
    void shouldThrowExceptionWhenAtendimentoIdDoesNotExist() {
        assertThatThrownBy(() -> repository.buscarUmPorAtendimentoId(AtendimentoId.novo()))
                .isInstanceOf(AtendimentoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("should return only atendimentos of the given animal")
    void shouldReturnOnlyAtendimentosOfGivenAnimal() {
        AnimalId a1 = novoAnimal();
        AnimalId a2 = novoAnimal();
        repository.salvar(emAndamento(a1));
        repository.salvar(atendimento(a1, StatusAtendimento.CONCLUIDO,
                LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1), null));
        repository.salvar(emAndamento(a2));

        assertThat(repository.buscarPorAnimalId(a1))
                .hasSize(2)
                .allMatch(x -> x.getAnimalId().equals(a1));
    }

    @Test
    @DisplayName("should violate uniqueness when saving two in-progress atendimentos for the same animal")
    void shouldViolateUniquenessWhenSavingTwoInProgressAtendimentosForSameAnimal() {
        AnimalId animal = novoAnimal();
        repository.salvar(emAndamento(animal));

        assertThatThrownBy(() -> repository.salvar(emAndamento(animal)))
                .isInstanceOf(UncategorizedSQLException.class)
                .hasMessageContaining("UNIQUE constraint failed");
    }

    @Test
    @DisplayName("should violate uniqueness when saving two atendimentos for the same agendamento")
    void shouldViolateUniquenessWhenSavingTwoAtendimentosForSameAgendamento() {
        AgendamentoId agendamento = AgendamentoId.of(UUID.randomUUID());
        repository.salvar(atendimento(novoAnimal(), StatusAtendimento.EM_ANDAMENTO,
                LocalDateTime.now(), null, agendamento));

        assertThatThrownBy(() -> repository.salvar(atendimento(novoAnimal(), StatusAtendimento.EM_ANDAMENTO,
                LocalDateTime.now(), null, agendamento)))
                .isInstanceOf(UncategorizedSQLException.class)
                .hasMessageContaining("UNIQUE constraint failed");
    }

    @Test
    @DisplayName("should allow multiple walk-in atendimentos without agendamento")
    void shouldAllowMultipleWalkInAtendimentosWithoutAgendamento() {
        repository.salvar(Atendimento.abrirProntoAtendimento(novoAnimal()));
        repository.salvar(Atendimento.abrirProntoAtendimento(novoAnimal()));
        repository.salvar(Atendimento.abrirProntoAtendimento(novoAnimal()));

        assertThat(contar("atendimento")).isEqualTo(3);
    }

    @Test
    @DisplayName("should allow a new in-progress atendimento when the previous one for the same animal is completed")
    void shouldAllowNewInProgressAtendimentoWhenPreviousOneForSameAnimalIsCompleted() {
        AnimalId animal = novoAnimal();
        repository.salvar(atendimento(animal, StatusAtendimento.CONCLUIDO,
                LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1), null));
        repository.salvar(emAndamento(animal));

        assertThat(repository.buscarPorAnimalId(animal)).hasSize(2);
    }

    @Test
    @DisplayName("should return true when animal has an in-progress atendimento")
    void shouldReturnTrueWhenAnimalHasInProgressAtendimento() {
        AnimalId animal = novoAnimal();
        repository.salvar(emAndamento(animal));

        assertThat(repository.existeEmAndamentoParaAnimal(animal)).isTrue();
    }

    @Test
    @DisplayName("should return false when animal has only completed or cancelled atendimentos")
    void shouldReturnFalseWhenAnimalHasOnlyCompletedOrCancelledAtendimentos() {
        AnimalId animal = novoAnimal();
        repository.salvar(atendimento(animal, StatusAtendimento.CONCLUIDO,
                LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1), null));
        repository.salvar(atendimento(animal, StatusAtendimento.CANCELADO,
                LocalDateTime.now().minusHours(4), LocalDateTime.now().minusHours(3), null));

        assertThat(repository.existeEmAndamentoParaAnimal(animal)).isFalse();
    }

    @Test
    @DisplayName("should return false when animal has no atendimentos")
    void shouldReturnFalseWhenAnimalHasNoAtendimentos() {
        assertThat(repository.existeEmAndamentoParaAnimal(novoAnimal())).isFalse();
    }

    @Test
    @DisplayName("should return false when the in-progress atendimento belongs to another animal")
    void shouldReturnFalseWhenInProgressAtendimentoBelongsToAnotherAnimal() {
        repository.salvar(emAndamento(novoAnimal()));
        assertThat(repository.existeEmAndamentoParaAnimal(novoAnimal())).isFalse();
    }

    @Test
    @DisplayName("should return true when an atendimento exists for the agendamento")
    void shouldReturnTrueWhenAtendimentoExistsForAgendamento() {
        AgendamentoId ag = AgendamentoId.of(UUID.randomUUID());
        repository.salvar(atendimento(novoAnimal(), StatusAtendimento.EM_ANDAMENTO,
                LocalDateTime.now(), null, ag));

        assertThat(repository.existeAtendimentoParaAgendamento(ag)).isTrue();
    }

    @Test
    @DisplayName("should return false when no atendimento exists for the agendamento")
    void shouldReturnFalseWhenNoAtendimentoExistsForAgendamento() {
        assertThat(repository.existeAtendimentoParaAgendamento(AgendamentoId.of(UUID.randomUUID()))).isFalse();
    }

    @Test
    @DisplayName("should not match any agendamento when the atendimento is a walk-in")
    void shouldNotMatchAnyAgendamentoWhenAtendimentoIsWalkIn() {
        repository.salvar(Atendimento.abrirProntoAtendimento(novoAnimal()));
        assertThat(repository.existeAtendimentoParaAgendamento(AgendamentoId.of(UUID.randomUUID()))).isFalse();
    }

    @Test
    @DisplayName("should not throw exception when agendamento is null")
    void shouldNotThrowExceptionWhenAgendamentoIsNull() {
        assertThatCode(() -> repository.existeAtendimentoParaAgendamento(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should retrieve all fields when saving with diagnosticos")
    void shouldRetrieveAllFieldsWhenSavingWithDiagnosticos() {
        Atendimento original = Atendimento.reconstituir(AtendimentoId.novo(), novoAnimal(), null,
                StatusAtendimento.EM_ANDAMENTO, LocalDateTime.now(), null, null, null, null,
                List.of(new Diagnostico("A01", "Gastrite", TipoDiagnostico.PRESUNTIVO),
                        new Diagnostico("B02", "Infeccao", TipoDiagnostico.DEFINITIVO)),
                List.of(), List.of());
        repository.salvar(original);

        Atendimento lido = repository.buscarUmPorAtendimentoId(original.getId());

        assertThat(lido.getDiagnosticos()).containsExactly(
                new Diagnostico("A01", "Gastrite", TipoDiagnostico.PRESUNTIVO),
                new Diagnostico("B02", "Infeccao", TipoDiagnostico.DEFINITIVO));
    }

    @Test
    @DisplayName("should preserve insertion order of diagnosticos")
    void shouldPreserveInsertionOrderOfDiagnosticos() {
        Atendimento original = Atendimento.reconstituir(AtendimentoId.novo(), novoAnimal(), null,
                StatusAtendimento.EM_ANDAMENTO, LocalDateTime.now(), null, null, null, null,
                List.of(new Diagnostico("Z", "ultimo-alfabeto", TipoDiagnostico.EMPIRICO),
                        new Diagnostico("A", "primeiro-alfabeto", TipoDiagnostico.EMPIRICO),
                        new Diagnostico("M", "meio", TipoDiagnostico.EMPIRICO)),
                List.of(), List.of());
        repository.salvar(original);

        Atendimento lido = repository.buscarUmPorAtendimentoId(original.getId());

        assertThat(lido.getDiagnosticos())
                .extracting(Diagnostico::codigo)
                .containsExactly("Z", "A", "M");
    }

    @Test
    @DisplayName("should remove old diagnosticos when resaving with fewer")
    void shouldRemoveOldDiagnosticosWhenResavingWithFewer() {
        AtendimentoId id = AtendimentoId.novo();
        AnimalId animal = novoAnimal();
        repository.salvar(Atendimento.reconstituir(id, animal, null, StatusAtendimento.EM_ANDAMENTO,
                LocalDateTime.now(), null, null, null, null,
                List.of(new Diagnostico("A", "a", TipoDiagnostico.EMPIRICO),
                        new Diagnostico("B", "b", TipoDiagnostico.EMPIRICO)),
                List.of(), List.of()));

        repository.salvar(Atendimento.reconstituir(id, animal, null, StatusAtendimento.EM_ANDAMENTO,
                LocalDateTime.now(), null, null, null, null,
                List.of(new Diagnostico("C", "c", TipoDiagnostico.DEFINITIVO)),
                List.of(), List.of()));

        assertThat(contar("diagnostico")).isEqualTo(1);
    }

    @Test
    @DisplayName("should reject diagnostico insert with the same composite key")
    void shouldRejectDiagnosticoInsertWithSameCompositeKey() {
        String atendimentoId = UUID.randomUUID().toString();
        String sql = "INSERT INTO diagnostico (atendimento_id, ordem, codigo, descricao, tipo) VALUES (?,?,?,?,?)";
        jdbc.update(sql, atendimentoId, 0, "A01", "Gastrite", "PRESUNTIVO");

        assertThatThrownBy(() -> jdbc.update(sql, atendimentoId, 0, "B02", "Infeccao", "DEFINITIVO"))
                .isInstanceOf(UncategorizedSQLException.class)
                .hasMessageContaining("UNIQUE constraint failed");
    }

    @Test
    @DisplayName("should order atendimentos by data hora descending")
    void shouldOrderAtendimentosByDataHoraDescending() {
        AnimalId animal = novoAnimal();
        Atendimento antigo = atendimento(animal, StatusAtendimento.CONCLUIDO,
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 1, 11, 0), null);
        Atendimento meio = atendimento(animal, StatusAtendimento.CONCLUIDO,
                LocalDateTime.of(2026, 1, 1, 10, 0, 30),
                LocalDateTime.of(2026, 1, 1, 11, 0), null);
        Atendimento recente = atendimento(animal, StatusAtendimento.CONCLUIDO,
                LocalDateTime.of(2026, 3, 5, 9, 0),
                LocalDateTime.of(2026, 3, 5, 10, 0), null);
        repository.salvar(meio);
        repository.salvar(antigo);
        repository.salvar(recente);

        assertThat(repository.buscarPorAnimalId(animal))
                .extracting(Atendimento::getId)
                .containsExactly(recente.getId(), meio.getId(), antigo.getId());
    }

    @Test
    @DisplayName("should retrieve status and items in order when saving with prescricao")
    void shouldRetrieveStatusAndItemsInOrderWhenSavingWithPrescricao() {
        Prescricao prescricao = Prescricao.reconstituir(PrescricaoId.novo(),
                List.of(new ItemPrescricao("Zeta", 10, "ORAL", "8/8h", 5),
                        new ItemPrescricao("Alfa", 20, "IV", "12/12h", 3)),
                StatusPrescricao.FINALIZADA, LocalDateTime.now());
        Atendimento original = Atendimento.reconstituir(AtendimentoId.novo(), novoAnimal(), null,
                StatusAtendimento.EM_ANDAMENTO, LocalDateTime.now(), null, null, null, null,
                List.of(), List.of(prescricao), List.of());
        repository.salvar(original);

        Prescricao lida = repository.buscarUmPorAtendimentoId(original.getId()).getPrescricoes().getFirst();

        assertThat(lida.getId()).isEqualTo(prescricao.getId());
        assertThat(lida.getStatus()).isEqualTo(StatusPrescricao.FINALIZADA);
        assertThat(lida.getItens()).extracting(ItemPrescricao::getMedicamento)
                .containsExactly("Zeta", "Alfa");
        assertThat(lida.getItens().getFirst().getDosagem()).isEqualTo(10);
    }

    @Test
    @DisplayName("should remove prescricoes and items when resaving without them")
    void shouldRemovePrescricoesAndItemsWhenResavingWithoutThem() {
        AtendimentoId id = AtendimentoId.novo();
        AnimalId animal = novoAnimal();
        Prescricao p = Prescricao.reconstituir(PrescricaoId.novo(),
                List.of(new ItemPrescricao("A", 1, "ORAL", "8/8h", 1)),
                StatusPrescricao.ABERTA, LocalDateTime.now());
        repository.salvar(Atendimento.reconstituir(id, animal, null, StatusAtendimento.EM_ANDAMENTO,
                LocalDateTime.now(), null, null, null, null, List.of(), List.of(p), List.of()));

        repository.salvar(Atendimento.reconstituir(id, animal, null, StatusAtendimento.EM_ANDAMENTO,
                LocalDateTime.now(), null, null, null, null, List.of(), List.of(), List.of()));

        assertThat(contar("prescricao")).isZero();
        assertThat(contar("item_prescricao")).isZero();
    }

    @Test
    @DisplayName("should retrieve status and result when saving with exames")
    void shouldRetrieveStatusAndResultWhenSavingWithExames() {
        ExameSolicitado pendente = new ExameSolicitado(new Exame(ExameId.of(UUID.randomUUID()), "Raio-X"), LocalDateTime.now());
        ExameSolicitado concluido = new ExameSolicitado(new Exame(ExameId.of(UUID.randomUUID()), "Ultrassom"), LocalDateTime.now());
        concluido.setResultado("Sem alteracoes");
        Atendimento original = Atendimento.reconstituir(AtendimentoId.novo(), novoAnimal(), null,
                StatusAtendimento.EM_ANDAMENTO, LocalDateTime.now(), null, null, null, null,
                List.of(), List.of(), List.of(pendente, concluido));
        repository.salvar(original);

        List<ExameSolicitado> exames = repository.buscarUmPorAtendimentoId(original.getId()).getExames();

        assertThat(exames.get(0).getStatus()).isEqualTo(ExameStatus.PENDENTE);
        assertThat(exames.get(1).getStatus()).isEqualTo(ExameStatus.CONCLUIDO);
        assertThat(exames.get(1).getResultado()).isEqualTo("Sem alteracoes");
    }

    @Test
    @DisplayName("should not keep conclusion date when a reopened atendimento is saved")
    void shouldNotKeepConclusionDateWhenReopenedAtendimentoIsSaved() {
        AnimalId animal = novoAnimal();
        LocalDateTime agora = LocalDateTime.now();
        Atendimento a = atendimento(animal, StatusAtendimento.CONCLUIDO, agora.minusHours(2), agora.minusHours(1), null);
        a.reabrir(agora);
        repository.salvar(a);

        Atendimento lido = repository.buscarUmPorAtendimentoId(a.getId());

        assertThat(lido.getStatus()).isEqualTo(StatusAtendimento.EM_ANDAMENTO);
        assertThat(lido.getDataHoraConclusao()).isNull();
    }

    @Test
    @DisplayName("should reject insert with duplicate primary key")
    void shouldRejectInsertWithDuplicatePrimaryKey() {
        String id = UUID.randomUUID().toString();
        jdbc.update(INSERT_ATENDIMENTO, id, UUID.randomUUID().toString(), null,
                "CONCLUIDO", LocalDateTime.now().toString());

        assertThatThrownBy(() -> jdbc.update(INSERT_ATENDIMENTO, id, UUID.randomUUID().toString(), null,
                "CONCLUIDO", LocalDateTime.now().toString()))
                .isInstanceOf(UncategorizedSQLException.class)
                .hasMessageContaining("UNIQUE constraint failed");
    }

    @Test
    @DisplayName("should reject insert with null animal id")
    void shouldRejectInsertWithNullAnimalId() {
        assertThatThrownBy(() -> jdbc.update(INSERT_ATENDIMENTO, UUID.randomUUID().toString(),
                null, null, "CONCLUIDO", LocalDateTime.now().toString()))
                .isInstanceOf(UncategorizedSQLException.class)
                .hasMessageContaining("NOT NULL constraint failed");
    }

    @Test
    @DisplayName("should reject insert with null status")
    void shouldRejectInsertWithNullStatus() {
        assertThatThrownBy(() -> jdbc.update(INSERT_ATENDIMENTO, UUID.randomUUID().toString(),
                UUID.randomUUID().toString(), null, null, LocalDateTime.now().toString()))
                .isInstanceOf(UncategorizedSQLException.class)
                .hasMessageContaining("NOT NULL constraint failed");
    }

    @Test
    @DisplayName("should accept invalid status on insert but fail on read")
    void shouldAcceptInvalidStatusOnInsertButFailOnRead() {
        String id = UUID.randomUUID().toString();
        jdbc.update(INSERT_ATENDIMENTO, id, UUID.randomUUID().toString(), null,
                "INVALIDO", LocalDateTime.now().toString());

        assertThatThrownBy(() -> repository.buscarUmPorAtendimentoId(AtendimentoId.of(UUID.fromString(id))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("should update sinais vitais and anamnese added after the first save")
    void shouldUpdateSinaisVitaisAndAnamneseAddedAfterFirstSave() {
        Atendimento a = emAndamento(novoAnimal());
        repository.salvar(a);

        a.registrarSinaisVitais(new SinaisVitais(39.0, 100, 8.0));
        a.registrarAnamnese(new Anamnese("Tosse", "Ha 3 dias"));
        repository.salvar(a);

        Atendimento lido = repository.buscarUmPorAtendimentoId(a.getId());
        assertThat(lido.getSinaisVitais()).isEqualTo(new SinaisVitais(39.0, 100, 8.0));
        assertThat(lido.getAnamnese()).isEqualTo(new Anamnese("Tosse", "Ha 3 dias"));
    }

    @Test
    @DisplayName("should not duplicate and should update status when saving the same id twice")
    void shouldNotDuplicateAndShouldUpdateStatusWhenSavingSameIdTwice() {
        Atendimento a = emAndamento(novoAnimal());
        repository.salvar(a);
        a.cancelar("Tutor desistiu");
        repository.salvar(a);

        assertThat(contar("atendimento")).isEqualTo(1);
        Atendimento lido = repository.buscarUmPorAtendimentoId(a.getId());
        assertThat(lido.getStatus()).isEqualTo(StatusAtendimento.CANCELADO);
        assertThat(lido.getJustificativa()).isEqualTo("Tutor desistiu");
    }

    @Test
    @DisplayName("should retrieve all item fields in order when saving with two prescricoes")
    void shouldRetrieveAllItemFieldsInOrderWhenSavingWithTwoPrescricoes() {
        Prescricao primeira = Prescricao.reconstituir(PrescricaoId.novo(),
                List.of(new ItemPrescricao("Dipirona", 10, "ORAL", "8/8h", 5)),
                StatusPrescricao.FINALIZADA, LocalDateTime.of(2026, 1, 1, 10, 0));
        Prescricao segunda = Prescricao.reconstituir(PrescricaoId.novo(),
                List.of(new ItemPrescricao("Amoxicilina", 20, "IV", "12/12h", 7)),
                StatusPrescricao.CANCELADA, LocalDateTime.of(2026, 1, 2, 11, 30));
        Atendimento original = Atendimento.reconstituir(AtendimentoId.novo(), novoAnimal(), null,
                StatusAtendimento.EM_ANDAMENTO, LocalDateTime.now(), null, null, null, null,
                List.of(), List.of(primeira, segunda), List.of());
        repository.salvar(original);

        List<Prescricao> lidas = repository.buscarUmPorAtendimentoId(original.getId()).getPrescricoes();

        assertThat(lidas).extracting(Prescricao::getId).containsExactly(primeira.getId(), segunda.getId());
        assertThat(lidas).extracting(Prescricao::getStatus)
                .containsExactly(StatusPrescricao.FINALIZADA, StatusPrescricao.CANCELADA);
        assertThat(lidas.get(1).getDataHoraEmissao()).isEqualTo(LocalDateTime.of(2026, 1, 2, 11, 30));

        ItemPrescricao item = lidas.get(1).getItens().getFirst();
        assertThat(item.getMedicamento()).isEqualTo("Amoxicilina");
        assertThat(item.getDosagem()).isEqualTo(20);
        assertThat(item.getVia()).isEqualTo("IV");
        assertThat(item.getFrequencia()).isEqualTo("12/12h");
        assertThat(item.getDiasDuracao()).isEqualTo(7);
    }
}

