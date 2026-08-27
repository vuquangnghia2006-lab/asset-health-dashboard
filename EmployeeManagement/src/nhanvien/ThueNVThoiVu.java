package nhanvien;

public class ThueNVThoiVu implements ThueStrategy {
    private static final double THUE_SUAT = 0.05;

    @Override
    public double TinhTienThue(double luong) {
        return luong * THUE_SUAT;
    }
}
