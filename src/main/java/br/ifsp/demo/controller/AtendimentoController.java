package br.ifsp.demo.controller;

import br.ifsp.demo.model.abertura.AgendamentoId;
import br.ifsp.demo.model.abertura.AnimalId;
import br.ifsp.demo.model.abertura.Atendimento;
import br.ifsp.demo.model.abertura.AtendimentoId;
import br.ifsp.demo.service.abertura.AtendimentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
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

    public record AtendimentoResponse(
            UUID id,
            UUID animalId,
            UUID agendamentoId,
            String status,
            LocalDateTime dataHoraAtendimento,
            String justificativa) {

        static AtendimentoResponse de(Atendimento a) {
            return new AtendimentoResponse(
                    a.getId().getValue(),
                    a.getAnimalId().getValue(),
                    a.getAgendamentoId() == null ? null : a.getAgendamentoId().getValue(),
                    a.getStatus().name(),
                    a.getDataHoraAtendimento(),
                    a.getJustificativa());
        }
    }
}