package greenNare.product.entity;

import greenNare.audit.Auditable;
import lombok.*;


import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Image extends Auditable implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int imageId;

    @Column(nullable = false/*, unique = true*/)
    String imageUri;

    @ManyToOne
    @JoinColumn(name = "reviewId")
    Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productId")
    Product product;

    public Image(String imageUri, Review review) {
        this.imageUri = imageUri;
        this.review = review;
    }
    public Image(String imageUri, Product product) {
        this.imageUri = imageUri;
        this.product = product;
    }

}
