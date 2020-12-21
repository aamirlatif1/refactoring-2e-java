package com.uf.invoice;

import com.uf.data.Invoice;
import com.uf.data.Performance;
import com.uf.data.Play;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import static java.lang.String.format;

public class InvoiceGenerator {

    public String plainStatement(final Invoice invoice, final Map<String, Play> plays) {
        return new Statement(invoice, plays).renderPlainText();
    }

    public String htmlStatement(final Invoice invoice, final Map<String, Play> plays) {
        return new Statement(invoice, plays).renderHtml();
    }

    private static class Statement {
        private Invoice invoice;
        private Map<String, Play> plays;

        public Statement(Invoice invoice, Map<String, Play> plays) {
            this.invoice = invoice;
            this.plays = plays;
        }


        public String renderPlainText() {
            var result = format("Statement for %s\n", invoice.customer);
            for (Performance perf : invoice.performances) {
                result += format(" %s: %s (%d seats)\n", playFor(perf).name, usd(amountFor(perf)), perf.audience);
            }
            result += format("Amount owed is %s\n", usd(totalAmount()));
            result += format("You earned %d credits\n", totalVolumeCredits());
            return result;
        }

        public String renderHtml() {
            var result = format("<h1>Statement for %s</h1>\n", invoice.customer);
            result += "<table>\n";
            result += "<tr><th>play</th><th>seats</th><th>cost</th></tr>";
            for (Performance perf : invoice.performances) {
                result += format(" <tr><td>%s</td><td>%s</td>", playFor(perf).name, perf.audience);
                result += format("<td>%s</td></tr>\n", usd(amountFor(perf)));
            }
            result += "</table>\n";
            result += format("<p>Amount owed is <em>%s</em></p>\n", usd(totalAmount()));
            result += format("<p>You earned <em>%d</em> credits</p>\n", totalVolumeCredits());
            return result;
        }

        private double totalAmount() {
            var result = 0.0;
            for (Performance perf : invoice.performances) {
                result += amountFor(perf);
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
            return NumberFormat.getCurrencyInstance(Locale.US).format(amount / 100.0);
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
