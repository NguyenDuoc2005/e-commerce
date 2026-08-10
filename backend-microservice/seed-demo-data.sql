SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS `ecommerce_auth` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `ecommerce_user` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `ecommerce_catalog` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `ecommerce_promotion` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `ecommerce_cart` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `ecommerce_order` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `ecommerce_user`;

CREATE TABLE IF NOT EXISTS `khach_hang` (
  `id` varchar(36) NOT NULL,
  `status` tinyint DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `ma_khach_hang` varchar(50) DEFAULT NULL,
  `ten_khach_hang` varchar(255) DEFAULT NULL,
  `so_dien_thoai` varchar(30) DEFAULT NULL,
  `tinh` varchar(100) DEFAULT NULL,
  `huyen` varchar(100) DEFAULT NULL,
  `xa` varchar(100) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `dia_chi` varchar(500) DEFAULT NULL,
  `avatar` varchar(1000) DEFAULT NULL,
  `cccd` varchar(30) DEFAULT NULL,
  `ngay_sinh` datetime(6) DEFAULT NULL,
  `gioi_timh` bit DEFAULT NULL,
  `mat_khau` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `nhan_vien` (
  `id` varchar(36) NOT NULL,
  `status` tinyint DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `ma_nhan_vien` varchar(50) DEFAULT NULL,
  `ten_nhan_vien` varchar(255) DEFAULT NULL,
  `tinh` varchar(100) DEFAULT NULL,
  `huyen` varchar(100) DEFAULT NULL,
  `xa` varchar(100) DEFAULT NULL,
  `so_dien_thoai` varchar(30) DEFAULT NULL,
  `dia_chi` varchar(500) DEFAULT NULL,
  `ngay_sinh` datetime(6) DEFAULT NULL,
  `avatar` varchar(1000) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `cccd` varchar(30) DEFAULT NULL,
  `vai_tro` tinyint DEFAULT 1,
  `gioi_timh` bit DEFAULT NULL,
  `chuc_vu` tinyint DEFAULT 0,
  `mat_khau` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `khach_hang` (`id`, `status`, `created_date`, `last_modified_date`, `ma_khach_hang`, `ten_khach_hang`, `so_dien_thoai`, `tinh`, `huyen`, `xa`, `email`, `dia_chi`, `avatar`, `cccd`, `ngay_sinh`, `gioi_timh`, `mat_khau`) VALUES
('10000000-0000-0000-0000-000000000001', 0, 1720000000000, 1720000000000, 'KH0001', 'Nguyen Van Demo', '0901111111', 'Ha Noi', 'Cau Giay', 'Dich Vong', 'customer1@ecommerce.local', '1 Xuan Thuy, Cau Giay, Ha Noi', NULL, '001000000001', '1995-01-15 00:00:00', 1, '$2a$10$5pfC09WEvwS7HqqvY2peKeWTmDInAVXkI/g7MQw.UFoGcHA8F.Tzm'),
('10000000-0000-0000-0000-000000000002', 0, 1720000000000, 1720000000000, 'KH0002', 'Tran Thi Sample', '0902222222', 'Ha Noi', 'Dong Da', 'Lang Ha', 'customer2@ecommerce.local', '2 Lang Ha, Dong Da, Ha Noi', NULL, '001000000002', '1996-05-20 00:00:00', 0, '$2a$10$5pfC09WEvwS7HqqvY2peKeWTmDInAVXkI/g7MQw.UFoGcHA8F.Tzm');

INSERT INTO `nhan_vien` (`id`, `status`, `created_date`, `last_modified_date`, `ma_nhan_vien`, `ten_nhan_vien`, `tinh`, `huyen`, `xa`, `so_dien_thoai`, `dia_chi`, `ngay_sinh`, `avatar`, `email`, `cccd`, `vai_tro`, `gioi_timh`, `chuc_vu`, `mat_khau`) VALUES
('00000000-0000-0000-0000-000000000001', 0, 1720000000000, 1720000000000, 'ADMIN001', 'Local Admin', 'Ha Noi', 'Cau Giay', 'Dich Vong', '0900000000', 'Local development', '1990-01-01 00:00:00', NULL, 'admin@ecommerce.local', '000000000001', 1, 1, 0, '$2a$10$5pfC09WEvwS7HqqvY2peKeWTmDInAVXkI/g7MQw.UFoGcHA8F.Tzm'),
('20000000-0000-0000-0000-000000000002', 0, 1720000000000, 1720000000000, 'NV0002', 'Staff Demo', 'Ha Noi', 'Ba Dinh', 'Lieu Giai', '0903333333', '3 Lieu Giai, Ba Dinh, Ha Noi', '1992-03-10 00:00:00', NULL, 'staff@ecommerce.local', '000000000002', 0, 1, 1, '$2a$10$5pfC09WEvwS7HqqvY2peKeWTmDInAVXkI/g7MQw.UFoGcHA8F.Tzm');

USE `ecommerce_catalog`;

CREATE TABLE IF NOT EXISTS `thuong_hieu` (`id` varchar(36) NOT NULL, `status` tinyint DEFAULT 0, `created_date` bigint DEFAULT NULL, `last_modified_date` bigint DEFAULT NULL, `ma_thuong_hieu` varchar(50) DEFAULT NULL, `ten_thuong_hieu` varchar(255) DEFAULT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS `xuat_su` (`id` varchar(36) NOT NULL, `status` tinyint DEFAULT 0, `created_date` bigint DEFAULT NULL, `last_modified_date` bigint DEFAULT NULL, `ma_xuat_su` varchar(50) DEFAULT NULL, `ten_xuat_su` varchar(255) DEFAULT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS `danh_muc` (`id` varchar(36) NOT NULL, `status` tinyint DEFAULT 0, `created_date` bigint DEFAULT NULL, `last_modified_date` bigint DEFAULT NULL, `ma_danh_muc` varchar(50) DEFAULT NULL, `ten_danh_muc` varchar(255) DEFAULT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS `loai_de` (`id` varchar(36) NOT NULL, `status` tinyint DEFAULT 0, `created_date` bigint DEFAULT NULL, `last_modified_date` bigint DEFAULT NULL, `ma_loai_de` varchar(50) DEFAULT NULL, `ten_loai_de` varchar(255) DEFAULT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS `chat_lieu` (`id` varchar(36) NOT NULL, `status` tinyint DEFAULT 0, `created_date` bigint DEFAULT NULL, `last_modified_date` bigint DEFAULT NULL, `ma_chat_lieu` varchar(50) DEFAULT NULL, `ten_chat_lieu` varchar(255) DEFAULT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS `kich_co` (`id` varchar(36) NOT NULL, `status` tinyint DEFAULT 0, `created_date` bigint DEFAULT NULL, `last_modified_date` bigint DEFAULT NULL, `ma_kich_co` varchar(50) DEFAULT NULL, `ten_kich_co` varchar(255) DEFAULT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS `mau_sac` (`id` varchar(36) NOT NULL, `status` tinyint DEFAULT 0, `created_date` bigint DEFAULT NULL, `last_modified_date` bigint DEFAULT NULL, `ma_mau_sac` varchar(50) DEFAULT NULL, `ten_mau_sac` varchar(255) DEFAULT NULL, `mau_sac` varchar(50) DEFAULT NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `san_pham` (
  `id` varchar(36) NOT NULL,
  `status` tinyint DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `ma_san_pham` varchar(50) DEFAULT NULL,
  `ten_san_pham` varchar(255) DEFAULT NULL,
  `mo_ta` text DEFAULT NULL,
  `id_thuong_hieu` varchar(36) DEFAULT NULL,
  `id_xuat_su` varchar(36) DEFAULT NULL,
  `id_danh_muc` varchar(36) DEFAULT NULL,
  `id_loai_de` varchar(36) DEFAULT NULL,
  `id_chat_lieu` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `san_pham_chi_tiet` (
  `id` varchar(36) NOT NULL,
  `status` tinyint DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `ma_san_pham` varchar(50) DEFAULT NULL,
  `gia_ban` double DEFAULT NULL,
  `anh_san_pham` varchar(1000) DEFAULT NULL,
  `so_luong` int DEFAULT NULL,
  `id_san_pham` varchar(36) DEFAULT NULL,
  `id_kich_co` varchar(36) DEFAULT NULL,
  `id_mau_sac` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `thuong_hieu` VALUES
('30000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'TH001','Nike'),
('30000000-0000-0000-0000-000000000002',0,1720000000000,1720000000000,'TH002','Adidas');
INSERT INTO `xuat_su` VALUES ('31000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'XS001','Viet Nam'),('31000000-0000-0000-0000-000000000002',0,1720000000000,1720000000000,'XS002','USA');
INSERT INTO `danh_muc` VALUES ('32000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'DM001','Sneaker'),('32000000-0000-0000-0000-000000000002',0,1720000000000,1720000000000,'DM002','Running');
INSERT INTO `loai_de` VALUES ('33000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'LD001','De cao su'),('33000000-0000-0000-0000-000000000002',0,1720000000000,1720000000000,'LD002','De foam');
INSERT INTO `chat_lieu` VALUES ('34000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'CL001','Da tong hop'),('34000000-0000-0000-0000-000000000002',0,1720000000000,1720000000000,'CL002','Vai mesh');
INSERT INTO `kich_co` VALUES ('35000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'KC039','39'),('35000000-0000-0000-0000-000000000002',0,1720000000000,1720000000000,'KC040','40'),('35000000-0000-0000-0000-000000000003',0,1720000000000,1720000000000,'KC041','41');
INSERT INTO `mau_sac` VALUES ('36000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'MS001','Trang','#ffffff'),('36000000-0000-0000-0000-000000000002',0,1720000000000,1720000000000,'MS002','Den','#111111'),('36000000-0000-0000-0000-000000000003',0,1720000000000,1720000000000,'MS003','Do','#dc2626');

INSERT INTO `san_pham` VALUES
('37000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'SP0001','Nike Air Demo','Giay sneaker demo cho local test','30000000-0000-0000-0000-000000000001','31000000-0000-0000-0000-000000000002','32000000-0000-0000-0000-000000000001','33000000-0000-0000-0000-000000000001','34000000-0000-0000-0000-000000000001'),
('37000000-0000-0000-0000-000000000002',0,1720000001000,1720000001000,'SP0002','Adidas Run Demo','Giay running demo cho local test','30000000-0000-0000-0000-000000000002','31000000-0000-0000-0000-000000000001','32000000-0000-0000-0000-000000000002','33000000-0000-0000-0000-000000000002','34000000-0000-0000-0000-000000000002');

INSERT INTO `san_pham_chi_tiet` VALUES
('38000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'SPCT0001',1200000,'https://placehold.co/600x600?text=Nike+White+39',50,'37000000-0000-0000-0000-000000000001','35000000-0000-0000-0000-000000000001','36000000-0000-0000-0000-000000000001'),
('38000000-0000-0000-0000-000000000002',0,1720000001000,1720000001000,'SPCT0002',1250000,'https://placehold.co/600x600?text=Nike+Black+40',35,'37000000-0000-0000-0000-000000000001','35000000-0000-0000-0000-000000000002','36000000-0000-0000-0000-000000000002'),
('38000000-0000-0000-0000-000000000003',0,1720000002000,1720000002000,'SPCT0003',990000,'https://placehold.co/600x600?text=Adidas+Red+41',42,'37000000-0000-0000-0000-000000000002','35000000-0000-0000-0000-000000000003','36000000-0000-0000-0000-000000000003');

USE `ecommerce_promotion`;

CREATE TABLE IF NOT EXISTS `phieu_giam_gia` (
  `id` varchar(36) NOT NULL,
  `status` tinyint DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `ma_phieu_giam_gia` varchar(50) DEFAULT NULL,
  `ten_phieu_giam_gia` varchar(255) DEFAULT NULL,
  `phan_tram` double DEFAULT NULL,
  `so_luong_phieu` int DEFAULT NULL,
  `ngay_bat_dau` datetime(6) DEFAULT NULL,
  `ngay_ket_thuc` datetime(6) DEFAULT NULL,
  `dieu_kien` double DEFAULT NULL,
  `gia_giam_toi_da` double DEFAULT NULL,
  `loai_giam` bit DEFAULT NULL,
  `kieu_giam` bit DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `phieu_giam_gia_chi_tiet_khach_hang` (
  `id` varchar(36) NOT NULL,
  `status` tinyint DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `ma_phieu_giam_gia_chi_tiet` varchar(100) DEFAULT NULL,
  `id_khach_hang` varchar(36) DEFAULT NULL,
  `id_phieu_giam_gia` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `dot_giam_gia` (
  `id` varchar(36) NOT NULL,
  `status` tinyint DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `ma_dot_giam_gia` varchar(50) DEFAULT NULL,
  `ten_dot_giam_gia` varchar(255) DEFAULT NULL,
  `phan_tram` double DEFAULT NULL,
  `mo_ta` text DEFAULT NULL,
  `ngay_bat_dau` bigint DEFAULT NULL,
  `ngay_ket_thuc` bigint DEFAULT NULL,
  `trang_thai_dot` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `dot_giam_gia_chi_tiet_san_pham` (
  `id` varchar(36) NOT NULL,
  `status` tinyint DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `ma_dot_giam_gia_chi_tiet_san_pham` varchar(100) DEFAULT NULL,
  `gia_truoc_khi_giam` double DEFAULT NULL,
  `gia_sau_khi_giam` double DEFAULT NULL,
  `trang_thai` varchar(50) DEFAULT NULL,
  `id_chi_tiet_san_pham` varchar(36) DEFAULT NULL,
  `id_dot_giam_gia` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `phieu_giam_gia` VALUES
('40000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'WELCOME10','Giam 10% don tu 500k',10,100,'2024-01-01 00:00:00','2099-12-31 23:59:59',500000,150000,0,1),
('40000000-0000-0000-0000-000000000002',0,1720000000000,1720000000000,'VIP200','Voucher ca nhan 200k',200000,20,'2024-01-01 00:00:00','2099-12-31 23:59:59',1000000,200000,1,0);
INSERT INTO `phieu_giam_gia_chi_tiet_khach_hang` VALUES
('41000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'PGGCT0001','10000000-0000-0000-0000-000000000001','40000000-0000-0000-0000-000000000002');
INSERT INTO `dot_giam_gia` VALUES
('42000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'DGG0001','Sale demo 15%',15,'Dot sale demo cho local',1704067200000,4102444799000,'DANG_KICH_HOAT');
INSERT INTO `dot_giam_gia_chi_tiet_san_pham` VALUES
('43000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'DGGCT0001',1200000,1020000,'DANG_SU_DUNG','38000000-0000-0000-0000-000000000001','42000000-0000-0000-0000-000000000001'),
('43000000-0000-0000-0000-000000000002',0,1720000000000,1720000000000,'DGGCT0002',1250000,1062500,'DANG_SU_DUNG','38000000-0000-0000-0000-000000000002','42000000-0000-0000-0000-000000000001');

USE `ecommerce_cart`;

CREATE TABLE IF NOT EXISTS `gio_hang` (
  `id` varchar(36) NOT NULL,
  `status` tinyint DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `id_khach_hang` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `gio_hang_chi_tiet` (
  `id` varchar(36) NOT NULL,
  `status` tinyint DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `last_modified_date` bigint DEFAULT NULL,
  `id_san_pham_chi_tiet` varchar(36) DEFAULT NULL,
  `id_gio_hang` varchar(36) DEFAULT NULL,
  `so_luong` int DEFAULT NULL,
  `tien` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `gio_hang` VALUES ('50000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'10000000-0000-0000-0000-000000000001');
INSERT INTO `gio_hang_chi_tiet` VALUES
('51000000-0000-0000-0000-000000000001',0,1720000000000,1720000000000,'38000000-0000-0000-0000-000000000001','50000000-0000-0000-0000-000000000001',1,1200000),
('51000000-0000-0000-0000-000000000002',0,1720000000000,1720000000000,'38000000-0000-0000-0000-000000000003','50000000-0000-0000-0000-000000000001',2,990000);

USE `ecommerce_order`;

CREATE TABLE IF NOT EXISTS `hoa_don` (
  `id` varchar(36) NOT NULL,
  `status` int DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `ma_hoa_don` varchar(50) DEFAULT NULL,
  `ten_hoa_don` varchar(255) DEFAULT NULL,
  `so_dien_thoai_khach_hang` varchar(30) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `ten_khach_hang` varchar(255) DEFAULT NULL,
  `phi_van_chuyen` double DEFAULT NULL,
  `dia_chi_giao_hang` varchar(500) DEFAULT NULL,
  `tong_tien_sau_giam` double DEFAULT NULL,
  `tong_tien` double DEFAULT NULL,
  `giam_gia` double DEFAULT NULL,
  `du_no` double DEFAULT NULL,
  `hoan_phi` double DEFAULT NULL,
  `ghi_chu` varchar(1000) DEFAULT NULL,
  `phuong_thuc_thanh_toan` tinyint DEFAULT NULL,
  `loai_hoa_don` tinyint DEFAULT NULL,
  `id_khach_hang` varchar(36) DEFAULT NULL,
  `id_voucher` varchar(36) DEFAULT NULL,
  `id_nhan_vien` varchar(36) DEFAULT NULL,
  `trang_thai_hoa_don` tinyint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `hoa_don_chi_tiet` (
  `id` varchar(36) NOT NULL,
  `status` int DEFAULT 0,
  `created_date` bigint DEFAULT NULL,
  `ma_hoa_don_chi_tiet` varchar(50) DEFAULT NULL,
  `ten_hoa_don_chi_tiet` varchar(255) DEFAULT NULL,
  `so_luong` int DEFAULT NULL,
  `gia_ban` double DEFAULT NULL,
  `id_spct` varchar(36) DEFAULT NULL,
  `id_hoa_don` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `lich_su_trang_thai_hoa_don` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `hoa_don_id` varchar(36) DEFAULT NULL,
  `trang_thai` tinyint DEFAULT NULL,
  `thoi_gian` datetime(6) DEFAULT NULL,
  `note` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `lich_su_thanh_toan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `so_tien` double DEFAULT NULL,
  `thoi_gian` datetime(6) DEFAULT NULL,
  `ma_giao_dich` varchar(100) DEFAULT NULL,
  `loai_giao_dich` varchar(50) DEFAULT NULL,
  `nhan_vien_id` varchar(36) DEFAULT NULL,
  `hoa_don_id` varchar(36) DEFAULT NULL,
  `ghi_chu` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `hoa_don` VALUES
('60000000-0000-0000-0000-000000000001',0,1720000000000,'HD0001','Don demo hoan thanh','0901111111','customer1@ecommerce.local','Nguyen Van Demo',30000,'1 Xuan Thuy, Cau Giay, Ha Noi',2190000,2400000,210000,0,0,'Seed demo order',1,2,'10000000-0000-0000-0000-000000000001','40000000-0000-0000-0000-000000000001',NULL,4),
('60000000-0000-0000-0000-000000000002',0,1720003600000,'HD0002','Don demo cho xac nhan','0902222222','customer2@ecommerce.local','Tran Thi Sample',30000,'2 Lang Ha, Dong Da, Ha Noi',1280000,1250000,0,0,0,'Seed pending order',1,2,'10000000-0000-0000-0000-000000000002',NULL,NULL,0);
INSERT INTO `hoa_don_chi_tiet` VALUES
('61000000-0000-0000-0000-000000000001',0,1720000000000,'HDCT0001','Nike Air Demo 39',1,1200000,'38000000-0000-0000-0000-000000000001','60000000-0000-0000-0000-000000000001'),
('61000000-0000-0000-0000-000000000002',0,1720000000000,'HDCT0002','Adidas Run Demo 41',1,990000,'38000000-0000-0000-0000-000000000003','60000000-0000-0000-0000-000000000001'),
('61000000-0000-0000-0000-000000000003',0,1720003600000,'HDCT0003','Nike Air Demo 40',1,1250000,'38000000-0000-0000-0000-000000000002','60000000-0000-0000-0000-000000000002');
INSERT INTO `lich_su_trang_thai_hoa_don` (`hoa_don_id`, `trang_thai`, `thoi_gian`, `note`) VALUES
('60000000-0000-0000-0000-000000000001',4,'2024-07-03 10:00:00','Seed demo: don hang hoan thanh'),
('60000000-0000-0000-0000-000000000002',0,'2024-07-03 11:00:00','Seed demo: don hang cho xac nhan');
INSERT INTO `lich_su_thanh_toan` (`so_tien`, `thoi_gian`, `ma_giao_dich`, `loai_giao_dich`, `nhan_vien_id`, `hoa_don_id`, `ghi_chu`) VALUES
(2190000,'2024-07-03 10:00:00','PAY-SEED-0001','1',NULL,'60000000-0000-0000-0000-000000000001','Seed demo payment');
