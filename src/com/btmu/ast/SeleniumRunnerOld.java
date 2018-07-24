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
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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

public class SeleniumRunnerOld {

    public final int TESTCASE = 1001;
    public final int STEP = 1002;
    public final int CASE = 1003;
    public final int FORM = 1004;
    public final int LINK = 1005;
    public final int INPUT = 1006;
    public final int URL = 1007;
    public final int SELECT = 1008;
    public final int IMAGE = 1009;
    public final int CAPTURE_SCREEN = 1010;
    public final int CAPTURE_CONFORMATION = 1011;
    public final int PAUSE = 1012;
    public final int CHECKBOX = 1013;
    public final int RADIO = 1014;

    String artifactPath;
    WebDriver driver;

    String testFileName = "";
    Element rootElement;

    public final String TODAY;

    public boolean testcaseFailed = false;

    public SeleniumRunnerOld() throws FileNotFoundException {

        // setup artifact path
        Date d = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd-HH.mm");
        artifactPath = "artifact/" + df.format(d);
        new File(artifactPath).mkdirs();

//        new File(logPath).mkdirs();
        //driver = new FirefoxDriver();
        //driver = new InternetExplorerDriver();
        TODAY = new SimpleDateFormat("MM/dd/yyyy").format(d);
    }

    public void startTest() {
        driver = new InternetExplorerDriver();
    }

    public void endTest() {
        driver.quit();
    }

