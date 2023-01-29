package com.csmarton.locking.service;

import org.hibernate.Session;
import org.junit.jupiter.api.*;

class AccountServiceTest {

    private AccountService accountService;
    private Session session;

    @BeforeEach
    public void setup() {
        accountService = new AccountService();
    }


    @Test
    public void testCreate() {
        accountService.persistAccount("HU123456", 100);
        accountService.persistAccount("DE456789", 100);

        accountService.getAccount("HU123456");

        accountService.initiateTransfer("HU123456", "DE456789", 40);
        accountService.getAccount("HU123456");
        accountService.getAccount("DE456789");

    }


  
}