package com.arvue.apps.easidemo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.Iterator;
import java.util.Locale;

import com.os.ec.payment.dto.AmountTransaction;
import com.os.ec.payment.dto.BalanceInformation;
import com.os.ec.payment.dto.BalanceTransaction;
import com.os.ec.payment.dto.CustomerProfile;
import com.os.ec.payment.dto.CustomerTransaction;
import com.os.ec.payment.value.CountryCode;
import com.os.ec.payment.value.Measure;
import com.os.ec.payment.value.MeasureName;
import com.os.ec.payment.value.MeasureUnitType;
import com.os.ec.payment.value.RoleType;
import com.os.ec.payment.value.TransactionHistory;
import com.os.ec.payment.value.HistoryItem;

import de.os.bss.client.*;
import de.os.bss.client.account.*;
import de.os.bss.client.customer.*;
import de.os.bss.client.payment.*;


public class BillingTest {

    /* Basic/Global settings and params */
    static private CustomerProxy customerProxy = BSSClient.createCustomerProxy();
    static private AccountProxy accountProxy = BSSClient.createAccountProxy();
    static private PaymentProxy paymentProxy = BSSClient.createPaymentProxy();

    static private String endUserId = "EC-D-DD-000815";        // Dave Developer is the BaaS enduser who manages his customers
    static private String customerNumber = "EC-U-UU-000042"; // This is a (new/existing) customer of Dave. Customer Number of customer to create or request data for - will be generated if omitted
               
    static private String name = "Unity User";                        // Name of new customer to create
    static private String login = "unity";                                // Login/ID of customer representative (will be used in other API calls for param 'onBehalfOf' together with the customer's endUserId, e.g. 'EC-U-UU-000042|unity' - customers that are not a single person can have more than one user which all have their own balances)
    static private String password = "unity123";                // Password of customer (not used/needed in the context of our demo - was originally for BaaS GUI)
    static private String email = "unity@user.com";        // Optional/not needed
    
    static public void registerUser() {
        /*************************************************************************************************************/                
        /*********************************** USER LOGS INTO SERVICE **************************************************/                
        /*************************************************************************************************************/                
        String referenceCode = "ref_reg_0000000042";        // Optional reference code (can be used for tracking in the future)
               
        //        Some personal data of the customer
        String country = "DEU";
        String postcode = "424242";
        String city = "Unity City";
        String street = "Unity Street";
        String houseNumber = "42";
        String bankName = "Billing in the Cloud Bank";
        String bankID = "123 456 789";
        String accountNumber = "0815 4242";
    
        /* registerCustomer() */
        CustomerTransaction registerCustomer = customerProxy.registerCustomer(endUserId, customerNumber, referenceCode, name, country, postcode, city, street, houseNumber, bankName, bankID, accountNumber, login, password, email);
               
        /* getCustomerProfileData() */
        CustomerProfile customerProfile = customerProxy.getCustomerProfileData(endUserId, customerNumber);
    }

    static public String getUserBalance(){
        /* getBalance() */
        String balanceId = "Main";        // Do not change this!
        String onBehalfOf = customerNumber + "|" + login;        // Do not change this!
        BalanceInformation balance = accountProxy.getBalance(endUserId, onBehalfOf , balanceId);
        String userBalance = balance.getAmount() + balance.getCurrency();
        return userBalance;
    }

    static public java.util.Collection<HistoryItem> getTransactionHistory() {
        /*************************************************************************************************************/                
        /*********************************** GET BALANCE OR HISTORY OF USER ******************************************/                
        /*************************************************************************************************************/                               
        /* getBalance() */
        String balanceId = "Main";        // Do not change this!
        String onBehalfOf = customerNumber + "|" + login;        // Do not change this!

        /* getHistory() */
        Integer maxReturnRecords = 0; // If you do not want all records to be returned limit the number of values to be returned, set maxReturnRecords > 0
        String startDate = "2013-01-01";        // A date formatted according to yyyy-MM-dd
        String endDate = "2013-01-31";                // A date formatted according to yyyy-MM-dd
        TransactionHistory transactionHistory = accountProxy.getHistory(endUserId, onBehalfOf, maxReturnRecords, startDate,endDate);
        return transactionHistory.getHistoryItems();
    }

    static public void chargeAmount() {
        /*************************************************************************************************************/                
        /*********************************** CHARGE USER FOR UPLOAD **************************************************/                
        /*************************************************************************************************************/                
        String balanceId = "Main";        // Do not change this!
        String onBehalfOf = customerNumber + "|" + login;        // Do not change this!
        String referenceCode = "ref_reg_0000000042";        // Optional reference code (can be used for tracking in the future)
       
        Double numberOfImages = 1.0;
        String code = "Image Upload";        // TO BE ADAPTED TO THE RESPECTIVE PRICE PLAN FOR YOUR SERVICE!!!
        String productID = "Advanced";        // TO BE ADAPTED TO THE RESPECTIVE PRICE PLAN OPTION FOR YOUR SERVICE!!!
        String description = "Usage of Image Upload service with option 'Advanced'";        // Text to appear on the bill/transaction history
        String serviceID = "sid123456";        // TO BE ADAPTED TO THE RESPECTIVE SERVICE YOU WANT TO BILL!!!
        String clientCorrelator = endUserId + "_" + onBehalfOf + "_" + serviceID;        // Adapt to your specific needs

        ArrayList<Measure> purchaseCategoryCode = new ArrayList<Measure>();
        Measure measure = new Measure();
        measure.setName("items");
        measure.setType(MeasureUnitType.OCCURRENCE.getUnitTypeName());
        measure.setAmount(numberOfImages);
        purchaseCategoryCode.add(measure);

        referenceCode = onBehalfOf + "_" + serviceID + "_" + "0001";        // Unique code/ID for a future tracking

        AmountTransaction amountTransaction = null;

        /* getAmount() */
        amountTransaction = paymentProxy.getAmount(endUserId, code, onBehalfOf, productID, purchaseCategoryCode, serviceID);

        /* chargeAmount() */
        amountTransaction = paymentProxy.chargeAmount(endUserId, code, onBehalfOf, productID, description, clientCorrelator,
            purchaseCategoryCode, serviceID, referenceCode);

        /*************************************************************************************************************/                
    }
}
