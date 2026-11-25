package br.ifsp.demo.ui.base;

import br.ifsp.demo.ui.pages.AppointmentDetailsPageObject;
import br.ifsp.demo.ui.pages.AppointmentPageObject;
import br.ifsp.demo.ui.pages.LoginPageObject;
import br.ifsp.demo.ui.pages.RegisterPageObject;
import com.github.javafaker.Faker;

public class BaseAppointmentDetailsUITest extends BaseSeleniumTest {
    protected AppointmentDetailsPageObject appointmentDetailsPage;
    protected final Faker faker = new Faker();

    @Override
    protected void setInitialPage(){
        String name = faker.name().firstName();
        String lastname = faker.name().lastName();
        String email = faker.internet().emailAddress();
        String password = faker.internet().password(6,10);

        RegisterPageObject registerPage = new RegisterPageObject(driver);
        registerPage.openRegisterPage();
        registerPage.registerAccount(name, lastname, email, password, password);

        LoginPageObject loginPage = new LoginPageObject(driver);
        loginPage.authenticate(email, password);

        AppointmentPageObject appointmentsPage = new AppointmentPageObject(driver);
        appointmentsPage.selectAppointment();
        appointmentsPage.selectNewAnimal();
        appointmentDetailsPage = appointmentsPage.clickOpenAppointment();
    }
}
