package com.uf.invoice;

import com.uf.data.Invoice;
import com.uf.data.Performance;
import com.uf.data.Play;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

import static java.lang.String.format;

public class InvoiceGenerator {

    public String statement(final Invoice invoice, final Map<String, Play> plays) {
        return new Generator(invoice, plays).invoke();
    }

    private class Generator {
        private Invoice invoice;
        private Map<String, Play> plays;

        public Generator(Invoice invoice, Map<String, Play> plays) {
            this.invoice = invoice;
            this.plays = plays;
        }

        public String invoke() {
            var totalAmount = 0.0;
            var volumeCredits = 0;
            var result = format("Statement for %s\n", invoice.customer);
            final var currency = NumberFormat.getCurrencyInstance(Locale.US);

            for (Performance perf : invoice.performances) {
                if (!plays.containsKey(perf.playID))
                    throw new IllegalArgumentException("unknown type: " + perf.playID);

                var thisAmount = amountFor(perf);

                // add volume credits
                volumeCredits += Math.max(perf.audience - 30, 0);
                // add extra credit for every ten comedy attendees
                if ("comedy".equals(playFor(perf).type))
                    volumeCredits += perf.audience / 5;

                // print line for this order
                result += format(" %s: %s (%d seats)\n", playFor(perf).name, currency.format(thisAmount / 100.0), perf.audience);
                totalAmount += thisAmount;
            }

            result += format("Amount owed is %s\n", currency.format(totalAmount / 100.0));
            result += format("You earned %d credits\n", volumeCredits);
            return result;
        }

        private Play playFor(Performance aPerformance) {
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
