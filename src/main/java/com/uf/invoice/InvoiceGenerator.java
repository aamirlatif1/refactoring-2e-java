package com.uf.invoice;

import com.uf.data.Invoice;
import com.uf.data.Performance;
import com.uf.data.Play;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class InvoiceGenerator {

    public String plainStatement(final Invoice invoice, final Map<String, Play> plays) {
        return new Statement(invoice, plays).plainStatement();
    }

    public String htmlStatement(final Invoice invoice, final Map<String, Play> plays) {
        return new Statement(invoice, plays).htmlStatement();
    }

    private static class StatementData {
        private String customer;
        private double totalAmount;
        private int totalVolumeCredits;
        private List<PerformanceExt> performances;
    }

    private static class PerformanceExt extends Performance {
         private Play play;
         private double amount;
    }

    private static class Statement {
        private Invoice invoice;
        private Map<String, Play> plays;

        public Statement(Invoice invoice, Map<String, Play> plays) {
            this.invoice = invoice;
            this.plays = plays;
        }

        public String plainStatement() {
            return renderPlainText(getStatementData());
        }


        private StatementData getStatementData() {
            StatementData data = new StatementData();
            data.performances = invoice.performances.stream().map(this::enrich).collect(toList());
            data.customer = invoice.customer;
            data.totalAmount = totalAmount(data);
            data.totalVolumeCredits = totalVolumeCredits();
            return data;
        }

        private PerformanceExt enrich(Performance perf){
            PerformanceExt ext = new PerformanceExt();
            ext.playID = perf.playID;
            ext.audience = perf.audience;
            ext.play = playFor(perf);
            ext.amount = amountFor(perf);
            return ext;
        }

        public String htmlStatement() {
            return renderHtml(getStatementData());
        }

        private String renderPlainText(StatementData data) {
            StringBuilder result = new StringBuilder(format("Statement for %s\n", data.customer));
            for (PerformanceExt perf : data.performances) {
                result.append(format(" %s: %s (%d seats)\n", perf.play.name, usd(perf.amount), perf.audience));
            }
            result.append(format("Amount owed is %s\n", usd(data.totalAmount)));
            result.append(format("You earned %d credits\n", data.totalVolumeCredits));
            return result.toString();
        }

        private String renderHtml(StatementData data) {
            StringBuilder result = new StringBuilder(format("<h1>Statement for %s</h1>\n", data.customer));
            result.append("<table>\n");
            result.append("<tr><th>play</th><th>seats</th><th>cost</th></tr>");
            for (PerformanceExt perf : data.performances) {
                result.append(format(" <tr><td>%s</td><td>%s</td>", perf.play.name, perf.audience));
                result.append(format("<td>%s</td></tr>\n", usd(perf.amount)));
            }
            result.append("</table>\n");
            result.append(format("<p>Amount owed is <em>%s</em></p>\n", usd(data.totalAmount)));
            result.append(format("<p>You earned <em>%d</em> credits</p>\n", data.totalVolumeCredits));
            return result.toString();
        }

        private double totalAmount(StatementData data) {
            var result = 0.0;
            for (PerformanceExt perf : data.performances) {
                result += perf.amount;
            }
            return result;
        }

        private int totalVolumeCredits() {
            var result = 0;
            for (Performance perf : invoice.performances) {
                result += volumeCreditsFor(perf);
            }
            return result;
        }

        private String usd(double amount) {
            return NumberFormat.getCurrencyInstance(Locale.US)
                    .format(amount / 100.0);
        }

        private int volumeCreditsFor(Performance aPerformance) {
            var result = 0;
            result += Math.max(aPerformance.audience - 30, 0);
            if ("comedy".equals(playFor(aPerformance).type))
                result += aPerformance.audience / 5;
            return result;
        }

        private Play playFor(Performance aPerformance) {
            if (!plays.containsKey(aPerformance.playID)) {
                throw new IllegalArgumentException("unknown type: " + aPerformance.playID);
            }
            return plays.get(aPerformance.playID);
        }

        private double amountFor(Performance aPerformance) {
            var result = 0.0;
            switch (playFor(aPerformance).type) {
                case "tragedy":
                    result = 40000.0;
                    if (aPerformance.audience > 30) {
                        result += 1000 * (aPerformance.audience - 30);
                    }
                    break;
                case "comedy":
                    result = 30000.0;
                    if (aPerformance.audience > 20) {
                        result += 10000 + 500 * (aPerformance.audience - 20);
                    }
                    result += 300 * aPerformance.audience;
                    break;
                default:
                    throw new IllegalArgumentException("unknown type: " + playFor(aPerformance).type);
            }
            return result;
        }
    }
}
