package org.example.calculator.model;

public class DividerHistoryRow {
    private final String login;     // для админа, у обычного null
    private final String topology;  // "Верх: … | Низ: … (N)"
    private final String values;    // "Верх: … → …Ω | Низ: … → …Ω"
    private final double vout;
    private final double errPct;
    private final int    elements;
    private final double powerMw;
    private final String created;   // дата/время

    public DividerHistoryRow(String login, String topology, String values,
                             double vout, double errPct, int elements, double powerMw,
                             String created) {
        this.login = login; this.topology = topology; this.values = values;
        this.vout = vout; this.errPct = errPct; this.elements = elements; this.powerMw = powerMw;
        this.created = created;
    }

    public String getLogin()    { return login; }
    public String getTopology() { return topology; }
    public String getValues()   { return values; }
    public double getVout()     { return vout; }
    public double getErrPct()   { return errPct; }
    public int    getElements() { return elements; }
    public double getPowerMw()  { return powerMw; }
    public String getCreated()  { return created; }
}
