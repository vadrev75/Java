import com.awesome.gic.interfaces.InterestRuleService;
import com.awesome.gic.models.Account;
import com.awesome.gic.models.Statement;
import com.awesome.gic.models.Transaction;
import com.awesome.gic.models.InterestRule;
import com.awesome.gic.services.InterestRuleServiceImpl;
import com.awesome.gic.services.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.awesome.gic.interfaces.AccountService;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TransactionServiceTest {

    // Removed @InjectMocks from here
    @Mock
    private AccountService accountService;

    @Mock
    private InterestRuleService interestRuleService;

    // Declare TransactionServiceImpl here
    private TransactionServiceImpl transactionService;

    // Declare InterestRuleServiceImpl here
    private InterestRuleServiceImpl interestRuleServiceUnderTest;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Initialize TransactionServiceImpl with mocked dependencies
        transactionService = new TransactionServiceImpl(accountService, interestRuleService);
        interestRuleServiceUnderTest = new InterestRuleServiceImpl(); // Initialize InterestRuleServiceImpl
    }

    // --- TransactionServiceImpl Tests ---
    @Test
    void createTransaction_deposit_shouldCreateTransactionAndIncreaseBalance() throws Exception {
        String accountId = "AC001";
        Account account = new Account(accountId);
        when(accountService.getAccount(accountId)).thenReturn(account);

        String dateStr = "20250413";
        double amount = 100.0;
        Transaction transaction = transactionService.createTransaction(dateStr, accountId, "D", amount);

        assertNotNull(transaction);
        assertEquals(LocalDate.parse(dateStr, DATE_FORMATTER), transaction.getDate());
        assertEquals("D", transaction.getType());
        assertEquals(amount, transaction.getAmount());
        verify(accountService).getAccount(accountId);
        assertEquals(amount, account.getBalance()); // Check balance update

    }

    @Test
    void createTransaction_withdrawal_shouldCreateTransactionAndDecreaseBalance() throws Exception {
        String accountId = "AC001";
        Account account = new Account(accountId);
        account.setBalance(200.0);
        when(accountService.getAccount(accountId)).thenReturn(account);

        String dateStr = "20250413";
        double amount = 50.0;
        Transaction transaction = transactionService.createTransaction(dateStr, accountId, "W", amount);

        assertNotNull(transaction);
        assertEquals(LocalDate.parse(dateStr, DATE_FORMATTER), transaction.getDate());
        assertEquals("W", transaction.getType());
        assertEquals(amount, transaction.getAmount());
        assertEquals(200.0 - amount, account.getBalance()); //check the balance
    }

    @Test
    void createTransaction_newAccount_shouldCreateAccountAndTransaction() throws Exception {
        String accountId = "NEW001";
        when(accountService.getAccount(accountId)).thenReturn(null);
        Account newAccount = new Account(accountId);
        when(accountService.createAccount(accountId)).thenReturn(newAccount);

        String dateStr = "20250413";
        double amount = 50.0;
        Transaction transaction = transactionService.createTransaction(dateStr, accountId, "D", amount);

        assertNotNull(transaction);
        verify(accountService).getAccount(accountId);
        verify(accountService).createAccount(accountId);
        assertEquals(amount, newAccount.getBalance()); // Check initial balance of new account.
    }

    @Test
    void createTransaction_invalidDate() {
        assertThrows(Exception.class, () -> transactionService.createTransaction("04122022", "AC001", "D", 100.0));
    }

    @Test
    void createTransaction_invalidLength() {
        assertThrows(Exception.class, () -> transactionService.createTransaction("33224423232", "AC001", "D", 100.0));
    }

    @Test
    void createTransaction_invalidType() {
        assertThrows(Exception.class, () -> transactionService.createTransaction("20250413", "AC001", "X", 100.0));
    }

    @Test
    void createTransaction_invalidAmount() {
        assertThrows(Exception.class, () -> transactionService.createTransaction("20250413", "AC001", "D", -10.0));
    }

    @Test
    void createTransaction_invalidAmountDecimals() {
        assertThrows(Exception.class, () -> transactionService.createTransaction("20250413", "AC001", "D", 10.43343434));
    }

    @Test
    void createTransaction_insufficientBalance() throws Exception {
        String accountId = "AC001";
        Account account = new Account(accountId);
        account.setBalance(50.0);
        when(accountService.getAccount(accountId)).thenReturn(account);
        assertThrows(Exception.class, () -> transactionService.createTransaction("20250413", accountId, "W", 100.0));
    }

    @Test
    void generateTransactionId_shouldGenerateUniqueId() throws Exception {
        LocalDate date = LocalDate.of(2025, 4, 13);
        String id1 = transactionService.generateTransactionId(date);
        String id2 = transactionService.generateTransactionId(date);
        assertNotEquals(id1, id2);
        assertTrue(id1.startsWith("20250413-01"));
        assertTrue(id2.startsWith("20250413-02"));
    }

    @Test
    void getTransactionCountForDate_shouldReturnCorrectCount() throws Exception {
        LocalDate date = LocalDate.of(2025, 4, 13);
        transactionService.generateTransactionId(date);
        transactionService.generateTransactionId(date);
        assertEquals(2, transactionService.getTransactionCount(date));
        assertEquals(0, transactionService.getTransactionCount(LocalDate.of(2025, 4, 14)));
    }

    @Test
    void generateMonthlyStatement_noAccount() {
        when(accountService.getAccount("NONEXISTENT")).thenReturn(null);
        List<Statement> statement = transactionService.generateMonthlyStatement("NONEXISTENT", "202504");
        assertTrue(statement.isEmpty());
    }

    @Test
    void generateMonthlyStatement_invalidYearMonthFormat() {
        Account account = new Account("AC001");
        when(accountService.getAccount("AC001")).thenReturn(account);
        List<Statement> statement = transactionService.generateMonthlyStatement("AC001", "20254");
        assertTrue(statement.isEmpty());
    }

    @Test
    void generateMonthlyStatement_noTransactionsForMonth() {
        Account account = new Account("AC001");
        account.addTransaction(new Transaction(LocalDate.of(2025, 3, 31), "TXN001", "D", 100.0));
        when(accountService.getAccount("AC001")).thenReturn(account);
        when(interestRuleService.getApplicableInterestRule(any())).thenReturn(null);
        List<Statement> statement = transactionService.generateMonthlyStatement("AC001", "202504");
        assertTrue(statement.isEmpty());
    }

    @Test
    void generateMonthlyStatement_withTransactionsAndInterest() {
        String accountId = "AC001";
        Account account = new Account(accountId);
        account.addTransaction(new Transaction(LocalDate.of(2025, 4, 5), "TXN001", "D", 50.0));
        account.addTransaction(new Transaction(LocalDate.of(2025, 4, 15), "TXN002", "W", 20.0));
        when(accountService.getAccount(accountId)).thenReturn(account);
        when(interestRuleService.getApplicableInterestRule(LocalDate.of(2025, 4, 5))).thenReturn(new InterestRule(LocalDate.of(2025, 4, 1), "IR002", 1.0));
        //rule change
        when(interestRuleService.getApplicableInterestRule(LocalDate.of(2025, 4, 6))).thenReturn(new InterestRule(LocalDate.of(2025, 4, 1), "IR002", 2.0));
        when(interestRuleService.getApplicableInterestRule(LocalDate.of(2025, 4, 15))).thenReturn(new InterestRule(LocalDate.of(2025, 4, 1), "IR002", 1.0));
        when(interestRuleService.getApplicableInterestRule(LocalDate.of(2025, 4, 30))).thenReturn(new InterestRule(LocalDate.of(2025, 4, 1), "IR002", 1.0));


        List<Statement> statement = transactionService.generateMonthlyStatement(accountId, "202504");
        assertEquals(3, statement.size()); // 2 transactions + 1 interest line
        assertEquals("20250405", statement.get(0).getDate());
        assertEquals(50.0, statement.get(0).getAmount());
        assertEquals("20250415", statement.get(1).getDate());
        assertEquals(20.0, statement.get(1).getAmount());
        assertEquals("20250430", statement.get(2).getDate());
        assertEquals("I", statement.get(2).getType());
    }

    @Test
    void calculateBalanceAtStartOfMonth_noTransactionsBeforeMonth() {
        Account account = new Account("AC001");
        account.addTransaction(new Transaction(LocalDate.of(2025, 4, 1), "TXN001", "D", 100.0));
        double balance = transactionService.getStartingBalance(account, LocalDate.of(2025, 4, 1));
        assertEquals(0.0, balance);
    }

    @Test
    void calculateBalanceAtStartOfMonth_withTransactionsBeforeMonth() {
        Account account = new Account("AC001");
        account.addTransaction(new Transaction(LocalDate.of(2025, 3, 31), "TXN001", "D", 50.0));
        account.addTransaction(new Transaction(LocalDate.of(2025, 3, 15), "TXN002", "W", 20.0));
        account.addTransaction(new Transaction(LocalDate.of(2025, 4, 1), "TXN003", "D", 10.0));
        double balance = transactionService.getStartingBalance(account, LocalDate.of(2025, 4, 1));
        assertEquals(30.0, balance);
    }

    @Test
    void testCurrentDateBeforeEndDate() {
        LocalDate currentDate = LocalDate.of(2024, 4, 10);
        LocalDate endDate = LocalDate.of(2024, 4, 15);
        assertEquals(5, transactionService.findDays(currentDate, endDate));
    }

    @Test
    void testCurrentDateEqualToEndDate() {
        LocalDate currentDate = LocalDate.of(2024, 4, 13);
        LocalDate endDate = LocalDate.of(2024, 4, 13);
        assertEquals(0, transactionService.findDays(currentDate, endDate));
    }
}