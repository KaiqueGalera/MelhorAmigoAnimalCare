package br.ifsp.demo.controller;

import br.ifsp.demo.controller.dto.PrescricaoDtos;
import br.ifsp.demo.model.abertura.AgendamentoId;
import br.ifsp.demo.model.abertura.AnimalId;
import br.ifsp.demo.model.abertura.Atendimento;
import br.ifsp.demo.model.abertura.AtendimentoId;
import br.ifsp.demo.model.clinico.Anamnese;
import br.ifsp.demo.model.clinico.Diagnostico;
import br.ifsp.demo.model.clinico.SinaisVitais;
import br.ifsp.demo.model.exame.Exame;
import br.ifsp.demo.model.exame.ExameId;
import br.ifsp.demo.model.exame.ExameSolicitado;
import br.ifsp.demo.service.abertura.AtendimentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/atendimentos")
public class AtendimentoController {

    private final AtendimentoService service;

    public AtendimentoController(AtendimentoService service) {
        this.service = service;
    }

    // RF02 — abrir atendimento avulso (pronto atendimento)
    @PostMapping("/pronto-atendimento")
    public ResponseEntity<AtendimentoResponse> abrirProntoAtendimento(
            @RequestBody AbrirProntoAtendimentoRequest request) {
        Atendimento atendimento = service.abrirProntoAtendimento(
                AnimalId.of(obrigatorio(request.animalId(), "animalId")));
        return criado(atendimento);
    }

    // RF01 — abrir atendimento a partir de um agendamento
    @PostMapping("/agendados")
    public ResponseEntity<AtendimentoResponse> abrirComAgendamento(
            @RequestBody AbrirComAgendamentoRequest request) {
        Atendimento atendimento = service.abrirAtendimentoComAgendamento(
                AnimalId.of(obrigatorio(request.animalId(), "animalId")),
                AgendamentoId.of(obrigatorio(request.agendamentoId(), "agendamentoId")));
        return criado(atendimento);
    }

    // RF03 — cancelar com justificativa
    @PostMapping("/{id}/cancelamento")
    public ResponseEntity<AtendimentoResponse> cancelar(
            @PathVariable UUID id, @RequestBody CancelarRequest request) {
        Atendimento atendimento = service.cancelarAtendimento(AtendimentoId.of(id), request.justificativa());
        return ResponseEntity.ok(AtendimentoResponse.de(atendimento));
    }

    // RF04 — concluir
    @PostMapping("/{id}/conclusao")
    public ResponseEntity<AtendimentoResponse> concluir(@PathVariable UUID id) {
        Atendimento atendimento = service.concluirAtendimento(AtendimentoId.of(id));
        return ResponseEntity.ok(AtendimentoResponse.de(atendimento));
    }

    // RF05 — reabrir dentro da janela de tempo
    @PostMapping("/{id}/reabertura")
    public ResponseEntity<AtendimentoResponse> reabrir(@PathVariable UUID id) {
        Atendimento atendimento = service.reabrirAtendimento(AtendimentoId.of(id), LocalDateTime.now());
        return ResponseEntity.ok(AtendimentoResponse.de(atendimento));
    }

