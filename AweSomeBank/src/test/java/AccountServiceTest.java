import com.awesome.gic.models.Account;
import com.awesome.gic.models.Transaction;
import com.awesome.gic.services.AccountServiceImpl;
import com.awesome.gic.services.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import com.awesome.gic.interfaces.AccountService;

public class AccountServiceTest {

    private AccountService accountService;
    private TransactionServiceImpl transactionService;

    @BeforeEach
    public void setUp() {
        accountService = new AccountServiceImpl();
    }

    @Test
    public void testCreateAccount() {
        // Create a new account
        Account account = accountService.createAccount("AC001");

        // Verify account was created with the correct ID
        assertNotNull(account);
        assertEquals("AC001", account.getAccountId());
        assertEquals(0.0, account.getBalance());
        assertTrue(account.getTransactions().isEmpty());
    }

    @Test
    public void testGetAccount() {
        // Create a new account
        accountService.createAccount("AC001");

        // Verify the account can be retrieved
        Account account = accountService.getAccount("AC001");
        assertNotNull(account);
        assertEquals("AC001", account.getAccountId());

        // Verify non-existent account returns null
        assertNull(accountService.getAccount("AC002"));
    }

    @Test
    public void testGetAllAccounts() {
        // Create multiple accounts
        accountService.createAccount("AC001");
        accountService.createAccount("AC002");
        accountService.createAccount("AC003");

        // Verify all accounts are returned
        List<Account> accounts = accountService.getAllAccounts();
        assertEquals(3, accounts.size());

        // Verify the accounts contain the expected IDs
        boolean foundAC001 = false, foundAC002 = false, foundAC003 = false;

        for (Account account : accounts) {
            if (account.getAccountId().equals("AC001")) foundAC001 = true;
            if (account.getAccountId().equals("AC002")) foundAC002 = true;
            if (account.getAccountId().equals("AC003")) foundAC003 = true;
        }

        assertTrue(foundAC001);
        assertTrue(foundAC002);
        assertTrue(foundAC003);
    }

    @Test
    public void testAccountTransactions()  throws Exception{
        // Create a new account
        String accountId = "AC001";
        Account account = new Account(accountId);

        Transaction deposit = new Transaction(LocalDate.of(2025, 4, 5), "20250405-01", "D", 100.0);
        Transaction withdral = new Transaction(LocalDate.of(2025, 4, 15), "20250415-02", "W", 50.0);

        account.addTransaction(deposit);
        account.addTransaction(withdral);
        // Verify transactions were added
        assertEquals(2, account.getTransactions().size());

        // Verify balance was updated correctly
        assertEquals(50.0, account.getBalance());
    }
}
