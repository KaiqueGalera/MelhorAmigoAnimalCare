package br.ifsp.demo.controller;

import br.ifsp.demo.controller.dto.PrescricaoDtos.*;
import br.ifsp.demo.model.abertura.AtendimentoId;
import br.ifsp.demo.model.prescricao.ItemPrescricaoId;
import br.ifsp.demo.model.prescricao.Prescricao;
import br.ifsp.demo.model.prescricao.PrescricaoId;
import br.ifsp.demo.service.prescricao.PrescricaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/atendimentos/{atendimentoId}/prescricoes")
public class PrescricaoController {

    private final PrescricaoService service;

    public PrescricaoController(PrescricaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PrescricaoResponse> emitir(@PathVariable UUID atendimentoId, @RequestBody EmitirRequest req) {
        Prescricao p = service.emitirPrescricao(AtendimentoId.of(atendimentoId), req.toDomain());
        return ResponseEntity.status(HttpStatus.CREATED).body(PrescricaoResponse.from(p));
    }

    @GetMapping
    public List<PrescricaoResponse> listar(@PathVariable UUID atendimentoId) {
        return service.consultarPrescricoes(AtendimentoId.of(atendimentoId)).stream()
                .map(PrescricaoResponse::from).toList();
    }

    @PutMapping("/{prescricaoId}/itens/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editarItem(@PathVariable UUID atendimentoId, @PathVariable UUID prescricaoId,
                           @PathVariable UUID itemId, @RequestBody ItemRequest req) {
        ItemPrescricaoId id = ItemPrescricaoId.of(itemId);
        service.editarItem(AtendimentoId.of(atendimentoId), PrescricaoId.of(prescricaoId), id, req.toItemExistente(id));
    }

    @DeleteMapping("/{prescricaoId}/itens/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerItem(@PathVariable UUID atendimentoId, @PathVariable UUID prescricaoId,
                            @PathVariable UUID itemId) {
        service.removerItem(AtendimentoId.of(atendimentoId), PrescricaoId.of(prescricaoId), ItemPrescricaoId.of(itemId));
    }

    @PatchMapping("/{prescricaoId}/finalizar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void finalizar(@PathVariable UUID atendimentoId, @PathVariable UUID prescricaoId) {
        service.finalizar(AtendimentoId.of(atendimentoId), PrescricaoId.of(prescricaoId));
    }

    @PatchMapping("/{prescricaoId}/cancelar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable UUID atendimentoId, @PathVariable UUID prescricaoId) {
        service.cancelar(AtendimentoId.of(atendimentoId), PrescricaoId.of(prescricaoId));
    }
}
