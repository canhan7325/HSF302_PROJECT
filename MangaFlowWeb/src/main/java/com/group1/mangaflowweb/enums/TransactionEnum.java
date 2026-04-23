package com.group1.mangaflowweb.enums;

public enum TransactionEnum {
    PENDING("Đang chờ xử lý"),
    SUCCESS("Thành công"),
    FAILED("Thất bại");

    private final String displayName;

    TransactionEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
