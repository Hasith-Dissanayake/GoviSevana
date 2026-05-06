package lk.javainstitute.govisevana.model;

public class BankDetailsModel {

    private String bankName;
    private String branchName;
    private String accountNumber;
    private String accountHolderName;

    public BankDetailsModel() {}

    public BankDetailsModel(String bankName, String branchName, String accountNumber, String accountHolderName) {
        this.bankName = bankName;
        this.branchName = branchName;
        this.accountNumber = accountNumber;
        this.accountHolderName = accountHolderName;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }
}
