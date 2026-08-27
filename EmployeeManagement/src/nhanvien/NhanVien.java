package nhanvien;

public class NhanVien {
    private String maSo;
    private String hoTen;
    private double luongCoBan;
    private ThueStrategy thueApDung;

    public NhanVien(String maSo, String hoTen, double luongCoBan) {
        this.maSo = maSo;
        this.hoTen = hoTen;
        this.luongCoBan = luongCoBan;
    }

    public String getMaSo() {
        return maSo;
    }

    public void setMaSo(String maSo) {
        this.maSo = maSo;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public double getLuongCoBan() {
        return luongCoBan;
    }

    public void setLuongCoBan(double luongCoBan) {
        this.luongCoBan = luongCoBan;
    }

    public ThueStrategy getThueApDung() {
        return thueApDung;
    }

    public void setThueApDung(ThueStrategy thueApDung) {
        this.thueApDung = thueApDung;
    }

    public double TinhLuongThucLinh() {
        return luongCoBan;
    }
}
