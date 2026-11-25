package br.ifsp.demo.ui.pages;

import br.ifsp.demo.ui.base.BasePageObject;
import br.ifsp.demo.ui.base.ViewportSupport;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class RegisterPageObject extends BasePageObject {
    private final By nameInput= By.name("name");
    private final By lastNameInput = By.name("lastname");
    private final By emailInput = By.name("email");
    private final By passwordInput = By.name("password");
    private final By confirmPasswordInput = By.name("confirmPassword");
    private final By registerButton = By.cssSelector("form button[type='submit']");
    private final By fieldErrors = By.cssSelector(".field__error");
    private final By registerError = By.cssSelector(".alert--error");
    private final By loginLink = By.linkText("Entrar");

    private final ViewportSupport viewport;

    public RegisterPageObject(WebDriver driver){
        super(driver);
        this.viewport = new ViewportSupport(driver);
    }

    public void openRegisterPage(){
        driver.get("http://localhost:5173/registrar");
    }

    public void fillRegistrationForm(String name, String lastname, String email, String password, String confirmPassword){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.visibilityOfElementLocated(nameInput)).sendKeys(name);
        wait.until(ExpectedConditions.visibilityOfElementLocated(lastNameInput)).sendKeys(lastname);
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput)).sendKeys(email);
        wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput)).sendKeys(password);
        wait.until(ExpectedConditions.visibilityOfElementLocated(confirmPasswordInput)).sendKeys(confirmPassword);
    }

    public void clickRegister(){
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(registerButton))
                .click();
    }

    public void registerAccount(String name, String lastname, String email, String password, String confirmPassword){
        fillRegistrationForm(name, lastname, email, password, confirmPassword);
        clickRegister();

        new WebDriverWait(driver,Duration.ofSeconds(10))
                .until(ExpectedConditions.urlContains("/login"));
    }

    public List<String> fieldErrorMessages(){
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(fieldErrors));

        return driver.findElements(fieldErrors)
                .stream()
                .map(WebElement::getText)
                .toList();
    }

    public void submitRegistrationForm(String name, String lastname, String email, String password, String confirmPassword){
        fillRegistrationForm(name, lastname, email, password, confirmPassword);
        clickRegister();
    }

    public String registerErrorMessage(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(registerError))
                .getText();
    }

    public boolean isFormFullyVisible(){
        return driver.findElements(By.cssSelector("form input[name]"))
                .stream()
                .allMatch(WebElement::isDisplayed)
                && driver.findElement(registerButton).isDisplayed();
    }

    public void emulateViewport(int width, int height){
        viewport.emulate(width, height);
        openRegisterPage();
    }

    public boolean hasHorizontalScroll(){
        return viewport.hasHorizontalScroll();
    }

    public void navigateToLoginPage(){
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(loginLink))
                .click();
    }
}
