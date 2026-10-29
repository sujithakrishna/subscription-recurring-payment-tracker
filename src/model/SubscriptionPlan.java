package model;

public class SubscriptionPlan {

    private int planId;
    private String planName;
    private String providerName;
    private String category;
    private String description;
    private String status;

    public SubscriptionPlan() {
    }

    public SubscriptionPlan(
            int planId,
            String planName,
            String providerName,
            String category,
            String description,
            String status) {

        this.planId = planId;
        this.planName = planName;
        this.providerName = providerName;
        this.category = category;
        this.description = description;
        this.status = status;
    }

    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}