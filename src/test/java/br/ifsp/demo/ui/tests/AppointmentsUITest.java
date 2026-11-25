package br.ifsp.demo.ui.tests;

import br.ifsp.demo.ui.base.BaseSeleniumTest;
import br.ifsp.demo.ui.pages.AppointmentDetailsPageObject;
import br.ifsp.demo.ui.pages.AppointmentPageObject;
import br.ifsp.demo.ui.pages.LoginPageObject;
import br.ifsp.demo.ui.pages.RegisterPageObject;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import  static org.assertj.core.api.Assertions.*;

@Tag("UITest")
public class AppointmentsUITest extends BaseSeleniumTest {

    private AppointmentPageObject appointmentPage;

    @Override
    protected  void setInitialPage(){
        Faker faker = new Faker();

        String name = faker.name().firstName();
        String lastname = faker.name().lastName();
        String email = faker.internet().emailAddress();
        String password = faker.internet().password(6,10);

        RegisterPageObject registerPage = new RegisterPageObject(driver);
        registerPage.openRegisterPage();
        registerPage.registerAccount(name, lastname, email, password, password);

        LoginPageObject loginPage = new LoginPageObject(driver);
        loginPage.authenticate(email, password);

        appointmentPage = new AppointmentPageObject(driver);

    }


    @Test
    @DisplayName("shouldOpenEmergencyAppointmentWithNewAnimal")
    void shouldOpenEmergencyAppointmentWithNewAnimal() {
        appointmentPage.selectAppointment();
        appointmentPage.selectNewAnimal();

        assertThat(appointmentPage.selectedAnimal())
                .isEqualTo("Novo animal");

        AppointmentDetailsPageObject detailsPage = appointmentPage.clickOpenAppointment();

        assertThat(detailsPage.appointmentId()).isNotBlank();
        assertThat(detailsPage.appointmentOrigin()).isEqualTo("Pronto atendimento");
    }

    @DisplayName("shouldOpenScheduledAppointmentWithNewAnimal")
    void shouldOpenScheduledAppointmentWithNewAnimal() {
        appointmentPage.selectScheduledAppointment();
        appointmentPage.selectNewAnimal();

        assertThat(appointmentPage.selectedAnimal())
                .isEqualTo("Novo animal");

        AppointmentDetailsPageObject detailsPage = appointmentPage.clickOpenAppointment();

        assertThat(detailsPage.appointmentId()).isNotBlank();
        assertThat(detailsPage.appointmentOrigin()).isEqualTo("Agendamento");
    }

    @Test
    @DisplayName("shouldRejectEmergencyAppointmentForAnimalAlreadyInProgress")
    void shouldRejectEmergencyAppointmentForAnimalAlreadyInProgress() {
        appointmentPage.selectAppointment();
        appointmentPage.selectNewAnimal();

        AppointmentDetailsPageObject firstAppointment = appointmentPage.clickOpenAppointment();
        String animalId = firstAppointment.animalId();
        assertThat(firstAppointment.appointmentId()).isNotBlank();

        AppointmentPageObject appointmentPage = firstAppointment.backToAppointments();

        appointmentPage.selectAppointment();
        appointmentPage.selectFirstExistingAnimal();
        appointmentPage.clickOpenAppointment();

        assertThat(appointmentPage.openingErrorMessage()).isEqualTo("Animal - "  + animalId + " já possui atendimento em andamento!");
    }

    @Test
    @DisplayName("shouldSearchExistingAppointment")
    void shouldSearchExistingAppointment() {
        appointmentPage.selectAppointment();
        appointmentPage.selectNewAnimal();

        AppointmentDetailsPageObject createdAppointment = appointmentPage.clickOpenAppointment();

        String appointmentId = createdAppointment.appointmentId();

        AppointmentPageObject appointmentsPage = createdAppointment.backToAppointments();

        appointmentsPage.enterAppointmentId(appointmentId);

        AppointmentDetailsPageObject foundAppointment = appointmentsPage.clickSearchAppointment();

        assertThat(foundAppointment.appointmentId()).isEqualTo(appointmentId);

    }

    @Test
    @DisplayName("shouldShowErrorWhenSearchingNoneexistentAppointment")
    void shouldShowErrorWhenSearchingNoneexistentAppointment() {
        String nonexistentId = UUID.randomUUID().toString();

        appointmentPage.enterAppointmentId(nonexistentId);

        AppointmentDetailsPageObject detailsPage = appointmentPage.clickSearchAppointment();

        assertThat(detailsPage.loadErrorMessage()).isEqualTo("Atendimento não encontrado: " + nonexistentId);

    }

    @Test
    @DisplayName("shouldKeepSearchButtonDisabledWhenAppointmentIdIsEmpty")
    void shouldKeepSearchButtonDisabledWhenAppointmentIdIsEmpty() {
        assertThat(appointmentPage.isSearchButtonEnabled()).isFalse();
        assertThat(appointmentPage.heading()).isEqualTo("Atendimentos");
        
    }

