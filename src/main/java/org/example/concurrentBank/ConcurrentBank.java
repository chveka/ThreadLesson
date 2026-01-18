package org.example.concurrentBank;

import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;

class ConcurrentBank {
    private final ConcurrentHashMap<String, BankAccount> accounts;

    public ConcurrentBank() {
        this.accounts = new ConcurrentHashMap<>();
    }

    public BankAccount createAccount(double initialBalance) {
        BankAccount account = new BankAccount(initialBalance);
        accounts.put(account.getAccountNumber(), account);
        return account;
    }

    public boolean transfer(BankAccount fromAccount, BankAccount toAccount, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        if (fromAccount == toAccount) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        BankAccount firstLock = fromAccount;
        BankAccount secondLock = toAccount;
        if (fromAccount.getAccountNumber().compareTo(toAccount.getAccountNumber()) > 0) {
            firstLock = toAccount;
            secondLock = fromAccount;
        }
        firstLock.getLock().lock();

        try {
            secondLock.getLock().lock();
            try {
                if (fromAccount.getBalance() >= amount) {
                    fromAccount.withdraw(amount);
                    toAccount.deposit(amount);
                    return true;
                }
                return false;
            } finally {
                secondLock.getLock().unlock();
            }
        } finally {
            firstLock.getLock().unlock();
        }
    }

    public double getTotalBalance() {
        double total = 0;
        accounts.values().stream()
                .sorted(Comparator.comparing(BankAccount::getAccountNumber))
                .forEach(account -> account.getLock().lock());

        try {
            for (BankAccount account : accounts.values()) {
                total += account.getBalance();
            }
        } finally {
            accounts.values().stream()
                    .sorted((a1, a2) -> a2.getAccountNumber().compareTo(a1.getAccountNumber()))
                    .forEach(account -> account.getLock().unlock());
        }
        return total;
    }
}