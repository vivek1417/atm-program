package com.cash;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class ATM
{
    private static Scanner sc = new Scanner(System.in);

    private static final Map<String, AtomicInteger> currencyNotesMap = new LinkedHashMap<String, AtomicInteger>();

    /**
     * Add denominations if any new notes to introduce in future
     */
    private static Integer[] denominations = new Integer[]
    {
            10, 5, 1, 20
    };
    static
    {
        //Sorted by descending order
        Arrays.sort(denominations, Collections.reverseOrder());
        for (int i = 0; i < denominations.length; i++)
        {
            currencyNotesMap.put(denominations[i] + "s", new AtomicInteger(0));
        }
    }

    public static void main(String[] args)
    {
        Account current = new Account();
        current.setType("Current");
        current.setBalance(0.00);
        current.setRate(0.00);
        boolean session = true;
        int depositCounter = 1;
        int withdrawCounter = 1;
        while (session)
        {
            System.out.println("Please select \n1 for Deposit \n" + "2 for withdraw\n" + "3 for Exit");
            int selection = sc.nextInt();

            switch (selection)
            {
                case 1:
                    System.out.print(String.format("Deposit %d: ", depositCounter));
                    String denomination = sc.next();
                    deposit(denomination, current);
                    depositCounter++;
                    break;
                case 2:
                    System.out.print(String.format("Withdraw %d: ", withdrawCounter));
                    int enteredAmount = sc.nextInt();
                    withdraw(enteredAmount, current);
                    withdrawCounter++;
                    break;

                case 3:
                    session = false;
                    break;
            }
        }

    }

    /**
     * Withdraw the amount from current Account
     *
     * @param enteredAmount The withdraw amount to dispense
     * @param current The Current Account
     */
    private static void withdraw(int enteredAmount, Account current)
    {
        if (!checkWithdrawAmount(enteredAmount, current))
        {
            current.withdraw(enteredAmount);
            countCurrency(enteredAmount, denominations);
            display(current);
        }

    }

    /**
     * Deposit the amount for each denominations
     *
     * @param depositDenomination
     * @param current Account
     */
    private static void deposit(String depositDenomination, Account current)
    {
        if (!validateDenominations(depositDenomination))
        {
            current.deposit(calculateTheDepositAmountUsingDenominations());
            display(current);
        }

    }

    /**
     * Calculate the deposit amount using denomination notes and its value
     *
     * @return depositAmount
     */
    private static double calculateTheDepositAmountUsingDenominations()
    {
        int depositAmount = 0;
        int counter = 0;
        for (Entry<String, AtomicInteger> entries : currencyNotesMap.entrySet())
        {
            depositAmount = depositAmount + (denominations[counter] * entries.getValue().intValue());
            counter++;
        }
        return depositAmount;
    }

    /**
     * Print the current balance along with denominations
     *
     * @param current the Account Object
     */
    private static void display(Account current)
    {
        int counter = 0;
        int totalAmount = 0;
        final StringBuilder builder = new StringBuilder();
        for (Entry<String, AtomicInteger> entries : currencyNotesMap.entrySet())
        {
            builder.append(entries.getKey()).append("=").append(entries.getValue());
            String val = currencyNotesMap.size() == counter ? "" : ", ";
            builder.append(val);
            totalAmount = totalAmount + (denominations[counter] * entries.getValue().intValue());
            counter++;
        }
        builder.append("Total").append("=").append(current.getBalance());
        System.out.println(String.format("Balance: %s", builder.toString()));
    }

    /**
     * Validate the deposit denominations
     *
     * @param denominations The Currency notes and its no of notes
     * @return true / false if its valid denominations
     */
    private static boolean validateDenominations(String denominations)
    {
        final String[] currencyNotes = denominations.split(",");
        final AtomicInteger currencyCounter = new AtomicInteger(0);
        boolean isValid = false;
        for (int i = 0; i < currencyNotes.length; i++)
        {
            final String[] values = currencyNotes[i].split(":");
            int currencyNotesCount = Integer.valueOf(values[1].trim());
            if (currencyNotesCount == 0)
            {
                currencyCounter.addAndGet(1);
                isValid = true;
            }
            else if (currencyNotesCount < 0)
            {
                System.out.println("Output: Incorrect deposit amount");
                isValid = true;
            }
            else
            {
                AtomicInteger notes = currencyNotesMap.get(values[0]);
                notes.getAndAdd(currencyNotesCount);
                isValid = false;
            }

        }
        if (currencyCounter.intValue() == currencyNotes.length)
        {
            System.out.println("Output: Deposit amount cannot be zero");
            currencyNotesMap.clear();
            isValid = true;
        }

        return isValid;
    }

    /**
     *  Validate withdraw amount if negative, zero or current balance
     *
     * @param enteredAmount
     * @param current
     * @return false if withdraw amount is invalid
     */
    private static boolean checkWithdrawAmount(int enteredAmount, Account current)
    {
        if (enteredAmount <= 0 || enteredAmount > current.getBalance())
        {
            System.out.println("Output: Incorrect or insuffiecient funds");
            return true;
        }
        return false;
    }

    private static void countCurrency(int amount, Integer[] denominations)
    {
        Map<String, Integer> dispenseNotesMap = new LinkedHashMap<String, Integer>();
        for (Integer currencyNote : denominations)
        {
            AtomicInteger availableCurrencyNotes = currencyNotesMap.get(currencyNote + "s");
            if (amount >= currencyNote.intValue() && availableCurrencyNotes.intValue() > 0)
            {
                int desiredCurrencyNotes = amount / currencyNote;
                int notesToBeUsed = (availableCurrencyNotes.intValue() < desiredCurrencyNotes)
                        ? availableCurrencyNotes.intValue()
                        : desiredCurrencyNotes;
                amount = amount - notesToBeUsed * currencyNote;
                availableCurrencyNotes.set(availableCurrencyNotes.get() - notesToBeUsed);
                dispenseNotesMap.put(currencyNote + "s", notesToBeUsed);
            }
        }

        //print dispensed notes
        dispenseNotes(dispenseNotesMap);
    }

    /**
     * Print the dispensed notes while withdrawing from current account
     *
     * @param dispenseNotesMap It holds the tree object
     */
    private static void dispenseNotes(Map<String, Integer> dispenseNotesMap)
    {
        Iterator<Entry<String, Integer>> i = dispenseNotesMap.entrySet().iterator();
        StringBuilder sb = new StringBuilder();
        int counter = 0;
        while (i.hasNext())
        {
            Map.Entry<java.lang.String, java.lang.Integer> entry = i.next();
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(entry.getValue());
            String comma = (dispenseNotesMap.size() == counter) ? "" : ", ";
            sb.append(comma);
            counter++;
        }
        System.out.println(String.format("Dispensed: %s", sb.toString()));
    }

}

class Account
{
    private String type;

    private double balance;

    private double rate;

    /**
     *
     */
    public Account()
    {

    }

    /**
     * @param type
     * @param balance
     * @param rate
     */
    public Account(String type, double balance, double rate)
    {
        this.type = type;
        this.balance = balance;
        this.rate = rate;
    }

    /**
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * @return the balance
     */
    public double getBalance()
    {
        return balance;
    }

    /**
     * @param balance the balance to set
     */
    public void setBalance(double balance)
    {
        this.balance = balance;
    }

    /**
     * @return the rate
     */
    public double getRate()
    {
        return rate;
    }

    /**
     * @param rate the rate to set
     */
    public void setRate(double rate)
    {
        this.rate = rate;
    }

    /**
     * Add the depositAmount with balance
     * @param depositAmount
     */
    void deposit(double depositAmount)
    {
        balance = depositAmount;
    }

    /**
     * Subtract the withdrawAmount from balance
     *
     * @param withdrawAmount
     */
    void withdraw(double withdrawAmount)
    {
        balance -= withdrawAmount;
    }
}
