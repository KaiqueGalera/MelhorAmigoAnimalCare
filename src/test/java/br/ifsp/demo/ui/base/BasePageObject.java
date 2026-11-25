package br.ifsp.demo.ui.base;

import org.openqa.selenium.WebDriver;

public class BasePageObject {
    protected final WebDriver driver;

    public BasePageObject(WebDriver driver){
        this.driver = driver;
    }

    public String currentUrl(){
        return driver.getCurrentUrl();
    }
}
