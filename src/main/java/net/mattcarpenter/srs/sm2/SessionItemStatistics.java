package net.mattcarpenter.srs.sm2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SessionItemStatistics {
    private boolean lapsedDuringSession = false;
    private int mostRecentScore = 0;
}
