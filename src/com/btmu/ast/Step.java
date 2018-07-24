/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.btmu.ast;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.openqa.selenium.By;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author u000783
 */
public class Step {

    Element step;
    int frameIdx;

    public Step(Element step) {
        this.step = step;
    }

    public StepType getStepType() {
        String keyWord = step.getAttribute("type");
        if (keyWord.equals("url")) {
            return StepType.URL;
        }
        if (keyWord.equals("input")) {
            return StepType.INPUT;
        }
        if (keyWord.equals("pause")) {
            return StepType.PAUSE;
        }

        if (keyWord.equals("form")) {
            return StepType.FORM;
        }
        if (keyWord.equals("link")) {
            return StepType.LINK;
        }
        if (keyWord.equals("select")) {
            return StepType.SELECT;
        }
        if (keyWord.equals("image")) {
            return StepType.IMAGE;
        }
        if (keyWord.equals("screen-capture")) {
            return StepType.CAPTURE_SCREEN;
        }
        if (keyWord.equals("conf-capture")) {
            return StepType.CAPTURE_CONFORMATION;
        }
        if (keyWord.equals("checkbox")) {
            return StepType.CHECKBOX;
        }
        if (keyWord.equals("radio")) {
            return StepType.RADIO;
        }
        if (keyWord.equals("frame")) {
            return StepType.FRAME;
        }
        return StepType.UNKNOWN;
    }

    public int getIdx() {
        String value = step.getAttribute("idx");
        if (value != null && !value.isEmpty()) {
            return Integer.parseInt(value);
        }
        return -1;
    }
    
    public String getValue() {
        Step refStep = getReferenceStep();
        if (refStep != null) {
            return refStep.getValue();
        }

        String value = step.getAttribute("value");
        if (value != null) {
            if (value.contains("{today}")) {
                value = value.replace("{today}", new SimpleDateFormat("MM-dd-yyyy").format(new Date()));
            }
        }
        return value.replace("&amp;", "&");
    }

    public void setValue(String value) {
        step.setAttribute("value", value);
    }

    public String getId() {
        return step.getAttribute("id");
    }

    public By getBy() {
        String by = step.getAttribute("by");

        if (by != null) {
            String[] _findBy = by.split(":");
            if (_findBy[0].equals("name")) {
                return By.name(_findBy[1]);
            } else if (_findBy[0].equals("id")) {
                return By.id(_findBy[1]);
            } else if (_findBy[0].equals("text") || _findBy[0].equals("link")) {
                return By.linkText(_findBy[1]);
            } else if (_findBy[0].equals("partialtext")) {
                return By.partialLinkText(_findBy[1]);
            } else if (_findBy[0].equals("xpath")) {
                return By.xpath(_findBy[1]);
            } else if (_findBy[0].equals("css")) {
                return By.cssSelector(_findBy[1]);
            }            
            else if (_findBy[0].equals("index")) {
                frameIdx = Integer.parseInt(_findBy[1]);
            }
        }
        return null;
    }
    
    public int getFrameIndex() {
        return frameIdx;
    }

    public boolean getTrim() {
        return !"false".equalsIgnoreCase(step.getAttribute("trim"));
    }

    public boolean isReportError() {
        return !"false".equalsIgnoreCase(step.getAttribute("reporterror"));
    }

    public boolean isOptional() {
        return "true".equalsIgnoreCase(step.getAttribute("optional"));
    }
    
    public Step getReferenceStep() {
        String ref = step.getAttribute("ref");
        if (ref == null || ref.length() == 0) {
            return null;
        }

        Node parent = step.getParentNode();

        NodeList nList = parent.getChildNodes();
        for (int i = 0; i < nList.getLength(); i++) {
            Node sibling = nList.item(i);
            if (sibling.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element sib = (Element) sibling;
            if (ref.equalsIgnoreCase(sib.getAttribute("id"))) {
                return new Step(sib);
            }
        }
        return null;
    }

    public String getChopFrom() {
        return step.getAttribute("chopf");
    }

    public String getChopTo() {
        return step.getAttribute("chopt");
    }

    public boolean useOptionValue() {
        return "true".equalsIgnoreCase(step.getAttribute("optionvalue"));
    }

    public String getDescription() {
        return step.getAttribute("description");
    }

//    public boolean isPause() {
//        return "true".equalsIgnoreCase(step.getAttribute("pause"));
//    }
}
