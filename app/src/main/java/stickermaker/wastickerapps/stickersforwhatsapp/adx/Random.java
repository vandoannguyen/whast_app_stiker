package stickermaker.wastickerapps.stickersforwhatsapp.adx;

public class Random {
    public static int nextInt(int min, int max) {
        java.util.Random rand = new java.util.Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
}
