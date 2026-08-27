package nhanvien;

public class NhanVienChinhThuc extends NhanVien implements IInThongTin {
    private double thuong;

    public NhanVienChinhThuc(String maSo, String hoTen, double luongCoBan, double thuong) {
        super(maSo, hoTen, luongCoBan);
        this.thuong = thuong;
        this.setThueApDung(new ThueNVCThuc());
    }

    public double getThuong() {
        return thuong;
    }

    public void setThuong(double thuong) {
        this.thuong = thuong;
    }

    @Override
    public double TinhLuongThucLinh() {
        return getLuongCoBan() + thuong;
    }

    @Override
    public void InChiTiet() {
        System.out.println("Ma so: " + getMaSo());
        System.out.println("Ho ten: " + getHoTen());
        System.out.println("Luong co ban: " + getLuongCoBan());
        System.out.println("Thuong: " + thuong);
    }
}
