package br.ifsp.demo.ui.pages;

import br.ifsp.demo.ui.base.BasePageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class AppointmentDetailsPageObject extends BasePageObject {
    private final By appointmentId = By.cssSelector(".header-card h1.h-id");
    private final By appointmentOrigin = By.xpath("//dt[normalize-space()='Origem']/following-sibling::dd");
    private final By backToAppointmentsLink = By.cssSelector("a.back-link");
    private final By animalId = By.xpath("//dt[normalize-space()='Animal']/following-sibling::dd");
    private final By loadError = By.cssSelector(".alert--error > span");
    private final By cancelButton = By.xpath(  "//section[contains(@class, 'header-card')]//button[normalize-space()='Cancelar atendimento']");
    private final By cancellationReasonInput = By.cssSelector("dialog[open] textarea");
    private final By confirmCancelButton = By.cssSelector("dialog[open] button[type='submit']");
    private final By cancelledStatus = By.cssSelector(".header-card .badge--cancelado");
    private final By temperatureInput = By.name("temperaturaC");
    private final By heartRateInput = By.name("frequenciaCardiaca");
    private final By weightInput = By.name("pesoKg");
    private final By registerVitalsButton = By.cssSelector(".grid--2 .card:first-child form button[type='submit']");
    private final By vitalsSavedMessage = By.cssSelector(".grid--2 .card:first-child .alert--success");
    private final By temperatureError = By.cssSelector(".field input[name='temperaturaC'] + .field__error");
    private final By heartRateError = By.cssSelector(".field input[name='frequenciaCardiaca'] + .field__error");
    private final By weightError = By.cssSelector(".field input[name='pesoKg'] + .field__error");
    private final By complaintInput = By.name("queixaPrincipal");
    private final By historyInput = By.name("historicoClinico");
    private final By registerAnamnesisButton = By.cssSelector(".grid--2 .card:nth-child(2) form button[type='submit']");
    private final By complaintError = By.cssSelector(".field input[name='queixaPrincipal'] + .field__error");
    private final By historyError = By.cssSelector(".field textarea[name='historicoClinico'] + .field__error");
    private final By anamneseSavedMessage = By.cssSelector(".grid--2 .card:nth-child(2) .alert--success");
    private final By diagnosisCodeInput = By.name("codigo");
    private final By diagnosisDescriptionInput = By.name("descricao");
    private final By diagnosisTypeInput = By.name("tipo");
    private final By addDiagnosisButton = By.cssSelector(".form--divided button[type='submit']");
    private final By diagnosisRows = By.cssSelector(".table tbody tr");
    private final By diagnosisRequirement = By.xpath("//ul[@aria-label='Requisitos para concluir']/li[contains(., 'Ao menos um diagnóstico')]");
    private final By diagnosisCodeError = By.cssSelector(".field input[name='codigo'] + .field__error");
    private final By diagnosisDescriptionError = By.cssSelector(".field input[name='descricao'] + .field__error");
    private final By concludeAppointmentButton = By.xpath("//section[contains(@class, 'header-card')]//button[normalize-space()='Concluir atendimento']");
    private final By confirmConclusionButton = By.cssSelector("dialog[open] button[type='submit']");
    private final By completedStatus = By.cssSelector(".header-card .badge--concluido");
    private final By reopenAppointmentButton = By.xpath("//section[contains(@class, 'header-card')]//button[normalize-space()='Reabrir (até 24h)']");
    private final By reopenDialogTitle = By.xpath("//dialog[@open]//h3[normalize-space()='Reabrir atendimento?']");
    private final By confirmReopenButton = By.xpath("//dialog[@open][.//h3[normalize-space()='Reabrir atendimento?']]//button[@type='submit']");
    private final By inProgressStatus = By.cssSelector(".header-card .badge--em_andamento");

    private final By examsTab = By.id("tab-exames");
    private final By examRequestHeading = By.xpath("//h2[normalize-space()='Solicitar exames']");
    private final By requestedExamsHeading = By.xpath("//h2[normalize-space()='Exames solicitados']");
    private final By examNameInput = By.cssSelector("input[placeholder='ex.: Hemograma completo']");
    private final By examDateButton = By.cssSelector(".dtp__trigger");
    private final By examTimeSelect = By.cssSelector(".dtp__time");
    private final By nextMonthButton = By.cssSelector(".dtp__nav[aria-label='Próximo mês']");
    private final By addAnotherExamButton = By.xpath("//h2[normalize-space()='Solicitar exames']/ancestor::section[contains(@class, 'card')]//button[normalize-space()='+ Adicionar exame']");
    private final By examRequestRows = By.xpath("//h2[normalize-space()='Solicitar exames']/ancestor::section[contains(@class, 'card')]//fieldset[contains(@class, 'item-row')]");
    private final By removeExamButton = By.xpath("//fieldset[contains(@class, 'item-row')]//button[normalize-space()='Remover']");
    private final By requestExamsButton = By.xpath("//h2[normalize-space()='Solicitar exames']/ancestor::section[contains(@class, 'card')]//button[normalize-space()='Solicitar']");
    private final By requestedExamNames = By.cssSelector(".exam-item strong");
    private final By examResultInput = By.cssSelector(".exam-item .form--compact textarea");
    private final By submitExamResultButton = By.cssSelector(".exam-item .form--compact button[type='submit']");
    private final By examResultError = By.cssSelector(".exam-item .form--compact .field__error");
    private final By registeredExamResult = By.cssSelector(".exam-item__result");
    private final By examNameError =  By.cssSelector(".field input[placeholder='ex.: Hemograma completo'] + .field__error");
    private final By examDateError = By.cssSelector(".field .dtp + .field__error");
    private final By noRequestedExamsMessage =  By.cssSelector("p.empty");
    private final By clinicalInputs = By.cssSelector(
            "input[name='temperaturaC'], input[name='frequenciaCardiaca'], "
                    + "input[name='pesoKg'], input[name='queixaPrincipal'], "
                    + "textarea[name='historicoClinico']");


    private final By prescriptionsTab = By.id("tab-prescricoes");
    private final By noPrescriptionsMessage = By.cssSelector("p.empty");
    private final By prescriptionMedicationInput = By.cssSelector(".grid--item .field:nth-child(1) input");
    private final By prescriptionDosageInput = By.cssSelector(".grid--item .field:nth-child(2) input");
    private final By prescriptionRouteSelect = By.cssSelector(".grid--item .field:nth-child(3) select");
    private final By prescriptionFrequencyInput = By.cssSelector(".grid--item .field:nth-child(4) input");
    private final By prescriptionDaysInput = By.cssSelector(".grid--item .field:nth-child(5) input");
    private final By submitPrescriptionButton = By.xpath("//button[normalize-space()='Emitir prescrição']");
    private final By prescriptionMedicationNames = By.cssSelector(".rx-item strong");
    private final By prescriptionMedicationError = By.cssSelector(".grid--item .field:nth-child(1) .field__error");
    private final By prescriptionDosageError = By.cssSelector(".grid--item .field:nth-child(2) .field__error");
    private final By prescriptionItem = By.cssSelector(".rx-item");
    private final By prescriptionFrequencyError = By.cssSelector(".grid--item .field:nth-child(4) .field__error");
    private final By prescriptionDaysError =  By.cssSelector(".grid--item .field:nth-child(5) .field__error");
    private final By prescriptionItemRows = By.cssSelector("form fieldset.item-row");
    private final By addPrescriptionItemButton = By.cssSelector("form .row--between button.btn--secondary");
    private final By prescriptionItems = By.cssSelector(".rx-list .rx-item");
    private final By editItemButton = By.cssSelector("button.btn--ghost");
    private final By editingItem = By.cssSelector(".rx-item--editing");
    private final By saveItemButton =  By.xpath(".//button[normalize-space()='Salvar item']");
    private final By finalizePrescriptionButton = By.xpath("//section[contains(@class, 'card')][.//span[contains(@class, 'badge--aberta')]]" + "//button[normalize-space()='Finalizar']");
    private final By confirmFinalizePrescriptionButton = By.xpath("//dialog[@open]//button[normalize-space()='Finalizar']");
    private final By prescriptionFinalizedStatus = By.cssSelector(".badge--finalizada");
    private final By prescriptionItemActions = By.xpath(".//button[normalize-space()='Editar' or normalize-space()='Remover']");
    private final By confirmAlertPrescription = By.xpath("//dialog[@open]//h3[normalize-space()='Finalizar prescrição?']");
    private final By cancelPrescriptionButton = By.xpath("//section[contains(@class, 'card')][.//ul[contains(@class, 'rx-list')]]" + "//button[normalize-space()='Cancelar prescrição']");
    private final By prescriptionCancelledStatus = By.cssSelector(".badge--cancelada");
    private final By confirmCancelAlertPrescription = By.xpath("//dialog[@open]//h3[normalize-space()='Cancelar prescrição?']");
    private final By prescriptionItemsEmptyMessage = By.xpath("//section[contains(@class, 'card')][.//span[contains(@class, 'badge--aberta')]]" + "//p[contains(@class, 'empty')]");
    private final By noOpenPrescriptionRequirement = By.xpath("//ul[@aria-label='Requisitos para concluir']/li" + "[contains(., 'Nenhuma prescrição aberta')]");

    public AppointmentDetailsPageObject(WebDriver driver){
        super(driver);
    }

    public String appointmentId(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(appointmentId))
                .getText();
    }

    public String appointmentOrigin(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(appointmentOrigin))
                .getText();
    }

    public AppointmentPageObject backToAppointments(){
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(backToAppointmentsLink))
                .click();

        AppointmentPageObject page = new AppointmentPageObject(driver);
        page.heading();
        return page;
    }

    public String animalId(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(animalId))
                .getText();
    }

    public String loadErrorMessage(){
        return new WebDriverWait(driver,Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(loadError))
                .getText();
    }

    public void cancelAppointment(String justification){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.elementToBeClickable(cancelButton)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(cancellationReasonInput)).sendKeys(justification);
        wait.until(ExpectedConditions.elementToBeClickable(confirmCancelButton)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(cancelledStatus));
    }

    public void registerVitalSigns(String temperature, String heartRate, String weight){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.visibilityOfElementLocated(temperatureInput)).sendKeys(temperature);
        wait.until(ExpectedConditions.visibilityOfElementLocated(heartRateInput)).sendKeys(heartRate);
        wait.until(ExpectedConditions.visibilityOfElementLocated(weightInput)).sendKeys(weight);
        wait.until(ExpectedConditions.elementToBeClickable(registerVitalsButton)).click();

    }

    public String vitalsSaveMessage(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(vitalsSavedMessage)).getText();
    }

    public String temperatureErrorMessage(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(temperatureError)).getText();
    }

    public String heartRateErrorMessage(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(heartRateError)).getText();
    }

    public String weightErrorMessage(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(weightError)).getText();
    }

    public void updateVitalSigns(String temperature, String heartRate, String weight){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement previousMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(vitalsSavedMessage));

        WebElement temperatureField = wait.until(ExpectedConditions.visibilityOfElementLocated(temperatureInput));
        temperatureField.clear();
        temperatureField.sendKeys(temperature);

        WebElement heartRateField = wait.until(ExpectedConditions.visibilityOfElementLocated(heartRateInput));
        heartRateField.clear();
        heartRateField.sendKeys(heartRate);

        WebElement weightField = wait.until(ExpectedConditions.visibilityOfElementLocated(weightInput));
        weightField.clear();
        weightField.sendKeys(weight);

        wait.until(ExpectedConditions.visibilityOfElementLocated(vitalsSavedMessage));

        wait.until(ExpectedConditions.elementToBeClickable(registerVitalsButton)).click();
        wait.until(ExpectedConditions.stalenessOf(previousMessage));
        wait.until(ExpectedConditions.visibilityOfElementLocated(vitalsSavedMessage));

    }

    public String temperatureValue(){
        return driver.findElement(temperatureInput).getAttribute("value");
    }

    public String heartValue(){
        return driver.findElement(heartRateInput).getAttribute("value");
    }

    public String weightValue(){
        return driver.findElement(weightInput).getAttribute("value");
    }

    public String vitalsSubmitButtonText(){
        return driver.findElement(registerVitalsButton).getText();
    }

    public void registerAnmnesis(String complaint, String history){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.visibilityOfElementLocated(complaintInput)).sendKeys(complaint);
        wait.until(ExpectedConditions.visibilityOfElementLocated(historyInput)).sendKeys(history);

        wait.until(ExpectedConditions.elementToBeClickable(registerAnamnesisButton)).click();
    }

    public String complaintErrorMessage(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(complaintError))
                .getText();
    }

    public String historyErrorMessage(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(historyError))
                .getText();
    }

    public String anamneseSavedMessage(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(anamneseSavedMessage))
                .getText();
    }

    public void updateAnamnesis(String complaint, String history){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement previousMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(anamneseSavedMessage));

        WebElement complaintField = wait.until(ExpectedConditions.visibilityOfElementLocated(complaintInput));
        complaintField.clear();
        complaintField.sendKeys(complaint);

        WebElement historyField = wait.until(ExpectedConditions.visibilityOfElementLocated(historyInput));
        historyField.clear();
        historyField.sendKeys(history);

        wait.until(ExpectedConditions.visibilityOfElementLocated(anamneseSavedMessage));

        wait.until(ExpectedConditions.elementToBeClickable(registerAnamnesisButton)).click();
        wait.until(ExpectedConditions.stalenessOf(previousMessage));
        wait.until(ExpectedConditions.visibilityOfElementLocated(anamneseSavedMessage));

    }

    public String complaintValue(){
        return driver.findElement(complaintInput).getAttribute("value");
    }

    public String historyValue(){
        return driver.findElement(historyInput).getAttribute("value");
    }

    public String anamnesisSubmitButtonText(){
        return driver.findElement(registerAnamnesisButton).getText();
    }

    public void addDiagnosis(String code, String description, String type){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.visibilityOfElementLocated(diagnosisCodeInput)).sendKeys(code);
        wait.until(ExpectedConditions.visibilityOfElementLocated(diagnosisDescriptionInput)).sendKeys(description);
        wait.until(ExpectedConditions.visibilityOfElementLocated(diagnosisTypeInput)).sendKeys(type);
        wait.until(ExpectedConditions.elementToBeClickable(addDiagnosisButton)).click();

    }

    public boolean hasDiagnosis(String code, String description, String type){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        List<WebElement> rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(diagnosisRows));

        return rows.stream().anyMatch(row -> {
            List<WebElement> cells = row.findElements(By.tagName("td"));

            return cells.size() == 3
                    && cells.get(0).getText().equals(code)
                    && cells.get(1).getText().equals(description)
                    && cells.get(2).getText().equals(type);
        });
    }

    public boolean isDiagnosisRequirementComplete(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(diagnosisRequirement))
                .getAttribute("class")
                .contains("is-ok");
    }

    public String diagnosisCodeErrorMessage(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(diagnosisCodeError))
                .getText();
    }

    public String diagnosisDescriptionErrorMessage(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(diagnosisDescriptionError))
                .getText();
    }

    public boolean clinicalFieldAreDisaled(){
        List<WebElement> fields = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(clinicalInputs));

        return fields.stream().noneMatch(WebElement::isEnabled);
    }

    public boolean isDiagnosisFormDisplayed(){
        return driver.findElements(addDiagnosisButton)
                .stream()
                .anyMatch(WebElement::isDisplayed);
    }

    public boolean isConcludeAppointmentButtonEnabled(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(concludeAppointmentButton))
                .isEnabled();
    }

    public void concludeAppointment(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.elementToBeClickable(concludeAppointmentButton)).sendKeys(Keys.RETURN);
        wait.until(ExpectedConditions.visibilityOfElementLocated(confirmConclusionButton));
        wait.until(ExpectedConditions.elementToBeClickable(confirmConclusionButton)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(completedStatus));
    }

    public boolean isAppointmentConcluded(){
        return !driver.findElements(completedStatus).isEmpty();
    }

    public void reopenAppointment(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.elementToBeClickable(reopenAppointmentButton))
                .sendKeys(Keys.RETURN);

        wait.until(ExpectedConditions.visibilityOfElementLocated(reopenDialogTitle));

        wait.until(ExpectedConditions.elementToBeClickable(confirmReopenButton))
                .click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(inProgressStatus));
    }

    public boolean isAppointmentInProgress(){
        return !driver.findElements(inProgressStatus).isEmpty();
    }

    //---------Exames--------

    public void openExamsTab(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.elementToBeClickable(examsTab)).click();
        wait.until(ExpectedConditions.attributeToBe(examsTab, "aria-selected", "true"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(requestedExamsHeading));
    }

    public boolean isExamRequestFormDisplayed(){
        return driver.findElements(examRequestHeading)
                .stream()
                .anyMatch(WebElement::isDisplayed);
    }

    public void fillExamRequestAt(int index, String name, LocalDate examDate, String time){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        List<WebElement> rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(examRequestRows));
        WebElement row = rows.get(index);

        row.findElement(examNameInput).sendKeys(name);
        row.findElement(examDateButton).click();

        if (examDate.getMonth() != LocalDate.now().getMonth()) {
            wait.until(ExpectedConditions.elementToBeClickable(nextMonthButton)).click();
        }

        String dateLabel = examDate.format(
                DateTimeFormatter.ofPattern(
                        "EEEE, d 'de' MMMM 'de' yyyy",
                        Locale.forLanguageTag("pt-BR")
                )
        );

        By examDay = By.xpath("//button[@aria-label='" + dateLabel + "']");
        wait.until(ExpectedConditions.elementToBeClickable(examDay)).click();

        rows = wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(examRequestRows)
        );
        row = rows.get(index);

        if(time !=null && !time.isBlank()){
            new Select(row.findElement(examTimeSelect))
                    .selectByVisibleText(time);
        }
    }

    public String examNameValue(){
        return driver.findElement(examNameInput).getAttribute("value");
    }

    public String examDateValue(){
        return driver.findElement(examDateButton)
                .findElement(By.cssSelector("span:first-child"))
                .getText();
    }

    public String examTimeValue(){
        return new Select(driver.findElement(examTimeSelect))
                .getFirstSelectedOption()
                .getText();
    }

    public void addAnotherExam() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.elementToBeClickable(addAnotherExamButton)).click();
        wait.until(ExpectedConditions.numberOfElementsToBe(examRequestRows, 2));
    }

    public int examRequestRowCount() {
        return driver.findElements(examRequestRows).size();
    }

    public boolean isRemoveExamButtonDisplayed() {
        return driver.findElements(removeExamButton)
                .stream()
                .anyMatch(WebElement::isDisplayed);
    }

    public  void submitExamRequest(){
        new WebDriverWait(driver,Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(requestExamsButton))
                        .click();
    }

    public List<String> requestedExamNames(){
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(requestedExamNames));

        return driver.findElements(requestedExamNames)
                .stream()
                .map(WebElement::getText)
                .toList();
    }

    public void submitExamResult(String result){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.visibilityOfElementLocated(examResultInput)).sendKeys(result);

        wait.until(ExpectedConditions.elementToBeClickable(submitExamResultButton)).click();
    }

    public String examResultErrorMessage(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(examResultError))
                .getText();
    }

    public String registeredExamResult(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(registeredExamResult))
                .getText();
    }


    public void removeExamRequestAt(int index){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        List<WebElement> rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(examRequestRows));

        int remainingRows = rows.size() -1;

        rows.get(index)
                .findElement(By.xpath(".//button[normalize-space()='Remover']"))
                .click();

        wait.until(ExpectedConditions.numberOfElementsToBe(examRequestRows,remainingRows));
    }

    public String examNameErrorMessage(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(examNameError))
                .getText();
    }

    public void enterExamNameAt(int index, String name){
        List<WebElement> rows = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(examRequestRows));

        rows.get(index)
                .findElement(examNameInput)
                .sendKeys(name);
    }

    public String examDateErrorMessage(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(examDateError))
                .getText();
    }

    public void selectExamTimeAt(int index, String time){
        List<WebElement> rows = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(examRequestRows));

        rows.get(index)
                .findElement(examTimeSelect)
                .sendKeys(time);
    }

    public boolean isExamDateSelectable(int index, LocalDate date){
        List<WebElement> rows = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(examRequestRows));

        rows.get(index).findElement(examDateButton).click();

        String dateLabel = date.format(
                DateTimeFormatter.ofPattern(
                        "EEEe, d 'de' MMMM 'de' yyyy",
                        Locale.forLanguageTag("pt-BR")
                )
        );

        By dateButton = By.xpath(
                "//div[@role='dialog']//button[@aria-label='\" + dateLabel + \"']"
        );

        return driver.findElements(dateButton).stream()
                .anyMatch(WebElement::isEnabled);
    }

    public boolean isExamResultFormDisplayed(){
        return driver.findElements(submitExamResultButton)
                .stream()
                .anyMatch(WebElement::isDisplayed);
    }

    public String noRequestedExamsMessage() {
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(noRequestedExamsMessage))
                .getText();
    }


    //---------Prescrições--------

    public void openPrescriptionTab(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.elementToBeClickable(prescriptionsTab)).sendKeys(Keys.RETURN);
        wait.until(ExpectedConditions.attributeToBe(prescriptionsTab, "aria-selected", "true"));
    }

    public String noPrescriptionsMessage(){
        return new WebDriverWait(driver,Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(noPrescriptionsMessage))
                .getText();
    }

    public void fillPrescriptionItem(int index, String medication, String dosage, String route, String frequency, String days){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(prescriptionItemRows, index));

        driver.findElements(prescriptionItemRows)
                .get(index)
                .findElement(prescriptionMedicationInput)
                .sendKeys(medication);

        driver.findElements(prescriptionItemRows)
                .get(index)
                .findElement(prescriptionDosageInput)
                .sendKeys(dosage);

        new Select(driver.findElements(prescriptionItemRows)
                .get(index).
                findElement(prescriptionRouteSelect))
                .selectByVisibleText(route);

        driver.findElements(prescriptionItemRows)
                .get(index)
                .findElement(prescriptionFrequencyInput)
                .sendKeys(frequency);

        driver.findElements(prescriptionItemRows)
                .get(index)
                .findElement(prescriptionDaysInput)
                .sendKeys(days);

    }

    public void submitPrescription(){
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(submitPrescriptionButton))
                .click();
    }

    public List<String> prescriptionMedicationNames(){
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(prescriptionMedicationNames));

        return driver.findElements(prescriptionMedicationNames)
                .stream()
                .map(WebElement::getText)
                .toList();
    }

    public String prescriptionMedicationErrorMessage(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(prescriptionMedicationError))
                .getText();

    }

    public String prescriptionDosageErrorMessage(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(prescriptionDosageError))
                .getText();
    }

    public String prescriptionItemText(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(prescriptionItem))
                .getText();
    }

    public String prescriptionFrequencyErrorMessage(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(prescriptionFrequencyError))
                .getText();
    }

    public String prescriptionDaysErrorMessage(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(prescriptionDaysError))
                .getText();
    }

    public void addAnotherPrescriptionItem(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        int currentRows = driver.findElements(prescriptionItemRows).size();

        wait.until(ExpectedConditions.elementToBeClickable(addPrescriptionItemButton))
                .click();

        wait.until(ExpectedConditions.numberOfElementsToBe(prescriptionItemRows, currentRows + 1));
    }

    public void removePrescriptionRequestAt(int index){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        List<WebElement> rows = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(prescriptionItemRows));

        int remainingRows = rows.size() -1;

        rows.get(index)
                .findElement(By.xpath(".//button[normalize-space()='Remover item']"))
                .click();

        wait.until(ExpectedConditions.numberOfElementsToBe(prescriptionItemRows,remainingRows));
    }

    public void editPrescriptionItemAt(
            int index,
            String medication,
            String dosage,
            String route,
            String frequency,
            String days
    ){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        List<WebElement> items = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(prescriptionItems));

        items.get(index).findElement(editItemButton).click();

        WebElement itemBeingEdited = wait.until(ExpectedConditions.visibilityOfElementLocated(editingItem));

        WebElement medicationField = itemBeingEdited.findElement(prescriptionMedicationInput);
        medicationField.clear();
        medicationField.sendKeys(medication);

        WebElement dosageField = itemBeingEdited.findElement(prescriptionDosageInput);
        dosageField.clear();
        dosageField.sendKeys(dosage);

        new Select(itemBeingEdited.findElement(prescriptionRouteSelect))
                .selectByVisibleText(route);

        WebElement frequencyField = itemBeingEdited.findElement(prescriptionFrequencyInput);
        frequencyField.clear();
        frequencyField.sendKeys(frequency);

        WebElement daysField = itemBeingEdited.findElement(prescriptionDaysInput);
        daysField.clear();
        daysField.sendKeys(days);

        wait.until(ExpectedConditions.elementToBeClickable(saveItemButton)).click();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(editingItem));
    }

    public String prescriptionItemTextAt(int index){
        List<WebElement> items = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(prescriptionItems));

        return items.get(index).getText();
    }

    public void removeIssuedPrescriptionItemAt(int index){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        List<WebElement> items = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(prescriptionItems));

        int remainingItems = items.size() -1;

        items.get(index)
                .findElement(By.xpath(".//button[normalize-space()='Remover']"))
                .click();

        wait.until(ExpectedConditions.numberOfElementsToBe(
                prescriptionItems,
                remainingItems
        ));
    }

    public void finalizeIssuedPrescription(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.elementToBeClickable(finalizePrescriptionButton))
                .click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(confirmAlertPrescription));

        wait.until(ExpectedConditions.elementToBeClickable(confirmFinalizePrescriptionButton))
                .click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(prescriptionFinalizedStatus));

    }

    public boolean hasPrescriptionItemActionsAt(int index){
        List<WebElement> items = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(prescriptionItems));

        return !items.get(index).findElements(prescriptionItemActions).isEmpty();
    }

    public void cancelIssuedPrescription(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.elementToBeClickable(cancelPrescriptionButton))
                .click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(confirmCancelAlertPrescription));

        wait.until(ExpectedConditions.elementToBeClickable(confirmCancelButton))
                        .click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(prescriptionCancelledStatus));
    }

    public boolean isPrescriptionCancelled(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(prescriptionCancelledStatus))
                .isDisplayed();
    }

    public String prescriptionItemsEmptyMessage(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(prescriptionItemsEmptyMessage))
                .getText();
    }

    public boolean isPrescriptionFinalizeButtonEnabled(){
        return new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(
                        finalizePrescriptionButton
                ))
                .isEnabled();
    }

    public boolean isNoOpenPrescriptionRequirementComplete(){
        return new  WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.visibilityOfElementLocated(
                        noOpenPrescriptionRequirement
                ))
                .getAttribute("class")
                .contains("is-ok");
    }

    public void waitForPrescriptionMedicationCount(int expectedCount) {
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.numberOfElementsToBe(
                        prescriptionMedicationNames,
                        expectedCount
                ));
    }

    public boolean isPrescriptionFormDisplayed() {
        return driver.findElements(submitPrescriptionButton)
                .stream()
                .anyMatch(WebElement::isDisplayed);
    }
}


