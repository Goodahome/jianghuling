package com.jianghu.ling.feedback;

import com.jianghu.ling.feedback.service.FeedbackStatusRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeedbackStatusRulesTest {

    @Test
    void transit_newAllowedTargets() {
        assertTrue(FeedbackStatusRules.canTransit("NEW", "PROCESSING"));
        assertTrue(FeedbackStatusRules.canTransit("NEW", "RESOLVED"));
        assertTrue(FeedbackStatusRules.canTransit("NEW", "CLOSED"));
        assertFalse(FeedbackStatusRules.canTransit("NEW", "NEW"));
    }

    @Test
    void transit_processingAllowedTargets() {
        assertTrue(FeedbackStatusRules.canTransit("PROCESSING", "RESOLVED"));
        assertTrue(FeedbackStatusRules.canTransit("PROCESSING", "CLOSED"));
        assertFalse(FeedbackStatusRules.canTransit("PROCESSING", "NEW"));
        assertFalse(FeedbackStatusRules.canTransit("PROCESSING", "PROCESSING"));
    }

    @Test
    void transit_terminalForbidden() {
        assertFalse(FeedbackStatusRules.canTransit("RESOLVED", "CLOSED"));
        assertFalse(FeedbackStatusRules.canTransit("CLOSED", "PROCESSING"));
        assertFalse(FeedbackStatusRules.canTransit("RESOLVED", "NEW"));
        assertTrue(FeedbackStatusRules.isTerminal("RESOLVED"));
        assertTrue(FeedbackStatusRules.isTerminal("CLOSED"));
    }

    @Test
    void typeAndStatusEnums() {
        assertTrue(FeedbackStatusRules.isValidType("BUG"));
        assertTrue(FeedbackStatusRules.isValidType("SUGGEST"));
        assertTrue(FeedbackStatusRules.isValidType("COMPLAINT"));
        assertTrue(FeedbackStatusRules.isValidType("OTHER"));
        assertFalse(FeedbackStatusRules.isValidType("UNKNOWN"));
        assertTrue(FeedbackStatusRules.isValidStatus("NEW"));
        assertFalse(FeedbackStatusRules.isValidStatus("DONE"));
    }
}
