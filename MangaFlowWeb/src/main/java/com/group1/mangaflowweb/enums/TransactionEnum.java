package com.group1.mangaflowweb.enums;

public enum TransactionEnum {
    PENDING("Đang chờ xử lý"),
    SUCCESS("Thành công"),
    FAILED("Thất bại"),
    CANCELED("Đã hủy"),
    UPDATED("Nâng cấp");

    private final String displayName;

    TransactionEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