    // Consulta por id
    @GetMapping("/{id}")
    public ResponseEntity<AtendimentoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(AtendimentoResponse.de(service.buscarAtendimento(AtendimentoId.of(id))));
    }

    // Registro clínico — sinais vitais
    @PutMapping("/{id}/sinais-vitais")
    public ResponseEntity<AtendimentoResponse> registrarSinaisVitais(
            @PathVariable UUID id, @RequestBody SinaisVitais sinaisVitais) {
        Atendimento atendimento = service.registrarSinaisVitais(AtendimentoId.of(id), sinaisVitais);
        return ResponseEntity.ok(AtendimentoResponse.de(atendimento));
    }

    // Registro clínico — anamnese
    @PutMapping("/{id}/anamnese")
    public ResponseEntity<AtendimentoResponse> registrarAnamnese(
            @PathVariable UUID id, @RequestBody Anamnese anamnese) {
        Atendimento atendimento = service.registrarAnamnese(AtendimentoId.of(id), anamnese);
        return ResponseEntity.ok(AtendimentoResponse.de(atendimento));
    }

    // Registro clínico — diagnóstico
    @PostMapping("/{id}/diagnosticos")
    public ResponseEntity<AtendimentoResponse> registrarDiagnostico(
            @PathVariable UUID id, @RequestBody Diagnostico diagnostico) {
        Atendimento atendimento = service.registrarDiagnostico(AtendimentoId.of(id), diagnostico);
        return ResponseEntity.ok(AtendimentoResponse.de(atendimento));
    }

    // Exames — solicitação
    @PostMapping("/{id}/exames")
    public ResponseEntity<AtendimentoResponse> solicitarExames(
            @PathVariable UUID id, @RequestBody SolicitarExamesRequest request) {
        Atendimento atendimento = service.solicitarExames(AtendimentoId.of(id), request.paraDominio());
        return ResponseEntity.ok(AtendimentoResponse.de(atendimento));
    }

    // Exames — registro de resultado
    @PatchMapping("/{id}/exames/{exameId}/resultado")
    public ResponseEntity<ExameSolicitadoResponse> registrarResultadoExame(
            @PathVariable UUID id, @PathVariable UUID exameId, @RequestBody ResultadoExameRequest request) {
        ExameSolicitado exame = service.registrarResultadoExame(
                AtendimentoId.of(id), ExameId.of(exameId), request.resultado());
        return ResponseEntity.ok(ExameSolicitadoResponse.de(exame));
    }

    private ResponseEntity<AtendimentoResponse> criado(Atendimento atendimento) {
        URI location = URI.create("/api/v1/atendimentos/" + atendimento.getId().getValue());
        return ResponseEntity.created(location).body(AtendimentoResponse.de(atendimento));
    }

    private static UUID obrigatorio(UUID valor, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException("Campo obrigatório ausente: " + campo);
        }
        return valor;
    }

    // ---------------- DTOs ----------------

    public record AbrirProntoAtendimentoRequest(UUID animalId) {}

    public record AbrirComAgendamentoRequest(UUID animalId, UUID agendamentoId) {}

    public record CancelarRequest(String justificativa) {}

    public record ExameRequest(UUID exameId, String nome, LocalDateTime data) {
        ExameSolicitado paraDominio() {
            if (nome == null || nome.isBlank()) {
                throw new IllegalArgumentException("Campo obrigatório ausente: nome");
            }
            if (data == null) {
                throw new IllegalArgumentException("Campo obrigatório ausente: data");
            }
            return new ExameSolicitado(new Exame(ExameId.of(obrigatorio(exameId, "exameId")), nome), data);
        }
    }

    public record SolicitarExamesRequest(List<ExameRequest> exames) {
        List<ExameSolicitado> paraDominio() {
            return exames == null ? List.of() : exames.stream().map(ExameRequest::paraDominio).toList();
        }
    }

    public record ResultadoExameRequest(String resultado) {}

    public record ExameSolicitadoResponse(
            UUID exameId,
            String nome,
            LocalDateTime data,
            String resultado,
            String status) {

        static ExameSolicitadoResponse de(ExameSolicitado e) {
            return new ExameSolicitadoResponse(
                    e.getExame().getId().getValue(),
                    e.getExame().getNome(),
                    e.getData(),
                    e.getResultado(),
                    e.getStatus().name());
        }
    }

    public record AtendimentoResponse(
            UUID id,
            UUID animalId,
            UUID agendamentoId,
            String status,
            LocalDateTime dataHoraAtendimento,
            String justificativa,
            LocalDateTime dataHoraConclusao,
            SinaisVitais sinaisVitais,
            Anamnese anamnese,
            List<Diagnostico> diagnosticos,
            List<PrescricaoDtos.PrescricaoResponse> prescricoes,
            List<ExameSolicitadoResponse> exames) {

        static AtendimentoResponse de(Atendimento a) {
            return new AtendimentoResponse(
                    a.getId().getValue(),
                    a.getAnimalId().getValue(),
                    a.getAgendamentoId() == null ? null : a.getAgendamentoId().getValue(),
                    a.getStatus().name(),
                    a.getDataHoraAtendimento(),
                    a.getJustificativa(),
                    a.getDataHoraConclusao(),
                    a.getSinaisVitais(),
                    a.getAnamnese(),
                    List.copyOf(a.getDiagnosticos()),
                    a.getPrescricoes().stream().map(PrescricaoDtos.PrescricaoResponse::de).toList(),
                    a.getExames().stream().map(ExameSolicitadoResponse::de).toList());
        }
    }
}
