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
import br.ifsp.demo.model.prescricao.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class SqliteAtendimentoRepository implements AtendimentoRepository {

    private final JdbcTemplate jdbc;
    private final RowMapper<LinhaAtendimento> linhaMapper = this::lerLinha;

    public SqliteAtendimentoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void salvar(Atendimento atendimento) {
        String id = atendimento.getId().getValue().toString();
        SinaisVitais sv = atendimento.getSinaisVitais();
        Anamnese an = atendimento.getAnamnese();

        jdbc.update("""
                INSERT INTO atendimento (id, animal_id, agendamento_id, status, data_hora_atendimento,
                                                 data_hora_conclusao, justificativa,
                                                 temperatura_c, frequencia_cardiaca, peso_kg,
                                                 queixa_principal, historico_clinico)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        ON CONFLICT(id) DO UPDATE SET
                            status = excluded.status,
                            data_hora_conclusao = excluded.data_hora_conclusao,
                            justificativa = excluded.justificativa,
                            temperatura_c = excluded.temperatura_c,
                            frequencia_cardiaca = excluded.frequencia_cardiaca,
                            peso_kg = excluded.peso_kg,
                            queixa_principal = excluded.queixa_principal,
                            historico_clinico = excluded.historico_clinico
                """,
                id,
                atendimento.getAnimalId().getValue().toString(),
                atendimento.getAgendamentoId() == null ? null : atendimento.getAgendamentoId().getValue().toString(),
                atendimento.getStatus().name(),
                atendimento.getDataHoraAtendimento().toString(),
                atendimento.getDataHoraConclusao() == null ? null : atendimento.getDataHoraConclusao().toString(),
                atendimento.getJustificativa(),
                sv == null ? null : sv.temperaturaC(),
                sv == null ? null : sv.frequenciaCardiaca(),
                sv == null ? null : sv.pesoKg(),
                an == null ? null : an.queixaPrincipal(),
                an == null ? null : an.historicoClinico());

        jdbc.update("DELETE FROM diagnostico WHERE atendimento_id = ?", id);
        List<Diagnostico> diagnosticos = atendimento.getDiagnosticos();
        for (int i = 0; i < diagnosticos.size(); i++) {
            Diagnostico d = diagnosticos.get(i);
            jdbc.update("""
                    INSERT INTO diagnostico (atendimento_id, ordem, codigo, descricao, tipo)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                    id, i, d.codigo(), d.descricao(), d.tipo().name());
        }

        jdbc.update("""
                DELETE FROM item_prescricao
                WHERE prescricao_id IN (SELECT id FROM prescricao WHERE atendimento_id = ?)
                """, id);
        jdbc.update("DELETE FROM prescricao WHERE atendimento_id = ?", id);

        List<Prescricao> prescricoes = atendimento.getPrescricoes();
        for (int i = 0; i < prescricoes.size(); i++) {
            Prescricao p = prescricoes.get(i);
            String prescricaoId = p.getId().getValue().toString();
            jdbc.update("""
                    INSERT INTO prescricao (id, atendimento_id, ordem, status, data_hora_emissao)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                    prescricaoId, id, i, p.getStatus().name(), p.getDataHoraEmissao().toString());

            List<ItemPrescricao> itens = p.getItens();
            for (int j = 0; j < itens.size(); j++) {
                ItemPrescricao item = itens.get(j);
                jdbc.update("""
                        INSERT INTO item_prescricao (id, prescricao_id, ordem, medicamento, dosagem,
                                                     via, frequencia, dias_duracao)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                        item.getId().getValue().toString(), prescricaoId, j,
                        item.getMedicamento(), item.getDosagem(), item.getVia(),
                        item.getFrequencia(), item.getDiasDuracao());
            }
        }

        jdbc.update("DELETE FROM exame_solicitado WHERE atendimento_id = ?", id);
        List<ExameSolicitado> exames = atendimento.getExames();
        for (int i = 0; i < exames.size(); i++) {
            ExameSolicitado e = exames.get(i);
            jdbc.update("""
            INSERT INTO exame_solicitado (atendimento_id, ordem, exame_id, exame_nome,
                                          data_hora, resultado, status)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """,
                    id, i, e.getExame().getId().getValue().toString(), e.getExame().getNome(),
                    e.getData().toString(), e.getResultado(), e.getStatus().name());
        }

    }

    @Override
    public Atendimento buscarUmPorAtendimentoId(AtendimentoId id) {
        List<LinhaAtendimento> linhas = jdbc.query(
                "SELECT * FROM atendimento WHERE id = ?",
                linhaMapper,
                id.getValue().toString());
        if (linhas.isEmpty()) {
            throw new AtendimentoNaoEncontradoException(id);
        }
        return hidratar(linhas.getFirst());
    }

    @Override
    public List<Atendimento> buscarPorAnimalId(AnimalId animalId) {
        return jdbc.query(
                        "SELECT * FROM atendimento WHERE animal_id = ? ORDER BY data_hora_atendimento DESC",
                        linhaMapper,
                        animalId.getValue().toString())
                .stream()
                .map(this::hidratar)
                .toList();
    }

    @Override
    public boolean existeEmAndamentoParaAnimal(AnimalId animalId) {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM atendimento WHERE animal_id = ? AND status = ?",
                Integer.class,
                animalId.getValue().toString(),
                StatusAtendimento.EM_ANDAMENTO.name());
        return total != null && total > 0;
    }

    @Override
    public boolean existeAtendimentoParaAgendamento(AgendamentoId agendamentoId) {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM atendimento WHERE agendamento_id = ?",
                Integer.class,
                agendamentoId.getValue().toString());
        return total != null && total > 0;
    }

    private LinhaAtendimento lerLinha(ResultSet rs, int rowNum) throws SQLException {
        String agendamento = rs.getString("agendamento_id");
        String conclusao = rs.getString("data_hora_conclusao");

        SinaisVitais sinais = null;
        Anamnese anamnese = null;

        String queixa = rs.getString("queixa_principal");
        if (queixa != null) {
            anamnese = new Anamnese(queixa, rs.getString("historico_clinico"));
        }

        double temperatura = rs.getDouble("temperatura_c");
        if (!rs.wasNull()) {
            sinais = new SinaisVitais(
                    temperatura,
                    rs.getInt("frequencia_cardiaca"),
                    rs.getDouble("peso_kg"));
        }

        return new LinhaAtendimento(
                AtendimentoId.of(UUID.fromString(rs.getString("id"))),
                AnimalId.of(UUID.fromString(rs.getString("animal_id"))),
                agendamento == null ? null : AgendamentoId.of(UUID.fromString(agendamento)),
                StatusAtendimento.valueOf(rs.getString("status")),
                LocalDateTime.parse(rs.getString("data_hora_atendimento")),
                conclusao == null ? null : LocalDateTime.parse(conclusao),
                rs.getString("justificativa"),
                sinais,
                anamnese);
    }

    private Atendimento hidratar(LinhaAtendimento l) {
        List<Diagnostico> diagnosticos = jdbc.query(
                "SELECT codigo, descricao, tipo FROM diagnostico WHERE atendimento_id = ? ORDER BY ordem",
                (rs, n) -> new Diagnostico(
                        rs.getString("codigo"),
                        rs.getString("descricao"),
                        TipoDiagnostico.valueOf(rs.getString("tipo"))),
                l.id().getValue().toString());

        List<ExameSolicitado> exames = jdbc.query(
                "SELECT exame_id, exame_nome, data_hora, resultado, status FROM exame_solicitado " +
                        "WHERE atendimento_id = ? ORDER BY ordem",
                (rs, n) -> {
                    Exame exame = new Exame(ExameId.of(UUID.fromString(rs.getString("exame_id"))),
                            rs.getString("exame_nome"));
                    ExameSolicitado s = new ExameSolicitado(exame, LocalDateTime.parse(rs.getString("data_hora")));
                    if (ExameStatus.valueOf(rs.getString("status")) == ExameStatus.CONCLUIDO) {
                        s.setResultado(rs.getString("resultado"));
                    }
                    return s;
                },
                l.id().getValue().toString());

        return Atendimento.reconstituir(
                l.id(), l.animalId(), l.agendamentoId(), l.status(),
                l.dataHoraAtendimento(), l.dataHoraConclusao(), l.justificativa(),
                l.sinaisVitais(), l.anamnese(), diagnosticos,
                carregarPrescricoes(l.id().getValue().toString()), exames);
    }

    private List<Prescricao> carregarPrescricoes(String atendimentoId) {
        List<LinhaPrescricao> linhas = jdbc.query(
                "SELECT id, status, data_hora_emissao FROM prescricao WHERE atendimento_id = ? ORDER BY ordem",
                (rs, n) -> new LinhaPrescricao(
                        PrescricaoId.of(UUID.fromString(rs.getString("id"))),
                        StatusPrescricao.valueOf(rs.getString("status")),
                        LocalDateTime.parse(rs.getString("data_hora_emissao"))),
                atendimentoId);

        return linhas.stream()
                .map(l -> Prescricao.reconstituir(l.id(), carregarItens(l.id()), l.status(), l.dataHoraEmissao()))
                .toList();
    }

    private List<ItemPrescricao> carregarItens(PrescricaoId prescricaoId) {
        return jdbc.query(
                """
                SELECT id, medicamento, dosagem, via, frequencia, dias_duracao
                FROM item_prescricao WHERE prescricao_id = ? ORDER BY ordem
                """,
                (rs, n) -> new ItemPrescricao(
                        ItemPrescricaoId.of(UUID.fromString(rs.getString("id"))),
                        rs.getString("medicamento"),
                        rs.getInt("dosagem"),
                        rs.getString("via"),
                        rs.getString("frequencia"),
                        rs.getInt("dias_duracao")),
                prescricaoId.getValue().toString());
    }

    private record LinhaPrescricao(PrescricaoId id, StatusPrescricao status, LocalDateTime dataHoraEmissao) {
    }

    private record LinhaAtendimento(
            AtendimentoId id,
            AnimalId animalId,
            AgendamentoId agendamentoId,
            StatusAtendimento status,
            LocalDateTime dataHoraAtendimento,
            LocalDateTime dataHoraConclusao,
            String justificativa,
            SinaisVitais sinaisVitais,
            Anamnese anamnese) {
    }
}