package net.mattcarpenter.srs;

import lombok.Builder;

import java.util.List;

@Builder
public class CardMaster {
    private List<Combination> combinations;
}
