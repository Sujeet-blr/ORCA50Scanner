package in.mobiux.android.orca50scanner.stocklitev2.utils;

import android.content.Context;

import java.util.HashSet;
import java.util.Set;


import static in.mobiux.android.orca50scanner.stocklitev2.utils.RFIDUtils.MatchingRule.MR3;

public class RFIDUtils {

    private static RFIDUtils INSTANCE;
    private Context context;
    private SessionManager sm;
    private Set<String> acronyms = new HashSet<>();

    private RFIDUtils(Context context) {
        this.context = context;
        sm = SessionManager.getInstance(context);
        acronyms = sm.getStringSet(MR3.name());
    }

    public static RFIDUtils getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new RFIDUtils(context);
        }
        return INSTANCE;
    }

    public enum MatchingRule {
        MR1, MR2, MR3
    }

    public enum NonMatchingRule {
        NMR1, NMR2
    }

    public enum DisplayRule {
        D1, D2, D21, D3
    }

    public MatchingRule getMatchingRule() {
        String str = sm.getStringValue("matchingRule");
        if (str.isEmpty()) {
            return MatchingRule.MR1;
        }
        return MatchingRule.valueOf(str);
    }

    public void setMatchingRule(MatchingRule rule) {
        sm.setStringValue("matchingRule", rule.name());
    }

    public NonMatchingRule getNonMatchingRule() {
        String str = sm.getStringValue("nonMatchingRule");
        if (str.isEmpty())
            return NonMatchingRule.NMR1;
        return NonMatchingRule.valueOf(str);
    }

    public void setNonMatchingRule(NonMatchingRule rule) {
        sm.setStringValue("nonMatchingRule", rule.name());
    }

    public void setDisplayRule(DisplayRule rule) {
        sm.setStringValue("displayRule", rule.name());
    }

    public DisplayRule getDisplayRule() {
        String str = sm.getStringValue("displayRule");

        if (str.isEmpty()) {
            return DisplayRule.D1;
        } else {
            return DisplayRule.valueOf(str);
        }
    }

    public Set<String> getAcronyms() {
        return sm.getStringSet(MR3.name());
    }

    public void addAcronym(String str) {
        acronyms.add(str);
        sm.setStringSet(MR3.name(),acronyms);
    }

    public void removeAcronym(String str) {
        acronyms.remove(str);
        sm.setStringSet(MR3.name(), acronyms);
    }
}
