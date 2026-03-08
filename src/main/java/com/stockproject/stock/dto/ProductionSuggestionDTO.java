package com.stockproject.stock.dto;

import java.math.BigDecimal;
import java.util.List;

public class ProductionSuggestionDTO {

    public List<SuggestionItem> suggestions;
    public BigDecimal totalValue;

    public static class SuggestionItem {
        public Long productId;
        public String productName;
        public BigDecimal productValue;
        public int quantity;
        public BigDecimal subtotal;

        public SuggestionItem() {}

        public SuggestionItem(Long productId, String productName, BigDecimal productValue,
                              int quantity, BigDecimal subtotal) {
            this.productId = productId;
            this.productName = productName;
            this.productValue = productValue;
            this.quantity = quantity;
            this.subtotal = subtotal;
        }
    }
}
