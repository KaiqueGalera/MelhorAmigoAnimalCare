package br.ifsp.demo.controller.prescricao;

import br.ifsp.demo.controller.BaseApiIntegrationTest;
import br.ifsp.demo.model.abertura.AnimalId;
import br.ifsp.demo.model.abertura.Atendimento;
import br.ifsp.demo.model.prescricao.ItemPrescricao;
import br.ifsp.demo.model.prescricao.Prescricao;
import br.ifsp.demo.model.prescricao.StatusPrescricao;
import br.ifsp.demo.repository.AtendimentoRepository;
import br.ifsp.demo.security.user.User;
import br.ifsp.demo.shared.EntityBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@Tag("ApiTest")
@Tag("IntegrationTest")
@DisplayName("PrescricaoController API")
class PrescricaoControllerApiTest extends BaseApiIntegrationTest {

    private static final String PASSWORD = "password";

    private String token;

    @Autowired
    private AtendimentoRepository atendimentoRepository;


    @BeforeEach
    void authSetup() {
        User user = registerUser(PASSWORD);
        token = authenticate(user.getEmail(), PASSWORD);
    }

    private Atendimento givenAtendimentoEmAndamento() {
        Atendimento atendimento = Atendimento.abrirProntoAtendimento(AnimalId.of(UUID.randomUUID()));
        atendimentoRepository.salvar(atendimento);
        return atendimento;
    }

    @Nested
    @DisplayName("POST /{atendimentoId}/prescricoes")
    class Emitir {

        @Test
        @DisplayName("Should return 201 with an open prescription and the items sent")
        void shouldReturn201WithOpenPrescription() {
            UUID id = givenAtendimentoEmAndamento().getId().getValue();

            Map<String, Object> item = Map.of(
                    "medicamento", "Dipirona",
                    "dosagem", 10,
                    "via", "Oral",
                    "frequencia", "8/8h",
                    "diasDuracao", 5);

            given().contentType("application/json").port(port)
                    .header("Authorization", "Bearer " + token)
                    .body(Map.of("itens", List.of(item)))
                    .when().post("/api/v1/atendimentos/" + id + "/prescricoes")
                    .then().log().ifValidationFails(LogDetail.BODY)
                    .statusCode(201)
                    .contentType(ContentType.JSON)
                    .body("id", notNullValue())
                    .body("status", equalTo("ABERTA"))
                    .body("dataHoraEmissao", notNullValue())
                    .body("itens", hasSize(1))
                    .body("itens[0].id", notNullValue())
                    .body("itens[0].medicamento", equalTo("Dipirona"))
                    .body("itens[0].dosagem", equalTo(10))
                    .body("itens[0].via", equalTo("Oral"))
                    .body("itens[0].frequencia", equalTo("8/8h"))
                    .body("itens[0].diasDuracao", equalTo(5));
        }

        @Test
        @DisplayName("Should return 400 when item list is empty")
        void shouldReturn400WhenItemListIsEmpty() {
            UUID id = givenAtendimentoEmAndamento().getId().getValue();

            given().contentType("application/json").port(port)
                    .header("Authorization", "Bearer " + token)
                    .body(Map.of("itens", List.of()))
                    .when().post("/api/v1/atendimentos/" + id + "/prescricoes")
                    .then().log().ifValidationFails(LogDetail.BODY)
                    .statusCode(400)
                    .contentType(ContentType.JSON)
                    .body("message", containsString("pelo menos um item"));
        }
    }

    @Nested
    @DisplayName("GET /{atendimentoId}/prescricoes")
    class Listar {

        @Test
        @DisplayName("Should return 200 with the prescriptions and their items")
        void shouldReturn200WithPrescriptionsAndItems() {
            Atendimento atendimento = EntityBuilder.createCompleteAtendimento();
            ItemPrescricao item = EntityBuilder.createRandomItemPrescricao();
            Prescricao prescricao = atendimento.emitirPrescricao(List.of(item));
            atendimentoRepository.salvar(atendimento);
            UUID id = atendimento.getId().getValue();

            given().contentType("application/json").port(port)
                    .header("Authorization", "Bearer " + token)
                    .when().get("/api/v1/atendimentos/" + id + "/prescricoes")
                    .then().log().ifValidationFails(LogDetail.BODY)
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("$", hasSize(1))
                    .body("[0].id", equalTo(prescricao.getId().getValue().toString()))
                    .body("[0].status", equalTo("ABERTA"))
                    .body("[0].itens", hasSize(1))
                    .body("[0].itens[0].medicamento", equalTo(item.getMedicamento()));
        }

