package net.contal.demo.services;

import net.contal.demo.AccountNumberUtil;
import net.contal.demo.DbUtils;
import net.contal.demo.modal.BankTransaction;
import net.contal.demo.modal.CustomerAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * TODO use BankServiceTest class
 */
@Service
@Transactional
public class BankService {

    //USE this class to access database , you can call openASession to access database
    private final DbUtils dbUtils;
    @Autowired
    public BankService(DbUtils dbUtils) {
        this.dbUtils = dbUtils;
    }


    /**
     * @param customerAccount firstName, lastName already provided
     * @return accountNumber
     */
    public String createAnAccount(CustomerAccount customerAccount){
        int accountNumber = AccountNumberUtil.generateAccountNumber();
        customerAccount.setAccountNumber(accountNumber);

        customerAccount.setAccountBalance(0.0);

        dbUtils.openASession().saveOrUpdate(customerAccount);

        return String.valueOf(accountNumber);
    }


    /**
     * @param accountNumber target account number
     * @param amount amount to register as transaction
     * @return boolean , if added as transaction
     */
    public boolean addTransactions(int accountNumber , Double amount){

        if (amount == null){
            return false;
        }

        String hql = "FROM CustomerAccount WHERE accountNumber = :accountNumber";
        List<CustomerAccount> accounts = dbUtils.openASession()
                .createQuery(hql, CustomerAccount.class)
                .setParameter("accountNumber", accountNumber)
                .getResultList();

        if (accounts.isEmpty()) {
            return false;
        }

        CustomerAccount account = accounts.get(0);

        BankTransaction transaction = new BankTransaction();
        transaction.setCustomerAccount(account);
        transaction.setTransactionAmount(amount);
        transaction.setTransactionDate(new Date());

        // Saves the transaction
        dbUtils.openASession().saveOrUpdate(transaction);

        return true;
    }


    /**
     * @param accountNumber target account
     * @return account balance
     */
    public double getBalance(int accountNumber){

        String hql = "FROM CustomerAccount WHERE accoutNumber = :accountNumber";

        List<CustomerAccount> accounts = dbUtils.openASession()
                .createQuery(hql, CustomerAccount.class)
                .setParameter("accountNumber", accountNumber)
                .getResultList();
        if(accounts.isEmpty()){
            return 0d;
        }

        CustomerAccount account =  accounts.get(0);
        double totalBalance = 0.0;

        for (BankTransaction transaction: account.getTransactions()){
            totalBalance += transaction.getTransactionAmount();
        }
        return totalBalance;
    }


    /**
     * @param accountNumber accountNumber
     * @return HashMap [key: date , value: double]
     */
    public Map<Date,Double> getDateBalance(int accountNumber){
        Map<Date, Double> dateBalanceMap = new HashMap<>();
        String hql = "FROM BankTransaction WHERE customerAccount.accountNumber = :accountNumber ORDER BY transactionDate";
        List<BankTransaction> transactions = dbUtils.openASession()
                .createQuery(hql, BankTransaction.class)
                .setParameter("accountNumber", accountNumber)
                .getResultList();

        double cumulativeBalance = 0.0;
        for (BankTransaction transaction: transactions) {
            cumulativeBalance += transaction.getTransactionAmount();

            Date transactionDate = transaction.getTransactionDate();
            dateBalanceMap.put(transactionDate, cumulativeBalance);
        }
        return dateBalanceMap;

    }

    public Map<Date, Double> getCumulativeBalanceByTransactionDate(String jsonInput) {
        Map<Date, Double> dateBalanceMap = new HashMap<>();
        double cumulativeBalance = 0.0;

        try {
            JsonObject jsonObject = JsonParser.parseString(jsonInput).getAsJsonObject();
            int accountNumber = jsonObject.get("accountNumber").getAsInt();

            String hql = "FROM BankTransaction WHERE customerAccount.accountNumber = :accountNumber ORDER BY transactionDate";
            List<BankTransaction> transactions = dbUtils.openASession()
                    .createQuery(hql, BankTransaction.class)
                    .setParameter("accountNumber", accountNumber)
                    .getResultList();

            for (BankTransaction transaction : transactions) {
                cumulativeBalance += transaction.getTransactionAmount();
                Date transactionDate = transaction.getTransactionDate();
                dateBalanceMap.put(transactionDate, cumulativeBalance);
            }
        } catch (Exception e) {
            // e.printStackTrace(); // Log the exception using the app Logger
        }

        return dateBalanceMap;
    }

    public List<BankTransaction> getLastTransactions(int accountNumber, int maxTransactions) {
        String hql = "FROM BankTransaction WHERE customerAccount.accountNumber = :accountNumber ORDER BY transactionDate DESC";
        List<BankTransaction> transactions = dbUtils.openASession()
                .createQuery(hql, BankTransaction.class)
                .setParameter("accountNumber", accountNumber)
                .setMaxResults(10)
                .getResultList();

        return transactions;
    }

    public CustomerAccount getAccountDetails(int accountNumber) {
        String hql = "FROM CustomerAccount WHERE accountNumber = :accountNumber";
        List<CustomerAccount> accounts = dbUtils.openASession()
                .createQuery(hql, CustomerAccount.class)
                .setParameter("accountNumber", accountNumber)
                .getResultList();

        if (accounts.isEmpty()) {
            return null;
        }

        return accounts.get(0);
    }




}
