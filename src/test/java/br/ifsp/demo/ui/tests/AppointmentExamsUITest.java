package br.ifsp.demo.ui.tests;

import br.ifsp.demo.ui.base.BaseAppointmentDetailsUITest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("UI Test")
public class AppointmentExamsUITest extends BaseAppointmentDetailsUITest {
    @Test
    @DisplayName("shouldHideExamRequestFormAfterConclusion")
    void shouldHideExamRequestFormAfterConclusion() {
        appointmentDetailsPage.registerVitalSigns(String.valueOf(faker.number().randomDouble(1,35,40)),
                String.valueOf(faker.number().randomNumber()),
                String.valueOf(faker.number().randomDouble(2,1, 40)));

        appointmentDetailsPage.registerAnmnesis(faker.lorem().sentence(), faker.lorem().sentence());

        String code = faker.number().digits(3);
        String description = faker.lorem().sentence();
        String type = "Presuntivo";

        appointmentDetailsPage.addDiagnosis(code,description,type);
        appointmentDetailsPage.concludeAppointment();
        appointmentDetailsPage.openExamsTab();

        assertThat(appointmentDetailsPage.isExamRequestFormDisplayed()).isFalse();

    }

    @Test
    @DisplayName("shouldFillSingleExamRequest")
    void shouldFillSingleExamRequest() {
        appointmentDetailsPage.openExamsTab();

        String name = faker.lorem().word();
        LocalDate date= LocalDate.now().plusDays(1);
        String expectedDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")));
        String selectedTime = "09:00";

        appointmentDetailsPage.fillExamRequestAt(0, name, date, selectedTime);

        assertThat(appointmentDetailsPage.examNameValue()).isEqualTo(name);
        assertThat(appointmentDetailsPage.examDateValue()).isEqualTo(expectedDate);
        assertThat(appointmentDetailsPage.examTimeValue()).isEqualTo(selectedTime);

    }

    @Test
    @DisplayName("shouldAddAnotherExamRequestRow")
    void shouldAddAnotherExamRequestRow() {
        appointmentDetailsPage.openExamsTab();

        String name = faker.lorem().word();
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime time = LocalTime.of(9,0);

        appointmentDetailsPage.fillExamRequestAt(0, name, date, time.format(DateTimeFormatter.ofPattern("HH:mm")));

        assertThat(appointmentDetailsPage.examRequestRowCount()).isEqualTo(1);

        appointmentDetailsPage.addAnotherExam();

        assertThat(appointmentDetailsPage.examRequestRowCount()).isEqualTo(2);
        assertThat(appointmentDetailsPage.examNameValue()).isEqualTo(name);
        assertThat(appointmentDetailsPage.isRemoveExamButtonDisplayed()).isTrue();

    }

    @Test
    @DisplayName("shouldSubmitSingleExamRequest")
    void shouldSubmitSingleExamRequest() {
        appointmentDetailsPage.openExamsTab();

        String name = faker.lorem().word();
        LocalDate date = LocalDate.now().plusDays(1);
        String time = LocalTime.of(9,0).format(DateTimeFormatter.ofPattern("HH:mm"));

        appointmentDetailsPage.fillExamRequestAt(0, name, date, time);
        appointmentDetailsPage.submitExamRequest();

        assertThat(appointmentDetailsPage.requestedExamNames()).contains(name);

    }

    @Test
    @DisplayName("shouldRejectEmptyExamResult")
    void shouldRejectEmptyExamResult() {
        appointmentDetailsPage.openExamsTab();

        String name = faker.lorem().word();
        LocalDate date = LocalDate.now().plusDays(1);
        String time = LocalTime.of(9,0).format(DateTimeFormatter.ofPattern("HH:mm"));

        appointmentDetailsPage.fillExamRequestAt(0, name, date, time);
        appointmentDetailsPage.submitExamRequest();
        appointmentDetailsPage.submitExamResult("");

        assertThat(appointmentDetailsPage.examResultErrorMessage())
                .isEqualTo("O resultado deve ser informado.");

    }