    public boolean isTestcaseFailed() {
        return testcaseFailed;
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

    public Element parseXML(File xmlFile) throws ParserConfigurationException, SAXException, IOException {
        this.testFileName = xmlFile.getName();

        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new File("xsd/TestCase.xsd"));
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(xmlFile));

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        rootElement = doc.getDocumentElement();
        return rootElement;
    }

    @SuppressWarnings("SleepWhileInLoop")
    public void blink(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        js.executeScript("arguments[0].focus()", element);

        for (int i = 0; i < 30; i++) {
            js.executeScript("arguments[0].setAttribute('style','border: solid 2px')", element);
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(SeleniumRunnerOld.class.getName()).log(Level.SEVERE, null, ex);
            }
            js.executeScript("arguments[0].setAttribute('style','border: solid 2px red')", element);
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
        }
    }

    public int decodeKeyWord(String keyWord) {
        if (keyWord.equals("step")) {
            return STEP;
        }
        if (keyWord.equals("url")) {
            return URL;
        }
        if (keyWord.equals("input")) {
            return INPUT;
        }
        if (keyWord.equals("pause")) {
            return PAUSE;
        }
        if (keyWord.equals("form")) {
            return FORM;
        }
        if (keyWord.equals("link")) {
            return LINK;
        }
        if (keyWord.equals("select")) {
            return SELECT;
        }
        if (keyWord.equals("image")) {
            return IMAGE;
        }
        if (keyWord.equals("screen-capture")) {
            return CAPTURE_SCREEN;
        }
        if (keyWord.equals("conf-capture")) {
            return CAPTURE_CONFORMATION;
        }
        if (keyWord.equals("case")) {
            return CASE;
        }
        if (keyWord.equals("testcase")) {
            return TESTCASE;
        }
        if (keyWord.equals("checkbox")) {
            return CHECKBOX;
        }
        if (keyWord.equals("radio")) {
            return RADIO;
        }

        return 0;
    }

    public void executeTest(Element testcase, boolean retry) {
        
    }
    
    public void runTest(Element testcases, int numOfPasses) throws IOException,
            TransformerException,
            InterruptedException {
        WebDriverWait waitCondition = new WebDriverWait(driver, 30);
        testcases.setAttribute("startTime", (new Date()).toString());
        for (int k = 0; k < numOfPasses; k++) {
            NodeList cases = testcases.getChildNodes();
            for (int i = 0; i < cases.getLength(); i++) {
            
                Node tcase = cases.item(i);
                if (tcase.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                NamedNodeMap caseAtt = tcase.getAttributes();
                String caseId = caseAtt.getNamedItem("id").getNodeValue();
                Node _status = caseAtt.getNamedItem("status");

                // If status equals to passed that means test case was previouly 
                //  completed, skip to next test case.
                if (_status != null) {
                    if (_status.getNodeValue().equals("passed") || _status.getNodeValue().equals("skip")) {
                        continue;
                    }
                }

                Element fe = (Element) tcase;

                NodeList steps = tcase.getChildNodes();
                int stepCounter = 0;

                // setup conditional value.  we'll not log it as error if conditional is true.
                boolean conditional = false;
                boolean lookforoptionkey;
                try {
                    fe.setAttribute("startTime", (new Date()).toString());
                    for (int j = 0; j < steps.getLength(); j++) {

                        Node node = steps.item(j);

                        if (STEP != decodeKeyWord(node.getNodeName())) {
                            continue;
                        }
                        stepCounter++;
                        //System.out.println(fe.getAttribute("id") + " step-" + stepCounter);

                        Element step = (Element) node;

                        NamedNodeMap att = step.getAttributes();

                        conditional = false;
                        Node _conditional = att.getNamedItem("conditional");
                        if (_conditional != null) {
                            conditional = _conditional.getNodeValue().equalsIgnoreCase("true");
                        }

                        lookforoptionkey = false;
                        Node _lookforoptionkey = att.getNamedItem("lookforoptionkey");
                        if (_lookforoptionkey != null) {
                            lookforoptionkey = _lookforoptionkey.getNodeValue().equalsIgnoreCase("true");
                        }

                        By by = null;
                        Node _by = att.getNamedItem("by");
                        if (_by != null) {
                            String[] _findBy = _by.getNodeValue().split(":");
                            if (_findBy[0].equals("name")) {
                                by = By.name(_findBy[1]);
                            } else if (_findBy[0].equals("id")) {
                                by = By.id(_findBy[1]);
                            } else if (_findBy[0].equals("text")) {
                                by = By.linkText(_findBy[1]);
                            } else if (_findBy[0].equals("partialtext")) {
                                by = By.partialLinkText(_findBy[1]);
                            } else if (_findBy[0].equals("xpath")) {
                                by = By.xpath(_findBy[1]);
                            } else {
                                throw new Exception("Undefined by type '" + _findBy[0] + "'");
                            }
                        }

                        Node _value = att.getNamedItem("value");
                        String value = "";
                        if (_value != null) {
                            value = _value.getNodeValue();
                            if (value.contains("{today}")) {
                                value = value.replace("{today}", TODAY);
                            }
                        }

                        String _type = att.getNamedItem("type").getNodeValue();
                        int type = decodeKeyWord(_type);

                        WebElement targetElement = null;
                        if (by != null) {
                            if (type == RADIO || type == CHECKBOX) {

//                            } else if (type != FORM) {
//                                targetElement = waitCondition.until(ExpectedConditions.elementToBeClickable(by));
                            } else {
                                targetElement = driver.findElement(by);
                            }
                        }

                        Node _ref = att.getNamedItem("ref");
                        if (_ref != null) {
                            String ref[] = _ref.getNodeValue().split(":");
                            Element refElement = getReferenceElement(testcases, ref[0]);
                            value = refElement.getAttribute(ref[1]);
                            System.out.println(ref[0] + ":" + value);
                        }

                        switch (type) {
                            case LINK:
                            case IMAGE:
                                targetElement.click();
                                break;
                            case SELECT:
                                Select select = new Select(targetElement);
                                for (String v : value.split(",")) {
                                    if (lookforoptionkey) {
                                        select.selectByValue(value);
                                    } else {
                                        select.selectByVisibleText(v);
                                    }
                                }
                                break;
                            case CHECKBOX:
                                List<WebElement> targetElementsCheckBox = driver.findElements(by);
                                String[] values = value.split(",");
                                for (WebElement we : targetElementsCheckBox) {
                                    for (String v : values) {
                                        if (we.getAttribute("value").equals(v)) {
                                            we.click();
                                        }
                                    }
                                }
                                break;
                            case RADIO:
                                List<WebElement> targetElementsRadio = driver.findElements(by);
                                for (WebElement we : targetElementsRadio) {
                                    if (we.isDisplayed() && we.getAttribute("value").equals(value)) {
                                        we.click();
                                    }
                                }
                                break;
                            case INPUT:
                                targetElement.sendKeys(value);
                                break;
                            case CAPTURE_CONFORMATION:
                                String chop_from = step.getAttribute("chop_from");
                                String chop_to = step.getAttribute("chop_to");
                                String trim = step.getAttribute("trim");

                                String conf_value = targetElement.getText();

                                if (chop_from != null && chop_to != null) {
                                    int idx1 = conf_value.indexOf(chop_from);
                                    int idx2 = conf_value.indexOf(chop_to);
                                    conf_value = conf_value.substring(idx1 + chop_from.length(), idx2);
                                }
                                if (trim != null && trim.equalsIgnoreCase("true")) {
                                    conf_value = conf_value.trim();
                                }
                                step.setAttribute("confirmationId", conf_value);
                                break;
                            case CAPTURE_SCREEN:
                                File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                                File tmp = new File(artifactPath + "/" + caseId + "." + stepCounter + ".png");
                                copyfile(screenshot, tmp);
                                break;
                            case URL:
                                driver.get(value);
                                break;
                            case FORM:
                                targetElement.submit();
                                break;
                            case PAUSE:
                                Node _hl = att.getNamedItem("highlight");
                                if (_hl != null) {
                                    String[] highlight = _hl.getNodeValue().split(":");
                                    blink(driver.findElement(By.name(highlight[1])));
                                }
                                Thread.sleep(Integer.parseInt(value) * 1000);
                                break;
                            default:
                        }
                    }

                    fe.setAttribute("endTime", (new Date()).toString());
                    fe.setAttribute("status", "passed");
                    if (fe.getAttribute("err") != null && fe.getAttribute("err").length() > 0) {
                        fe.setAttribute("err", "");
                    }
                } catch (Exception e) {
                    if (!conditional) {
                        testcaseFailed = true;
                        if (k == numOfPasses - 1) {
                            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                            File tmp = new File(artifactPath + "/" + caseId + "." + stepCounter + ".error.png");
                            copyfile(screenshot, tmp);
                            fe.setAttribute("status", "failed");
                            fe.setAttribute("err", e.getMessage());
                            e.printStackTrace(System.out);
                        }
                    }
                }
            }
            if (!testcaseFailed) {
                break;
            }
        }

        testcases.setAttribute("endTime", (new Date()).toString());
        Source source = new DOMSource(testcases);

        File file = new File(artifactPath + "/" + getOutputName());
        Result result = new StreamResult(file);

        Transformer xformer = TransformerFactory.newInstance().newTransformer();

        xformer.transform(source, result);
    }

    public String getOutputName() {
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

    public static void main(String[] args) throws Exception {

        String testfile = "TestCase-Payments.xml";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-t")) {
                testfile = args[i + 1];
            }
        }

        SeleniumRunnerOld selenium = new SeleniumRunnerOld();
        Element testcases = selenium.parseXML(new File(testfile));
        selenium.startTest();
        selenium.runTest(testcases, 1);
        selenium.endTest();
    }
}
