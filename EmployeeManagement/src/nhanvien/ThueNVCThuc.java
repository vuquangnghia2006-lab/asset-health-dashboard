package nhanvien;

public class ThueNVCThuc implements ThueStrategy {
    private static final double THUE_SUAT = 0.10;

    @Override
    public double TinhTienThue(double luong) {
        return luong * THUE_SUAT;
    }
}
