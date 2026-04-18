package com.group.blog.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level= AccessLevel.PRIVATE)
public class IntrospectResponse {
    boolean valid;// để xác định token còn valide/ còn hiệu lực hay không
}