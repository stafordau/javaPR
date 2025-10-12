package org.example.calculator.model;

import java.util.List;

public class DividerResult {

    /** type: "1" — один; "S" — два последовательно; "P" — два параллельно */
    public record Branch(String type, List<Double> parts, double effective) {}

    private final Branch top;
    private final Branch bottom;
    private final double vout;
    private final double errorPct;
    private final int    count;
    private final double powerMilli;
    private final String topology;

    public DividerResult(Branch top, Branch bottom,
                         double vout, double errorPct, int count, double powerMilli,
                         String topology) {
        this.top = top; this.bottom = bottom;
        this.vout = vout; this.errorPct = errorPct;
        this.count = count; this.powerMilli = powerMilli;
        this.topology = topology;
    }

    public Branch getTop()        { return top; }
    public Branch getBottom()     { return bottom; }
    public double getVout()       { return vout; }
    public double getErrorPct()   { return errorPct; }
    public int    getCount()      { return count; }
    public double getPowerMilli() { return powerMilli; }
    public String getTopology()   { return topology; }

    public String prettyValues() {
        return "Top: " + branchToString(top) + " | Bottom: " + branchToString(bottom);
    }
    private String branchToString(Branch br) {
        String sep = switch (br.type()) { case "S" -> " + "; case "P" -> " || "; default -> ""; };
        String parts = br.parts().stream().map(DividerResult::fmtOhm)
                .reduce((a,b)->a+sep+b).orElse("");
        return parts + " → " + fmtOhm(br.effective());
    }
    public static String fmtOhm(double x) {
        if (x >= 1_000_000) return trim(x/1_000_000) + " MΩ";
        if (x >= 1_000)     return trim(x/1_000)     + " kΩ";
        return trim(x) + " Ω";
    }
    private static String trim(double v) {
        String s = String.format(java.util.Locale.US,"%.3f", v);
        return s.replaceAll("0+$","").replaceAll("\\.$","");
    }
}
