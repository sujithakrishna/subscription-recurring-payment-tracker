package model;

public class PaymentMethod {

    private int paymentMethodId;
    private int userId;
    private String methodType;
    private String methodName;
    private String maskedDetails;
    private boolean isDefault;
    private String status;

    public PaymentMethod() {
    }

    public PaymentMethod(
            int paymentMethodId,
            int userId,
            String methodType,
            String methodName,
            String maskedDetails,
            boolean isDefault,
            String status) {

        this.paymentMethodId = paymentMethodId;
        this.userId = userId;
        this.methodType = methodType;
        this.methodName = methodName;
        this.maskedDetails = maskedDetails;
        this.isDefault = isDefault;
        this.status = status;
    }

    public int getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(int paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getMethodType() {
        return methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMaskedDetails() {
        return maskedDetails;
    }

    public void setMaskedDetails(String maskedDetails) {
        this.maskedDetails = maskedDetails;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}