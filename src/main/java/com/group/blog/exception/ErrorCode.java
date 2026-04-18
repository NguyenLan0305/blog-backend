package com.group.blog.exception;

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999,"Uncategorized error"),
    INVALID_KEY(1001,"Invalid message key"),
    USER_EXITED(1002,"User Exited"),
    USERNAME_INVALID(1003,"Username must be at least 3 charactes"),
    INVALID_PASSWORD(1004,"Password must be at least 8 characters"),
    USER_NOT_EXITED(1005,"User Not Exited"),
    UNAUTHENTICATED(1006,"Unauthenticated"),
    PASSWORD_INCORRECT(1007, "Mật khẩu cũ không chính xác"),
    PASSWORD_NOT_MATCH(1008, "Mật khẩu xác nhận không khớp"),
    CATEGORY_NOT_FOUND(1009, "Danh mục không tồn tại"),
    BLOG_NOT_FOUND(1010, "Bài viết không tồn tại"),
    UNAUTHORIZED(1011, "Bạn không có quyền thực hiện hành động này"),
    CATEGORY_EXITED(1012, "Danh mục đã tồn tại"),
    ;
    private int code;
    private String message;
    ErrorCode(int code,String message){
        this.message=message;
        this.code=code;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