    @Test
    @DisplayName("shouldRejectExamResultWithoutOnlySpace")
    void shouldRejectExamResultWithoutOnlySpaces() {
        appointmentDetailsPage.openExamsTab();

        String name = faker.lorem().word();
        LocalDate date = LocalDate.now().plusDays(1);
        String time = LocalTime.of(9,0).format(DateTimeFormatter.ofPattern("HH:mm"));

        appointmentDetailsPage.fillExamRequestAt(0, name, date, time);
        appointmentDetailsPage.submitExamRequest();
        appointmentDetailsPage.submitExamResult(" ");

        assertThat(appointmentDetailsPage.examResultErrorMessage())
                .isEqualTo("O resultado deve ser informado.");
    }

    @Test
    @DisplayName("shouldTrimExamResult")
    void shouldTrimExamResult() {
        appointmentDetailsPage.openExamsTab();

        String name = faker.lorem().word();
        LocalDate date = LocalDate.now().plusDays(1);
        String time = LocalTime.of(9,0).format(DateTimeFormatter.ofPattern("HH:mm"));
        String result = " " + faker.lorem().sentence() + " ";
        appointmentDetailsPage.fillExamRequestAt(0, name, date, time);
        appointmentDetailsPage.submitExamRequest();
        appointmentDetailsPage.submitExamResult(result);

        assertThat(appointmentDetailsPage.registeredExamResult())
                .isEqualTo(result.trim());

    }

    @Test
    @DisplayName("shouldRegisterExamResult")
    void shouldRegisterExamResult() {
        appointmentDetailsPage.openExamsTab();

        String name = faker.lorem().word();
        LocalDate date = LocalDate.now().plusDays(1);
        String time = LocalTime.of(9,0).format(DateTimeFormatter.ofPattern("HH:mm"));
        String result = faker.lorem().sentence();

        appointmentDetailsPage.fillExamRequestAt(0, name, date, time);
        appointmentDetailsPage.submitExamRequest();
        appointmentDetailsPage.submitExamResult(result);

        assertThat(appointmentDetailsPage.registeredExamResult())
                .isEqualTo(result);

        assertThat(appointmentDetailsPage.isExamResultFormDisplayed())
                .isFalse();

    }

    @Test
    @DisplayName("shouldRemoveExamBeforeRequesting")
    void shouldRemoveExamBeforeRequesting() {
        appointmentDetailsPage.openExamsTab();

        String firstExam = faker.lorem().word();
        String removedExam = faker.lorem().word();
        LocalDate date = LocalDate.now().plusDays(1);
        String time = LocalTime.of(9,0).format(DateTimeFormatter.ofPattern("HH:mm"));
        String result = faker.lorem().sentence();

        appointmentDetailsPage.fillExamRequestAt(0, firstExam, date, time);
        appointmentDetailsPage.addAnotherExam();
        appointmentDetailsPage.fillExamRequestAt(1, removedExam, date, time);

        appointmentDetailsPage.removeExamRequestAt(1);

        assertThat(appointmentDetailsPage.examRequestRowCount()).isEqualTo(1);

        appointmentDetailsPage.submitExamRequest();

        assertThat(appointmentDetailsPage.requestedExamNames())
                .containsExactly(firstExam);

    }

    @Test
    @DisplayName("shouldRejectExamRequestWithoutName")
    void shouldRejectExamRequestWithoutName() {
        appointmentDetailsPage.openExamsTab();

        LocalDate date = LocalDate.now().plusDays(1);
        String time = LocalTime.of(9,0).format(DateTimeFormatter.ofPattern("HH:mm"));
        appointmentDetailsPage.fillExamRequestAt(0, "", date, time);
        appointmentDetailsPage.submitExamRequest();

        assertThat(appointmentDetailsPage.examNameErrorMessage())
                .isEqualTo("Informe o nome do exame.");

    }

    @Test
    @DisplayName("shouldTrimExamName")
    void shouldTrimExamName() {
        appointmentDetailsPage.openExamsTab();

        String name = " " + faker.lorem().sentence() + " ";
        LocalDate date = LocalDate.now().plusDays(1);
        String time = LocalTime.of(9,0).format(DateTimeFormatter.ofPattern("HH:mm"));
        appointmentDetailsPage.fillExamRequestAt(0, name, date, time);
        appointmentDetailsPage.submitExamRequest();

        assertThat(appointmentDetailsPage.requestedExamNames())
                .containsExactly(name.trim());

    }

