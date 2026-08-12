package model;

import java.util.ArrayList;
import java.util.List;
public class Dashboard {
	
	private List<BudgetStatus> budgetStatuses = new ArrayList<>();
    private double totalIncome;
    private double totalExpense;
    private double balance;
    private int totalCategories;

    public double getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(double totalIncome) {
        this.totalIncome = totalIncome;
    }

    public double getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(double totalExpense) {
        this.totalExpense = totalExpense;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public int getTotalCategories() {
        return totalCategories;
    }

    public void setTotalCategories(int totalCategories) {
        this.totalCategories = totalCategories;
    }
    
    public List<BudgetStatus> getBudgetStatuses() {
        return budgetStatuses;
    }

    public void setBudgetStatuses(List<BudgetStatus> budgetStatuses) {
        this.budgetStatuses = budgetStatuses;
    }
}