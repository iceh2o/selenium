package com.btmu.ast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SeleniumRunner {

    public final int TESTCASE = 1001;
    public final int STEP = 1002;
    public final int CASE = 1003;

    String artifactPath;
    WebDriver driver;

    Element rootElement;

    Properties config;

    public final String TODAY;

    public SeleniumRunner() throws FileNotFoundException, IOException {
        this(null);
    }

    public SeleniumRunner(String testfile) throws FileNotFoundException, IOException {
        config = new Properties();
        config.load(new FileInputStream("config/selenium.properties"));

        if (testfile != null) {
            config.setProperty("test_file", testfile);
        }

        // setup artifact path
        Date d = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd-HH.mm");
        artifactPath = config.getProperty("artifact_path") + df.format(d);
        new File(artifactPath).mkdirs();

        TODAY = new SimpleDateFormat("MM/dd/yyyy").format(d);
    }

    public void startTest() {
        //driver = new FirefoxDriver();
        //driver = new InternetExplorerDriver();
        String browser_driver = config.getProperty("browser_driver", "ChromeDriver");
        if (browser_driver.equalsIgnoreCase("InternetExplorerDriver")) {
            driver = new InternetExplorerDriver();
        } else if (browser_driver.equalsIgnoreCase("FirefoxDriver")) {
            driver = new FirefoxDriver();
        } else if (browser_driver.equalsIgnoreCase("ChromeDriver")) {
            String headless = config.getProperty("headless", "false");
            if ("true".equalsIgnoreCase(headless)) {
                ChromeOptions options = new ChromeOptions();
                options.addArguments("headless");
                options.addArguments("window-size=1200x800");
                driver = new ChromeDriver(options);
            } else {
                driver = new ChromeDriver();
            }
        } else if (browser_driver.equalsIgnoreCase("HtmlUnitDriver")) {            
            HtmlUnitDriver hud = new HtmlUnitDriver(true);
            //hud.setProxy("proxy2.btmna.com", 8080);
            driver = hud;
        } else {
            System.out.println("Unsupported Browser Driver: " + browser_driver);
        }
    }

    public void endTest() {
        driver.quit();
    }

    public void copyfile(File f1, File f2) throws FileNotFoundException, IOException {
        InputStream inStream = new FileInputStream(f1);
        OutputStream outStream = new FileOutputStream(f2);

        byte[] buffer = new byte[1024];

        int length;
        while ((length = inStream.read(buffer)) > 0) {
            outStream.write(buffer, 0, length);
        }
        inStream.close();
        outStream.close();
    }

    public Element parseXML() throws ParserConfigurationException, SAXException, IOException {
        File xmlFile = new File(config.getProperty("test_file"));
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new File(config.getProperty("xsd")));
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(xmlFile));

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        rootElement = doc.getDocumentElement();
        return rootElement;
    }

    public void highLight(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        js.executeScript("arguments[0].focus()", element);
        js.executeScript("arguments[0].setAttribute('style','border: solid 2px red')", element);

    }

    public void printNode(Node node) {
        System.out.print("<");
        System.out.print(node.getNodeName() + " ");
        NamedNodeMap nnp = node.getAttributes();
        for (int j = 0; j < nnp.getLength(); j++) {
            System.out.print(nnp.item(j).getNodeName() + "=\"" + nnp.item(j).getNodeValue() + "\" ");
        }
        System.out.println("/>");
    }

    private void executeTestCase(Element tcase, WebDriverWait waitCondition) throws NoSuchElementException, TimeoutException {
        String caseId = tcase.getAttribute("id");

        NodeList steps = tcase.getChildNodes();
        int stepCounter = 0;
        boolean failureRetry = true;
        for (int i = 0; i < steps.getLength(); i++) {
            Node node = steps.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            printNode(node);
            stepCounter++;
            Step step = new Step((Element) node);
            StepType type = step.getStepType();

            WebElement targetElement = null;
            By by = step.getBy();
            System.out.println("SteP: ::: " + step.getStepType());
            try {
                if (by != null) {
                    if (type == StepType.RADIO || type == StepType.CHECKBOX) {
                    } else if (type != StepType.FORM) {
                        targetElement = waitCondition.until(ExpectedConditions.elementToBeClickable(by));
                    } else {
                        targetElement = driver.findElement(by);
                    }
                }

//                if (step.isPause()) {
//                    highLight(targetElement);
//                    try {
//                        Thread.sleep(Integer.parseInt(config.getProperty("pause_time")) * 1000);
//                    } catch (InterruptedException ex) {
//                    }
//                }
                switch (type) {
                    case LINK:
                    case IMAGE:
                        targetElement.click();
                        break;
                    case SELECT:
                        Select select = new Select(targetElement);
                        for (String v : step.getValue().split(",")) {
                            if (step.useOptionValue()) {
                                select.selectByValue(v);
                            } else {
                                select.selectByVisibleText(v);
                            }
                        }
                        break;
                    case CHECKBOX:
                        List<WebElement> targetElementsCheckBox = driver.findElements(by);
                        int index = 0;
                        for (WebElement we : targetElementsCheckBox) {
                            if (step.getIdx() > -1 && step.getIdx() == index) {
                                we.click();
                                break;
                            } else {
                                for (String v : step.getValue().split(",")) {
                                    if (we.getAttribute("value").equals(v)) {
                                        we.click();
                                    }
                                }
                            }
                            index++;
                        }
                        break;
                    case RADIO:
                        List<WebElement> targetElementsRadio = driver.findElements(by);
                        for (WebElement we : targetElementsRadio) {
                            if (we.isDisplayed() && we.getAttribute("value").equals(step.getValue())) {
                                we.click();
                            }
                        }
                        break;
                    case INPUT:
                        targetElement.clear();
                        targetElement.sendKeys(step.getValue());
                        break;
                    case CAPTURE_CONFORMATION:
                        String chop_from = step.getChopFrom();
                        String chop_to = step.getChopTo();

                        String conf_value = targetElement.getText();

                        if (chop_from != null && chop_to != null) {
                            int idx1 = conf_value.indexOf(chop_from);
                            int idx2 = conf_value.indexOf(chop_to);
                            conf_value = conf_value.substring(idx1 + chop_from.length(), idx2);
                        }
                        if (step.getTrim()) {
                            conf_value = conf_value.trim();
                        }
                        step.setValue(conf_value);
                        System.out.println("capture_confirmation : " + conf_value);
                        break;
                    case CAPTURE_SCREEN:
                        captureScreenshot("/" + caseId + "." + stepCounter + ".png");
                        break;
                    case URL:
                        driver.get(step.getValue());
                        System.out.println(driver.getPageSource());
                        break;
                    case FORM:
                        targetElement.submit();
                        break;
                    case PAUSE: {
                        try {
                            highLight(targetElement);
                            Thread.sleep(Integer.parseInt(config.getProperty("pause_time")) * 1000);
                        } catch (InterruptedException ex) {
                        }
                        break;
                    }
                    case FRAME:
                        driver.switchTo().frame(step.getFrameIndex());
                        break;

                    default:
                }
                // this step is completed with error, reset failureRetry for next step
                failureRetry = true;
            } catch (TimeoutException te) {
                if (step.isOptional()) {
                    System.out.println("Step is optional, error is ignored");
                    continue;
                }
                if (failureRetry) {
                    System.out.println("TimeoutException detected on element " + step.getBy().toString());
                    // page is probably not finish loading, retry one more time
                    failureRetry = false;
                    // retry current step
                    i = i - 1;
                    continue;
                }
                if (step.isReportError()) {
                    // already did one retry, going to exit this test case with an error
                    captureScreenshot("/" + caseId + "." + stepCounter + "-error.png");
                    throw te;
                }
            } catch (NoSuchElementException nsee) {
                if (step.isOptional()) {
                    System.out.println("Step is optional, error is ignored");
                    continue;
                }
                if (failureRetry) {
                    // not able to find element, let's try redo previous step.
                    System.out.println("NoSuchElementException detected on element " + step.getBy().toString());
                    failureRetry = false;
                    if (i - 2 >= -1) {
                        // retry previous step
                        i = i - 2;
                    } else {
                        // retry current step
                        i = i - 1;
                    }
                    System.out.println("failure detected: " + caseId + ":" + stepCounter);
                    continue;
                }
                if (step.isReportError()) {
                    // already did one retry, going to exit this test case with an error
                    captureScreenshot("/" + caseId + "." + stepCounter + "-error.png");
                    throw nsee;
                }
            } catch (NoSuchFrameException nsfe) {
                nsfe.printStackTrace();
                if (step.isOptional()) {
                    continue;
                }
            }
        }
    }

    public void runTest(Element testcases) throws IOException,
            TransformerException,
            InterruptedException {

        WebDriverWait waitCondition = new WebDriverWait(driver, Integer.parseInt(config.getProperty("wait_time", "10")));
        testcases.setAttribute("startTime", (new Date()).toString());

        NodeList cases = testcases.getChildNodes();

        try {
            int numOfRetry = Integer.parseInt(config.getProperty("retry", "0")) + 1;
            do {
                for (int i = 0; i < cases.getLength(); i++) {

                    Node node = cases.item(i);
                    if (node.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }
                    Element tcase = (Element) node;

                    // If status equals to passed that means test case was previouly 
                    //  completed, skip to next test case.
                    String status = tcase.getAttribute("status");
                    if (status != null) {
                        if (status.equals("passed") || status.equals("skip")) {
                            continue;
                        }
                    }

                    //NodeList steps = tcase.getChildNodes();
                    try {
                        tcase.setAttribute("startTime", (new Date()).toString());
                        executeTestCase(tcase, waitCondition);
                        tcase.setAttribute("endTime", (new Date()).toString());
                        tcase.setAttribute("status", "passed");
                        tcase.removeAttribute("err");
                    } catch (NoSuchElementException | TimeoutException e) {
                        tcase.setAttribute("status", "failed");
                        tcase.setAttribute("err", e.getMessage());
                        e.printStackTrace(System.out);
                    }
                }
                numOfRetry--;
            } while (numOfRetry > 0);
        } catch (NoSuchWindowException nswe) {
            testcases.setAttribute("err", nswe.getMessage());
            nswe.printStackTrace(System.out);
        }
        testcases.setAttribute("endTime", (new Date()).toString());
        Source source = new DOMSource(testcases);

        File file = new File(artifactPath + "/" + getTestFileOutputName());
        Result result = new StreamResult(file);

        Transformer xformer = TransformerFactory.newInstance().newTransformer();

        xformer.transform(source, result);
    }

    public void captureScreenshot(String filename) {
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File tmp = new File(artifactPath + "/" + filename);
        try {
            copyfile(screenshot, tmp);
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        }
    }

    public String getTestFileOutputName() {
        String testFileName = config.getProperty("test_file");
        int idx = testFileName.lastIndexOf('.');
        String outputName = testFileName.substring(0, idx);
        outputName += "-output";
        outputName = outputName + testFileName.substring(idx, testFileName.length());
        return outputName;
    }

    public Element getReferenceElement(Element e, String id) {
        NodeList cn = e.getChildNodes();
        Element result;

        for (int i = 0; i < cn.getLength(); i++) {
            Node n = cn.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element ele = (Element) n;
            if (n.hasChildNodes()) {
                result = getReferenceElement(ele, id);
                if (result != null) {
                    return result;
                }
            } else {
                if (ele.getAttribute("id") != null && ele.getAttribute("id").equals(id)) {
                    return ele;
                }
            }
        }

        return null;
    }

    public static void main(String[] args) throws IOException {

        String testfile = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-t")) {
                testfile = args[i + 1];
            }
        }

        SeleniumRunner selenium = new SeleniumRunner(testfile);
        try {
            selenium.startTest();
            selenium.runTest(selenium.parseXML());
        } catch (IOException | TransformerException | InterruptedException | ParserConfigurationException | SAXException ex) {
            ex.printStackTrace(System.out);
        } finally {
            selenium.endTest();
        }
    }
}
