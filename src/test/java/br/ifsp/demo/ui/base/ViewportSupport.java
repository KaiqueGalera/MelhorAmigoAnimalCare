package br.ifsp.demo.ui.base;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.Map;

public class ViewportSupport {


    private final WebDriver driver;

    public ViewportSupport(WebDriver driver) {
        this.driver = driver;
    }

    public void emulate(int width, int height){
        ((ChromeDriver) driver).executeCdpCommand("Emulation.setDeviceMetricsOverride", Map.of(
                "width", width,
                "height", height,
                "deviceScaleFactor", 1,
                "mobile", width < 500));
    }

    public boolean hasHorizontalScroll(){
        Object result = ((JavascriptExecutor) driver).executeScript("return document.documentElement.scrollWidth > document.documentElement.clientWidth;");
        return Boolean.TRUE.equals(result);
    }

    public boolean isInsideViewport(WebElement element) {
        return (Boolean) ((JavascriptExecutor) driver).executeScript(
                "const r = arguments[0].getBoundingClientRect();" +
                        "return r.left >= 0 && r.right <= document.documentElement.clientWidth;",
                element);
    }

}
