package com.eeit219.work_order_system.modules.c.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCheckAcceptRequest(
        @NotBlank(message = "故障原因不可為空")
        @Size(max = 100, message = "故障原因不可超過 100 字")
        String failureCause,

        @NotBlank(message = "處理方式不可為空")
        @Size(max = 150, message = "處理方式不可超過 150 字")
        String repairAction,

        @Size(max = 80, message = "更換零件不可超過 80 字")
        String replacedParts,

        @NotBlank(message = "測試結果不可為空")
        @Size(max = 100, message = "測試結果不可超過 100 字")
        String testResult) {

    private static final int FEEDBACK_MAX_LENGTH = 500;

    public AdminCheckAcceptRequest {
        failureCause = trimToNull(failureCause);
        repairAction = trimToNull(repairAction);
        replacedParts = trimToNull(replacedParts);
        testResult = trimToNull(testResult);
    }

    @AssertTrue(message = "歸檔內容合併後不可超過 500 字")
    public boolean isFeedbackWithinLimit() {
        return toFeedback().length() <= FEEDBACK_MAX_LENGTH;
    }

    public String toFeedback() {
        return String.join("\n",
                "故障原因：" + valueOrEmpty(failureCause),
                "處理方式：" + valueOrEmpty(repairAction),
                "更換零件：" + (replacedParts == null ? "無" : replacedParts),
                "測試結果：" + valueOrEmpty(testResult));
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
