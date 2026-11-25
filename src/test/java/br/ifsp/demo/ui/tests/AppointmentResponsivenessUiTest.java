package br.ifsp.demo.ui.tests;
import br.ifsp.demo.ui.base.BaseSeleniumTest;
import br.ifsp.demo.ui.base.ViewportsProvider;
import br.ifsp.demo.ui.pages.AppointmentPageObject;
import br.ifsp.demo.ui.pages.LoginPageObject;
import br.ifsp.demo.ui.pages.RegisterPageObject;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Tag("UITest")
public class AppointmentResponsivenessUiTest extends BaseSeleniumTest {

    private AppointmentPageObject appointmentPage;

    @Override
    protected void setInitialPage() {
        Faker faker = new Faker();

        String name = faker.name().firstName();
        String lastname = faker.name().lastName();
        String email = faker.internet().emailAddress();
        String password = faker.internet().password(6, 10);

        RegisterPageObject registerPage = new RegisterPageObject(driver);
        registerPage.openRegisterPage();
        registerPage.registerAccount(name, lastname, email, password, password);

        LoginPageObject loginPage = new LoginPageObject(driver);
        loginPage.authenticate(email, password);

        appointmentPage = new AppointmentPageObject(driver);
    }

    @ParameterizedTest(name = "[{index}] {0} ({1}x{2})")
    @DisplayName("shouldDisplayMainElementsInDifferentViewports")
    @ArgumentsSource(ViewportsProvider.class)
    void shouldDisplayMainElementsInDifferentViewports(String device, int width, int height) {
        appointmentPage.emulateViewport(width, height);

        assertThat(appointmentPage.heading()).isEqualTo("Atendimentos");
        assertThat(appointmentPage.isOpenAppointmentButtonDisplayed()).isTrue();
        assertThat(appointmentPage.isSearchInputDisplayed()).isTrue();
        assertThat(appointmentPage.isSearchButtonDisplayed()).isTrue();
        assertThat(appointmentPage.isRecentCardDisplayed()).isTrue();
    }

    @ParameterizedTest(name = "[{index}] {0} ({1}x{2})")
    @ArgumentsSource(ViewportsProvider.class)
    @DisplayName("shouldNotHaveHorizontalScrollInDifferentViewports")
    void shouldNotHaveHorizontalScrollInDifferentViewports(String device, int width, int height) {
        appointmentPage.emulateViewport(width, height);
        assertThat(appointmentPage.hasHorizontalScroll()).isFalse();
    }
}
