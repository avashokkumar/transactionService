package com.valtech.poc.ts.util;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.valtech.poc.entities.AccountDetails;
import com.valtech.poc.ts.exception.AccountException;

@Service
public class ValidateAccount {

    // private static final Logger logger = LoggerFactory.getLogger(ValidateAccount.class);

    @Autowired
    private MessageUtil messageUtil;

    public Boolean validateAccount(AccountDetails accountDetails, BigDecimal amount, String type, Long accountId) {
        if (type.equals("from")) {
            if (accountDetails == null) {
                throw new AccountException(messageUtil.getMessage("sender.account.not.found", String.valueOf(accountId)));
            } else if (amount.compareTo(new BigDecimal(0)) <= 0) {
                throw new AccountException(messageUtil.getMessage("transfer.amount.greater.thanzero",
                        String.valueOf(accountDetails.getAccountNo())));
            } else if (accountDetails.getCurrentBalance().compareTo(amount) < 0) {
                throw new AccountException(messageUtil.getMessage("insufficent.funds.in.account",
                        String.valueOf(accountDetails.getAccountNo())));
            } else if (!accountDetails.getAccountStatus().equals("A")) {
                throw new AccountException(
                        messageUtil.getMessage("sender.account.is.inactive", accountDetails.getAccountStatus()));
            }
        } else {
            if (accountDetails == null) {
                throw new AccountException(messageUtil.getMessage("receiver.account.not.found", String.valueOf(accountId)));
            } else if (!accountDetails.getAccountStatus().equals("A")) {
                throw new AccountException(
                        messageUtil.getMessage("receiver.account.is.inactive", accountDetails.getAccountStatus()));
            }
        }
        return true;
    }
}
