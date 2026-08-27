package nhanvien;

import java.util.ArrayList;
import java.util.List;

public class CongTy {
    private List<NhanVien> danhSachNhanVien = new ArrayList<>();

    public void ThemNhanVien(NhanVien nv) {
        danhSachNhanVien.add(nv);
    }

    public List<NhanVien> getDanhSachNhanVien() {
        return danhSachNhanVien;
    }

    public double TinhTongLuongPhaiTra() {
        double tong = 0;
        for (NhanVien nv : danhSachNhanVien) {
            double luongThucLinh = nv.TinhLuongThucLinh();
            double tienThue = nv.getThueApDung().TinhTienThue(luongThucLinh);
            tong += luongThucLinh - tienThue;
        }
        return tong;
    }
}
