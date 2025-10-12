package org.example.calculator.service;

import org.example.calculator.model.DividerResult;

import java.util.*;
import static java.lang.Math.*;

public class DividerService {

    private static final double[] E6  = {10, 15, 22, 33, 47, 68};
    private static final double[] E12 = {10,12,15,18,22,27,33,39,47,56,68,82};
    private static final double[] E24 = {10,11,12,13,15,16,18,20,22,24,27,30,33,36,39,43,47,51,56,62,68,75,82,91};
    private static final double[] E96 = {100,102,105,107,110,113,115,118,121,124,127,130,133,137,140,143,147,150,154,158,162,165,169,174,178,182,187,191,196,200,205,210,215,221,226,232,237,243,249,255,261,267,274,280,287,294,301,309,316,324,332,340,348,357,365,374,383,392,402,412,422,432,442,453,464,475,487,499,511,523,536,549,562,576,590,604,619,634,649,665,681,698,715,732,750,768,787,806,825,845,866,887,909,931,953,976};

    public enum Series { E6, E12, E24, E96 }

    public List<Double> buildValues(Series series, double rmin, double rmax) {
        double[] base = switch (series) { case E6 -> E6; case E12 -> E12; case E24 -> E24; case E96 -> E96; };
        List<Double> vals = new ArrayList<>();
        for (int exp = -1; exp <= 7; exp++) {
            double decade = pow(10, exp);
            for (double m : base) {
                double r = m * decade;
                if (r >= rmin && r <= rmax) vals.add(r);
            }
        }
        return vals.stream().distinct().sorted().toList();
    }

    /** Лучший вариант (для экрана без таблицы). */
    public DividerResult searchBest(double vin, double vreq, double tolPct,
                                    Series series, double rmin, double rmax) {
        var list = search(vin, vreq, tolPct, series, rmin, rmax, 50);
        return list.isEmpty() ? null : list.get(0);
    }

    /** Лучшие по каждой топологии: ошибка → меньше элементов → меньшая мощность. */
    public List<DividerResult> search(double vin, double vreq, double tolPct,
                                      Series series, double rmin, double rmax,
                                      int limitPerTopology) {
        List<Double> base = buildValues(series, rmin, rmax);

        OptList one = buildSingle(base);
        OptList ser = buildSeries(base, rmin, rmax);
        OptList par = buildParallel(base, rmin, rmax);

        Map<String, DividerResult> best = new HashMap<>();
        double ratio = vin / vreq - 1.0;

        List<OptList> tops = List.of(one, ser, par);
        List<OptList> bots = List.of(one, ser, par);

        for (OptList topList : tops) {
            for (OptList botList : bots) {
                int total = topList.count + botList.count;
                if (total < 2 || total > 4) continue;

                String key = topList.type + "|" + botList.type + "|" + total;
                var already = best.get(key);
                if (already != null && already.getErrorPct() == 0.0) continue;

                for (Option bot : botList.items) {
                    double needTop = bot.eff * ratio;
                    Option top = topList.nearest(needTop);

                    double vout = vin * bot.eff / (top.eff + bot.eff);
                    double err  = abs((vout - vreq) / vreq * 100.0);
                    if (err > tolPct) continue;

                    double i = vin / (top.eff + bot.eff);
                    double pMilli = vin * i * 1000.0;

                    var t = new DividerResult.Branch(topList.type, top.parts, top.eff);
                    var b = new DividerResult.Branch(botList.type, bot.parts, bot.eff);
                    String topo = topoRu(topList.type) + " | " + topoRu(botList.type) + " (" + total + ")";

                    DividerResult cand = new DividerResult(t, b, vout, err, total, pMilli, topo);
                    best.merge(key, cand, DividerService::pickBetter);
                    if (best.get(key).getErrorPct() == 0.0) break;
                }
            }
        }

        List<DividerResult> out = new ArrayList<>(best.values());
        out.sort(Comparator
                .comparingDouble(DividerResult::getErrorPct)
                .thenComparingInt(DividerResult::getCount)
                .thenComparingDouble(DividerResult::getPowerMilli));

        if (out.size() > limitPerTopology) out = out.subList(0, limitPerTopology);
        return out;
    }

    private static DividerResult pickBetter(DividerResult a, DividerResult b) {
        int c = Double.compare(a.getErrorPct(), b.getErrorPct());
        if (c != 0) return c <= 0 ? a : b;
        c = Integer.compare(a.getCount(), b.getCount());
        if (c != 0) return c <= 0 ? a : b;
        return a.getPowerMilli() <= b.getPowerMilli() ? a : b;
    }

    private String topoRu(String type) {
        return switch (type) { case "1" -> "Верх: 1"; case "S" -> "Верх: S2"; case "P" -> "Верх: P2"; default -> ""; };
    }

    // ——— внутренние структуры ———
    private static class Option { final String type; final List<Double> parts; final double eff;
        Option(String type, List<Double> parts, double eff) { this.type=type; this.parts=parts; this.eff=eff; } }
    private static class OptList {
        final String type; final int count; final List<Option> items; final double[] effs;
        OptList(String type, int count, List<Option> items) {
            this.type=type; this.count=count; this.items=items;
            this.effs = items.stream().mapToDouble(o->o.eff).toArray();
        }
        Option nearest(double x) {
            int idx = Arrays.binarySearch(effs, x);
            if (idx < 0) idx = -idx - 1;
            if (idx <= 0) return items.getFirst();
            if (idx >= effs.length) return items.getLast();
            double d1 = abs(effs[idx]-x), d0 = abs(effs[idx-1]-x);
            return d0 <= d1 ? items.get(idx-1) : items.get(idx);
        }
    }

    private OptList buildSingle(List<Double> base) {
        List<Option> list = new ArrayList<>();
        for (double r : base) list.add(new Option("1", List.of(r), r));
        list.sort(Comparator.comparingDouble(o->o.eff));
        return new OptList("1", 1, list);
    }
    private OptList buildSeries(List<Double> base, double rmin, double rmax) {
        List<Option> list = new ArrayList<>();
        for (int i=0;i<base.size();i++)
            for (int j=i;j<base.size();j++) {
                double r1 = base.get(i), r2 = base.get(j), rs = r1+r2;
                if (rs>=rmin && rs<=rmax) list.add(new Option("S", List.of(r1,r2), rs));
            }
        list.sort(Comparator.comparingDouble(o->o.eff));
        return new OptList("S", 2, list);
    }
    private OptList buildParallel(List<Double> base, double rmin, double rmax) {
        List<Option> list = new ArrayList<>();
        for (int i=0;i<base.size();i++)
            for (int j=i;j<base.size();j++) {
                double r1 = base.get(i), r2 = base.get(j);
                double rp = 1.0/(1.0/r1 + 1.0/r2);
                if (rp>=rmin && rp<=rmax) list.add(new Option("P", List.of(r1,r2), rp));
            }
        list.sort(Comparator.comparingDouble(o->o.eff));
        return new OptList("P", 2, list);
    }
}
