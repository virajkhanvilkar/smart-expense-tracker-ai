package model;

public class BudgetStatus {

    private String categoryName;
    private double budgetAmount;
    private double spent;
    private double remaining;

    public BudgetStatus() {
    }

    public BudgetStatus(String categoryName, double budgetAmount,
                        double spent, double remaining) {
        this.categoryName = categoryName;
        this.budgetAmount = budgetAmount;
        this.spent = spent;
        this.remaining = remaining;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(double budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public double getSpent() {
        return spent;
    }

    public void setSpent(double spent) {
        this.spent = spent;
    }

    public double getRemaining() {
        return remaining;
    }

    public void setRemaining(double remaining) {
        this.remaining = remaining;
    }
}