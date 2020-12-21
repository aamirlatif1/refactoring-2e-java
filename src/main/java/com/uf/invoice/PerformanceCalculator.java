package com.uf.invoice;

import com.uf.data.Performance;
import com.uf.data.Play;

public class PerformanceCalculator {
    public Performance performance;
    public Play play;

    public PerformanceCalculator(Performance performance, Play play) {
        this.performance = performance;
        this.play = play;
    }

    public static PerformanceCalculator createPerformanceCalculator(Performance aPerformance, Play aPlay) {
        switch(aPlay.type) {
            case "tragedy": return new TragedyCalculator(aPerformance, aPlay);
            case "comedy" : return new ComedyCalculator(aPerformance, aPlay);
            default:
                throw new IllegalArgumentException("unknown type: "+aPlay.type);
        }
    }

    public double amount() {
        throw new IllegalArgumentException("subclass responsibility");
    }

    public int volumeCredits() {
        return Math.max(performance.audience - 30, 0);
    }
}
