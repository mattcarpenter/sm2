package net.mattcarpenter.srs.sm2;

import lombok.Getter;

@Getter
public class Review {
    private Item item;
    private int score;

    public Review(Item item, int score) {
        this.item = item;
        this.score = score;
    }
}
