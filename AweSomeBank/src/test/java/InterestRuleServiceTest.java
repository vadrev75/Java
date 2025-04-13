import com.awesome.gic.interfaces.InterestRuleService;
import com.awesome.gic.models.InterestRule;
import com.awesome.gic.services.InterestRuleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InterestRuleServiceTest {

    private InterestRuleService interestRuleService;


    @BeforeEach
    public void setUp() {
        interestRuleService = new InterestRuleServiceImpl();
    }

    @Test
    public void testAddInterestRule() throws Exception {
        // Add a new interest rule
        interestRuleService.addInterestRule("20230101", "RULE01", 1.95);

        // Verify rule was added
        List<InterestRule> rules = interestRuleService.getAllInterestRules();
        assertEquals(1, rules.size());
        assertEquals("RULE01", rules.get(0).getRuleId());
        assertEquals(1.95, rules.get(0).getRate());
    }

    @Test
    public void testAddInterestRuleInvalidRate() {
        // Test with negative rate
        Exception exception = assertThrows(Exception.class, () -> {
            interestRuleService.addInterestRule("20230101", "RULE01", -1.0);
        });
        assertTrue(exception.getMessage().contains("should be greater than 0"));

        // Test with rate >= 100
        exception = assertThrows(Exception.class, () -> {
            interestRuleService.addInterestRule("20230101", "RULE01", 100.0);
        });
        assertTrue(exception.getMessage().contains("should be greater than 0 and less than 100"));
    }

    @Test
    public void testRuleOverrideOnSameDate() throws Exception {
        // Add a rule
        interestRuleService.addInterestRule("20230101", "RULE01", 1.95);

        // Add another rule with the same date
        interestRuleService.addInterestRule("20230101", "RULE02", 2.0);

        // Verify only the latest rule is kept
        List<InterestRule> rules = interestRuleService.getAllInterestRules();
        assertEquals(1, rules.size());
        assertEquals("RULE02", rules.get(0).getRuleId());
        assertEquals(2.0, rules.get(0).getRate());
    }

    @Test
    public void testGetAllInterestRulesSorted() throws Exception {
        // Add rules in unsorted order
        interestRuleService.addInterestRule("20230601", "RULE03", 2.2);
        interestRuleService.addInterestRule("20230101", "RULE01", 1.95);
        interestRuleService.addInterestRule("20230520", "RULE02", 1.9);

        // Verify rules are returned in sorted order
        List<InterestRule> rules = interestRuleService.getAllInterestRules();
        assertEquals(3, rules.size());
        assertEquals("RULE01", rules.get(0).getRuleId());
        assertEquals("RULE02", rules.get(1).getRuleId());
        assertEquals("RULE03", rules.get(2).getRuleId());
    }

    @Test
    public void testGetApplicableInterestRule() throws Exception {
        // Add multiple rules
        interestRuleService.addInterestRule("20230101", "RULE01", 1.95);
        interestRuleService.addInterestRule("20230520", "RULE02", 1.9);
        interestRuleService.addInterestRule("20230615", "RULE03", 2.2);

        // Test date before any rule
        assertNull(interestRuleService.getApplicableInterestRule(LocalDate.of(2022, 12, 31)));

        // Test date with exact rule match
        InterestRule rule = interestRuleService.getApplicableInterestRule(LocalDate.of(2023, 1, 1));
        assertEquals("RULE01", rule.getRuleId());

        // Test date between rules
        rule = interestRuleService.getApplicableInterestRule(LocalDate.of(2023, 2, 1));
        assertEquals("RULE01", rule.getRuleId());

        // Test date matching later rule
        rule = interestRuleService.getApplicableInterestRule(LocalDate.of(2023, 5, 20));
        assertEquals("RULE02", rule.getRuleId());

        // Test date after all rules
        rule = interestRuleService.getApplicableInterestRule(LocalDate.of(2023, 12, 31));
        assertEquals("RULE03", rule.getRuleId());
    }

    private LocalDate parseDate(String dateStr) throws Exception {
        if (!dateStr.matches("\\d{8}")) {
            throw new Exception("Date should be in YYYYMMdd format");
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            return LocalDate.parse(dateStr, formatter);
        } catch (Exception e) {
            throw new Exception("Invalid date format. Please use YYYYMMdd");
        }
    }

    @Test
    void testValidDate() throws Exception {
        LocalDate expectedDate = LocalDate.of(2023, 12, 25);
        assertEquals(expectedDate, parseDate("20231225"));
    }

    @Test
    void testInvalidFormatTooShort() {
        Exception exception = assertThrows(Exception.class, () -> parseDate("202312"));
        assertEquals("Date should be in YYYYMMdd format", exception.getMessage());
    }

    @Test
    void testInvalidFormatWithHyphen() {
        Exception exception = assertThrows(Exception.class, () -> parseDate("2023-12-25"));
        assertEquals("Date should be in YYYYMMdd format", exception.getMessage());
    }


    @Test
    void testInvalidCharacters() {
        Exception exception = assertThrows(Exception.class, () -> parseDate("20a31225"));
        assertEquals("Date should be in YYYYMMdd format", exception.getMessage());
    }

    @Test
    void testInvalidDateException() throws Exception {
        Exception exception = assertThrows(Exception.class, () -> parseDate("20042025"));
        assertEquals("Invalid date format. Please use YYYYMMdd", exception.getMessage());
    }
}