    @Test
    @DisplayName("shouldRejectExamNameWithOnlySpaces")
    void shouldRejectExamNameWithOnlySpaces() {
        appointmentDetailsPage.openExamsTab();

        LocalDate date = LocalDate.now().plusDays(1);
        String time = LocalTime.of(9,0).format(DateTimeFormatter.ofPattern("HH:mm"));
        appointmentDetailsPage.fillExamRequestAt(0, " ", date, time);
        appointmentDetailsPage.submitExamRequest();

        assertThat(appointmentDetailsPage.examNameErrorMessage())
                .isEqualTo("Informe o nome do exame.");

    }

    @Test
    @DisplayName("shouldRejectExamRequestWithoutDateAndTime")
    void shouldRejectExamRequestWithoutDateAndTime() {
        appointmentDetailsPage.openExamsTab();

        appointmentDetailsPage.enterExamNameAt(0, faker.lorem().word());
        appointmentDetailsPage.submitExamRequest();

        assertThat(appointmentDetailsPage.examDateErrorMessage())
                .isEqualTo("Informe data e hora.");

    }

    @Test
    @DisplayName("shouldRejectExamRequestsWithoutTime")
    void shouldRejectExamRequestsWithoutTime() {
        appointmentDetailsPage.openExamsTab();

        LocalDate date = LocalDate.now().plusDays(1);
        appointmentDetailsPage.fillExamRequestAt(0, "", date, "");
        appointmentDetailsPage.submitExamRequest();

        assertThat(appointmentDetailsPage.examDateErrorMessage())
                .isEqualTo("Selecione o horário.");

    }

    @Test
    @DisplayName("shouldRejectExamRequestsWithoutDate")
    void shouldRejectExamRequestsWithoutDate() {
        appointmentDetailsPage.openExamsTab();


        String time = LocalTime.of(18,0).format(DateTimeFormatter.ofPattern("HH:mm"));
        appointmentDetailsPage.enterExamNameAt(0, faker.lorem().word());
        appointmentDetailsPage.selectExamTimeAt(0, time);
        appointmentDetailsPage.submitExamRequest();

        assertThat(appointmentDetailsPage.examDateErrorMessage())
                .isEqualTo("Selecione a data.");

    }

    @Test
    @DisplayName("shouldSubmitMultipleExamRequests")
    void shouldSubmitMultipleExamRequests() {
        appointmentDetailsPage.openExamsTab();

        String firstExam = faker.lorem().word();
        String secondExam = faker.lorem().word();
        LocalDate date= LocalDate.now().plusDays(1);
        String time = LocalTime.of(9,0).format(DateTimeFormatter.ofPattern("HH:mm"));

        appointmentDetailsPage.fillExamRequestAt(0,firstExam,date,time);
        appointmentDetailsPage.addAnotherExam();
        appointmentDetailsPage.fillExamRequestAt(1, secondExam, date, time);
        appointmentDetailsPage.submitExamRequest();

        assertThat(appointmentDetailsPage.requestedExamNames())
                .containsExactly(firstExam, secondExam);

    }

    @Test
    @DisplayName("shouldNotAllowPastExamDate")
    void shouldNotAllowPastExamDate() {
        appointmentDetailsPage.openExamsTab();

        LocalDate yesterday = LocalDate.now().minusDays(1);

        assertThat(appointmentDetailsPage.isExamDateSelectable(0, yesterday))
                .isFalse();
    }

    @Test
    @DisplayName("shouldShowEmptyStateWhenThereAreNoRequestedExams")
    void shouldShowEmptyStateWhenThereAreNoRequestedExams() {
        appointmentDetailsPage.openExamsTab();

        assertThat(appointmentDetailsPage.noRequestedExamsMessage())
                .isEqualTo("Nenhum exame solicitado.");

    }

    @Test
    @DisplayName("shouldShowRequiredErrorsForEmptyExamRequest")
    void shouldShowRequiredErrorsForEmptyExamRequest() {
        appointmentDetailsPage.openExamsTab();
        appointmentDetailsPage.submitExamRequest();

        assertThat(appointmentDetailsPage.examNameErrorMessage())
                .isEqualTo("Informe o nome do exame.");
        assertThat(appointmentDetailsPage.examDateErrorMessage())
                .isEqualTo("Informe data e hora.");

    }

    @Test
    @DisplayName("shouldModifyWindowOnExamPage")
    void shouldModifyWindowOnExamPage() {
        appointmentDetailsPage.openExamsTab();

        driver.manage().window().maximize();
        driver.manage().window().fullscreen();
        driver.manage().window().minimize();
    }

}
