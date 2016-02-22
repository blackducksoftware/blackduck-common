package com.blackducksoftware.integration.hub.report.risk.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Severity of the risk
 *
 * @author ydeboeck
 *
 */
public enum RiskPriority {

    UNKNOWN(-1), // nothing is known about the risk, it's semantically the same as an absent value

    OK(0), // everything OK : green

    LOW(10), // yellow
    MEDIUM(20), // orange
    HIGH(30); // red

    private static final Comparator<RiskPriority> DESC_RANK_COMPARATOR = new Comparator<RiskPriority>() {
        @Override
        public int compare(RiskPriority o1, RiskPriority o2) {
            return o2.rank - o1.rank;
        }
    };

    private static final List<RiskPriority> RISK_PRIORITY_LIST_DESCENDING_ORDER =
            Arrays.asList(RiskPriority.values());

    static {
        Collections.sort(RISK_PRIORITY_LIST_DESCENDING_ORDER, DESC_RANK_COMPARATOR);
    }

    // internal variable to compare instances and avoiding use of ordinal()
    private final int rank;

    private RiskPriority(int rank) {
        this.rank = rank;
    }

    /**
     * compute the highest risk priority of 2 risk priorities
     *
     * @param other
     * @return
     */
    public RiskPriority highest(RiskPriority other) {
        return other == null ? this : (rank > other.rank) ? this : other;
    }

    /**
     * lowest Risk Priority
     *
     * @return
     */
    public static final RiskPriority lowest() {
        return UNKNOWN;
    }

    /**
     * compute the highest risk priority of many risk priorities
     *
     * @param other
     * @return
     */
    public static RiskPriority highest(Iterable<RiskPriority> risks) {
        RiskPriority highest = RiskPriority.UNKNOWN;
        for (RiskPriority item : risks) {
            highest = highest.highest(item);
        }
        return highest;
    }

    /**
     * get RiskPriority list ordered by rank
     *
     * @return
     */
    public static List<RiskPriority> getRiskPrioritiesByDescendingRank() {
        return RISK_PRIORITY_LIST_DESCENDING_ORDER;
    }

    /**
     * @return rank
     */
    public int getRank() {
        return rank;
    }

    /**
     * Returns the enumeration corresponding to the provided numeric rank
     *
     * @param size
     * @return
     */
    public static RiskPriority valueOf(int rank) {
        if (rank <= UNKNOWN.rank) {
            return UNKNOWN;
        } else if (rank <= OK.rank) {
            return OK;
        } else if (rank <= LOW.rank) {
            return LOW;
        } else if (rank <= MEDIUM.rank) {
            return MEDIUM;
        } else {
            return HIGH;
        }
    }

    public static RiskPriority getRiskPriority(String riskPriority) {
        if (riskPriority.equalsIgnoreCase(LOW.name())) {
            return RiskPriority.LOW;
        } else if (riskPriority.equalsIgnoreCase(MEDIUM.name())) {
            return RiskPriority.MEDIUM;
        } else if (riskPriority.equalsIgnoreCase(HIGH.name())) {
            return RiskPriority.HIGH;
        } else {
            return RiskPriority.UNKNOWN;
        }
    }

}
