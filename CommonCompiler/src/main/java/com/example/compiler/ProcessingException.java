package com.example.compiler;

import javax.lang.model.element.Element;

/**
 * Created by Shen YunLong on 2018/08/08.
 */
public class ProcessingException extends Exception {
    Element element;

    public ProcessingException(Element element, String msg, Object... args) {
        super(String.format(msg, args));
        this.element = element;
    }

    public Element getElement() {
        return element;
    }
}