    @Test
    @DisplayName("shouldShowErrorWhenSearchingMalformedAppointmentId")
    void shouldShowErrorWhenSearchingMalformedAppointmentId() {
        appointmentPage.enterAppointmentId("id-invalido");

        AppointmentDetailsPageObject detailsPage = appointmentPage.clickSearchAppointment();

        assertThat(detailsPage.loadErrorMessage()).isEqualTo("Valor inválido para o parâmetro 'id'.");

    }

    @Test
    @DisplayName("shouldTrimWhitespaceWhenSearchinExistingAppointment")
    void shouldTrimWhitespaceWhenSearchinExistingAppointment() {
        appointmentPage.selectAppointment();
        appointmentPage.selectNewAnimal();

        AppointmentDetailsPageObject createdAppointment = appointmentPage.clickOpenAppointment();

        String appointmentId = createdAppointment.appointmentId();

        AppointmentPageObject appointmentPage = createdAppointment.backToAppointments();

        appointmentPage.enterAppointmentId(" " + appointmentId + " ");

        AppointmentDetailsPageObject foundAppointment = appointmentPage.clickSearchAppointment();

        assertThat(foundAppointment.appointmentId())
                .isEqualTo(appointmentId);
        
    }

    @Test
    @DisplayName("shouldKeepSearchButtonDisabledWhenAppointmentIdContainsOnlySpaces")
    void shouldKeepSearchButtonDisabledWhenAppointmentIdContainsOnlySpaces() {
        appointmentPage.enterAppointmentId("  ");

        assertThat(appointmentPage.isSearchButtonEnabled()).isFalse();
        assertThat(appointmentPage.heading()).isEqualTo("Atendimentos");


    }


    @Test
    @DisplayName("shouldAllowNewEmergencyAppointmentForAnimalAfterCancellation")
    void shouldAllowNewEmergencyAppointmentForAnimalAfterCancellation() {
        appointmentPage.selectAppointment();
        appointmentPage.selectNewAnimal();

        AppointmentDetailsPageObject previousAppointment = appointmentPage.clickOpenAppointment();

        String previousAppointmentId = previousAppointment.appointmentId();
        String animalId = previousAppointment.animalId();

        previousAppointment.cancelAppointment(new Faker().lorem().sentence());

        AppointmentPageObject appointmentsPage = previousAppointment.backToAppointments();

        appointmentsPage.selectAppointment();
        appointmentsPage.selectFirstExistingAnimal();

        AppointmentDetailsPageObject newAppointment = appointmentsPage.clickOpenAppointment();

        assertThat(newAppointment.appointmentId()).isNotBlank();
        assertThat(newAppointment.appointmentId()).isNotEqualTo(previousAppointmentId);
        assertThat(newAppointment.animalId()).isEqualTo(animalId);
        assertThat(newAppointment.appointmentOrigin()).isEqualTo("Pronto atendimento");

    }

    @Test
    @DisplayName("shouldClearRecentAppointments")
    void shouldClearRecentAppointments() {
        appointmentPage.selectAppointment();
        appointmentPage.selectNewAnimal();

        AppointmentDetailsPageObject createdAppointment = appointmentPage.clickOpenAppointment();

        createdAppointment.appointmentId();

        AppointmentPageObject appointmentsPage = createdAppointment.backToAppointments();

        assertThat(appointmentPage.isClearRecentButtonDisplayed())
                .isTrue();

        appointmentPage.clearRecentAppointments();

        assertThat(appointmentPage.recentAppointmentsMessage()).isEqualTo("Nenhum atendimento acessado ainda.");
        assertThat(appointmentsPage.isClearRecentButtonDisplayed()).isFalse();

    }

    @Test
    @DisplayName("shouldShowMostRecentAppointmentFirst")
    void shouldShowMostRecentAppointmentFirst() {
        appointmentPage.selectAppointment();
        appointmentPage.selectNewAnimal();

        AppointmentDetailsPageObject firstAppointment = appointmentPage.clickOpenAppointment();

        String firstId = firstAppointment.appointmentId();

        AppointmentPageObject appointmentsPage = firstAppointment.backToAppointments();

        appointmentsPage.selectAppointment();
        appointmentsPage.selectNewAnimal();

        AppointmentDetailsPageObject secondAppointment = appointmentsPage.clickOpenAppointment();

        String secondId = secondAppointment.appointmentId();

        appointmentsPage = secondAppointment.backToAppointments();

        assertThat(appointmentsPage.recentAppointmentShortIds()).containsExactly(secondId.substring(0,8) + "…", firstId.substring(0,8) + "…");

    }

    @Test
    @DisplayName("shouldModifyWindowOnAppointmentsPage")
    void shouldModifyWindowOnAppointmentsPage() {
        driver.manage().window().maximize();
        driver.manage().window().fullscreen();
        driver.manage().window().minimize();
    }
}