        @Test
        @DisplayName("Should return 200 with an empty list when atendimento has no prescriptions")
        void shouldReturn200WithEmptyList() {
            UUID id = givenAtendimentoEmAndamento().getId().getValue();

            given().contentType("application/json").port(port)
                    .header("Authorization", "Bearer " + token)
                    .when().get("/api/v1/atendimentos/" + id + "/prescricoes")
                    .then().log().ifValidationFails(LogDetail.BODY)
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("$", empty());
        }
    }

    @Nested
    @DisplayName("PUT /{atendimentoId}/prescricoes/{prescricaoId}/itens/{itemId}")
    class EditarItem {

        @Test
        @DisplayName("Should return 204 and update the item")
        void shouldReturn204AndUpdateItem() {
            Atendimento atendimento = EntityBuilder.createCompleteAtendimento();
            ItemPrescricao original = EntityBuilder.createRandomItemPrescricao();
            Prescricao prescricao = atendimento.emitirPrescricao(List.of(original));
            atendimentoRepository.salvar(atendimento);

            UUID atendimentoId = atendimento.getId().getValue();
            UUID prescricaoId = prescricao.getId().getValue();
            UUID itemId = original.getId().getValue();

            Map<String, Object> novoItem = Map.of(
                    "medicamento", "Dipirona",
                    "dosagem", 10,
                    "via", "Oral",
                    "frequencia", "8/8h",
                    "diasDuracao", 5);

            given().contentType("application/json").port(port)
                    .header("Authorization", "Bearer " + token)
                    .body(novoItem)
                    .when().put("/api/v1/atendimentos/" + atendimentoId
                            + "/prescricoes/" + prescricaoId + "/itens/" + itemId)
                    .then().log().ifValidationFails(LogDetail.BODY)
                    .statusCode(204)
                    .body(emptyOrNullString());

            ItemPrescricao editado = atendimentoRepository
                    .buscarUmPorAtendimentoId(atendimento.getId())
                    .getPrescricoes().get(0).getItens().get(0);

            assertThat(editado.getId().getValue()).isEqualTo(itemId);
            assertThat(editado.getMedicamento()).isEqualTo("Dipirona");
            assertThat(editado.getDosagem()).isEqualTo(10);
            assertThat(editado.getFrequencia()).isEqualTo("8/8h");
            assertThat(editado.getDiasDuracao()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should return 404 when prescription does not exist")
        void shouldReturn404WhenPrescriptionDoesNotExist() {
            UUID atendimentoId = givenAtendimentoEmAndamento().getId().getValue();

            Map<String, Object> novoItem = Map.of(
                    "medicamento", "Dipirona",
                    "dosagem", 10,
                    "via", "Oral",
                    "frequencia", "8/8h",
                    "diasDuracao", 5);

            given().contentType("application/json").port(port)
                    .header("Authorization", "Bearer " + token)
                    .body(novoItem)
                    .when().put("/api/v1/atendimentos/" + atendimentoId
                            + "/prescricoes/" + UUID.randomUUID() + "/itens/" + UUID.randomUUID())
                    .then().log().ifValidationFails(LogDetail.BODY)
                    .statusCode(404)
                    .contentType(ContentType.JSON)
                    .body("message", not(emptyOrNullString()));
        }
    }

    @Nested
    @DisplayName("DELETE /{atendimentoId}/prescricoes/{prescricaoId}/itens/{itemId}")
    class RemoverItem {

        @Test
        @DisplayName("Should return 204 and remove the item from the prescription")
        void shouldReturn204AndRemoveItem() {
            Atendimento atendimento = EntityBuilder.createCompleteAtendimento();
            ItemPrescricao toRemove = EntityBuilder.createRandomItemPrescricao();
            ItemPrescricao toKeep = EntityBuilder.createRandomItemPrescricao();
            Prescricao prescricao = atendimento.emitirPrescricao(List.of(toRemove, toKeep));
            atendimentoRepository.salvar(atendimento);

            UUID atendimentoId = atendimento.getId().getValue();
            UUID prescricaoId = prescricao.getId().getValue();

            given().contentType("application/json").port(port)
                    .header("Authorization", "Bearer " + token)
                    .when().delete("/api/v1/atendimentos/" + atendimentoId
                            + "/prescricoes/" + prescricaoId + "/itens/" + toRemove.getId().getValue())
                    .then().log().ifValidationFails(LogDetail.BODY)
                    .statusCode(204)
                    .body(emptyOrNullString());

            List<ItemPrescricao> itens = atendimentoRepository
                    .buscarUmPorAtendimentoId(atendimento.getId())
                    .getPrescricoes().get(0).getItens();

            assertThat(itens).hasSize(1);
            assertThat(itens.get(0).getId()).isEqualTo(toKeep.getId());
        }

        @Test
        @DisplayName("Should return 404 when prescription does not exist")
        void shouldReturn404WhenPrescriptionDoesNotExist() {
            UUID atendimentoId = givenAtendimentoEmAndamento().getId().getValue();

            given().contentType("application/json").port(port)
                    .header("Authorization", "Bearer " + token)
                    .when().delete("/api/v1/atendimentos/" + atendimentoId
                            + "/prescricoes/" + UUID.randomUUID() + "/itens/" + UUID.randomUUID())
                    .then().log().ifValidationFails(LogDetail.BODY)
                    .statusCode(404)
                    .contentType(ContentType.JSON)
                    .body("message", not(emptyOrNullString()));
        }
    }

    @Nested
    @DisplayName("PATCH /{atendimentoId}/prescricoes/{prescricaoId}/cancelar")
    class Cancelar {

        @Test
        @DisplayName("Should return 204 and set the open prescription to CANCELADA")
        void shouldReturn204AndCancelOpenPrescription() {
            Atendimento atendimento = EntityBuilder.createCompleteAtendimento();
            Prescricao prescricao = atendimento.emitirPrescricao(List.of(EntityBuilder.createRandomItemPrescricao()));
            atendimentoRepository.salvar(atendimento);

            UUID atendimentoId = atendimento.getId().getValue();
            UUID prescricaoId = prescricao.getId().getValue();

            given().contentType("application/json").port(port)
                    .header("Authorization", "Bearer " + token)
                    .when().patch("/api/v1/atendimentos/" + atendimentoId + "/prescricoes/"
                            + prescricaoId + "/cancelar")
                    .then().log().ifValidationFails(LogDetail.BODY)
                    .statusCode(204)
                    .body(emptyOrNullString());

            Prescricao cancelada = atendimentoRepository
                    .buscarUmPorAtendimentoId(atendimento.getId())
                    .getPrescricoes().getFirst();

            assertThat(cancelada.getId()).isEqualTo(prescricao.getId());
            assertThat(cancelada.getStatus()).isEqualTo(StatusPrescricao.CANCELADA);
        }

        @Test
        @DisplayName("Should return 409 when prescription is already cancelled")
        void shouldReturn409WhenAlreadyCancelled() {
            Atendimento atendimento = EntityBuilder.createCompleteAtendimento();
            Prescricao prescricao = atendimento.emitirPrescricao(
                    List.of(EntityBuilder.createRandomItemPrescricao()));
            prescricao.cancelar();
            atendimentoRepository.salvar(atendimento);

            given().contentType("application/json").port(port)
                    .header("Authorization", "Bearer " + token)
                    .when().patch("/api/v1/atendimentos/" + atendimento.getId().getValue()
                            + "/prescricoes/" + prescricao.getId().getValue() + "/cancelar")
                    .then().log().ifValidationFails(LogDetail.BODY)
                    .statusCode(409)
                    .contentType(ContentType.JSON)
                    .body("message", not(emptyOrNullString()));
        }

        @Test
        @DisplayName("Should return 409 when prescription is already finalized")
        void shouldReturn409WhenAlreadyFinalized() {
            Atendimento atendimento = EntityBuilder.createCompleteAtendimento();
            Prescricao prescricao = atendimento.emitirPrescricao(
                    List.of(EntityBuilder.createRandomItemPrescricao()));
            prescricao.finalizar();
            atendimentoRepository.salvar(atendimento);

            given().contentType("application/json").port(port)
                    .header("Authorization", "Bearer " + token)
                    .when().patch("/api/v1/atendimentos/" + atendimento.getId().getValue()
                            + "/prescricoes/" + prescricao.getId().getValue() + "/cancelar")
                    .then().log().ifValidationFails(LogDetail.BODY)
                    .statusCode(409)
                    .contentType(ContentType.JSON)
                    .body("message", not(emptyOrNullString()));

            Prescricao lida = atendimentoRepository
                    .buscarUmPorAtendimentoId(atendimento.getId())
                    .getPrescricoes().getFirst();
            assertThat(lida.getStatus()).isEqualTo(StatusPrescricao.FINALIZADA);
        }
    }

    @Nested
    @DisplayName("PATCH /{atendimentoId}/prescricoes/{prescricaoId}/finalizar")
    class Finalizar {

        @Test
        @DisplayName("Should return 204 and set the open prescription to FINALIZADA")
        void shouldReturn204AndSetTheOpenPrescriptionToFinalizada() {
            Atendimento atendimento = EntityBuilder.createCompleteAtendimento();
            Prescricao prescricao = atendimento.emitirPrescricao(
                    List.of(EntityBuilder.createRandomItemPrescricao()));
            atendimentoRepository.salvar(atendimento);

            given().contentType("application/json").port(port)
                    .header("Authorization", "Bearer " + token)
                    .when().patch("/api/v1/atendimentos/" + atendimento.getId().getValue()
                            + "/prescricoes/" + prescricao.getId().getValue() + "/finalizar")
                    .then().log().ifValidationFails(LogDetail.BODY)
                    .statusCode(204);

            Prescricao finalizada = atendimentoRepository
                    .buscarUmPorAtendimentoId(atendimento.getId())
                    .getPrescricoes().getFirst();

            assertThat(finalizada.getStatus()).isEqualTo(StatusPrescricao.FINALIZADA);
        }

        @Test
        @DisplayName("Should return 409 when prescription is already finalized")
        void shouldReturn409WhenPrescriptionIsAlreadyFinalized() {
            Atendimento atendimento = EntityBuilder.createCompleteAtendimento();
            Prescricao prescricao = atendimento.emitirPrescricao(
                    List.of(EntityBuilder.createRandomItemPrescricao()));
            prescricao.finalizar();
            atendimentoRepository.salvar(atendimento);

            given().contentType("application/json").port(port)
                    .header("Authorization", "Bearer " + token)
                    .when().patch("/api/v1/atendimentos/" + atendimento.getId().getValue()
                            + "/prescricoes/" + prescricao.getId().getValue() + "/finalizar")
                    .then().log().ifValidationFails(LogDetail.BODY)
                    .statusCode(409)
                    .contentType(ContentType.JSON)
                    .body("message", not(emptyOrNullString()));

            Prescricao lida = atendimentoRepository
                    .buscarUmPorAtendimentoId(atendimento.getId())
                    .getPrescricoes().getFirst();

            assertThat(lida.getStatus()).isEqualTo(StatusPrescricao.FINALIZADA);
        }

        @Test
        @DisplayName("Should return 409 when prescription is already cancelled")
        void shouldReturn409WhenPrescriptionIsAlreadyCancelled() {
            Atendimento atendimento = EntityBuilder.createCompleteAtendimento();
            Prescricao prescricao = atendimento.emitirPrescricao(
                    List.of(EntityBuilder.createRandomItemPrescricao()));
            prescricao.cancelar();
            atendimentoRepository.salvar(atendimento);

            given().contentType("application/json").port(port)
                    .header("Authorization", "Bearer " + token)
                    .when().patch("/api/v1/atendimentos/" + atendimento.getId().getValue()
                            + "/prescricoes/" + prescricao.getId().getValue() + "/finalizar")
                    .then().log().ifValidationFails(LogDetail.BODY)
                    .statusCode(409)
                    .contentType(ContentType.JSON)
                    .body("message", not(emptyOrNullString()));

            Prescricao lida = atendimentoRepository
                    .buscarUmPorAtendimentoId(atendimento.getId())
                    .getPrescricoes().getFirst();

            assertThat(lida.getStatus()).isEqualTo(StatusPrescricao.CANCELADA);
        }

        @Test
        @DisplayName("Should return 409 when finalizing a prescription with no items")
        void shouldReturn409WhenFinalizingAPrescriptionWithNoItems() {
            Atendimento atendimento = EntityBuilder.createCompleteAtendimento();
            ItemPrescricao item = EntityBuilder.createRandomItemPrescricao();
            Prescricao prescricao = atendimento.emitirPrescricao(List.of(item));

            prescricao.removerItem(item.getId());
            atendimentoRepository.salvar(atendimento);

            given().contentType("application/json").port(port)
                    .header("Authorization", "Bearer " + token)
                    .when().patch("/api/v1/atendimentos/" + atendimento.getId().getValue()
                            + "/prescricoes/" + prescricao.getId().getValue() + "/finalizar")
                    .then().log().ifValidationFails(LogDetail.BODY)
                    .statusCode(409)
                    .contentType(ContentType.JSON)
                    .body("message", not(emptyOrNullString()));

            Prescricao lida = atendimentoRepository
                    .buscarUmPorAtendimentoId(atendimento.getId())
                    .getPrescricoes().getFirst();

            assertThat(lida.getStatus()).isEqualTo(StatusPrescricao.ABERTA);
            assertThat(lida.getItens()).isEmpty();
        }

    }
}