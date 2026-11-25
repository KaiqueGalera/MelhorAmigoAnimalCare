package br.ifsp.demo.ui.tests;

import br.ifsp.demo.ui.base.BaseSeleniumTest;
import br.ifsp.demo.ui.base.ViewportsProvider;
import br.ifsp.demo.ui.pages.AppointmentPageObject;
import br.ifsp.demo.ui.pages.LoginPageObject;
import br.ifsp.demo.ui.pages.RegisterPageObject;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@Tag("UITest")
public class LoginUiTest extends BaseSeleniumTest {
    private LoginPageObject loginPage;

    @Override
    protected void setInitialPage(){
        loginPage = new LoginPageObject(driver);
        loginPage.openLoginPage();
    }

    @Test
    @DisplayName("shouldShowRequiredMessagesWhenSubmittingEmptyForm")
    void shouldShowRequiredMessagesWhenSubmittingEmptyForm() {
        loginPage.clickLogin();
        assertThat(loginPage.fieldErrorMessages()).contains("Informe o e-mail.", "Informe a senha.");

    }

    @Test
    @DisplayName("shouldMaskPassword")
    void shouldMaskPassword() {
        assertThat(loginPage.passwordFieldType()).isEqualTo("password");
    }

    @Test
    @DisplayName("shouldRequireEmail")
    void shouldRequireEmail() {
        loginPage.enterPassword("teste");
        loginPage.clickLogin();

        assertThat(loginPage.fieldErrorMessages()).containsExactly("Informe o e-mail.");

    }

    @Test
    @DisplayName("shouldRequirePassword")
    void shouldRequirePassword() {
        loginPage.enterEmail("teste");
        loginPage.clickLogin();

        assertThat(loginPage.fieldErrorMessages()).containsExactly("Informe a senha.");

    }

    @Test
    @DisplayName("shouldRejectWhitespaceOnlyValues")
    void shouldRejectWhitespaceOnlyValues() {
        loginPage.authenticate(" ", " ");

        assertThat(loginPage.fieldErrorMessages())
                .contains("Informe o e-mail.", "Informe a senha.");
    }

    @Test
    @DisplayName("shouldOpenAppointmentPageValidCredentials")
    void shouldOpenAppointmentPageValidCredentials() {
        Faker faker = new Faker();
        String name = faker.name().firstName();
        String lastname = faker.name().lastName();
        String email = faker.internet().emailAddress();
        String password = faker.internet().password(6,10);

        RegisterPageObject registerPage = new RegisterPageObject(driver);
        registerPage.openRegisterPage();
        registerPage.registerAccount(name, lastname, email, password, password);

        loginPage.authenticate(email, password);

        AppointmentPageObject appointmentPage = new AppointmentPageObject(driver);

        assertThat(appointmentPage.heading()).isEqualTo("Atendimentos");

    }

    @Test
    @DisplayName("shouldNavigateToRegistrationPage")
    void shouldNavigateToRegistrationPage() {
        loginPage.navigateToRegistrationPage();

        assertThat(loginPage.currentUrl()).endsWith("/registrar");

    }

    @Test
    @DisplayName("shouldModifyWindowOnLoginPage")
    void shouldModifyWindowOnLoginPage() {

        driver.manage().window().maximize();
        driver.manage().window().fullscreen();
        driver.manage().window().minimize();
    }


    @ParameterizedTest(name = "[{index}] {0} ({1}x{2})")
    @DisplayName("shouldDisplayAllLoginElementsInDifferentViewports")
    @ArgumentsSource(ViewportsProvider.class)
    void shouldDisplayAllLoginElementsInDifferentViewports(String device, int width, int height) {
        loginPage.emulateViewport(width, height);

        assertThat(loginPage.isEmailFieldDisplayed()).isTrue();
        assertThat(loginPage.isPasswordFieldDisplayed()).isTrue();
        assertThat(loginPage.isLoginButtonDisplayed()).isTrue();
        assertThat(loginPage.isRegisterLinkDisplayed()).isTrue();
    }


    @ParameterizedTest(name = "[{index}] {0} ({1}x{2})")
    @DisplayName("shouldNotHaveHorizontalScrollInDifferentViewports")
    @ArgumentsSource(ViewportsProvider.class)
    void shouldNotHaveHorizontalScrollInDifferentViewports(String device, int width, int height) {
        loginPage.emulateViewport(width, height);

        assertThat(loginPage.isFormFullyVisible()).isTrue();
        assertThat(loginPage.hasHorizontalScroll())
                .as("Scroll horizontal em %s (%dx%d)", device, width, height)
                .isFalse();
    }

    @ParameterizedTest(name = "[{index}] {0} ({1}x{2})")
    @DisplayName("shouldShowRequiredMessagesOnDifferentViewports")
    @ArgumentsSource(ViewportsProvider.class)
    void shouldShowRequiredMessagesOnDifferentViewports(String device, int width, int height) {
        loginPage.emulateViewport(width, height);

        loginPage.clickLogin();
        assertThat(loginPage.fieldErrorMessages())
                .contains("Informe o e-mail.", "Informe a senha.");
    }

    @ParameterizedTest
    @DisplayName("shouldKeepPasswordMaskedOnDifferentsViewports")
    @ArgumentsSource(ViewportsProvider.class)
    void shouldKeepPasswordMaskedOnDifferentsViewports(String device, int width, int height) {
        loginPage.emulateViewport(width, height);

        assertThat(loginPage.passwordFieldType()).isEqualTo("password");
    }

    @Test
    @DisplayName("shouldKeepFormUsableWhenRotatingMobileDevice")
    void shouldKeepFormUsableWhenRotatingMobileDevice() {
        loginPage.emulateViewport(375, 667);

        assertThat(loginPage.isEmailFieldDisplayed()).isTrue();
        assertThat(loginPage.isPasswordFieldDisplayed()).isTrue();
        assertThat(loginPage.isLoginButtonDisplayed()).isTrue();
        assertThat(loginPage.hasHorizontalScroll()).isFalse();

        loginPage.emulateViewport(667, 375);

        assertThat(loginPage.isEmailFieldDisplayed()).isTrue();
        assertThat(loginPage.isPasswordFieldDisplayed()).isTrue();
        assertThat(loginPage.isLoginButtonDisplayed()).isTrue();
        assertThat(loginPage.hasHorizontalScroll()).isFalse();
    }
}
