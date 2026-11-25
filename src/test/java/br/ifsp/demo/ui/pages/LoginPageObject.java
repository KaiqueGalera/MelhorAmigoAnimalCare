package br.ifsp.demo.ui.pages;

import br.ifsp.demo.ui.base.BasePageObject;
import br.ifsp.demo.ui.base.ViewportSupport;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class LoginPageObject extends BasePageObject {
    private final By emailInput = By.name("email");
    private final By passwordInput = By.name("password");
    private final By loginButton = By.cssSelector("form button[type='submit']");
    private final By fieldErrors = By.cssSelector(".field__error");
    private final By loginError = By.cssSelector(".alert--error");
    private final By registerLink = By.linkText("Cadastre-se");
    private final By successMessage = By.cssSelector(".alert--success");

    private final ViewportSupport viewport;

    public LoginPageObject(WebDriver driver){
        super(driver);
        this.viewport = new ViewportSupport(driver);
    }

    public void openLoginPage(){
        driver.get("http://localhost:5173/login");
    }

    public void authenticate(String email, String password){
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(emailInput))
                .sendKeys(email);

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(passwordInput))
                .sendKeys(password);

        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(loginButton))
                .click();

    }

    public void clickLogin(){
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(loginButton))
                .click();
    }

    public List<String> fieldErrorMessages(){
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(fieldErrors));

        return driver.findElements(fieldErrors)
                .stream()
                .map(WebElement::getText)
                .toList();
    }

    public String loginErrorMessage(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(loginError))
                .getText();
    }

    public void navigateToRegistrationPage(){
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(registerLink))
                .click();
    }

    public String passwordFieldType(){
        return driver.findElement(passwordInput).getAttribute("type");
    }

    public void enterPassword(String password){
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(passwordInput))
                .sendKeys(password);
    }

    public void enterEmail(String email){
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(emailInput))
                .sendKeys(email);
    }

    public String successMessage(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(successMessage))
                .getText();
    }

    public boolean isEmailFieldDisplayed() {
        return driver.findElement(emailInput).isDisplayed();
    }

    public boolean isPasswordFieldDisplayed() {
        return driver.findElement(passwordInput).isDisplayed();
    }

    public boolean isLoginButtonDisplayed() {
        return driver.findElement(loginButton).isDisplayed();
    }

    public boolean isRegisterLinkDisplayed() {
        return driver.findElement(registerLink).isDisplayed();
    }

    public void emulateViewport(int width, int height) {
        viewport.emulate(width, height);
        openLoginPage();
    }

    public boolean isFormFullyVisible(){
        return driver.findElements(By.cssSelector("form input[name]"))
                .stream()
                .allMatch(WebElement::isDisplayed)
                && driver.findElement(registerLink).isDisplayed();
    }

    public boolean hasHorizontalScroll(){
        return viewport.hasHorizontalScroll();
    }
}
