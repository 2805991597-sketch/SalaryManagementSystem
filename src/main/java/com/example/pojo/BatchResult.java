package com.example.pojo;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class BatchResult {
    private int successCount;
    private int skipCount;
    private List<String> skipMessages = new ArrayList<>();
}
