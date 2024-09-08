package net.contal.demo.controllers;

import net.contal.demo.modal.BankTransaction;
import net.contal.demo.modal.CustomerAccount;
import net.contal.demo.services.BankService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;

@RestController
@RequestMapping("/banks")
public class BankController {
    final Logger logger = LoggerFactory.getLogger(BankController.class);
    final BankService dataService;

    public BankController(BankService dataService) {
        this.dataService = dataService;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/createAccount")
    public long createAccount(@RequestBody String jsonInput) {
        logger.info("Creating account with input: {}", jsonInput);

        JsonObject jsonObject = JsonParser.parseString(jsonInput).getAsJsonObject();
        String firstName = jsonObject.get("firstName").getAsString();
        String lastName = jsonObject.get("lastName").getAsString();
        double accountBalance = jsonObject.get("accountBalance").getAsDouble();

        CustomerAccount account = new CustomerAccount();
        account.setFirstName(firstName);
        account.setLastName(lastName);
        account.setAccountBalance(accountBalance);

        String accountNumber = dataService.createAnAccount(account);
        return Long.parseLong(accountNumber);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/addtransaction")
    public void addTransaction(@RequestParam("accountNumber") String accountNumber, @RequestParam("amount") Double amount) {
        logger.info("Bank Account number is :{} , Transaction Amount {}", accountNumber, amount);
        boolean success = dataService.addTransactions(Integer.parseInt(accountNumber), amount);
        if (!success) {
            logger.error("Transaction failed for account number: {}", accountNumber);
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/account")
    public String getAccountDetails(@RequestParam("accountNumber") String accountNumber) {
        logger.info("Fetching details for account number: {}", accountNumber);
        // Assume a method exists in BankService: getAccountDetails(int accountNumber)
        return dataService.getAccountDetails(Integer.parseInt(accountNumber)).toString();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/transactions")
    public List<BankTransaction> getLastTransactions(@RequestBody String jsonInput) {
        logger.info("Fetching last 10 transactions for input: {}", jsonInput);
        // Assume a method exists in BankService: getLastTransactions(int accountNumber, int limit)
        int accountNumber = extractAccountNumber(jsonInput);
        return dataService.getLastTransactions(accountNumber, 10);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/balance")
    public Double getExistingBalance(@RequestBody String jsonInput) {
        logger.info("Fetching balance for input: {}", jsonInput);
        int accountNumber = extractAccountNumber(jsonInput);
        return dataService.getBalance(accountNumber);
    }

    private int extractAccountNumber(String jsonInput) {
        JsonObject jsonObject = JsonParser.parseString(jsonInput).getAsJsonObject();
        return jsonObject.get("accountNumber").getAsInt();
    }

}
