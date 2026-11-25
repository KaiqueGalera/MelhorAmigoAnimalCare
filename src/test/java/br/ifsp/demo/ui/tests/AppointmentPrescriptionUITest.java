package br.ifsp.demo.ui.tests;

import br.ifsp.demo.ui.base.BaseAppointmentDetailsUITest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UI Test")
public class AppointmentPrescriptionUITest extends BaseAppointmentDetailsUITest {

    @Test
    @DisplayName("shouldShowEmptyStateWhenThereAreNoPrescriptions")
    void shouldShowEmptyStateWhenThereAreNoPrescriptions() {
        appointmentDetailsPage.openPrescriptionTab();

        assertThat(appointmentDetailsPage.noPrescriptionsMessage())
                .isEqualTo("Nenhuma prescrição emitida neste atendimento.");
    }

    @Test
    @DisplayName("shouldEmitPrescriptionWithValidItem")
    void shouldEmitPrescriptionWithValidItem() {
        appointmentDetailsPage.openPrescriptionTab();

        String medication = faker.medical().medicineName();
        String dosage = String.valueOf(faker.number().numberBetween(1,10));
        String frequency = faker.number().numberBetween(1,3) + " vezes ao dia";
        String days = String.valueOf(faker.number().numberBetween(1,30));

        appointmentDetailsPage.fillPrescriptionItem(0, medication, dosage, "Oral", frequency,days);
        appointmentDetailsPage.submitPrescription();

        assertThat(appointmentDetailsPage.prescriptionMedicationNames())
                .containsExactly(medication);

    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    @DisplayName("shouldRejectPrescriptionMedication")
    void shouldRejectPrescriptionWithoutMedication(String medication) {

        appointmentDetailsPage.openPrescriptionTab();

        String dosage = String.valueOf(faker.number().numberBetween(1,10));
        String frequency = faker.number().numberBetween(1,3) + " vezes ao dia";
        String days = String.valueOf(faker.number().numberBetween(1,30));

        appointmentDetailsPage.fillPrescriptionItem(0, medication, dosage, "Oral", frequency,days);
        appointmentDetailsPage.submitPrescription();

        assertThat(appointmentDetailsPage.prescriptionMedicationErrorMessage())
                .isEqualTo("Obrigatório.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "0.1", ""})
    @DisplayName("shouldRejectPrescriptionDosage")
    void shouldRejectPrescriptionWithZeroDosage(String dosage) {

        appointmentDetailsPage.openPrescriptionTab();

        String medication = faker.medical().medicineName();
        String frequency = faker.number().numberBetween(1,3) + " vezes ao dia";
        String days = String.valueOf(faker.number().numberBetween(1,30));

        appointmentDetailsPage.fillPrescriptionItem(0, medication, dosage, "Oral", frequency,days);
        appointmentDetailsPage.submitPrescription();

        assertThat(appointmentDetailsPage.prescriptionDosageErrorMessage())
                .isEqualTo("Inteiro maior que zero.");
    }


    @ParameterizedTest(name = "shouldEmitPrescriptionWithRoute {0}")
    @ValueSource(strings = {
            "Oral",
            "Subcutânea",
            "Intramuscular",
            "Intravenosa",
            "Tópica",
            "Oftálmica",
            "Otológica"
    })
    @DisplayName("shouldEmitPrescriptionWithSelectedRoute")
    void shouldEmitPrescriptionWithSelectedRoute(String route) {
        appointmentDetailsPage.openPrescriptionTab();

        String medication = faker.medical().medicineName();
        String dosage = String.valueOf(faker.number().numberBetween(1,10));
        String frequency = faker.number().numberBetween(1,3) + " vezes ao dia";
        String days = String.valueOf(faker.number().numberBetween(1,30));

        appointmentDetailsPage.fillPrescriptionItem(0, medication, dosage, route, frequency,days);
        appointmentDetailsPage.submitPrescription();

        assertThat(appointmentDetailsPage.prescriptionItemText())
                .contains(route);

    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    @DisplayName("shouldRejectPrescription")
    void shouldRejectPrescription(String frequency) {

        appointmentDetailsPage.openPrescriptionTab();

        String medication = faker.medical().medicineName();
        String dosage = String.valueOf(faker.number().numberBetween(1,10));
        String days = String.valueOf(faker.number().numberBetween(1,30));

        appointmentDetailsPage.fillPrescriptionItem(0, medication, dosage, "Oral", frequency,days);
        appointmentDetailsPage.submitPrescription();

        assertThat(appointmentDetailsPage.prescriptionFrequencyErrorMessage())
                .isEqualTo("Obrigatória.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "0.1", ""})
    @DisplayName("shouldRejectDays")
    void shouldRejectDays(String days) {

        appointmentDetailsPage.openPrescriptionTab();

        String medication = faker.medical().medicineName();
        String frequency = faker.number().numberBetween(1,3) + " vezes ao dia";
        String dosage = String.valueOf(faker.number().numberBetween(1,10));

        appointmentDetailsPage.fillPrescriptionItem(0, medication, dosage , "Oral", frequency,days);
        appointmentDetailsPage.submitPrescription();

        assertThat(appointmentDetailsPage.prescriptionDaysErrorMessage())
                .isEqualTo("Inteiro maior que zero.");
    }

    @Test
    @DisplayName("shouldEmitPrescriptionWithMultipleItems")
    void shouldEmitPrescriptionWithMultipleItems() {

        appointmentDetailsPage.openPrescriptionTab();

        String firstMedication = faker.medical().medicineName();
        String secondMedication = faker.medical().medicineName();
        String dosage = String.valueOf(faker.number().numberBetween(1,10));
        String frequency = faker.number().numberBetween(1,3) + " vezes ao dia";
        String days = String.valueOf(faker.number().numberBetween(1,30));

        appointmentDetailsPage.fillPrescriptionItem(0, firstMedication, dosage, "Oral", frequency,days);
        appointmentDetailsPage.addAnotherPrescriptionItem();
        appointmentDetailsPage.fillPrescriptionItem(1, secondMedication, dosage, "Oral", frequency,days);
        appointmentDetailsPage.submitPrescription();

        assertThat(appointmentDetailsPage.prescriptionMedicationNames())
                .containsExactly(firstMedication, secondMedication);
    }

    @Test
    @DisplayName("shouldRemovePrescriptionItemBeforeEmiting")
    void shouldRemovePrescriptionItemBeforeEmiting() {
        appointmentDetailsPage.openPrescriptionTab();

        String firstMedication = faker.medical().medicineName();
        String removedMedication = faker.medical().medicineName();
        String dosage = String.valueOf(faker.number().numberBetween(1,10));
        String frequency = faker.number().numberBetween(1,3) + " vezes ao dia";
        String days = String.valueOf(faker.number().numberBetween(1,30));

        appointmentDetailsPage.fillPrescriptionItem(0, firstMedication, dosage, "Oral", frequency,days);
        appointmentDetailsPage.addAnotherPrescriptionItem();
        appointmentDetailsPage.fillPrescriptionItem(1, removedMedication, dosage, "Oral", frequency,days);

        appointmentDetailsPage.removePrescriptionRequestAt(1);
        appointmentDetailsPage.submitPrescription();

        assertThat(appointmentDetailsPage.prescriptionMedicationNames())
                .containsExactly(firstMedication);

    }

    @Test
    @DisplayName("shouldEditPrescriptionItemAtAnyPosition")
    void testName() {
        appointmentDetailsPage.openPrescriptionTab();

        String firstMedication = faker.medical().medicineName();
        String secondMedication = faker.medical().medicineName();
        String dosage = String.valueOf(faker.number().numberBetween(1,10));
        String frequency = faker.number().numberBetween(1,3) + " vezes ao dia";
        String days = String.valueOf(faker.number().numberBetween(1,30));

        appointmentDetailsPage.fillPrescriptionItem(0, firstMedication, dosage, "Oral", frequency,days);
        appointmentDetailsPage.addAnotherPrescriptionItem();
        appointmentDetailsPage.fillPrescriptionItem(1, secondMedication, dosage, "Oral", frequency,days);
        appointmentDetailsPage.submitPrescription();

        String updateMedication = faker.medical().medicineName();
        String updateDosage = "11";
        String updateFrequency = "4 vezes ao dia";
        String updateDays = "31";

        appointmentDetailsPage.editPrescriptionItemAt(1, updateMedication, updateDosage, "Subcutânea", updateFrequency, updateDays);

        assertThat(appointmentDetailsPage.prescriptionMedicationNames())
                .containsExactly(firstMedication, updateMedication);

        assertThat(appointmentDetailsPage.prescriptionItemTextAt(1))
                .contains(updateMedication, updateDosage, "Subcutânea", updateFrequency, updateDays);
    }

    @Test
    @DisplayName("shouldRemoveItemFromIssuedPrescription")
    void shouldRemoveItemFromIssuedPrescription() {
        appointmentDetailsPage.openPrescriptionTab();

        String firstMedication = faker.medical().medicineName();
        String secondMedication = faker.medical().medicineName();
        String dosage = String.valueOf(faker.number().numberBetween(1,10));
        String frequency = faker.number().numberBetween(1,3) + " vezes ao dia";
        String days = String.valueOf(faker.number().numberBetween(1,30));

        appointmentDetailsPage.fillPrescriptionItem(0, firstMedication, dosage, "Oral", frequency,days);
        appointmentDetailsPage.addAnotherPrescriptionItem();
        appointmentDetailsPage.fillPrescriptionItem(1, secondMedication, dosage, "Oral", frequency,days);
        appointmentDetailsPage.submitPrescription();

        appointmentDetailsPage.removeIssuedPrescriptionItemAt(1);

        assertThat(appointmentDetailsPage.prescriptionMedicationNames())
                .containsExactly(firstMedication);
    }

    @Test
    @DisplayName("shouldHideActionsAfterFinalizingPrescription")
    void shouldHideActionsAfterFinalizingPrescription() {

        appointmentDetailsPage.openPrescriptionTab();

        String firstMedication = faker.medical().medicineName();
        String dosage = String.valueOf(faker.number().numberBetween(1,10));
        String frequency = faker.number().numberBetween(1,3) + " vezes ao dia";
        String days = String.valueOf(faker.number().numberBetween(1,30));

        appointmentDetailsPage.fillPrescriptionItem(0, firstMedication, dosage, "Oral", frequency,days);
        appointmentDetailsPage.submitPrescription();

        appointmentDetailsPage.finalizeIssuedPrescription();

        assertThat(appointmentDetailsPage.hasPrescriptionItemActionsAt(0))
                .isFalse();
    }

    @Test
    @DisplayName("shouldHideItemActionsAndConcludingAppointmentAfterCancelingPrescription")
    void shouldHideItemActionsAndConcludingAppointmentAfterCancelingPrescription() {

        appointmentDetailsPage.registerVitalSigns(
                String.valueOf(faker.number().randomDouble(1, 36, 39)),
                String.valueOf(faker.number().numberBetween(60, 120)),
                String.valueOf(faker.number().randomDouble(2, 2, 40))
        );

        assertThat(appointmentDetailsPage.vitalsSaveMessage())
                .isEqualTo("Sinais vitais salvos.");

        appointmentDetailsPage.registerAnmnesis(
                faker.lorem().sentence(),
                faker.lorem().sentence()
        );

        assertThat(appointmentDetailsPage.anamneseSavedMessage())
                .isEqualTo("Anamnese salva.");

        String code = faker.number().digits(3);
        String description = faker.lorem().sentence();

        appointmentDetailsPage.addDiagnosis(code, description, "Presuntivo");
        assertThat(appointmentDetailsPage.hasDiagnosis(code, description, "Presuntivo"))
                .isTrue();

        appointmentDetailsPage.openPrescriptionTab();

        String firstMedication = faker.medical().medicineName();
        String dosage = String.valueOf(faker.number().numberBetween(1,10));
        String frequency = faker.number().numberBetween(1,3) + " vezes ao dia";
        String days = String.valueOf(faker.number().numberBetween(1,30));

        appointmentDetailsPage.fillPrescriptionItem(0, firstMedication, dosage, "Oral", frequency,days);
        appointmentDetailsPage.submitPrescription();

        assertThat(appointmentDetailsPage.isNoOpenPrescriptionRequirementComplete())
                .isFalse();
        assertThat(appointmentDetailsPage.isConcludeAppointmentButtonEnabled())
                .isFalse();

        appointmentDetailsPage.cancelIssuedPrescription();

        assertThat(appointmentDetailsPage.isPrescriptionCancelled())
                .isTrue();
        assertThat(appointmentDetailsPage.hasPrescriptionItemActionsAt(0))
                .isFalse();

        assertThat(appointmentDetailsPage.isNoOpenPrescriptionRequirementComplete())
                .isTrue();

        appointmentDetailsPage.concludeAppointment();

        assertThat(appointmentDetailsPage.isAppointmentConcluded())
                .isTrue();
    }

    @Test
    @DisplayName("shouldKeepAppointmentOpenAfterRemovingLastPrescriptionItem")
    void shouldKeepAppointmentOpenAfterRemovingLastPrescriptionItem() {
        appointmentDetailsPage.registerVitalSigns(
                String.valueOf(faker.number().randomDouble(1, 36, 39)),
                String.valueOf(faker.number().numberBetween(60, 120)),
                String.valueOf(faker.number().randomDouble(2, 2, 40))
        );

        assertThat(appointmentDetailsPage.vitalsSaveMessage())
                .isEqualTo("Sinais vitais salvos.");

        appointmentDetailsPage.registerAnmnesis(
                faker.lorem().sentence(),
                faker.lorem().sentence()
        );

        assertThat(appointmentDetailsPage.anamneseSavedMessage())
                .isEqualTo("Anamnese salva.");

        String code = faker.number().digits(3);
        String description = faker.lorem().sentence();

        appointmentDetailsPage.addDiagnosis(code, description, "Presuntivo");
        assertThat(appointmentDetailsPage.hasDiagnosis(code, description, "Presuntivo"))
                .isTrue();

        appointmentDetailsPage.openPrescriptionTab();

        String firstMedication = faker.medical().medicineName();
        String dosage = String.valueOf(faker.number().numberBetween(1,10));
        String frequency = faker.number().numberBetween(1,3) + " vezes ao dia";
        String days = String.valueOf(faker.number().numberBetween(1,30));

        appointmentDetailsPage.fillPrescriptionItem(0, firstMedication, dosage, "Oral", frequency,days);
        appointmentDetailsPage.submitPrescription();

        appointmentDetailsPage.removeIssuedPrescriptionItemAt(0);

        assertThat(appointmentDetailsPage.prescriptionItemsEmptyMessage())
                .isEqualTo("Sem itens.");

        assertThat(appointmentDetailsPage.isPrescriptionFinalizeButtonEnabled())
                .isFalse();

        assertThat(appointmentDetailsPage.isNoOpenPrescriptionRequirementComplete())
                .isFalse();

        assertThat(appointmentDetailsPage.isConcludeAppointmentButtonEnabled())
                .isFalse();

    }

    @Test
    @DisplayName("shouldAllowConcludingAppointmentAfterFinalizingPrescription")
    void shouldAllowConcludingAppointmentAfterFinalizingPrescription() {
        appointmentDetailsPage.registerVitalSigns(
                String.valueOf(faker.number().randomDouble(1, 36, 39)),
                String.valueOf(faker.number().numberBetween(60, 120)),
                String.valueOf(faker.number().randomDouble(2, 2, 40))
        );

        assertThat(appointmentDetailsPage.vitalsSaveMessage())
                .isEqualTo("Sinais vitais salvos.");

        appointmentDetailsPage.registerAnmnesis(
                faker.lorem().sentence(),
                faker.lorem().sentence()
        );

        assertThat(appointmentDetailsPage.anamneseSavedMessage())
                .isEqualTo("Anamnese salva.");

        String code = faker.number().digits(3);
        String description = faker.lorem().sentence();

        appointmentDetailsPage.addDiagnosis(code, description, "Presuntivo");
        assertThat(appointmentDetailsPage.hasDiagnosis(code, description, "Presuntivo"))
                .isTrue();

        appointmentDetailsPage.openPrescriptionTab();

        String medication = faker.medical().medicineName();
        String dosage = String.valueOf(faker.number().numberBetween(1,10));
        String frequency = faker.number().numberBetween(1,3) + " vezes ao dia";
        String days = String.valueOf(faker.number().numberBetween(1,30));

        appointmentDetailsPage.fillPrescriptionItem(0, medication, dosage, "Oral", frequency,days);
        appointmentDetailsPage.submitPrescription();

        assertThat(appointmentDetailsPage.prescriptionMedicationNames())
                .containsExactly(medication);

        assertThat(appointmentDetailsPage.isNoOpenPrescriptionRequirementComplete())
                .isFalse();
        assertThat(appointmentDetailsPage.isConcludeAppointmentButtonEnabled())
                .isFalse();

        appointmentDetailsPage.finalizeIssuedPrescription();

        assertThat(appointmentDetailsPage.isNoOpenPrescriptionRequirementComplete())
                .isTrue();
        assertThat(appointmentDetailsPage.isConcludeAppointmentButtonEnabled())
                .isTrue();

        appointmentDetailsPage.concludeAppointment();

        assertThat(appointmentDetailsPage.isAppointmentConcluded())
                .isTrue();

    }

    @Test
    @DisplayName("shouldKeepAppointmentOpenWhileAnotherPrescriptionIsOpen")
    void shouldKeepAppointmentOpenWhileAnotherPrescriptionIsOpen() {
        appointmentDetailsPage.registerVitalSigns(
                String.valueOf(faker.number().randomDouble(1, 36, 39)),
                String.valueOf(faker.number().numberBetween(60, 120)),
                String.valueOf(faker.number().randomDouble(2, 2, 40))
        );

        assertThat(appointmentDetailsPage.vitalsSaveMessage())
                .isEqualTo("Sinais vitais salvos.");

        appointmentDetailsPage.registerAnmnesis(
                faker.lorem().sentence(),
                faker.lorem().sentence()
        );

        assertThat(appointmentDetailsPage.anamneseSavedMessage())
                .isEqualTo("Anamnese salva.");

        String code = faker.number().digits(3);
        String description = faker.lorem().sentence();

        appointmentDetailsPage.addDiagnosis(code, description, "Presuntivo");
        assertThat(appointmentDetailsPage.hasDiagnosis(code, description, "Presuntivo"))
                .isTrue();

        appointmentDetailsPage.openPrescriptionTab();

        String firstmedication = faker.medical().medicineName();
        String secondmedication = faker.medical().medicineName();
        String dosage = String.valueOf(faker.number().numberBetween(1,10));
        String frequency = faker.number().numberBetween(1,3) + " vezes ao dia";
        String days = String.valueOf(faker.number().numberBetween(1,30));

        appointmentDetailsPage.fillPrescriptionItem(0, firstmedication, dosage, "Oral", frequency,days);
        appointmentDetailsPage.submitPrescription();
        assertThat(appointmentDetailsPage.prescriptionMedicationNames())
                .containsExactly(firstmedication);

        appointmentDetailsPage.fillPrescriptionItem(0, secondmedication, dosage, "Oral", frequency,days);
        appointmentDetailsPage.submitPrescription();
        appointmentDetailsPage.waitForPrescriptionMedicationCount(2);
        assertThat(appointmentDetailsPage.prescriptionMedicationNames())
                .containsExactly(secondmedication, firstmedication);

        assertThat(appointmentDetailsPage.isNoOpenPrescriptionRequirementComplete())
                .isFalse();

        appointmentDetailsPage.finalizeIssuedPrescription();

        assertThat(appointmentDetailsPage.isNoOpenPrescriptionRequirementComplete())
                .isFalse();
        assertThat(appointmentDetailsPage.isConcludeAppointmentButtonEnabled())
                .isFalse();

    }

    @Test
    @DisplayName("shouldHidePrescriptionFormAfterConcludingAppointment")
    void shouldHidePrescriptionFormAfterConcludingAppointment() {

        appointmentDetailsPage.registerVitalSigns(
                String.valueOf(faker.number().randomDouble(1, 36, 39)),
                String.valueOf(faker.number().numberBetween(60, 120)),
                String.valueOf(faker.number().randomDouble(2, 2, 40))
        );

        assertThat(appointmentDetailsPage.vitalsSaveMessage())
                .isEqualTo("Sinais vitais salvos.");

        appointmentDetailsPage.registerAnmnesis(
                faker.lorem().sentence(),
                faker.lorem().sentence()
        );

        assertThat(appointmentDetailsPage.anamneseSavedMessage())
                .isEqualTo("Anamnese salva.");

        String code = faker.number().digits(3);
        String description = faker.lorem().sentence();

        appointmentDetailsPage.addDiagnosis(code, description, "Presuntivo");
        assertThat(appointmentDetailsPage.hasDiagnosis(code, description, "Presuntivo"))
                .isTrue();

        appointmentDetailsPage.concludeAppointment();

        assertThat(appointmentDetailsPage.isAppointmentConcluded())
                .isTrue();

        appointmentDetailsPage.openPrescriptionTab();

        assertThat(appointmentDetailsPage.isPrescriptionFormDisplayed())
                .isFalse();
    }

    @Test
    @DisplayName("shouldHidePrescriptionFormAfterCancellationAppointment")
    void shouldHidePrescriptionFormAfterCancellationAppointment() {

        appointmentDetailsPage.cancelAppointment(faker.lorem().sentence());

        assertThat(appointmentDetailsPage.isAppointmentConcluded())
                .isFalse();

        appointmentDetailsPage.openPrescriptionTab();

        assertThat(appointmentDetailsPage.isPrescriptionFormDisplayed())
                .isFalse();

    }

    @Test
    @DisplayName("shouldModifyWindowOnPrescriptionPage")
    void shouldModifyWindowOnPrescriptionPage() {
        appointmentDetailsPage.openPrescriptionTab();

        driver.manage().window().maximize();
        driver.manage().window().fullscreen();
        driver.manage().window().minimize();
    }
}
