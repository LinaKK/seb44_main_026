package greenNare.cart.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.PrePersist;
import java.time.LocalDateTime;

@NoArgsConstructor
@Embeddable
@Getter
public class CartItem {
    private int productId;

    @CreatedDate
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
    public CartItem (int productId){
        this.productId = productId;
    }
}
