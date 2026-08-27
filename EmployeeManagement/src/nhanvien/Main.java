package nhanvien;

public class Main {
    public static void main(String[] args) {
        CongTy congTy = new CongTy();

        NhanVienChinhThuc nv1 = new NhanVienChinhThuc("NV001", "Nguyen Van A", 10000000, 2000000);
        NhanVienChinhThuc nv2 = new NhanVienChinhThuc("NV002", "Tran Thi B", 9000000, 1500000);
        NhanVienThoiVu nv3 = new NhanVienThoiVu("NV003", "Le Van C", 5000000);

        congTy.ThemNhanVien(nv1);
        congTy.ThemNhanVien(nv2);
        congTy.ThemNhanVien(nv3);

        for (NhanVien nv : congTy.getDanhSachNhanVien()) {
            if (nv instanceof IInThongTin) {
                System.out.println("--- Chi tiet nhan vien ---");
                ((IInThongTin) nv).InChiTiet();
            }
            double luongThucLinh = nv.TinhLuongThucLinh();
            double tienThue = nv.getThueApDung().TinhTienThue(luongThucLinh);
            System.out.println("Luong thuc linh: " + luongThucLinh);
            System.out.println("Tien thue: " + tienThue);
            System.out.println("Luong net nhan duoc: " + (luongThucLinh - tienThue));
        }

        System.out.println();
        System.out.println("Tong luong thuc te cong ty phai chi tra (sau thue): "
                + congTy.TinhTongLuongPhaiTra());
    }
}
