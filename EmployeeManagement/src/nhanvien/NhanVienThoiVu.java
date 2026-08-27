package nhanvien;

public class NhanVienThoiVu extends NhanVien implements IInThongTin {
    public NhanVienThoiVu(String maSo, String hoTen, double luongCoBan) {
        super(maSo, hoTen, luongCoBan);
        this.setThueApDung(new ThueNVThoiVu());
    }

    @Override
    public void InChiTiet() {
        System.out.println("Ma so: " + getMaSo());
        System.out.println("Ho ten: " + getHoTen());
        System.out.println("Luong co ban: " + getLuongCoBan());
    }
}
