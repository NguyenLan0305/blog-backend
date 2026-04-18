package com.group.blog.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class SlugUtils {

    public static String generateSlug(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "";
        }

        // 1. Chuyển tất cả thành chữ thường và cắt khoảng trắng 2 đầu
        String slug = title.trim().toLowerCase();

        // 2. Loại bỏ dấu tiếng Việt (Chuẩn hóa Unicode NFD)
        slug = Normalizer.normalize(slug, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        slug = pattern.matcher(slug).replaceAll("");

        // 3. Xử lý riêng chữ 'đ' (vì Normalizer không tự động chuyển đ thành d)
        slug = slug.replace("đ", "d");

        // 4. Thay thế tất cả các ký tự không phải là chữ cái hoặc số thành dấu gạch ngang (-)
        slug = slug.replaceAll("[^a-z0-9]+", "-");

        // 5. Loại bỏ các dấu gạch ngang liên tiếp (ví dụ: "---" biến thành "-")
        slug = slug.replaceAll("-+", "-");

        // 6. Xóa dấu gạch ngang bị thừa ở đầu hoặc cuối chuỗi (nếu có)
        slug = slug.replaceAll("^-|-$", "");

        return slug;
    }

    /*
    // Hàm main để bạn test thử trực tiếp ngay trong class này
    public static void main(String[] args) {
        String title1 = "Học Spring Boot 3 & JPA: Xây dựng Blog từ A-Z!";
        String title2 = "   Cách Xử Lý Lỗi NullPointerException (NPE) Trong Java   ";

        System.out.println(generateSlug(title1));
        // Kết quả: hoc-spring-boot-3-jpa-xay-dung-blog-tu-a-z

        System.out.println(generateSlug(title2));
        // Kết quả: cach-xu-ly-loi-nullpointerexception-npe-trong-java
    }
    */
}