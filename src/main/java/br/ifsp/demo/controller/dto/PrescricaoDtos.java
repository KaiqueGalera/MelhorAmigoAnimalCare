package br.ifsp.demo.controller.dto;

import br.ifsp.demo.model.prescricao.ItemPrescricao;
import br.ifsp.demo.model.prescricao.ItemPrescricaoId;
import br.ifsp.demo.model.prescricao.Prescricao;
import br.ifsp.demo.model.prescricao.StatusPrescricao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class PrescricaoDtos {
    private PrescricaoDtos() {}

    public record ItemRequest(UUID id, String medicamento, Integer dosagem, String via,
                              String frequencia, Integer diasDuracao) {
        public ItemPrescricao toNovoItem() {
            return new ItemPrescricao(medicamento, dosagem, via, frequencia, diasDuracao);
        }

        public ItemPrescricao toItemExistente(ItemPrescricaoId idDaUrl) {
            return new ItemPrescricao(idDaUrl, medicamento, dosagem, via, frequencia, diasDuracao);
        }
    }

    public record EmitirRequest(List<ItemRequest> itens) {
        public List<ItemPrescricao> toDomain() {
            if (itens == null) return List.of();
            return itens.stream().map(ItemRequest::toNovoItem).toList();
        }
    }

    public record ItemResponse(UUID id, String medicamento, Integer dosagem, String via,
                               String frequencia, Integer diasDuracao) {
        public static ItemResponse de(ItemPrescricao i) {
            return new ItemResponse(i.getId().getValue(), i.getMedicamento(), i.getDosagem(),
                    i.getVia(), i.getFrequencia(), i.getDiasDuracao());
        }
    }

    public record PrescricaoResponse(UUID id, StatusPrescricao status, LocalDateTime dataHoraEmissao,
                                     List<ItemResponse> itens) {
        public static PrescricaoResponse de(Prescricao p) {
            return new PrescricaoResponse(p.getId().getValue(), p.getStatus(), p.getDataHoraEmissao(),
                    p.getItens().stream().map(ItemResponse::de).toList());
        }
    }
}
