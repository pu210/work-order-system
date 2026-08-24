package com.eeit219.work_order_system.modules.b.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkOrderCreateRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    // ---------- 標題：必填，0/50 ----------

    @ParameterizedTest
    @ValueSource(ints = {0, 51})
    void title_isInvalid_whenBlankOrOverMaxLength(int length) {
        WorkOrderCreateRequest request = validRequest();
        request.setTitle("字".repeat(length));

        assertHasViolationOn(request, "title");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 49, 50})
    void title_isValid_withinLength(int length) {
        WorkOrderCreateRequest request = validRequest();
        request.setTitle("字".repeat(length));

        assertNoViolationOn(request, "title");
    }

    @Test
    void title_isInvalid_whenOnlyWhitespace() {
        WorkOrderCreateRequest request = validRequest();
        request.setTitle("   ");

        assertHasViolationOn(request, "title");
    }

    // ---------- 位置：必填，0/100 ----------

    @ParameterizedTest
    @ValueSource(ints = {0, 101})
    void locationDetail_isInvalid_whenBlankOrOverMaxLength(int length) {
        WorkOrderCreateRequest request = validRequest();
        request.setLocationDetail("字".repeat(length));

        assertHasViolationOn(request, "locationDetail");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 100})
    void locationDetail_isValid_withinLength(int length) {
        WorkOrderCreateRequest request = validRequest();
        request.setLocationDetail("字".repeat(length));

        assertNoViolationOn(request, "locationDetail");
    }

    // ---------- 描述：選填，0/300 ----------

    @Test
    void description_isValid_whenBlank() {
        WorkOrderCreateRequest request = validRequest();
        request.setDescription("");

        assertNoViolationOn(request, "description");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 300})
    void description_isValid_withinLength(int length) {
        WorkOrderCreateRequest request = validRequest();
        request.setDescription("字".repeat(length));

        assertNoViolationOn(request, "description");
    }

    @Test
    void description_isInvalid_whenOverMaxLength() {
        WorkOrderCreateRequest request = validRequest();
        request.setDescription("字".repeat(301));

        assertHasViolationOn(request, "description");
    }

    // ---------- 聯絡電話：選填，須 10 碼數字 ----------

    @Test
    void contactPhone_isValid_whenNull() {
        WorkOrderCreateRequest request = validRequest();
        request.setContactPhone(null);

        assertNoViolationOn(request, "contactPhone");
    }

    // 這條在記錄一個容易誤解的行為：@Pattern 只有 null 會自動放行，空字串不會，
    // 這裡不是「應該通過」而是「目前實際上不會通過」，故意寫下來避免之後被誤改壞或誤以為是 bug
    @Test
    void contactPhone_isInvalid_whenEmptyString() {
        WorkOrderCreateRequest request = validRequest();
        request.setContactPhone("");

        assertHasViolationOn(request, "contactPhone");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0912345678", "0900000000"})
    void contactPhone_isValid_whenExactlyTenDigits(String phone) {
        WorkOrderCreateRequest request = validRequest();
        request.setContactPhone(phone);

        assertNoViolationOn(request, "contactPhone");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "091234567",      // 少於 10 碼
            "09123456789",    // 多於 10 碼
            "0912-345678",    // 含連字號
            "0912 345678",    // 含空格
            "091234567a"      // 含英文字母
    })
    void contactPhone_isInvalid_whenNotExactlyTenDigits(String phone) {
        WorkOrderCreateRequest request = validRequest();
        request.setContactPhone(phone);

        assertHasViolationOn(request, "contactPhone");
    }

    // ---------- 細項類別：必填 ----------

    @Test
    void subCategoryId_isInvalid_whenNull() {
        WorkOrderCreateRequest request = validRequest();
        request.setSubCategoryId(null);

        assertHasViolationOn(request, "subCategoryId");
    }

    // ---------- helper ----------

    private WorkOrderCreateRequest validRequest() {
        WorkOrderCreateRequest request = new WorkOrderCreateRequest();
        request.setTitle("冷氣故障");
        request.setSubCategoryId(1);
        request.setLocationDetail("A棟301教室");
        request.setContactPhone(null);
        request.setDescription(null);
        return request;
    }

    private void assertHasViolationOn(WorkOrderCreateRequest request, String propertyName) {
        Set<ConstraintViolation<WorkOrderCreateRequest>> violations = validator.validate(request);
        boolean hasViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(propertyName));
        assertTrue(hasViolation, "預期 " + propertyName + " 應該違規，但實際沒有：" + violations);
    }

    private void assertNoViolationOn(WorkOrderCreateRequest request, String propertyName) {
        Set<ConstraintViolation<WorkOrderCreateRequest>> violations = validator.validate(request);
        boolean hasViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(propertyName));
        assertFalse(hasViolation, "預期 " + propertyName + " 應該通過，但實際違規：" + violations);
    }
}
