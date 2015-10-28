package kz.gereski.m.homebank.util;

public class Screen {
    int x, y;
    public Screen(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getWidth() {
        return x;
    }

    public int getHeight() {
        return y;
    }
}
