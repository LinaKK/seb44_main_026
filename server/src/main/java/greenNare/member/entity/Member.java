package greenNare.member.entity;

//import greenNare.cart.entity.Cart;
import greenNare.cart.entity.CartItem;
import greenNare.challenge.entity.Challenge;
import greenNare.reply.entity.Reply;
import lombok.*;
import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Member implements Serializable {
    private static final long serialVeresionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int memberId;

    @Column(nullable = false, updatable = false, unique = true)
    private String email;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 100, nullable = true)
    private String password;
    @Column(nullable = false)
    private int point;

//    @OneToMany(mappedBy = "member")
//    private List<Cart> carts;

    @OneToMany(mappedBy = "member")
    private List<Challenge>  challenges;
    @OneToMany(mappedBy = "member")
    private List<Reply>  reply;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "CART_PRODUCT")
    private List<CartItem> cartItemList = new ArrayList<>();

    public Member(String email, String name, String password, int point){
        this.email = email;
        this.name = name;
        this.password = password;
        this.point = point;
    }
}
