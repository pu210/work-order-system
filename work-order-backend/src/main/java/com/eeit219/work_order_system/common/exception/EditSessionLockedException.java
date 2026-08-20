package com.eeit219.work_order_system.common.exception;

public class EditSessionLockedException extends RuntimeException {

    public EditSessionLockedException(String editorName) {
        super("此工單目前由「" + editorName + "」編輯中");
    }
}