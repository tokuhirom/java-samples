package com.example;

import com.example.domain.Blog;
import lombok.NonNull;

public class FactoryData {
    public static Blog blog(@NonNull String title) {
        Blog blog = new Blog();
        blog.setTitle(title);
        return blog;
    }
}
