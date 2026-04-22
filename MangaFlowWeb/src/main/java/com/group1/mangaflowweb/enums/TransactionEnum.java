package com.group1.mangaflowweb.enums;

public enum TransactionEnum {
    PENDING,     // đang chờ xử lý
    SUCCESS,     // thành công
    FAILED,      // thất bại
    CANCELED,    // hủy
    UPDATE        // nâng cấp từ gói thấp (chưa hoạt động)
}
