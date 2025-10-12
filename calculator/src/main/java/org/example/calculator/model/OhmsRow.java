package org.example.calculator.model;

public class OhmsRow {
    private final String login; // может быть null для обычных юзеров
    private final double iValue; private final String iUnit;
    private final double vValue; private final String vUnit;
    private final double rValue; private final String rUnit;

    public OhmsRow(String login,
                   double iValue, String iUnit,
                   double vValue, String vUnit,
                   double rValue, String rUnit) {
        this.login = login;
        this.iValue = iValue;
        this.iUnit = iUnit;
        this.vValue = vValue;
        this.vUnit = vUnit;
        this.rValue = rValue;
        this.rUnit = rUnit;
    }

    // === геттеры ===
    public String getLogin() { return login; }
    public double getIValue() { return iValue; }
    public String getIUnit() { return iUnit; }
    public double getVValue() { return vValue; }
    public String getVUnit() { return vUnit; }
    public double getRValue() { return rValue; }
    public String getRUnit() { return rUnit; }
}
