package br.ifsp.demo.ui.pages;

import br.ifsp.demo.ui.base.BasePageObject;
import br.ifsp.demo.ui.base.ViewportSupport;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class AppointmentPageObject extends BasePageObject {
    private final By heading = By.cssSelector(".page-head h1");
    private final By openingTypeButtons = By.cssSelector(".segmented button[role='radio']");
    private final By animalSelect = By.cssSelector(".card select");
    private final By openAppointmentButton = By.cssSelector(".grid--2 .card:first-child form button[type='submit']");
    private final By openingError = By.cssSelector(".alert--error");
    private final By appointmentIdInput = By.cssSelector("input[placeholder='UUID']");
    private final By searchButton = By.cssSelector(".form--inline button[type='submit']");
    private final By recentAppointments = By.cssSelector(".list__row");
    private final By recentEmptyMessage = By.cssSelector(".card .empty");
    private final By clearRecentButton = By.cssSelector(".card__actions button");

    private final ViewportSupport viewport;

    public AppointmentPageObject(WebDriver driver){
        super(driver);
        this.viewport = new ViewportSupport(driver);
    }

    public void openAppointmentsPage(){
        driver.get("http://localhost:5173");
    }

    public String heading(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(heading))
                .getText();
    }

    public void selectAppointment(){
        selectOpeningType("Pronto atendimento");
    }

    public void selectScheduledAppointment(){
        selectOpeningType("Com agendamento");
    }

    private void selectOpeningType(String label) {

        List<WebElement> buttons = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(openingTypeButtons));

        for (WebElement button : buttons) {
            if (button.getText().equals(label)) {
                button.click();
                return;
            }
        }
        throw new NoSuchElementException("Opening type not found: " + label);
    }

    public AppointmentDetailsPageObject clickSearchAppointment(){
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(searchButton))
                .click();

        return new AppointmentDetailsPageObject(driver);
    }

    public void enterAppointmentId(String id){
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(appointmentIdInput))
                .sendKeys(id);
    }

    public void selectNewAnimal(){
        new Select(driver.findElement(animalSelect))
                .selectByVisibleText("Novo animal");
    }

    public void selectAnimal(String visibleText){
        new Select(driver.findElement(animalSelect))
                .selectByVisibleText(visibleText);
    }

    public String selectedAnimal(){
        return new Select(driver.findElement(animalSelect))
                .getFirstSelectedOption().getText();
    }

    public void openRecentAppointment(int index){
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(recentAppointments));

        driver.findElements(recentAppointments)
                .get(index)
                .click();
    }
    public AppointmentDetailsPageObject clickOpenAppointment(){
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(openAppointmentButton))
                .click();

        return new AppointmentDetailsPageObject(driver);
    }

    public void clearRecentAppointments(){
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(clearRecentButton))
                .click();
    }

    public boolean isClearRecentButtonDisplayed(){
        return !driver.findElements(clearRecentButton).isEmpty()
                && driver.findElement(clearRecentButton).isDisplayed();
    }
    public String openingErrorMessage(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(openingError))
                .getText();
    }
    public String recentAppointmentsMessage(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(recentEmptyMessage))
                .getText();
    }

    public void selectFirstExistingAnimal(){
        new Select(driver.findElement(animalSelect)).selectByIndex(1);
    }

    public boolean isSearchButtonEnabled(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(searchButton))
                .isEnabled();
    }

    public List<String> recentAppointmentShortIds(){
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(recentAppointments));

        return driver.findElements(recentAppointments)
                .stream()
                .map(appointment -> appointment.findElement(By.cssSelector(".mono")).getText()).toList();
    }

    public void emulateViewport(int width, int height) {
        viewport.emulate(width, height);
    }

    public boolean hasHorizontalScroll() {
        return viewport.hasHorizontalScroll();
    }

    public boolean isOpenAppointmentButtonDisplayed() {
        return driver.findElement(openAppointmentButton).isDisplayed();
    }

    public boolean isSearchInputDisplayed() {
        return driver.findElement(searchButton).isDisplayed();
    }

    public boolean isSearchButtonDisplayed() {
        return driver.findElement(searchButton).isDisplayed();
    }

    public boolean isRecentCardDisplayed() {
        return driver.findElement(By.xpath("//*[self::h1 or self::h2 or self::h3][contains(normalize-space(), 'Recentes neste navegador')]")).isDisplayed();
    }
}
