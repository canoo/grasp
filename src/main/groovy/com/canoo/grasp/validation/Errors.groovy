package com.canoo.grasp.validation

/**
 * todo: replace with Spring Errors when it is part of our project. 
 */
public interface Errors {

    List getAllErrors()
    int getErrorCount()

//    void addAllErrors(Errors errors)
//    FieldError getFieldError()
//    FieldError getFieldError(String field)
//    int getFieldErrorCount()
//    int getFieldErrorCount(String field)
//    List<FieldError> getFieldErrors()
//    List<FieldError> getFieldErrors(String field)
//    Class getFieldType(String field)
//    Object getFieldValue(String field)
//    ObjectError getGlobalError()
//    int getGlobalErrorCount()
//    List<ObjectError> getGlobalErrors()
//    String getNestedPath()
//    String getObjectName()
//    boolean hasErrors()
//    boolean hasFieldErrors()
//    boolean hasFieldErrors(String field)
//    boolean hasGlobalErrors()
//    void popNestedPath()
//    void pushNestedPath(String subPath)
//    void reject(String errorCode)
//    void reject(String errorCode, Object[] errorArgs, String defaultMessage)
//    void reject(String errorCode, String defaultMessage)
//    void rejectValue(String field, String errorCode)
//    void rejectValue(String field, String errorCode, Object[] errorArgs, String defaultMessage)
//    void rejectValue(String field, String errorCode, String defaultMessage)
//    void setNestedPath(String nestedPath)
}